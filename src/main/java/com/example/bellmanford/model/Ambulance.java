package com.example.bellmanford.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "ambulances")
public class Ambulance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Hospital getHospital() {
        return hospital;
    }

    @JsonProperty("hospitalId")
    public Long getHospitalId() {
        return hospital != null ? hospital.getId() : null;
    }

    @JsonProperty("hospitalOsmNodeId")
    public String getHospitalOsmNodeId() {
        return (hospital != null && hospital.getNode() != null) ? String.valueOf(hospital.getNode().getId()) : null; 
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
