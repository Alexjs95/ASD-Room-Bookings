package dbtools;

import javafx.beans.property.*;

public class Rows {
    private final SimpleStringProperty date = new SimpleStringProperty();
    private final SimpleStringProperty roomname = new SimpleStringProperty();
    private final SimpleIntegerProperty size = new SimpleIntegerProperty();
    private final SimpleStringProperty type = new SimpleStringProperty();
    private final SimpleBooleanProperty term = new SimpleBooleanProperty();
    private final SimpleBooleanProperty am = new SimpleBooleanProperty();
    private final SimpleBooleanProperty pm = new SimpleBooleanProperty();

    public Rows() {

    }

    public Rows(String date, String roomname, int size, String roomtype, Boolean term, Boolean am, Boolean pm) {
        setDate(date);
        setRoomname(roomname);
        setSize(size);
        setType(roomtype);
        setTerm(term);
        setAm(am);
        setPm(pm);
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

    public int getSize() {
        return size.get();
    }

    public SimpleIntegerProperty sizeProperty() {
        return size;
    }

    public void setSize(int size) {
        this.size.set(size);
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public boolean isTerm() {
        return term.get();
    }

    public SimpleBooleanProperty termProperty() {
        return term;
    }

    public void setTerm(boolean term) {
        this.term.set(term);
    }

    public boolean isAm() {
        return am.get();
    }

    public SimpleBooleanProperty amProperty() {
        return am;
    }

    public void setAm(boolean am) {
        this.am.set(am);
    }

    public boolean isPm() {
        return pm.get();
    }

    public SimpleBooleanProperty pmProperty() {
        return pm;
    }

    public void setPm(boolean pm) {
        this.pm.set(pm);
    }
}
