package com.example.bellmanford.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "hospitals")
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "hospital")
    private java.util.List<Ambulance> ambulances;


    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "location_id", referencedColumnName = "id", nullable = false, unique = true)
    private Location location;

    @Column(nullable = false)
    private String name;

    public Hospital() {}

    public Hospital(Location location, String name) {
        this.location = location;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
