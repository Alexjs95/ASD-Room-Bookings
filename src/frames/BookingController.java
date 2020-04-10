package frames;

import dbtools.Rows;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    private ObservableList<Rows> data;

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

        // Row click event
        tableView.setOnMouseClicked((MouseEvent event) -> {
            if (tableView.getSelectionModel().getSelectedItem() != null) {
                Rows selectedItem = tableView.getSelectionModel().getSelectedItem();
                System.out.println(selectedItem.getRoomname());
            }
        });


        // Loops through the observable list to ensure everything
        for (int i = 0; i < data.size(); i++) {
            Rows temp = new Rows();
            temp = data.get(i);
            System.out.println(temp.getDate());
            System.out.println(temp.getRoomname());
            System.out.println(temp.getType());
            System.out.println(temp.getSize());
            System.out.println(temp.isAm());
            System.out.println(temp.isPm());
            System.out.println(temp.isTerm());
            System.out.println("NEXT");
        }
        System.out.println(data);
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
