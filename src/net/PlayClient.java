package net;

import marblegame.Match;
import marblegame.MatchBuilder;
import marblegame.Util;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class PlayClient {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public PlayClient() throws IOException {
        String host;
        host = Util.isLenovo() ? "vandenbrand.eu" : "localhost";
        int port = 6020;
        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to " + host + ":" + port);
        } catch (ConnectException e) {
            System.err.println("Could not connect to " + host + ":" + port);
        }
    }

    public static void main(String[] args) throws IOException {
        new PlayClient().run();
    }

    private void run() throws IOException {
        if (reader == null) {
            return;
        }
        try {
            Match m;
            int response;
            m = getMatch();
            while (!m.isPlayerWinner()) {
                response = getResponse(m);
                m.move(response);
                System.out.println("move = " + response);
            }
            writer.println(PlayServer.MSG_CLOSE);
        } finally {
            socket.close();
        }
    }

    private int getResponse(Match m) throws IOException {
        JSONObject jsonObject = Match.Serializer.toJson(m);
        String json = jsonObject.toJSONString();
        writer.println(json);
        String response = reader.readLine();
        int result = Integer.parseInt(response);
        return result;
    }

    private Match getMatch() {
        return new MatchBuilder(2).createMatch();
    }
}
