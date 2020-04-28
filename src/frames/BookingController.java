package frames;

import dbtools.Bookings;
import dbtools.Employee;
import dbtools.Rows;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import utils.ConnectionUtil;

import javax.swing.*;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.logging.Filter;

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
    @FXML DatePicker dpDateFilter;
    @FXML Button btnResetFilter;
    @FXML ComboBox<String> cbTypeFilter;

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

    private ObservableList<String> typeOptions = FXCollections.observableArrayList(
            "Lecture Theatre",
            "Computer lab",
            "Seminar room",
            "Meeting space"
    );

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
            ReadBookingTask task = new ReadBookingTask(socket, this);
            Thread thread = new Thread(task);
            thread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        TableData tableData = new TableData();
        data = tableData.getData();
        tableView.setItems(data);

        cbTypeFilter.setItems(typeOptions);

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

                tableBookings.setItems(bookings);
            }
        });

        dpDateFilter.setOnAction((ActionEvent -> {
            LocalDate date = dpDateFilter.getValue();       // get dates from date pickers

            FilteredList<Rows> filteredList = new FilteredList<>(data, p-> true);
            filteredList.setPredicate(rows -> {
                if (rows.getDate().equals(date.toString())) {       // compare date to selected data
                    return true;
                } else {
                    return false;
                }
            });
            SortedList<Rows> sortedList = new SortedList<>(filteredList);   // wrap filtered list into sorted list
            sortedList.comparatorProperty().bind(tableView.comparatorProperty());
            tableView.setItems(sortedList);

        }));

        btnResetFilter.setOnAction((ActionEvent -> {
            tableView.setItems(data);
        }));

        cbTypeFilter.setOnAction((ActionEvent -> {
            String type = cbTypeFilter.getValue();
            FilteredList<Rows> filteredList = new FilteredList<>(data, p-> true);
            filteredList.setPredicate(rows -> {
                if (rows.getType().equals(type)) { // compare type of room in table to selected type.
                    return true;
                } else {
                    return false;
                }
            });
            SortedList<Rows> sortedList = new SortedList<>(filteredList);
            sortedList.comparatorProperty().bind(tableView.comparatorProperty());
            tableView.setItems(sortedList);
        }));

        btnBook.setOnAction((ActionEvent event) -> {
            String bookingFor = txtName.getText();
            String contact = txtContact.getText();
            String notes = txtNotes.getText();
            Boolean am = chkAM.isSelected();
            Boolean pm = chkPM.isSelected();


            if (bookingFor.isEmpty() || contact.isEmpty() || notes.isEmpty() || !am && !pm) {
                callPopup("You must enter who the booking is for, select whether it will be AM or PM or both " +
                        " enter contact details and include notes.", "Missing details");
            } else {
                String makeBooking = "INSERT into roombookingsystem.bookings (ROOM_ID, AVAILABILITY_ID, EMPLOYEE_ID, BOOKED_FOR, CONTACT, NOTES) VALUES (?,?,?,?,?,?)";
                String updateAvailabilities = "UPDATE roombookingsystem.availability SET AM = ?, PM = ? WHERE availability_ID = ?";

                try {
                    if (am && pm) { // booking for AM and PM
                        am = false;
                        pm = false;
                        notes = notes + " Booked for AM & PM";
                    } else if (am) {    // Booking just for AM
                        am = false;     // No longer available.
                        notes = notes + " Booked for AM";
                        if (selectedItem.isPm()) {   // Check if PM in is available.
                            pm = true;  // Change to true as room is available and not being booked.
                        }
                    } else if (pm) {    // Booking room at PM.
                        pm = false;     // room becomes unavailable.
                        notes = notes + " Booked for PM";
                        if (selectedItem.isAm()) {  // Check whether AM is available.
                            am = true;      // set am to true so it stays available.
                        }
                    }

                    PreparedStatement ps3 = conn.prepareStatement(makeBooking);
                    ps3.setInt(1, room_id);
                    ps3.setInt(2, avail_id);
                    ps3.setInt(3, empID);
                    ps3.setString(4, bookingFor);
                    ps3.setString(5, contact);
                    ps3.setString(6, notes);

                    ps3.execute();

                    PreparedStatement ps4 = conn.prepareStatement(updateAvailabilities);
                    ps4.setBoolean(1, am);
                    ps4.setBoolean(2, pm);
                    ps4.setInt(3, avail_id);

                    ps4.execute();

                    output.writeUTF("RefreshTable");
                    output.flush();

                    resetForm();

                } catch (Exception e) {
                    callPopup("Booking failed", "Booking failed");
                    System.out.println(e);
                }
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

    static void callPopup(String message, String title) {
        LoginController.popup(message, title);
    }
}