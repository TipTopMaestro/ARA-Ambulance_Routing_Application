package com.example.bellmanford.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_logs")
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "dispatcher_id", nullable = false)
    private User dispatcher;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "ambulance_id", nullable = false)
    private Ambulance ambulance;

    @Column(nullable = false)
    private String emergencyType;

    @ManyToOne
    @JoinColumn(name = "source_location_id", nullable = false)
    private Location sourceLocation;

    @ManyToOne
    @JoinColumn(name = "target_location_id", nullable = false)
    private Location targetLocation;

    @ManyToOne
    @JoinColumn(name = "path_id")
    private GeneratedPath generatedPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime arrivedAtPatientAt;
    private LocalDateTime completedAt;

    public enum MissionStatus {
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

    public User getDispatcher() { return dispatcher; }
    public void setDispatcher(User dispatcher) { this.dispatcher = dispatcher; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public Ambulance getAmbulance() { return ambulance; }
    public void setAmbulance(Ambulance ambulance) { this.ambulance = ambulance; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public Location getSourceLocation() { return sourceLocation; }
    public void setSourceLocation(Location sourceLocation) { this.sourceLocation = sourceLocation; }

    public Location getTargetLocation() { return targetLocation; }
    public void setTargetLocation(Location targetLocation) { this.targetLocation = targetLocation; }

    public GeneratedPath getGeneratedPath() { return generatedPath; }
    public void setGeneratedPath(GeneratedPath generatedPath) { this.generatedPath = generatedPath; }

    public MissionStatus getStatus() { return status; }
    public void setStatus(MissionStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public LocalDateTime getArrivedAtPatientAt() { return arrivedAtPatientAt; }
    public void setArrivedAtPatientAt(LocalDateTime arrivedAtPatientAt) { this.arrivedAtPatientAt = arrivedAtPatientAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
