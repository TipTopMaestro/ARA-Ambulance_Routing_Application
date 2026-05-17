package com.example.bellmanford.model;

import org.locationtech.jts.geom.Point;
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

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "emergency_type")
    private String emergencyType;

    @Column(name = "start_location", columnDefinition = "POINT")
    private Point startLocation;

    @Column(name = "end_location", columnDefinition = "POINT")
    private Point endLocation;

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
        PENDING, DISPATCHED, EN_ROUTE, TRANSPORT, COMPLETED, CANCELLED
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
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }
    public Point getStartLocation() { return startLocation; }
    public void setStartLocation(Point startLocation) { this.startLocation = startLocation; }
    public Point getEndLocation() { return endLocation; }
    public void setEndLocation(Point endLocation) { this.endLocation = endLocation; }
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
