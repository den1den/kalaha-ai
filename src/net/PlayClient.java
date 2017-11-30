package net;

import marblegame.Match;
import marblegame.MatchBuilder;
import marblegame.Util;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class PlayClient {
    public static int timeout = 3000;

    private final SocketAddress target;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public PlayClient(String host) {
        this(host, 6020);
    }

    public PlayClient(String host, int port) {
        target = new InetSocketAddress(host, port);
        socket = new Socket();
        connect();
    }

    public static void main(String[] args) throws IOException {
        String host;
        if (args.length >= 1) {
            host = args[0];
        } else {
            host = Util.isLenovo() ? "vandenbrand.eu" : "localhost";
        }
        new PlayClient(host).run();
    }

    private void connect() {
        long t0 = System.currentTimeMillis();
        try {
            System.out.println("Connecting to " + target);
            socket.connect(target, timeout);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            long t1 = System.currentTimeMillis();
            System.out.println("Connected to " + target + " (in " + (t1 - t0) + "ms)");
        } catch (SocketTimeoutException e) {
            System.err.println("Could not connect to " + target + " (timeout after " + timeout + "ms)");
        } catch (UnknownHostException e) {
            System.err.println("Could not find host " + target);
        } catch (Exception e) {
            System.err.println("Could not connect to " + target + " " + e.getMessage());
        }
    }

    private void run() throws IOException {
        try {
            Match m;
            int response;
            m = getMatch();
            while (!m.isPlayerWinner()) {
                response = getResponseImpl(m);
                m.move(response);
                System.out.println("move = " + response);
            }
            writer.println(PlayServer.MSG_CLOSE);
        } finally {
            socket.close();
        }
    }

    public boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }

    public int getResponse(Match m) {
        try {
            return getResponseImpl(m);
        } catch (IOException e) {
            System.err.println("Client could not get response");
            e.printStackTrace();
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    System.err.println("Socket could not be closed");
                    e1.printStackTrace();
                }
            }
            return -1;
        }
    }

    private int getResponseImpl(Match m) throws IOException {
        ensureOpenSocket();
        JSONObject jsonObject = Match.Serializer.toJson(m);
        String json = jsonObject.toJSONString();
        writer.println(json);
        String response = reader.readLine();
        try {
            int result = Integer.parseInt(response);
            return result;
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    private void ensureOpenSocket() throws IOException {
        if (socket.isClosed()) {
            socket = new Socket();
        }
        if (!socket.isConnected()) {
            connect();
        }
    }

    private Match getMatch() {
        return new MatchBuilder(2).createMatch();
    }
}
