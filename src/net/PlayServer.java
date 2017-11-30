package net;

import marblegame.Match;
import marblegame.Util;
import marblegame.players.AiPlayer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PlayServer {
    static final String MSG_CLOSE = "close";
    private boolean running = true;
    private ServerSocket serverSocket;
    private ThreadPoolExecutor executor;

    public PlayServer() throws IOException {
        serverSocket = new ServerSocket(6020);
        int nThreads;
        nThreads = Util.isLenovo() ? 2 : 8;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
    }

    private static File cacheFile;
    private static Map<String, Integer> cache = null;

    static {
        cacheFile = new File("server-cache.obj");
        if (cacheFile.exists()) {
            try (ObjectInputStream fio = new ObjectInputStream(new FileInputStream(cacheFile))) {
                cache = (ConcurrentHashMap) fio.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
        }
    }

    public static int doAiMove(Match a) {
        AiPlayer aiPlayer = new AiPlayer("Server", a);
        int maxDepth;
        maxDepth = Util.isLenovo() ? 10 : 15;
        int move = aiPlayer.calcMove(maxDepth);
        return move;
    }

    public static void main(String[] args) throws IOException {
        new PlayServer().run();
    }

    void run() throws IOException {
        System.out.println("Server is running on " + serverSocket.getInetAddress());
        while (running) {
            Socket socket = serverSocket.accept();
            executor.execute(new Req(socket));
        }
    }

    private void saveCache() {
        try {
            cacheFile.createNewFile();
            try (
                    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
                synchronized (cache) {
                    writer.writeObject(cache);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Req implements Runnable {
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

        public void process(String read) throws ParseException {
            Integer move;
            if ((move = cache.get(read)) == null) {
                JSONObject jsonObject = (JSONObject) parser.parse(read);
                Match m = Match.Serializer.fromJSONObject(jsonObject);
                move = doAiMove(m);
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
                    if (MSG_CLOSE.equals(read) || read == null) {
                        saveCache();
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
