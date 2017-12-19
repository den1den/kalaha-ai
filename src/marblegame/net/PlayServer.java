package marblegame.net;

import marblegame.Util;
import marblegame.gamemechanics.Match;
import marblegame.solvers.AiSolver;
import marblegame.solvers.Solver;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PlayServer {
    private ServerSocket serverSocket;
    private final ExecutorService handler;

    static private Cache cache;
    private Solver solver;

    private PlayServer(Solver solver, int port, ExecutorService handler) throws IOException {
        this.solver = solver;
        this.handler = handler;
        serverSocket = new ServerSocket(port);
        System.out.println("Server listening on " + serverSocket.getInetAddress());
    }

    public static PlayServer getSimplePlayServer(Solver solver) throws IOException {
        int nThreads = Util.isLenovo() ? 2 : 8;
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        return new PlayServer(solver, 6020, executor);
    }

    /**
     * Single threaded play server
     *
     * @param solver
     * @return
     * @throws IOException
     */
    public static PlayServer getBlockingPlayServer(Solver solver) throws IOException {
        return new PlayServer(solver, 6020, null);
    }

    static Cache getCache() {
        if (cache == null) {
            File cacheFile = new File("server-cache.obj");
            if (cacheFile.exists()) {
                try (ObjectInputStream fio = new ObjectInputStream(new FileInputStream(cacheFile))) {
                    cache = (Cache) fio.readObject();
                    cache.cacheFile = cacheFile;
                    System.out.println("Cache file read in");
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    cacheFile.delete();
                    System.err.println("Cache file deleted: " + e);
                } catch (ClassCastException e) {
                    cacheFile.delete();
                    System.err.println("Cache file deleted: " + e);
                }
            }
            if (cache == null) {
                cache = new Cache(cacheFile);
            }
        }
        return cache;
    }

    public static void main(String[] args) throws Exception {
        PlayServer server = PlayServer.getSimplePlayServer(new AiSolver());
        server.run();
    }

    public void run() throws IOException {
        if (solver == null) {
            throw new IllegalStateException("Solver cannot be null");
        }
        while (true) {
            Socket socket = serverSocket.accept();
            Req req = new Req(socket);
            if (handler == null) {
                req.run();
            } else {
                handler.execute(req);
            }
        }
    }

    public void runOnOwnHandler() {
        if (handler == null) {
            throw new IllegalStateException("Server must have a handler to process its own queue");
        }
        if (handler instanceof ThreadPoolExecutor) {
            if (((ThreadPoolExecutor) handler).getMaximumPoolSize() <= 1) {
                throw new IllegalStateException("Server must have at least two threads to process its own queue");
            }
        }
        handler.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    PlayServer.this.run();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void cancel() throws IOException {
        serverSocket.close();
        if (handler != null)
            handler.shutdown();
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    private static class Cache implements Serializable {
        File cacheFile;
        private Date lastModified;
        private Map<String, Integer> cache = new ConcurrentHashMap<>();

        Cache(File cacheFile) {
            this.cacheFile = cacheFile;
        }

        Integer get(String request) {
            return cache.get(request);
        }

        void put(String request, Integer move) {
            cache.put(request, move);
            saveCache();
        }

        synchronized void saveCache() {
            lastModified = new Date();
            try {
                cacheFile.createNewFile();
                try (
                        ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(cacheFile))
                ) {
                    writer.writeObject(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Req implements Runnable {
        final Socket socket;
        final JSONParser parser = new JSONParser();
        PrintWriter writer;
        BufferedReader reader;

        Req(Socket socket) throws IOException {
            this.socket = socket;
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connection opened to " + this.socket.getInetAddress());
        }

        void process(String read) throws ParseException {
            Integer move;
            Cache cache = getCache();
            if ((move = cache.get(read)) == null) {
                JSONObject jsonObject = (JSONObject) parser.parse(read);
                Match m = Match.Serializer.fromJSONObject(jsonObject);

                move = solver.solve(m);

                cache.put(read, move);
            }
            writer.println(move);
        }

        @Override
        public void run() {
            String read = null;
            try {
                do {
                    read = reader.readLine();
                    if (read == null) {
                        break;
                    }
                    process(read);
                } while (true);
            } catch (IOException e) {
                throw new Error(e);
            } catch (ParseException e) {
                System.err.println("Could not parse: " + read);
            } finally {
                try {
                    socket.close();
                    if (handler instanceof ThreadPoolExecutor) {
                        ThreadPoolExecutor handler = (ThreadPoolExecutor) PlayServer.this.handler;
                        System.out.println("Connection closed to " + this.socket.getInetAddress()
                                + " (" + (handler.getQueue().size() + handler.getActiveCount() - 1) + " remaining)");
                    } else {
                        System.out.println("Connection closed to " + this.socket.getInetAddress());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
