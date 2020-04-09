package frames;

import dbtools.Employee;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.ConnectionUtil;

import javafx.event.ActionEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class LoginController implements Initializable  {
    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    Stage dialog = new Stage();
    Scene scene;

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
                Employee e1 = new Employee();       // Create a new employee
                e1.setUsername(rs.getString("USERNAME"));       // Assign values
                e1.setRole(rs.getString("ROLE"));

                Node source = (Node) event.getSource();
                dialog = (Stage) source.getScene().getWindow();
                dialog.close();

                if (e1.getRole() == "manager") {
                    System.out.println("load manager page");
                    scene = new Scene(FXMLLoader.load(getClass().getResource("Manager.fxml")));
                } else {
                    System.out.println("load booking form");
                    scene = new Scene(FXMLLoader.load(getClass().getResource("Booking.fxml")));
                }

                dialog.setScene(scene);
                dialog.show();
            }
        }catch (Exception x) {
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
