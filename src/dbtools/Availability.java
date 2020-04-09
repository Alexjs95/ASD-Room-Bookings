package dbtools;

public class Availability {
    private int availability_id;
    private String date;
    private boolean termTime;
    private boolean am;     // 0 not available 1 available
    private boolean pm;

    public int getAvailability_id() {
        return availability_id;
    }

    public void setAvailability_id(int availability_id) {
        this.availability_id = availability_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isTermTime() {
        return termTime;
    }

    public void setTermTime(boolean termTime) {
        this.termTime = termTime;
    }

    public boolean isAm() {
        return am;
    }

    public void setAm(boolean am) {
        this.am = am;
    }

    public boolean isPm() {
        return pm;
    }

    public void setPm(boolean pm) {
        this.pm = pm;
    }
}
