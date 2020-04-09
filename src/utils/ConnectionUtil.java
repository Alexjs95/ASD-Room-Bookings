package utils;

import java.sql.Connection;
import java.sql.DriverManager;


// Class to have the retrieve a connection to the DB
public class ConnectionUtil {
    Connection conn = null;

    public static Connection connectDB() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/roombookingsystem?&serverTimezone=BST", "test", "test");
            return conn;
        } catch (Exception e) {
            System.out.println("cannot connect to DB");
            System.out.println(e);
            return null;
        }
    }

}
