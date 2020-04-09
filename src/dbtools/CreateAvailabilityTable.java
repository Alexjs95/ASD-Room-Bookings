package dbtools;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateAvailabilityTable {
    private static final String AVAILABILITY ="CREATE TABLE roombookingsystem.availability ("
            + "AVAILABILITY_ID INT NOT NULL AUTO_INCREMENT,"
            + "DATE DATE NOT NULL,"
            + "TERMTIME BOOLEAN NOT NULL,"
            + "AM BOOLEAN NOT NULL,"
            + "PM BOOLEAN NOT NULL,"
            + "PRIMARY KEY (AVAILABILITY_ID))";

    PreparedStatement ps = null;

    public void create(Connection conn) {
        try {
            ps = conn.prepareStatement(AVAILABILITY);
            ps.executeUpdate();
            System.out.println("Availability table created");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
