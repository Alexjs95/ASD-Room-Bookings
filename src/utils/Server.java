package utils;

import frames.ConnectionTask;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    List<ConnectionTask> lstConnections = new ArrayList<ConnectionTask>();

    public static void main(String args[]) {
        Server server = new Server();
        server.start();
    }

    public void start() {
        // Create a new thread
        new Thread(() -> {
            try {
                ServerSocket ss = new ServerSocket(ConnectionUtil.port);
                while (true) {  // Loops to listen for a connection req, then adds to list.
                    Socket socket = ss.accept();
                    ConnectionTask connection = new ConnectionTask(socket, this);   // Create new connection
                    lstConnections.add(connection);
                    System.out.println("Connection made to: " + connection);
                    Thread thread = new Thread(connection); // Create a new thread of that connection.
                    thread.start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void msgToAll(String message) {      // method used to send message to all connected clients.
        for (ConnectionTask connection : this.lstConnections) {
            connection.sendMsg(message);
        }
    }
}
