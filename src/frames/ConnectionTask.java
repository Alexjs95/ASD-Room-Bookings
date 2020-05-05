package frames;

import utils.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Socket;

public class ConnectionTask implements Runnable {
    Socket socket;
    Server server;
    DataInputStream input;
    DataOutputStream output;

    // Constructor
    public ConnectionTask(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());   //input and output data streams
            output = new DataOutputStream(socket.getOutputStream());

            while (true) {      // infinite loop waiting for messages
                String msg = input.readUTF();       // get message from client
                server.msgToAll(msg);     // send message using server msgToAll method
            }
        } catch (EOFException e) {
            System.out.println("Connection closed");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    //send message to client
    public void sendMsg(String message) {
        try {
            output.writeUTF(message);   // used to send message to the client.
            output.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
