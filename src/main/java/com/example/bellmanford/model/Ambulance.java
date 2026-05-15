package com.example.bellmanford.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ambulances")
public class Ambulance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmbulanceStatus status;

    public enum AmbulanceStatus {
        AVAILABLE, RESERVED, EN_ROUTE, TRANSPORT, MAINTENANCE
    }

    public Ambulance() {
    }

    public Ambulance(Hospital hospital, AmbulanceStatus status) {
        this.hospital = hospital;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Hospital getHospital() {
        return hospital;
    }

    @JsonProperty("hospitalId")
    public java.util.UUID getHospitalId() {
        return hospital != null ? hospital.getId() : null;
    }

    @JsonProperty("hospitalOsmNodeId")
    public String getHospitalOsmNodeId() {
        return null; 
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public AmbulanceStatus getStatus() {
        return status;
    }

    public void setStatus(AmbulanceStatus status) {
        this.status = status;
    }
}
