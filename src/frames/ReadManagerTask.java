package frames;

import dbtools.Rows;
import javafx.collections.ObservableList;

import java.io.DataInputStream;
import java.net.Socket;

public class ReadManagerTask implements Runnable {
    Socket socket;
    ManagerController Mclient;
    DataInputStream input;


    public ReadManagerTask(Socket socket, ManagerController client) {
        this.socket = socket;
        this.Mclient = client;
    }

    @Override
    public void run() {
        while (true) {      // infinite loop for listening.
            try {
                input = new DataInputStream(socket.getInputStream());       // new input
                String msg = input.readUTF();       // get message from input sent from client.
                System.out.println(msg);
                if (msg.equals("RefreshTable")) {   // check the contents of message, new booking so reset tableview.
                    Mclient.tableView.setItems(null);
                    ObservableList<Rows> data;
                    TableData tableData = new TableData();
                    data = tableData.getData();
                    Mclient.resetForm();
                    Mclient.setTableView(data);
                }
            } catch (Exception ex) {
                System.out.println("error reading from server");
                ex.printStackTrace();
                break;
            }
        }
    }
}
