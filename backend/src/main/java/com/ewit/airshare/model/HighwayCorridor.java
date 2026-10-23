package com.ewit.airshare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "highway_corridors")
public class HighwayCorridor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "corridor_name", nullable = false, length = 100)
    private String corridorName;

    public HighwayCorridor() {}

    public HighwayCorridor(Integer id, String corridorName) {
        this.id = id;
        this.corridorName = corridorName;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCorridorName() { return corridorName; }
    public void setCorridorName(String corridorName) { this.corridorName = corridorName; }
}
