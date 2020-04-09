package dbtools;

public class Booking {
    private int booking_id;
    private int availability_id;
    private int employee_id;
    private String booked_for;
    private String contact;
    private String notes;

    public Booking(int booking_id) {
        this.booking_id = booking_id;
    }

    public int getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(int booking_id) {
        this.booking_id = booking_id;
    }

    public int getAvailability_id() {
        return availability_id;
    }

    public void setAvailability_id(int availability_id) {
        this.availability_id = availability_id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public String getBooked_for() {
        return booked_for;
    }

    public void setBooked_for(String booked_for) {
        this.booked_for = booked_for;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
