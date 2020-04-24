package frames;

import dbtools.Employee;
import dbtools.Holiday;
import dbtools.Room;
import dbtools.Rows;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utils.ConnectionUtil;

import java.io.DataOutputStream;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ManagerController extends Thread implements Initializable {
    @FXML
    public TableView<Rows> tableView = new TableView<Rows>();
    @FXML public TableColumn dateCol = new TableColumn();
    @FXML public TableColumn roomnameCol = new TableColumn();
    @FXML public TableColumn roomtypeCol = new TableColumn();
    @FXML public TableColumn roomsizeCol = new TableColumn();
    @FXML public TableColumn termCol = new TableColumn();
    @FXML public TableColumn amCol = new TableColumn();
    @FXML public TableColumn pmCol = new TableColumn();

    @FXML Pane paneNewRoom;
    @FXML Label lblUser;
    @FXML Label lblRoom;
    @FXML TextField txtName;
    @FXML TextField txtSize;
    @FXML TextField txtType;
    @FXML TextField txtName1;

    @FXML TextField txtNotes;
    @FXML CheckBox chkAM;
    @FXML CheckBox chkPM;
    @FXML ChoiceBox cbDays;
    @FXML ChoiceBox cbWeeks;
    @FXML DatePicker dpStart;
    @FXML DatePicker dpEnd;

    @FXML Button btnBook;
    @FXML Button btnNewRoom;
    @FXML Button btnAddRoom;
    @FXML Button btnRemoveRoom;
    @FXML Button btnAddHols;



    Employee currUser;
    int empID;
    int avail_id;
    int room_id;

    Stage dialog = new Stage();
    Scene scene;

    Connection conn;
    PreparedStatement ps = null;
    Rows selectedItem;
    DataOutputStream output = null;

    private ObservableList<Rows> data;

    public ManagerController() {
        conn = ConnectionUtil.connectDB();
    }

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Links the columns to the table in the fxml file.
        dateCol.setCellValueFactory(new PropertyValueFactory<Rows, String>("date"));
        roomnameCol.setCellValueFactory(new PropertyValueFactory<Rows, String>("roomname"));
        roomtypeCol.setCellValueFactory(new PropertyValueFactory<Rows, String>("type"));
        roomsizeCol.setCellValueFactory(new PropertyValueFactory<Rows, Integer>("size"));
        termCol.setCellValueFactory(new PropertyValueFactory<Rows, Boolean>("term"));
        amCol.setCellValueFactory(new PropertyValueFactory<Rows, Boolean>("am"));
        pmCol.setCellValueFactory(new PropertyValueFactory<Rows, Boolean>("pm"));

        try {
            Socket socket = new Socket(ConnectionUtil.host, ConnectionUtil.port);
            output = new DataOutputStream(socket.getOutputStream());
            ReadManagerTask task = new ReadManagerTask(socket, this);
            Thread thread = new Thread(task);
            thread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        TableData tableData = new TableData();
        data = tableData.getData();
        tableView.setItems(data);

        // Row click event
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (tableView.getSelectionModel().getSelectedItem() != null) {
                selectedItem = tableView.getSelectionModel().getSelectedItem();
                String room = selectedItem.getRoomname();
                String date = selectedItem.getDate();
                lblRoom.setText(room);



                String getAvailID = "SELECT * FROM roombookingsystem.availability where DATE = ?";
                String getRoomID = "SELECT * FROM roombookingsystem.rooms where NAME = ?";
                try {
                    PreparedStatement ps = conn.prepareStatement(getAvailID);
                    ps.setString(1, date);
                    ResultSet rs = ps.executeQuery();
                    rs.first();
                    avail_id = rs.getInt("AVAILABILITY_ID");

                    PreparedStatement ps2 = conn.prepareStatement(getRoomID);
                    ps2.setString(1, room);
                    ResultSet rs2 = ps2.executeQuery();

                    rs2.first();
                    room_id = rs2.getInt("ROOM_ID");

                } catch (Exception e) {
                    e.printStackTrace();
                }



            }
        });
        btnNewRoom.setOnAction((ActionEvent event) -> {
            paneNewRoom.setVisible(true);
        });

        btnAddRoom.setOnAction((ActionEvent event) -> {
            String name = txtName1.getText();
            String size = txtSize.getText();
            String type = txtType.getText();

            Room room = new Room();
            room.setRoomtype(type);
            //room.setSize(size);
        });

        btnRemoveRoom.setOnAction((ActionEvent event) -> {

        });

        btnBook.setOnAction((ActionEvent event) -> {

        });

        btnAddHols.setOnAction((ActionEvent event) -> {
            LocalDate start = dpStart.getValue();       // get dates from date pickers
            LocalDate end = dpEnd.getValue();
            LocalDate date = start;     // set the date as the holiday starting date

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  //formats to SQL format

            if (start == null || end == null) {
                callPopup("Ensure you have selected a start and end date for term.", "Missing term date");
            } else {

                Holiday holiday = new Holiday();        // create a holiday
                holiday.setStart(dtf.format(start));
                holiday.setEnd(dtf.format(end));

                String query = "INSERT INTO roombookingsystem.holidays (START, END) VALUES (?,?)";      // inserts holiday
                String query2 = "UPDATE roombookingsystem.availability SET TERMTIME = ?, AM = ?, PM = ? WHERE DATE = ?";  // updates availabilities

                try {
                    PreparedStatement ps = conn.prepareStatement(query);    //code for adding holiday to db
                    ps.setString(1, holiday.getStart());
                    ps.setString(2, holiday.getEnd());
                    ps.execute();
                    ps.close();

                    ps = conn.prepareStatement(query2);
                    while (date.isBefore(end) || date.equals(end)) {    // loops through the dates between start and end
                        ps.setBoolean(1, false);        // false for holidays
                        ps.setBoolean(2, true); // true for am available
                        ps.setBoolean(3, true); // true for pm available
                        ps.setString(4, dtf.format(date));
                        ps.addBatch();

                        date = date.plusDays(1);        // increment date by 1 day
                    }
                    ps.executeBatch();
                    output.writeUTF("BookingAdded");        // inform server of change
                    output.flush();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void setTableView(ObservableList list) {
        data = list;
        tableView.setItems(list);
    }

    public void resetForm() {
        btnRemoveRoom.setDisable(false);

    }

    static void callPopup(String message, String title) {
        LoginController.popup(message, title);
    }
}
