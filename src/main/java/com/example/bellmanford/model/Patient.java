package com.example.bellmanford.model;

import jakarta.persistence.*;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    private String name;

    @Column(name = "contact_number")
    @com.fasterxml.jackson.annotation.JsonProperty("contactNumber")
    private String contactNumber;

    @Column(name = "emergency_type", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("emergencyType")
    private String emergencyType;

    public Patient() {}

    public Patient(Long id, String name, String contactNumber, String emergencyType) {
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
        this.emergencyType = emergencyType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }
}
