package dbtools;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateBookingTable {
    private static final String BOOKINGS ="CREATE TABLE roombookingsystem.bookings ("
            + "BOOKING_ID INT NOT NULL AUTO_INCREMENT,"
            + "ROOM_ID INT NOT NULL,"
            + "AVAILABILITY_ID INT NOT NULL,"
            + "EMPLOYEE_ID INT NOT NULL,"
            + "BOOKED_FOR VARCHAR(45) NOT NULL,"
            + "CONTACT VARCHAR(45) NOT NULL,"
            + "NOTES VARCHAR(60) NOT NULL,"
            + "PRIMARY KEY (BOOKING_ID),"
            + "FOREIGN KEY (ROOM_ID) REFERENCES roombookingsystem.rooms(ROOM_ID),"
            + "FOREIGN KEY (AVAILABILITY_ID) REFERENCES roombookingsystem.availability(AVAILABILITY_ID),"
            + "FOREIGN KEY (EMPLOYEE_ID) REFERENCES roombookingsystem.employees(EMPLOYEE_ID))";

    PreparedStatement ps = null;

    public void create(Connection conn) {
        try {
            ps = conn.prepareStatement(BOOKINGS);
            ps.executeUpdate();
            System.out.println("Booking table created");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
