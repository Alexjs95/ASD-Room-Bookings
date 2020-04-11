package frames;

import dbtools.Employee;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utils.ConnectionUtil;

import javafx.event.ActionEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class LoginController implements Initializable  {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    Stage dialog = new Stage();

    Connection conn;
    PreparedStatement ps = null;
    ResultSet rs = null;

    public LoginController() {
        conn = ConnectionUtil.connectDB();
    }

    @FXML
    private void handleBtnLogin(ActionEvent event) {
        String username = txtUsername.getText();
        //String password = txtPassword.getText();

        //String query = "SELECT * FROM roombookingsystem.employees WHERE username = ? and password = ?";
        String query = "SELECT * FROM roombookingsystem.employees WHERE username = ?";
        try {
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            //ps.setString(2, password);
            rs = ps.executeQuery();

            if (!rs.next()) {       // If the rs if empty then no employee was found.
                popup("Unsuccessful Login attempt. Please try again.", "Unsuccessful");
                // Failed Login attempt
            } else {
                Employee user = new Employee();       // Create a new employee
                user.setUsername(rs.getString("USERNAME"));       // Assign values
                user.setRole(rs.getString("ROLE"));
                user.setForename(rs.getString("FORENAME"));
                user.setSurname(rs.getString("SURNAME"));
                int id = rs.getInt("Employee_ID");

                Node source = (Node) event.getSource();
                dialog = (Stage) source.getScene().getWindow();
                dialog.close();

                if (user.getRole() == "manager") {
                    System.out.println("load manager page");

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Manager.fxml"));
                    dialog.setScene(new Scene(loader.load()));
                    //BookingController bc = loader.getController();
                    //bc.setUser(user, id);
                } else {
                    System.out.println("load booking form");

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Booking.fxml"));
                    dialog.setScene(new Scene(loader.load()));
                    BookingController bc = loader.getController();
                    bc.setUser(user, id);       // Passes user to booking controller.
                }

                dialog.show();
            }
        }catch (Exception x) {
            x.printStackTrace();
            System.out.println(x);
        }
    }

    public static void popup(String message, String title) {
        Alert alert =  new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
