package com.example.bellmanford.controller;

import com.example.bellmanford.model.Ambulance;
import com.example.bellmanford.repository.AmbulanceRepository;
import com.example.bellmanford.repository.HospitalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ambulances")
public class AmbulanceController {

    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;

    public AmbulanceController(AmbulanceRepository ambulanceRepository, HospitalRepository hospitalRepository) {
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @GetMapping
    public ResponseEntity<List<Ambulance>> getAllAmbulances() {
        return ResponseEntity.ok(ambulanceRepository.findAll());
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<Ambulance>> getAmbulancesByHospital(@PathVariable Long hospitalId) {
        return hospitalRepository.findById(hospitalId)
            .map(h -> ResponseEntity.ok(ambulanceRepository.findByHospital(h)))
            .orElse(ResponseEntity.notFound().build());
    }
}
