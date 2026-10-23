package com.ewit.airshare.dto;

public class AddRiderRequest {
    private String passengerName;
    private String flightNo;
    private int seatsNeeded = 1;
    private int cabinBags = 1;
    private int checkinBags = 1;
    private String dropName;
    private double lon;
    private double lat;

    public AddRiderRequest() {}

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getFlightNo() { return flightNo; }
    public void setFlightNo(String flightNo) { this.flightNo = flightNo; }

    public int getSeatsNeeded() { return seatsNeeded; }
    public void setSeatsNeeded(int seatsNeeded) { this.seatsNeeded = seatsNeeded; }

    public int getCabinBags() { return cabinBags; }
    public void setCabinBags(int cabinBags) { this.cabinBags = cabinBags; }

    public int getCheckinBags() { return checkinBags; }
    public void setCheckinBags(int checkinBags) { this.checkinBags = checkinBags; }

    public String getDropName() { return dropName; }
    public void setDropName(String dropName) { this.dropName = dropName; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
}
