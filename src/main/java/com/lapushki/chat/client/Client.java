package com.lapushki.chat.client;

import com.lapushki.chat.server.Connection;
import com.lapushki.chat.server.ConnectionListener;
import com.lapushki.chat.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

public class Client implements ConnectionListener {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static final String HOST = "localhost";
    private static final int PORT = 8081;

    Client() {
        Connection connection;
        Scanner scan = new Scanner(System.in);
        try {
            String msg = "";
            connection = new Connection(this, HOST, PORT);
            while(!msg.equals("exit")) {
                msg = scan.nextLine();
                connection.sendMessage(msg);
            }
            connection.disconnect();
        }catch (IOException ex) {
            printMessage("Connection exception: "+ex);
        }
    }

    @Override
    public synchronized void onConnectionReady(Connection connection) {
        printMessage("Connection opened");
        log.info("Connection opened: " + connection.getSocket().getInetAddress());
    }

    @Override
    public synchronized void onReceiveString(Connection connection, String message) {
        printMessage(message);
        log.info("Receive message: " + message + ", from " + connection.getSocket().getInetAddress());
    }

    @Override
    public synchronized void onDisconnect(Connection connection) {
        printMessage("Connection closed");
        log.info("Connection closed: " + connection.getSocket().getInetAddress());
    }

    @Override
    public synchronized void onException(Connection connection, Exception ex) {
        printMessage("Connection exception: "+ex);
        log.error("Exception " + ex);
    }

    private synchronized void printMessage(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        new Client();
    }
}