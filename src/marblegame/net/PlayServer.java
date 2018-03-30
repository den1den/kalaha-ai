package marblegame.net;

import marblegame.Util;
import marblegame.gamemechanics.Match;
import marblegame.solvers.AiSolver;
import marblegame.solvers.Solver;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PlayServer implements Closeable {
    final int port;

    private ServerSocket serverSocket;

    static private Cache cache;
    private Solver solver;

    public PlayServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        PlayServer playServer = new PlayServer(6022);
        playServer.setSolver(new AiSolver());

        boolean multiThreaded = args.length > 0;

        if (multiThreaded) {
            int nThreads = Util.isLenovo() ? 2 : 8;
            playServer.run(Executors.newFixedThreadPool(nThreads));
        } else {
            playServer.runBlocking();
        }
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

    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    public void runBlocking() throws IOException {
        assertCanRun();
        while (true) {
            Socket socket = serverSocket.accept();
            new Req(socket).run();
        }
    }

    public void run(@NotNull Executor handler) throws IOException {
        assertCanRun();
        while (true) {
            Socket socket = serverSocket.accept();
            handler.execute(new Req(socket));
        }
    }

    public void start(@NotNull Executor handler) {
        if (handler instanceof ThreadPoolExecutor) {
            if (((ThreadPoolExecutor) handler).getMaximumPoolSize() <= 1) {
                throw new IllegalStateException("Server must have at least two threads to process its own queue");
            }
        }
        handler.execute(() -> {
            try {
                runBlocking();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void listen() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server socket opened on " + serverSocket);
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }

    public void cancel() throws IOException {
        serverSocket.close();
    }

    private void assertCanRun() throws IOException {
        if (solver == null) {
            throw new IllegalStateException("Solver cannot be null");
        }
        if (serverSocket == null) {
            listen();
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
                try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
                    writer.writeObject(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
