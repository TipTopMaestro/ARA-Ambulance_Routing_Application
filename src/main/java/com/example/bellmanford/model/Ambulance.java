package com.example.bellmanford.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ambulances")
public class Ambulance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ambulanceCode;

    @ManyToOne
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

    public Ambulance(String ambulanceCode, Hospital hospital, AmbulanceStatus status) {
        this.ambulanceCode = ambulanceCode;
        this.hospital = hospital;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAmbulanceCode() {
        return ambulanceCode;
    }

    public void setAmbulanceCode(String ambulanceCode) {
        this.ambulanceCode = ambulanceCode;
    }

    public Hospital getHospital() {
        return hospital;
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
