package com.example.bellmanford.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_logs")
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dispatcher_id", nullable = false)
    private User dispatcher;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ambulance_id", nullable = false)
    private Ambulance ambulance;

    @Column(nullable = false)
    private String emergencyType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "path_id", nullable = false)
    private GeneratedPath generatedPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime arrivedAtPatientAt;
    private LocalDateTime completedAt;

    public enum RequestStatus {
        PENDING, DISPATCHED, EN_ROUTE, TRANSPORT, COMPLETED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
    }

    public RequestLog() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }

    public User getDispatcher() { return dispatcher; }
    public void setDispatcher(User dispatcher) { this.dispatcher = dispatcher; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public Ambulance getAmbulance() { return ambulance; }
    public void setAmbulance(Ambulance ambulance) { this.ambulance = ambulance; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public GeneratedPath getGeneratedPath() { return generatedPath; }
    public void setGeneratedPath(GeneratedPath generatedPath) { this.generatedPath = generatedPath; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public LocalDateTime getArrivedAtPatientAt() { return arrivedAtPatientAt; }
    public void setArrivedAtPatientAt(LocalDateTime arrivedAtPatientAt) { this.arrivedAtPatientAt = arrivedAtPatientAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    // Jackson-friendly getters for frontend compatibility
    @JsonProperty("patientName")
    public String getPatientName() {
        return patient != null ? patient.getName() : null;
    }

    @JsonProperty("ambulanceId")
    public String getAmbulanceIdString() {
        return ambulance != null ? ambulance.getId().toString() : null;
    }

    @JsonProperty("hospitalId")
    public Long getHospitalId() {
        return hospital != null ? hospital.getId() : null;
    }

    @JsonProperty("hospitalOsmNodeId")
    public String getHospitalOsmNodeId() {
        return hospital != null && hospital.getLocation() != null ? hospital.getLocation().getOsmNodeId() : null;
    }

    @JsonProperty("patientLocationName")
    public String getPatientLocationName() {
        return generatedPath != null && generatedPath.getTargetLocation() != null
            ? generatedPath.getTargetLocation().getName()
            : null;
    }

    @JsonProperty("pathId")
    public Long getPathId() {
        return generatedPath != null ? generatedPath.getId() : null;
    }

    @JsonProperty("dispatchTime")
    public LocalDateTime getDispatchTime() {
        return dispatchedAt;
    }

    @JsonProperty("estimatedTime")
    public Double getEstimatedTimeMinutes() {
        return (generatedPath != null && generatedPath.getEstimatedTimeSeconds() != null) 
            ? generatedPath.getEstimatedTimeSeconds() / 60.0 
            : null;
    }

    @JsonProperty("routeCoordinatesJson")
    public String getRouteCoordinatesJson() {
        return generatedPath != null ? generatedPath.getRouteJson() : null;
    }

    @JsonProperty("patientLat")
    public Double getPatientLat() {
        return (patient != null && generatedPath != null && generatedPath.getTargetLocation() != null)
            ? generatedPath.getTargetLocation().getLatitude()
            : null;
    }

    @JsonProperty("patientLng")
    public Double getPatientLng() {
        return (patient != null && generatedPath != null && generatedPath.getTargetLocation() != null)
            ? generatedPath.getTargetLocation().getLongitude()
            : null;
    }
}
