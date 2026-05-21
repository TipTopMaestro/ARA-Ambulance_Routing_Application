package com.example.bellmanford.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private Long missionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dispatcher_id", nullable = false)
    private User dispatcher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ambulance_id", nullable = false)
    private Ambulance ambulance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "start_lat")
    private Double startLat;

    @Column(name = "start_lng")
    private Double startLng;

    @Column(name = "end_lat")
    private Double endLat;

    @Column(name = "end_lng")
    private Double endLng;

    @Column(name = "estimated_time")
    private Double estimatedTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "path_json", columnDefinition = "json")
    private String pathJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MissionStatus status;

    @Column(name = "dispatch_time")
    private LocalDateTime dispatchTime;

    @Column(name = "transport_time")
    private LocalDateTime transportTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    public enum MissionStatus {
        PENDING_CONFIRMATION, EN_ROUTE, COMPLETED, CANCELLED
    }

    public Mission() {}

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
    public User getDispatcher() { return dispatcher; }
    public void setDispatcher(User dispatcher) { this.dispatcher = dispatcher; }
    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }
    public Ambulance getAmbulance() { return ambulance; }
    public void setAmbulance(Ambulance ambulance) { this.ambulance = ambulance; }

    @com.fasterxml.jackson.annotation.JsonProperty("ambulanceId")
    public Long getAmbulanceId() {
        return ambulance != null ? ambulance.getId() : null;
    }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Double getStartLat() { return startLat; }
    public void setStartLat(Double startLat) { this.startLat = startLat; }
    public Double getStartLng() { return startLng; }
    public void setStartLng(Double startLng) { this.startLng = startLng; }
    public Double getEndLat() { return endLat; }
    public void setEndLat(Double endLat) { this.endLat = endLat; }
    public Double getEndLng() { return endLng; }
    public void setEndLng(Double endLng) { this.endLng = endLng; }
    public Double getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Double estimatedTime) { this.estimatedTime = estimatedTime; }
    public String getPathJson() { return pathJson; }
    public void setPathJson(String pathJson) { this.pathJson = pathJson; }
    public MissionStatus getStatus() { return status; }
    public void setStatus(MissionStatus status) { this.status = status; }
    public LocalDateTime getDispatchTime() { return dispatchTime; }
    public void setDispatchTime(LocalDateTime dispatchTime) { this.dispatchTime = dispatchTime; }
    public LocalDateTime getTransportTime() { return transportTime; }
    public void setTransportTime(LocalDateTime transportTime) { this.transportTime = transportTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
}
