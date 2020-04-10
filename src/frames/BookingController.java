package frames;

import dbtools.Employee;
import dbtools.Rows;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BookingController implements Initializable {
    @FXML public TableView<Rows> tableView = new TableView<Rows>();
    @FXML public TableColumn dateCol = new TableColumn();
    @FXML public TableColumn roomnameCol = new TableColumn();
    @FXML public TableColumn roomtypeCol = new TableColumn();
    @FXML public TableColumn roomsizeCol = new TableColumn();
    @FXML public TableColumn termCol = new TableColumn();
    @FXML public TableColumn amCol = new TableColumn();
    @FXML public TableColumn pmCol = new TableColumn();

    @FXML Label lblUser;
    @FXML Label lblDate;
    @FXML Label lblRoom;
    @FXML TextField txtName;
    @FXML TextField txtNotes;
    @FXML TextField txtContact;
    @FXML CheckBox chkAM;
    @FXML CheckBox chkPM;
    @FXML Button btnBook;

    private ObservableList<Rows> data;

    Employee currUser;

    Stage dialog = new Stage();
    Scene scene;

    Connection conn;
    PreparedStatement ps = null;
    ResultSet rsRooms = null;
    ResultSet rsAvailable = null;
    ResultSet rsBookings = null;

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

        data = getInitialData();
        tableView.setItems(data);

        //lblUser.setText("Logged in as: " + currUser.getUsername());
        //System.out.println(currUser.getUsername());
        // Row click event
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (tableView.getSelectionModel().getSelectedItem() != null) {
                resetForm();
                Rows selectedItem = tableView.getSelectionModel().getSelectedItem();
                lblDate.setText(selectedItem.getDate());
                lblRoom.setText(selectedItem.getRoomname());

                //disable check boxes if the value is false in the row.
                if (!selectedItem.isAm()) {
                    chkAM.setDisable(true);
                }
                if (!selectedItem.isPm()) {
                    chkPM.setDisable(true);
                }

            }
        });
    }

    void setUser(Employee employee) {
        lblUser.setText(employee.getUsername());
    }

    void resetForm() {
        lblRoom.setText("");
        lblDate.setText("");
        txtContact.setText("");
        txtName.setText("");
        txtNotes.setText("");
        chkAM.setDisable(false);
        chkPM.setDisable(false);
    }

    public ObservableList getInitialData() {
        List list = new ArrayList();

        String queryRooms = "SELECT * FROM roombookingsystem.rooms";
        String queryAvailable = "SELECT * FROM roombookingsystem.availability";
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
