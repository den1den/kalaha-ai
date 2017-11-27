package net;

import marblegame.Match;
import marblegame.players.AiPlayer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlayServer {
    static final String MSG_CLOSE = "close";
    private boolean running = true;
    private ServerSocket serverSocket;
    private Executor executor;

    public PlayServer() throws IOException {
        serverSocket = new ServerSocket(6020);
        executor = Executors.newFixedThreadPool(1);
    }

    public static int doAiMove(Match a) {
        AiPlayer aiPlayer = new AiPlayer("Server", a);
        int move = aiPlayer.calcMove(14);
        return move;
    }

    public static void main(String[] args) throws IOException {
        new PlayServer().run();
    }

    void run() throws IOException {
        while (running) {
            System.out.println("Server listening...");
            Socket socket = serverSocket.accept();
            executor.execute(new Req(socket));
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
        }

        public void process(String read) throws ParseException {
            JSONObject jsonObject = (JSONObject) parser.parse(read);
            Match m = Match.Serializer.fromJSONObject(jsonObject);
            int move = doAiMove(m);
            writer.println(move);
        }

        @Override
        public void run() {
            String read;
            try {
                do {
                    read = reader.readLine();
                    if (MSG_CLOSE.equals(read) || read == null) {
                        break;
                    }
                    process(read);
                } while (true);
            } catch (IOException | ParseException e) {
                throw new Error(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
