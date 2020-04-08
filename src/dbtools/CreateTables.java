package dbtools;

import utils.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;


// Class should only need to be ran once to set up the DB
public class CreateTables {

    public static void main(String[] args) {
        Connection conn = ConnectionUtil.connectDB();       // Establish connection to DB
        CreateEmployeeTable employeeTable = new CreateEmployeeTable();      // New employee Table
        employeeTable.create(conn);     // Create the employee table


        Employee emp1 = new Employee(); // Create a new Employee
        emp1.setForename("Alex");
        emp1.setRole("booking");
        emp1.setSurname("Scotson");
        emp1.setUsername("test");

        // Inserting employee into DB
        String query =  "INSERT into roombookingsystem.employees (forename, surname, username, role) values (?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, emp1.getForename());
            ps.setString(2, emp1.getSurname());
            ps.setString(3, emp1.getUsername());
            ps.setString(4, emp1.getRole());

            ps.execute();
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            conn.close();       // Close connection to DB
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
