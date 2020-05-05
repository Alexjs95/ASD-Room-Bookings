package frames;

import dbtools.Employee;
import dbtools.Holiday;
import dbtools.Room;
import dbtools.Rows;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    @FXML ComboBox<Integer> cbbSize;
    @FXML ComboBox<String> cbbType;
    @FXML TextField txtName1;

    @FXML TextField txtNotes;
    @FXML CheckBox chkAM;
    @FXML CheckBox chkPM;
    @FXML ChoiceBox cbDays;
    @FXML ChoiceBox cbWeeks;
    @FXML DatePicker dpStart;
    @FXML DatePicker dpEnd;
    @FXML DatePicker dpDateFilter;
    @FXML ComboBox<String> cbTypeFilter;

    @FXML Button btnBook;
    @FXML Button btnNewRoom;
    @FXML Button btnAddRoom;
    @FXML Button btnRemoveRoom;
    @FXML Button btnAddHols;
    @FXML Button btnResetFilter;
    @FXML Button btnLogout;


    Employee currUser;
    int empID;
    int avail_id;
    int room_id;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    Stage dialog = new Stage();
    Scene scene;

    Connection conn;
    PreparedStatement ps = null;
    Rows selectedItem;
    DataOutputStream output = null;

    private ObservableList<Rows> data;
    private ObservableList<String> typeOptions = FXCollections.observableArrayList(
        "Lecture Theatre",
        "Computer lab",
        "Seminar room",
        "Meeting space"
    );

    private ObservableList<Integer> lectureSizes = FXCollections.observableArrayList(
           50, 100, 150, 200
    );

    private ObservableList<Integer> pcLabSizes = FXCollections.observableArrayList(
            10, 20, 30, 40
    );

    private ObservableList<Integer> seminarSizes = FXCollections.observableArrayList(
            10, 20, 30, 40
    );

    private ObservableList<Integer> meetingSizes = FXCollections.observableArrayList(
            5, 10, 15, 20
    );

    private ObservableList<Integer> bookForDays = FXCollections.observableArrayList(
            1, 2, 3, 4, 5, 6, 7
    );

    private ObservableList<Integer> bookForWeeks = FXCollections.observableArrayList(
            1, 2, 3, 4
    );



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

        cbbType.setItems(typeOptions);      // add options to combo boxes
        cbDays.setItems(bookForDays);
        cbWeeks.setItems(bookForWeeks);
        cbTypeFilter.setItems(typeOptions);


        // Row click event
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (tableView.getSelectionModel().getSelectedItem() != null) {
                selectedItem = tableView.getSelectionModel().getSelectedItem();
                String room = selectedItem.getRoomname();
                String date = selectedItem.getDate();
                lblRoom.setText("Selected room: " + room);

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

                txtName1.setDisable(false);
                txtNotes.setDisable(false);
                chkAM.setDisable(false);
                chkPM.setDisable(false);
                cbDays.setDisable(false);
                cbWeeks.setDisable(false);
                btnBook.setDisable(false);
                btnRemoveRoom.setDisable(false);
            }
        });
        btnNewRoom.setOnAction((ActionEvent event) -> {
            paneNewRoom.setVisible(true);
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
            resetForm();
        }));

        btnResetFilter.setOnAction((ActionEvent -> {
            dpDateFilter.getEditor().clear();
            cbTypeFilter.getSelectionModel().clearSelection();
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
            resetForm();
            resetForm();

        }));



        btnAddRoom.setOnAction((ActionEvent event) -> {
            String name = txtName.getText();
            String type = cbbType.getValue();
            int size = 0;
            try {
                size = cbbSize.getValue();
            } catch (Exception e) { }


            if (name.equals("") || type.equals("") || size == 0) {
                callPopup("To add new room you must enter the name, select the type of room and the size", "New room error");
            } else {
                Room room = new Room();
                room.setRoomname(name);
                room.setRoomtype(type);
                room.setSize(size);

                String query = "INSERT into roombookingsystem.rooms (NAME, SIZE, TYPE) VALUES (?, ?, ?)";
                try {
                    PreparedStatement ps = conn.prepareStatement(query);    //code for adding holiday to db
                    ps.setString(1, room.getRoomname());
                    ps.setInt(2, room.getSize());
                    ps.setString(3, room.getRoomtype());
                    ps.execute();
                    ps.close();

                    callPopup("Room sucessfully added to system", "Room added");
                    resetForm();
                    output.writeUTF("RefreshTable");        // inform server of change
                    output.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnRemoveRoom.setOnAction((ActionEvent event) -> {
            String room = selectedItem.getRoomname();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + room + " and associated bookings? ", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            String delBookings = "DELETE FROM roombookingsystem.bookings WHERE ROOM_ID = ?";
            String delRoom = "DELETE FROM roombookingsystem.rooms WHERE ROOM_ID = ?";
            if (alert.getResult() == ButtonType.YES) {      // check users choice on the alert
                try {
                    PreparedStatement ps = conn.prepareStatement(delBookings);
                    ps.setInt(1, room_id);
                    ps.execute();       // remove bookings with room id
                    ps.clearParameters();

                    ps = conn.prepareStatement(delRoom);
                    ps.setInt(1, room_id);  // remove the room from db.
                    ps.execute();
                    ps.close();

                    output.writeUTF("RefreshTable");
                    output.flush();
                    callPopup(room + " has been deleted.", "Room deleted");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        cbbType.setOnAction((ActionEvent event) -> {
            String val = cbbType.getValue().toString();
            if (val.equals("Lecture Theatre")) {
                cbbSize.setItems(lectureSizes);
            } else if (val.equals("Computer lab")) {
                cbbSize.setItems(pcLabSizes);
            } else if (val.equals("Seminar room")) {
                cbbSize.setItems(seminarSizes);
            } else if (val.equals("Meeting space")) {
                cbbSize.setItems(meetingSizes);
            } else {
                cbbSize.setItems(null);
            }
        });

        btnBook.setOnAction((ActionEvent event) -> {
            String bookingFor = txtName1.getText();
            Boolean am = chkAM.isSelected();
            Boolean pm = chkPM.isSelected();
            String contact = lblUser.getText();
            String notes = txtNotes.getText();
            String date = selectedItem.getDate();
            LocalDate startDate = LocalDate.parse(date);
            LocalDate endDate = startDate;

            int days = 0;
            int weeks = 0;

            try {
                days = (int) cbDays.getValue();
            } catch (Exception e) { }
            try {
                weeks = (int) cbWeeks.getValue();
            } catch (Exception e) { }

            if (bookingFor.isEmpty() || notes.isEmpty() || (!am && !pm) || (days == 0 && weeks == 0)) {
                callPopup("You must enter who the booking is for, select whether it will be AM or PM or both " +
                        "and enter reasons why it'll be unavailable. Also for how long the room will be unavailable for in either days and/or weeks.", "Missing details");
            } else {
                String makeBooking = "INSERT into roombookingsystem.bookings (ROOM_ID, AVAILABILITY_ID, EMPLOYEE_ID, BOOKED_FOR, CONTACT, NOTES) VALUES (?,?,?,?,?,?)";
                String getAvailID = "SELECT * FROM roombookingsystem.availability where DATE = ?";
                String updateAvailabilities = "UPDATE roombookingsystem.availability SET AM = ?, PM = ? WHERE availability_ID = ?";

                if (days != 0 && weeks != 0) {      // if both days and weeks are not 0 then
                    endDate = endDate.plusDays(days);       // add days to end date
                    endDate = endDate.plusWeeks(weeks);     // add weeks to end date
                } else if (days != 0) {     // if only days is not empty
                    endDate = endDate.plusDays(days);       // only add days
                } else if (weeks != 0) {       // same for weeks
                    endDate = endDate.plusWeeks(weeks);
                }

                try {
                    PreparedStatement ps;
                    while (startDate.isBefore(endDate) || date.equals(endDate)) {       // loop between start and end
                        ps = conn.prepareStatement(getAvailID); // get availability id for the current date
                        ps.setString(1, startDate.toString());
                        ResultSet rs = ps.executeQuery();
                        rs.first();
                        int a_id = rs.getInt(1);
                        boolean getAM = rs.getBoolean(4);
                        boolean getPM = rs.getBoolean(5);

                        if (chkAM.isSelected() && chkPM.isSelected()) { // booking for AM and PM
                            am = false;
                            pm = false;
                        } else if (chkAM.isSelected()) {    // Booking just for AM
                            am = false;     // No longer available.
                            if (getPM) {   // Check if PM in is available.
                                pm = true;  // Change to true as room is available and not being booked.
                            }
                        } else if (chkPM.isSelected()) {    // Booking room at PM.
                            pm = false;     // room becomes unavailable.
                            if (getAM) {  // Check whether AM is available.
                                am = true;      // set am to true so it stays available.
                            }
                        }

                        ps.clearParameters();

                        ps = conn.prepareStatement(makeBooking);       // update bookings table
                        ps.setInt(1, room_id);
                        ps.setInt(2, a_id);     // insert current availability id into query
                        ps.setInt(3, empID);
                        ps.setString(4, bookingFor);
                        ps.setString(5, contact);
                        ps.setString(6, notes);
                        ps.execute();

                        ps.clearParameters();
                        ps = conn.prepareStatement(updateAvailabilities);   // update availabilities table
                        ps.setBoolean(1, am);
                        ps.setBoolean(2, pm);
                        ps.setInt(3, a_id);
                        ps.execute();
                        startDate = startDate.plusDays(1);
                        ps.clearParameters();
                    }

                    output.writeUTF("RefreshTable");
                    output.flush();

                    resetForm();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnAddHols.setOnAction((ActionEvent event) -> {
            LocalDate start = dpStart.getValue();       // get dates from date pickers
            LocalDate end = dpEnd.getValue();
            LocalDate date = start;     // set the date as the holiday starting date

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
                    output.writeUTF("RefreshTable");        // inform server of change
                    output.flush();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnLogout.setOnAction((ActionEvent -> {
            try {
                output.close();
                Platform.exit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    void setUser(Employee employee, int id) {
        lblUser.setText(employee.getUsername());
        empID = id;
    }

    public void setTableView(ObservableList list) {
        data = list;
        tableView.setItems(list);
    }

    public void resetForm() {
        btnRemoveRoom.setDisable(false);
        txtName.setText("");
        cbbType.setItems(typeOptions);
        cbbSize.setItems(null);
        paneNewRoom.setVisible(false);
        txtName1.setText("");
        txtNotes.setText("");
        room_id = 0;
        avail_id = 0;
        txtName1.setDisable(true);
        txtNotes.setDisable(true);
        chkAM.setDisable(true);
        chkPM.setDisable(true);
        cbDays.setDisable(true);
        cbWeeks.setDisable(true);
        btnBook.setDisable(true);
        btnRemoveRoom.setDisable(true);
        chkPM.setSelected(false);
        chkAM.setSelected(false);
        cbDays.setValue(0);
        cbWeeks.setValue(0);
    }

    static void callPopup(String message, String title) {
        LoginController.popup(message, title);
    }
}
