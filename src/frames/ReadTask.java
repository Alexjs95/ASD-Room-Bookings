package frames;

import dbtools.Rows;
import javafx.collections.ObservableList;

import java.io.DataInputStream;
import java.net.Socket;

public class ReadTask implements Runnable {
    Socket socket;
    BookingController client;
    ManagerController clientM;
    DataInputStream input;

    // Constructor
    public ReadTask(Socket socket, BookingController client) {
        this.socket = socket;
        this.client = client;
    }

    public ReadTask(Socket socket, ManagerController client) {
        this.socket = socket;
        this.clientM = client;
    }

    @Override
    public void run() {
        while (true) {      // infinite loop for listening.
            try {
                input = new DataInputStream(socket.getInputStream());       // new input
                String msg = input.readUTF();       // get message from input sent from client.
                System.out.println(msg);
                if (msg.equals("BookingAdded")) {   // check the contents of message, new booking so reset tableview.
                    client.tableView.setItems(null);
                    ObservableList<Rows> data;
                    TableData tableData = new TableData();

                    data = tableData.getData();    //get data
                    client.setTableView(data);       // send data to client.
                    clientM.setTableView(data);
                }
            } catch (Exception ex) {
                System.out.println("error reading from server");
                ex.printStackTrace();
                break;
            }
        }
    }
}
