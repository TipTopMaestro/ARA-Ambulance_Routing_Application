package com.example.bellmanford.repository;

import com.example.bellmanford.model.RequestLog;
import com.example.bellmanford.model.User;
import com.example.bellmanford.model.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    List<RequestLog> findByDispatcher(User dispatcher);
    List<RequestLog> findByDriver(User driver);
    Optional<RequestLog> findByDriverAndStatusIn(User driver, List<RequestLog.MissionStatus> statuses);
    List<RequestLog> findByAmbulance(Ambulance ambulance);
}
