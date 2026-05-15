package com.example.bellmanford.model;

import org.locationtech.jts.geom.Point;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "mission_id")
    private UUID missionId;

    @Column(name = "dispatcher_id")
    private UUID dispatcherId;

    @Column(name = "driver_id")
    private UUID driverId;

    @Column(name = "ambulance_id")
    private UUID ambulanceId;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "emergency_type")
    private String emergencyType;

    @Column(name = "start_location", columnDefinition = "GEOMETRY")
    private Point startLocation;

    @Column(name = "end_location", columnDefinition = "GEOMETRY")
    private Point endLocation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "path_json", columnDefinition = "json")
    private String pathJson;

    @Column(name = "status")
    private String status;

    @Column(name = "dispatch_time")
    private LocalDateTime dispatchTime;

    @Column(name = "transport_time")
    private LocalDateTime transportTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    public UUID getMissionId() { return missionId; }
    public UUID getDispatcherId() { return dispatcherId; }
    public UUID getDriverId() { return driverId; }
    public UUID getAmbulanceId() { return ambulanceId; }
    public String getPatientName() { return patientName; }
    public String getEmergencyType() { return emergencyType; }
    public Point getStartLocation() { return startLocation; }
    public Point getEndLocation() { return endLocation; }
    public String getPathJson() { return pathJson; }
    public String getStatus() { return status; }
    public LocalDateTime getDispatchTime() { return dispatchTime; }
    public LocalDateTime getTransportTime() { return transportTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    
    // Compatibility setters
    public void setMissionId(UUID missionId) { this.missionId = missionId; }
    public void setDispatcherId(UUID dispatcherId) { this.dispatcherId = dispatcherId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public void setAmbulanceId(UUID ambulanceId) { this.ambulanceId = ambulanceId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }
    public void setStartLocation(Point startLocation) { this.startLocation = startLocation; }
    public void setEndLocation(Point endLocation) { this.endLocation = endLocation; }
    public void setPathJson(String pathJson) { this.pathJson = pathJson; }
    public void setStatus(String status) { this.status = status; }
    public void setDispatchTime(LocalDateTime dispatchTime) { this.dispatchTime = dispatchTime; }
    public void setTransportTime(LocalDateTime transportTime) { this.transportTime = transportTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    // Hacky bridge for old code
    public UUID getPatientId() { return dispatcherId; } 
    public java.util.UUID getHospitalId() { return ambulanceId; }
    public Long getPathId() { return 0L; }
}
