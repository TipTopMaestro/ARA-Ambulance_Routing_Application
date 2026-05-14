package com.example.bellmanford.model;

import java.time.LocalDateTime;

public class Mission {
    private Long id;
    private String patientName;
    private String emergencyType;

    // Patient Location
    private String patientLocationName;
    private Double patientLat;
    private Double patientLng;

    private String hospitalId;
    private String ambulanceId;
    private Long driverId;
    private String status;

    private String routeCoordinatesJson;
    private Double estimatedTime;

    private LocalDateTime dispatchTime;
    private LocalDateTime transportTime;
    private LocalDateTime arrivalTime;

    public Mission() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getPatientLocationName() { return patientLocationName; }
    public void setPatientLocationName(String patientLocationName) { this.patientLocationName = patientLocationName; }

    public Double getPatientLat() { return patientLat; }
    public void setPatientLat(Double patientLat) { this.patientLat = patientLat; }

    public Double getPatientLng() { return patientLng; }
    public void setPatientLng(Double patientLng) { this.patientLng = patientLng; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getAmbulanceId() { return ambulanceId; }
    public void setAmbulanceId(String ambulanceId) { this.ambulanceId = ambulanceId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRouteCoordinatesJson() { return routeCoordinatesJson; }
    public void setRouteCoordinatesJson(String routeCoordinatesJson) { this.routeCoordinatesJson = routeCoordinatesJson; }

    public Double getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Double estimatedTime) { this.estimatedTime = estimatedTime; }

    public LocalDateTime getDispatchTime() { return dispatchTime; }
    public void setDispatchTime(LocalDateTime dispatchTime) { this.dispatchTime = dispatchTime; }

    public LocalDateTime getTransportTime() { return transportTime; }
    public void setTransportTime(LocalDateTime transportTime) { this.transportTime = transportTime; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
}
