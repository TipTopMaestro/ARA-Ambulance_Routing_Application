package com.example.bellmanford.controller;

import com.example.bellmanford.model.RequestLog;
import com.example.bellmanford.model.Mission;
import com.example.bellmanford.service.MissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @PostMapping
    public ResponseEntity<RequestLog> createMission(@RequestBody Mission mission) {
        return ResponseEntity.ok(missionService.createMission(mission));
    }

    @GetMapping
    public ResponseEntity<List<RequestLog>> getMissionLogs() {
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    @GetMapping("/active/{driverId}")
    public ResponseEntity<RequestLog> getActiveMission(@PathVariable java.util.UUID driverId) {
        return missionService.getActiveMissionForDriver(driverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RequestLog> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        try {
            return ResponseEntity.ok(missionService.updateMissionStatus(id, statusUpdate.get("status")));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
