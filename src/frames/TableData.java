package frames;

import dbtools.Rows;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TableData {

    private ObservableList<Rows> data;
    PreparedStatement ps = null;
    ResultSet rsRooms = null;
    ResultSet rsAvailable = null;
    ResultSet rsBookings = null;
    ResultSet rsHolidays = null;

    public ObservableList getData() {
        List list = new ArrayList();
        Connection conn = ConnectionUtil.connectDB();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  //formats to SQL format
        LocalDate todayDate = LocalDate.now();       // Get todays today
        boolean term;
        boolean am;
        boolean pm;
        String queryRooms = "SELECT * FROM roombookingsystem.rooms";
        String queryAvailable = "SELECT * FROM roombookingsystem.availability WHERE DATE >= ?";
        String queryBookings = "SELECT * FROM roombookingsystem.bookings";
        String queryHolidays = "SELECT * FROM roombookingsystem.holidays";
        int count = 0;


        try {
            ps = conn.prepareStatement(queryRooms);
            rsRooms = ps.executeQuery();
            ps.clearParameters();
            ps = conn.prepareStatement(queryAvailable);
            ps.setString(1, todayDate.toString());
            rsAvailable = ps.executeQuery();
            ps.clearParameters();
            ps = conn.prepareStatement(queryBookings);
            rsBookings = ps.executeQuery();
            ps.clearParameters();
            ps = conn.prepareStatement(queryHolidays);
            rsHolidays = ps.executeQuery();
            ps.clearParameters();

            // Loops through all availabilities
            while (rsHolidays.next()) {
                String holStart = rsHolidays.getString(2);
                String holEnd = rsHolidays.getString(3);
                boolean isHoliday = false;

                while(rsAvailable.next()) {
                    String date = rsAvailable.getString(2);

                     term = rsAvailable.getBoolean(3);
                     am = rsAvailable.getBoolean(4);
                     pm = rsAvailable.getBoolean(5);


                    // Each available date is for each room so loop through all rooms.
                    while (rsRooms.next()) {

                        Rows row = new Rows();
                        row.setDate(date);
                        row.setAm(am);
                        row.setPm(pm);
                        row.setTerm(term);
                        row.setRoomname(rsRooms.getString(2));
                        row.setSize(rsRooms.getInt(3));
                        row.setType(rsRooms.getString(4));
                        list.add(count, row);
                        count ++;
                    }
                    rsRooms.beforeFirst();
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
