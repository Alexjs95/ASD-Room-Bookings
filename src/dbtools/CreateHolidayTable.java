package dbtools;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateHolidayTable {
    private static final String HOLIDAYS ="CREATE TABLE roombookingsystem.holidays ("
            + "HOLIDAY_ID INT NOT NULL AUTO_INCREMENT,"
            + "START VARCHAR(45) NOT NULL,"
            + "END VARCHAR(45) NOT NULL,"
            + "PRIMARY KEY (HOLIDAY_ID))";

    PreparedStatement ps = null;

    public void create(Connection conn) {
        try {
            ps = conn.prepareStatement(HOLIDAYS);
            ps.executeUpdate();
            System.out.println("holidays table created");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
