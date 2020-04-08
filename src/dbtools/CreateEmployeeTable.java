package dbtools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class CreateEmployeeTable {
    private static final String EMPLOYEES ="CREATE TABLE roombookingsystem.employees ("
            + "Employee_ID INT NOT NULL AUTO_INCREMENT,"
            + "USERNAME VARCHAR(45) NOT NULL,"
            + "FORENAME VARCHAR(45) NOT NULL,"
            + "SURNAME VARCHAR(45) NOT NULL,"
            + "ROLE VARCHAR(20) NOT NULL,"
            + "PRIMARY KEY (Employee_ID))";

    PreparedStatement ps = null;

    public void create(Connection conn) {
        try {
            ps = conn.prepareStatement(EMPLOYEES);
            ps.executeUpdate();
            System.out.println("Employee table created");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
