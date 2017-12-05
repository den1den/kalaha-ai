package net;

import marblegame.Util;
import marblegame.gamemechanics.Match;
import marblegame.players.AiPlayer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PlayServer {
    private ServerSocket serverSocket;
    private ThreadPoolExecutor executor;
    private static PlayServer instance = null;
    static private Cache cache;
    private net.Solver solver;

    private PlayServer() throws IOException {
        serverSocket = new ServerSocket(6020);
        int nThreads = Util.isLenovo() ? 2 : 8;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
        System.out.println("Server listening on " + serverSocket.getInetAddress());
    }

    public PlayServer(net.Solver solver) throws IOException {
        this();
        this.solver = solver;
    }

    public static PlayServer getPlayServer() throws IOException {
        if (instance == null) {
            instance = new PlayServer();
        }
        return instance;
    }

    static Cache getCache() {
        if (cache == null) {
            File cacheFile = new File("server-cache.obj");
            if (cacheFile.exists()) {
                try (ObjectInputStream fio = new ObjectInputStream(new FileInputStream(cacheFile))) {
                    cache = (Cache) fio.readObject();
                    cache.cacheFile = cacheFile;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    cacheFile.delete();
                    cache = new Cache(cacheFile);
                } catch (ClassCastException e) {
                    cacheFile.delete();
                    System.err.println("Renewing cache: " + e);
                    cache = new Cache(cacheFile);
                }
            } else {
                cache = new Cache(cacheFile);
            }
        }
        return cache;
    }

    public static Runnable runnable() {
        return () -> {
            try {
                PlayServer playServer = PlayServer.getPlayServer();
                playServer.run();
            } catch (IOException e) {
                throw new Error(e);
            }
        };
    }

    public static void main(String[] args) throws Exception {
        PlayServer server = PlayServer.getPlayServer();
        server.run();
    }

    public void run() throws IOException {
        if (solver == null) {
            solver = new Solver();
        }
        while (true) {
            Socket socket = serverSocket.accept();
            Req req = new Req(socket);
            executor.execute(req);
        }
    }

    public void setSolver(net.Solver solver) {
        this.solver = solver;
    }

    private static class Cache {
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
                    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
                    writer.writeObject(cache);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Solver implements net.Solver {

        @Override
        public int solve(Match m) {
            AiPlayer aiPlayer = new AiPlayer("", m);
            int maxDepth;
            maxDepth = Util.isLenovo() ? 10 : 15;
            return aiPlayer.calcMove(maxDepth);
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
                    System.out.println("Connection closed to " + this.socket.getInetAddress()
                            + " (" + (executor.getQueue().size() + executor.getActiveCount() - 1) + " remaining)");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
