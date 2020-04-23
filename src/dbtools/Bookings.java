package dbtools;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Bookings {
    private final SimpleStringProperty date = new SimpleStringProperty();
    private final SimpleStringProperty roomname = new SimpleStringProperty();
    private final SimpleStringProperty booked_for = new SimpleStringProperty();
    private final SimpleStringProperty contact = new SimpleStringProperty();
    private final SimpleStringProperty notes = new SimpleStringProperty();

    public Bookings() {
    }


    public String getDate() {
        return date.get();
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public String getRoomname() {
        return roomname.get();
    }

    public SimpleStringProperty roomnameProperty() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname.set(roomname);
    }

    public String getBooked_for() {
        return booked_for.get();
    }

    public SimpleStringProperty booked_forProperty() {
        return booked_for;
    }

    public void setBooked_for(String booked_for) {
        this.booked_for.set(booked_for);
    }

    public String getContact() {
        return contact.get();
    }

    public SimpleStringProperty contactProperty() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact.set(contact);
    }

    public String getNotes() {
        return notes.get();
    }

    public SimpleStringProperty notesProperty() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes.set(notes);
    }
}
