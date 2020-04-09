package dbtools;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateRoomTable {
    private static final String ROOMS ="CREATE TABLE roombookingsystem.rooms ("
            + "ROOM_ID INT NOT NULL AUTO_INCREMENT,"
            + "NAME VARCHAR(45) NOT NULL,"
            + "SIZE INT NOT NULL,"
            + "TYPE VARCHAR(45) NOT NULL,"
            + "PRIMARY KEY (ROOM_ID))";

    PreparedStatement ps = null;

    public void create(Connection conn) {
        try {
            ps = conn.prepareStatement(ROOMS);
            ps.executeUpdate();
            System.out.println("Rooms table created");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
