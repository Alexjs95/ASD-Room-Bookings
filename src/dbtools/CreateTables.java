package dbtools;

import utils.ConnectionUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;


// Class should only need to be ran once to set up the DB
public class CreateTables {


    public static void main(String[] args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  //formats to SQL format
        LocalDate date = LocalDate.now();       // Get todays today
        Connection conn = ConnectionUtil.connectDB();       // Establish connection to DB
        CreateEmployeeTable employeeTable = new CreateEmployeeTable();      // New employee Table
        employeeTable.create(conn);     // Create the employee table

        CreateRoomTable roomTable = new CreateRoomTable();
        roomTable.create(conn);

        CreateAvailabilityTable availabilityTable = new CreateAvailabilityTable();
        availabilityTable.create(conn);

        CreateBookingTable bookingTable = new CreateBookingTable();
        bookingTable.create(conn);

        CreateHolidayTable holidayTable = new CreateHolidayTable();
        holidayTable.create(conn);

        Employee emp1 = new Employee(); // Create a new Employee
        emp1.setForename("Alex");
        emp1.setRole("booking");
        emp1.setSurname("Scotson");
        emp1.setUsername("test");

        Employee emp2 = new Employee();
        emp2.setForename("Alex");
        emp2.setRole("manager");
        emp2.setSurname("Scotson");
        emp2.setUsername("man");

        Room room1 = new Room();
        room1.setRoomname("SJG/05");
        room1.setSize(20);
        room1.setRoomtype("computer");

        Room room2 = new Room();
        room2.setRoomname("Lecture Theatre 5");
        room2.setSize(150);
        room2.setRoomtype("Lecture");

        // Inserting employee into DB
        String query1 =  "INSERT into roombookingsystem.employees (forename, surname, username, role) values (?,?,?,?)";
        String query2 = "INSERT into roombookingsystem.rooms (name, size, type) values (?,?,?)";

        try {
            PreparedStatement ps = conn.prepareStatement(query1);
            ps.setString(1, emp1.getForename());
            ps.setString(2, emp1.getSurname());
            ps.setString(3, emp1.getUsername());
            ps.setString(4, emp1.getRole());
            ps.execute();

            ps.setString(1, emp2.getForename());
            ps.setString(2, emp2.getSurname());
            ps.setString(3, emp2.getUsername());
            ps.setString(4, emp2.getRole());
            ps.execute();
            ps.clearParameters();

            ps = conn.prepareStatement(query2);
            ps.setString(1, room1.getRoomname());
            ps.setInt(2, room1.getSize());
            ps.setString(3, room1.getRoomtype());
            ps.execute();

            ps.setString(1, room2.getRoomname());
            ps.setInt(2, room2.getSize());
            ps.setString(3, room2.getRoomtype());
            ps.execute();
            ps.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        Holiday holiday = new Holiday();
        holiday.setStart(dtf.format(date));
        holiday.setEnd(dtf.format(date.plusDays(5)));       // by default make holiday of 5 days from first day

        String query4 = "INSERT INTO roombookingsystem.holidays (START, END) VALUES (?,?)";     // insert into table

            try {
                PreparedStatement ps = conn.prepareStatement(query4);
                ps.setString(1, holiday.getStart());
                ps.setString(2, holiday.getEnd());
                ps.execute();
                ps.close();
            } catch (Exception ex) {
                ex.printStackTrace();
        }

        // Inserting 3 months of Room availability into DB.
        String[] schoolDays = {"monday", "tuesday", "wednesday", "thursday", "friday"};

        LocalDate endDate = date.plusMonths(3);      // Add 3 months to todays date.

        String query3 = "INSERT INTO roombookingsystem.availability (DATE, TERMTIME, AM, PM)"
                + " VALUES " + "(?, ?, ?, ?)";
        Boolean isHoliday = false;
        try {
            PreparedStatement ps = conn.prepareStatement(query3);
            //Loop through each day between now and the end day, incrementing 1 day at a time.
            for (LocalDate currdate = date; currdate.isBefore(endDate); currdate=currdate.plusDays(1)) {
                String day = currdate.getDayOfWeek().toString().toLowerCase();
                if (Arrays.stream(schoolDays).anyMatch(day::equals)) {      // Checks whether the curr date is a school day
                    ps.setString(1, dtf.format(currdate));

                    if (dtf.format(currdate).equals(holiday.getStart()) || isHoliday) {     // check if the current date is a holiday
                        isHoliday = true;
                        ps.setBoolean(2, false);  // FALSE for holidays
                        ps.setBoolean(3, true);  // AM is available
                        ps.setBoolean(4, true);  // PM is available
                        ps.addBatch();
                        if (dtf.format(currdate).equals(holiday.getEnd())) {       // if current date is end date
                            isHoliday = false;      // reset to term time
                        }
                    } else {
                        ps.setBoolean(2, true);     // TRUE for TERM time
                        ps.setBoolean(3, false); // AM not available
                        ps.setBoolean(4, true);  // PM is available
                        ps.addBatch();
                    }


                } else {
                    if (dtf.format(currdate).equals(holiday.getStart())) {
                        isHoliday = true;
                    }
                    ps.setString(1, dtf.format(currdate));
                    ps.setBoolean(2, true);     // FALSE for WEEKENDS
                    ps.setBoolean(3, true); // AM is available
                    ps.setBoolean(4, true);  // PM is available
                    ps.addBatch();
                }
            }
            ps.executeBatch();      // Execute all of the queries added to the prepared statement.
            ps.close();
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
