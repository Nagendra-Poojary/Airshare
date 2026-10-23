package com.ewit.airshare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ride_requests")
public class RideRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passenger_name", nullable = false, length = 60)
    private String passengerName;

    @Column(name = "flight_no", nullable = false, length = 15)
    private String flightNo;

    @Column(name = "seats_needed", nullable = false)
    private int seatsNeeded = 1;

    @Column(name = "cabin_bags", nullable = false)
    private int cabinBags = 1;

    @Column(name = "checkin_bags", nullable = false)
    private int checkinBags = 0;

    // Generated column in DB: (cabin_bags + (checkin_bags * 2))
    @Column(name = "luggage_units", insertable = false, updatable = false)
    private Integer luggageUnits;

    @Column(name = "drop_name", nullable = false, length = 100)
    private String dropName;

    @Column(name = "curb_ready_time")
    private LocalDateTime curbReadyTime = LocalDateTime.now();

    @Column(name = "status", length = 20)
    private String status = "WAITING";

    public RideRequest() {}

    public RideRequest(String passengerName, String flightNo, int seatsNeeded, int cabinBags, int checkinBags, String dropName) {
        this.passengerName = passengerName;
        this.flightNo = flightNo;
        this.seatsNeeded = seatsNeeded;
        this.cabinBags = cabinBags;
        this.checkinBags = checkinBags;
        this.dropName = dropName;
        this.curbReadyTime = LocalDateTime.now();
        this.status = "WAITING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Integer getLuggageUnits() {
        if (luggageUnits != null) return luggageUnits;
        return cabinBags + (checkinBags * 2);
    }
    public void setLuggageUnits(Integer luggageUnits) { this.luggageUnits = luggageUnits; }

    public String getDropName() { return dropName; }
    public void setDropName(String dropName) { this.dropName = dropName; }

    public LocalDateTime getCurbReadyTime() { return curbReadyTime; }
    public void setCurbReadyTime(LocalDateTime curbReadyTime) { this.curbReadyTime = curbReadyTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
