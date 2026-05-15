package com.example.bellmanford.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

import com.example.bellmanford.model.GraphEdge;
import com.example.bellmanford.model.GraphNode;
import com.example.bellmanford.model.Hospital;
import com.example.bellmanford.model.Ambulance;
import com.example.bellmanford.model.User;
import com.example.bellmanford.repository.UserRepository;
import com.example.bellmanford.repository.AmbulanceRepository;
import com.example.bellmanford.repository.HospitalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class MockDatabaseService {

    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final List<GraphEdge> edges = new ArrayList<>();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final UserRepository userRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;

    public MockDatabaseService(UserRepository userRepository, 
                               AmbulanceRepository ambulanceRepository,
                               HospitalRepository hospitalRepository) {
        this.userRepository = userRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @PostConstruct
    public void init() {
        try {
            InputStream is = getClass().getResourceAsStream("/osm_data.json");
            if (is == null) {
                System.err.println("Could not find osm_data.json in resources");
                return;
            }
            
            JsonNode root = objectMapper.readTree(is);
            JsonNode elements = root.get("elements");
            
            if (elements == null || !elements.isArray()) {
                System.err.println("Invalid OSM JSON structure");
                return;
            }

            for (JsonNode el : elements) {
                String type = el.get("type").asText();
                if ("node".equals(type)) {
                    String id = el.get("id").asText();
                    double lat = el.get("lat").asDouble();
                    double lon = el.get("lon").asDouble();
                    GraphNode node = new GraphNode(id, lat, lon);
                    nodes.put(id, node);
                }
            }

            seedDatabase();
            System.out.println("Mock Database Initialization complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seedDatabase() {
        if (userRepository.count() == 0) {
            userRepository.save(new User("dispatcher1", "password", User.UserRole.DISPATCHER));
            userRepository.save(new User("driver1", "password", User.UserRole.DRIVER));
            userRepository.save(new User("driver2", "password", User.UserRole.DRIVER));
        }

        if (hospitalRepository.count() == 0) {
            seedHospital("6051423911", "Panabo Polymedic Hospital");
            seedHospital("1621665249", "Good Shepherd Hospital");
            seedHospital("1621650390", "Rivera Medical Center");
        }

        if (ambulanceRepository.count() == 0) {
            hospitalRepository.findAll().forEach(h -> {
                ambulanceRepository.save(new Ambulance(h, Ambulance.AmbulanceStatus.AVAILABLE));
            });
        }
    }

    private void seedHospital(String osmId, String name) {
        GraphNode node = nodes.get(osmId);
        if (node != null) {
            Hospital h = new Hospital();
            h.setName(name);
            h.setLocation(geometryFactory.createPoint(new Coordinate(node.getLongitude(), node.getLatitude())));
            hospitalRepository.save(h);
        }
    }

    public List<GraphNode> getSignificantNodes() {
        return new ArrayList<>(nodes.values());
    }

    public Map<String, GraphNode> getNodes() {
        return nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }
    
    public GraphNode getNode(String id) {
        return nodes.get(id);
    }
}
