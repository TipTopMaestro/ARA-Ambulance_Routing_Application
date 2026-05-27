package com.example.bellmanford.repository;

import com.example.bellmanford.model.RouteNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteNodeRepository extends JpaRepository<RouteNode, String> {
}
