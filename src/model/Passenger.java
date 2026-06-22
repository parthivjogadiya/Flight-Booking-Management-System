package model;

public class Passenger {
    private int passengerId;
    private String passengerName;
    private String phoneNumber;
    private String email;

    public Passenger() {
    }

    public Passenger(int passengerId, String passengerName, String phoneNumber, String email) {
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Passenger(String passengerName, String phoneNumber, String email) {
        this(0, passengerName, phoneNumber, email);
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
