package com.example.bellmanford.repository;

import com.example.bellmanford.model.Mission;
import com.example.bellmanford.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    Optional<Mission> findByDriverAndStatusIn(User driver, List<Mission.MissionStatus> statuses);
    List<Mission> findByDispatcher(User dispatcher);
}
