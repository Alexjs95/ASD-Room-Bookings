package frames;

import com.mysql.cj.protocol.Resultset;
import dbtools.Bookings;
import dbtools.Employee;
import dbtools.Rows;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import utils.ConnectionUtil;

import javax.xml.transform.Result;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BookingController extends Thread implements Initializable    {
    @FXML public TableView<Rows> tableView = new TableView<Rows>();
    @FXML public TableColumn dateCol = new TableColumn();
    @FXML public TableColumn roomnameCol = new TableColumn();
    @FXML public TableColumn roomtypeCol = new TableColumn();
    @FXML public TableColumn roomsizeCol = new TableColumn();
    @FXML public TableColumn termCol = new TableColumn();
    @FXML public TableColumn amCol = new TableColumn();
    @FXML public TableColumn pmCol = new TableColumn();

    @FXML public TableView<Bookings> tableBookings = new TableView<>();
    @FXML public TableColumn dateCol2 = new TableColumn();
    @FXML public TableColumn nameCol = new TableColumn();
    @FXML public TableColumn bookingForCol = new TableColumn();
    @FXML public TableColumn contactCol = new TableColumn();
    @FXML public TableColumn notesCol = new TableColumn();


    @FXML Label lblUser;
    @FXML Label lblDate;
    @FXML Label lblRoom;
    @FXML Label lblID;
    @FXML TextField txtName;
    @FXML TextField txtNotes;
    @FXML TextField txtContact;
    @FXML CheckBox chkAM;
    @FXML CheckBox chkPM;
    @FXML Button btnBook;

    private ObservableList<Rows> data;
    private ObservableList<Bookings> bookings;

    Employee currUser;
    int empID;
    Stage dialog = new Stage();
    Scene scene;

    Connection conn;
    PreparedStatement ps = null;
    ResultSet rsRooms = null;
    ResultSet rsAvailable = null;
    ResultSet rsBookings = null;
    Rows selectedItem;
    DataOutputStream output = null;

    int avail_id;
    int room_id;

    public BookingController() {
        conn = ConnectionUtil.connectDB();
    }



    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        // Links the columns to the table in the fxml file.
        dateCol.setCellValueFactory(new PropertyValueFactory<Rows, String>("date"));
        roomnameCol.setCellValueFactory(new PropertyValueFactory<Rows, String>("roomname"));
        roomtypeCol.setCellValueFactory(new PropertyValueFactory<Rows, String>("type"));
        roomsizeCol.setCellValueFactory(new PropertyValueFactory<Rows, Integer>("size"));
        termCol.setCellValueFactory(new PropertyValueFactory<Rows, Boolean>("term"));
        amCol.setCellValueFactory(new PropertyValueFactory<Rows, Boolean>("am"));
        pmCol.setCellValueFactory(new PropertyValueFactory<Rows, Boolean>("pm"));

        dateCol2.setCellValueFactory(new PropertyValueFactory<Bookings, String>("date"));
        nameCol.setCellValueFactory(new PropertyValueFactory<Bookings, String>("roomname"));
        bookingForCol.setCellValueFactory(new PropertyValueFactory<Bookings, String>("booked_for"));
        contactCol.setCellValueFactory(new PropertyValueFactory<Bookings, String>("contact"));
        notesCol.setCellValueFactory(new PropertyValueFactory<Bookings, String>("notes"));

        try {
            Socket socket = new Socket(ConnectionUtil.host, ConnectionUtil.port);
            output = new DataOutputStream(socket.getOutputStream());
            ReadTask task = new ReadTask(socket, this);
            Thread thread = new Thread(task);
            thread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getData();
        tableView.setItems(data);

        // Row click event
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (tableView.getSelectionModel().getSelectedItem() != null) {
                resetForm();
                selectedItem = tableView.getSelectionModel().getSelectedItem();
                String date = selectedItem.getDate();
                String room = selectedItem.getRoomname();
                lblDate.setText(date);
                lblRoom.setText(room);

                //disable check boxes if the value is false in the row.
                if (!selectedItem.isAm()) {
                    chkAM.setDisable(true);
                }
                if (!selectedItem.isPm()) {
                    chkPM.setDisable(true);
                }
                if (!selectedItem.isPm() && !selectedItem.isPm()) {     // If both am is pm is false
                    txtNotes.setDisable(true);      // then no booking can be made for that day
                    txtName.setDisable(true);
                    txtContact.setDisable(true);
                }

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
                getBookings(room_id, avail_id, date, room);

                bookings.forEach((bookings) -> {
                    System.out.println(bookings.getDate());
                    System.out.println(bookings.getRoomname());
                    System.out.println(bookings.getBooked_for());
                    System.out.println(bookings.getContact());
                    System.out.println(bookings.getNotes());
                });
                System.out.println(bookings);

                tableBookings.setItems(bookings);
            }
        });

        btnBook.setOnAction((ActionEvent event) -> {
            String bookingFor = txtName.getText();
            String contact = txtContact.getText();
            String notes = txtNotes.getText();
            Boolean am = chkAM.isSelected();
            Boolean pm = chkPM.isSelected();

            // Ensure all values contain something
            // ensure at least 1 check box is selected
            // dont allow both check boxes to be selected



            String makeBooking = "INSERT into roombookingsystem.bookings (ROOM_ID, AVAILABILITY_ID, EMPLOYEE_ID, BOOKED_FOR, CONTACT, NOTES) VALUES (?,?,?,?,?,?)";
            //String getAvailID = "SELECT * FROM roombookingsystem.availability where DATE = ?";
            //String getRoomID = "SELECT * FROM roombookingsystem.rooms where NAME = ?";
            String updateAvailabilities = "UPDATE roombookingsystem.availability SET AM = ?, PM = ? WHERE availability_ID = ?";

            try {
                PreparedStatement ps3 = conn.prepareStatement(makeBooking);
                ps3.setInt(1, room_id);
                ps3.setInt(2, avail_id);
                ps3.setInt(3, empID);
                ps3.setString(4, bookingFor);
                ps3.setString(5, contact);
                ps3.setString(6, notes);

                ps3.execute();

                if (am && pm) { // Booking for AM and PM
                    am = false;
                    pm = false;
                } else if (am) {    // Booking just for AM
                    am = false;     // No longer available.
                    if (selectedItem.isPm()) {   // Check if PM in is available.
                        pm = true;  // Change to true as room is available and not being booked.
                    }
                } else if (pm) {    // Booking room at PM.
                    pm = false;     // room becomes unavailable.
                    if (selectedItem.isAm()) {  // Check whether AM is available.
                        am = true;      // set am to true so it stays available.
                    }
                }


                PreparedStatement ps4 = conn.prepareStatement(updateAvailabilities);
                ps4.setBoolean(1, am);
                ps4.setBoolean(2, pm);
                ps4.setInt(3, avail_id);

                ps4.execute();

                output.writeUTF("BookingAdded");
                output.flush();

                resetForm();

            } catch (Exception e) {
                System.out.println(e);
            }
        });

