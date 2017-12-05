package net;

import marblegame.Util;
import marblegame.gamemechanics.Match;
import marblegame.gamemechanics.MatchBuilder;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class PlayClient {
    static int timeout = 3000;

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
    }

    public static void main(String[] args) throws IOException {
        String host;
        if (args.length >= 1) {
            host = args[0];
        } else {
            host = Util.isLenovo() ? "vandenbrand.eu" : "localhost";
        }
        PlayClient testClient = new PlayClient(host);

        try {
            int response;
            int result;
            Match m = new MatchBuilder(2).createMatch();
            do {
                response = testClient.getResponseImpl(m);
                result = m.move(response);
                if (result == Match.MOVE_RESULT_WIN) {
                    System.out.println("winning move = " + response);
                    break;
                } else if (m.isPad()) {
                    System.out.println("blocked opponent = " + response);
                    break;
                }
            } while (true);
        } finally {
            testClient.socket.close();
        }
    }

    public void connect() {
        try {
            connectImpl();
        } catch (SocketTimeoutException e) {
            System.err.println("Could not connect to " + target + " (timeout after " + timeout + "ms)");
        } catch (UnknownHostException e) {
            System.err.println("Could not find host " + target);
        } catch (Exception e) {
            System.err.println("Could not connect to " + target + " " + e.getMessage());
        }
    }

    public void connectImpl() throws IOException {
        long t0 = System.currentTimeMillis();
        System.out.println("Connecting to " + target);
        socket.connect(target, timeout);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        long t1 = System.currentTimeMillis();
        System.out.println("Connected to " + target + " (in " + (t1 - t0) + "ms)");
    }

    public boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }

    public int getResponse(Match m) {
        try {
            return getResponseImpl(m);
        } catch (IOException e) {
            System.err.println("Client could not get response (" + e + "), closing socket");
            //e.printStackTrace();
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

    public int getResponseImpl(Match m) throws IOException {
        ensureOpenSocket();
        JSONObject jsonObject = Match.Serializer.toJson(m);
        String json = jsonObject.toJSONString();
        writer.println(json);
        String response = reader.readLine();
        if (response == null) {
            socket.close();
            throw new IOException("reader stream was finished");
        }
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
            System.out.println("ensureOpenSocket: creating new socket");
        }
        try {
            if (!socket.isConnected()) {
                connectImpl();
            }
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }
}
