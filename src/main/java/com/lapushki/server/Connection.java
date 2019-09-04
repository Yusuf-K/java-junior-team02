package com.lapushki.server;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.Collection;

public class Connection {

    private final Socket socket;
    private final Thread thread;
    private final ConnectionListener listener;
    private final BufferedReader in;
    private final BufferedWriter out;

    Connection(ConnectionListener listener, String ip, int port) throws IOException {
        this(listener, new Socket(ip, port));
    }

    Connection(ConnectionListener listener, Socket socket) throws IOException {
        this.listener = listener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Charset.forName("Unicode");
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        thread = new Thread((new Runnable() {
            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        String msg = in.readLine();
                        listener.onReceivedMessage(Connection.this, msg);
                    }
                } catch (IOException ex) {
                    listener.onException(Connection.this, ex);
                } finally {
                    listener.onDisconnect(Connection.this);
                }
            }
        }));
        thread.start();
    }

    public synchronized void sendMessage(Message msg) {
        try {
            out.write(msg + "\r\n");
            out.flush();
        } catch (IOException e) {
            listener.onException(Connection.this, e);
            disconnect();
        }
    }

    public synchronized void sendMessage(Collection<Message> msgs) {
        try {
            out.write(msgs + "\r\n");
            out.flush();
        } catch (IOException e) {
            listener.onException(Connection.this, e);
            disconnect();
        }
    }


    public synchronized void disconnect() {
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onException(Connection.this, e);
        }
    }

    @Override
    public String toString() {
        return "Connection: " + socket.getInetAddress() + ": " + socket.getPort();
    }

}