//        data.addListener((ListChangeListener<Rows>) change -> {
//            while (change.next()) {
//                tableView.setItems(data);
//            }
//        });
    }

    void setUser(Employee employee, int id) {
        lblUser.setText(employee.getUsername());
        empID = id;
    }

    void resetForm() {
        lblRoom.setText("");
        lblDate.setText("");
        txtContact.setText("");
        txtName.setText("");
        txtNotes.setText("");
        chkAM.setDisable(false);
        chkPM.setDisable(false);
        chkAM.setSelected(false);
        chkPM.setSelected(false);
        txtContact.setDisable(false);
        txtName.setDisable(false);
        txtNotes.setDisable(false);
    }

    public void setTableView(ObservableList list) {
        data = list;
        tableView.setItems(list);
    }

    public void getBookings(int room_id, int avail_id, String date, String name) {
        List list = new ArrayList();

        String getBookings = "SELECT * FROM roombookingsystem.bookings WHERE ROOM_ID = ? AND AVAILABILITY_ID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(getBookings);
            ps.setInt(1, room_id);
            ps.setInt(2, avail_id);
            ResultSet rs;
            rs = ps.executeQuery();
            while(rs.next()) {
                Bookings booking = new Bookings();
                booking.setDate(date);
                booking.setRoomname(name);
                booking.setBooked_for(rs.getString(5));
                booking.setContact(rs.getString(6));
                booking.setNotes(rs.getString(7));
                list.add(booking);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        bookings = FXCollections.observableList(list);
    }


    public ObservableList getData() {
        List list = new ArrayList();

        String queryRooms = "SELECT * FROM roombookingsystem.rooms";
        String queryAvailable = "SELECT * FROM roombookingsystem.availability";     // change to only today and after
        String queryBookings = "SELECT * FROM roombookingsystem.bookings";

        try {
            ps = conn.prepareStatement(queryRooms);
            rsRooms = ps.executeQuery();
            ps.clearParameters();
            ps = conn.prepareStatement(queryAvailable);
            rsAvailable = ps.executeQuery();
            ps.clearParameters();
            ps = conn.prepareStatement(queryBookings);
            rsBookings = ps.executeQuery();
            ps.clearParameters();

            // Loops through all availabilities
            while(rsAvailable.next()) {
                Rows row = new Rows();
                row.setDate(rsAvailable.getString(2));
                row.setAm(rsAvailable.getBoolean(4));
                row.setPm(rsAvailable.getBoolean(5));
                row.setTerm(rsAvailable.getBoolean(3));

                rsRooms.beforeFirst();
                // Each available date is for each room so loop through all rooms.
                while (rsRooms.next()) {
                    row.setRoomname(rsRooms.getString(2));
                    row.setSize(rsRooms.getInt(3));
                    row.setType(rsRooms.getString(4));
                    list.add(row);
                }
            }
        }catch (Exception e) {
            System.out.println(e);
        }
        // convert list to observable list
        data = FXCollections.observableList(list);
        return data;
    }
}