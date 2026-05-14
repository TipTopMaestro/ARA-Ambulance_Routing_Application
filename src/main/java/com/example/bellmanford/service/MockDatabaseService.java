package com.example.bellmanford.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.bellmanford.model.GraphEdge;
import com.example.bellmanford.model.GraphNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class MockDatabaseService {

    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final List<GraphEdge> edges = new ArrayList<>();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    private final com.example.bellmanford.repository.UserRepository userRepository;
    private final com.example.bellmanford.repository.AmbulanceRepository ambulanceRepository;
    private final com.example.bellmanford.repository.HospitalRepository hospitalRepository;
    private final com.example.bellmanford.repository.LocationRepository locationRepository;

    public MockDatabaseService(com.example.bellmanford.repository.UserRepository userRepository, 
                               com.example.bellmanford.repository.AmbulanceRepository ambulanceRepository,
                               com.example.bellmanford.repository.HospitalRepository hospitalRepository,
                               com.example.bellmanford.repository.LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.locationRepository = locationRepository;
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

            // First pass: extract all nodes and count usage to find intersections
            Map<String, Integer> nodeUsage = new HashMap<>();
            for (JsonNode el : elements) {
                String type = el.get("type").asText();
                if ("node".equals(type)) {
                    String id = el.get("id").asText();
                    double lat = el.get("lat").asDouble();
                    double lon = el.get("lon").asDouble();
                    GraphNode node = new GraphNode(id, lat, lon);
                    
                    if (el.has("tags")) {
                        JsonNode tags = el.get("tags");
                        if (tags.has("name")) {
                            node.setName(tags.get("name").asText());
                        } else if (tags.has("highway") && "traffic_signals".equals(tags.get("highway").asText())) {
                            node.setName("Traffic Light at Intersection");
                        }
                    }
                    nodes.put(id, node);
                } else if ("way".equals(type)) {
                    JsonNode wayNodes = el.get("nodes");
                    if (wayNodes != null) {
                        for (JsonNode nodeRef : wayNodes) {
                            String nodeId = nodeRef.asText();
                            nodeUsage.put(nodeId, nodeUsage.getOrDefault(nodeId, 0) + 1);
                        }
                    }
                }
            }

            // Second pass: build simplified graph
            for (JsonNode el : elements) {
                String type = el.get("type").asText();
                if ("way".equals(type)) {
                    JsonNode wayNodes = el.get("nodes");
                    if (wayNodes != null && wayNodes.isArray() && wayNodes.size() >= 2) {
                        
                        boolean oneway = false;
                        if (el.has("tags") && el.get("tags").has("oneway")) {
                            oneway = "yes".equals(el.get("tags").get("oneway").asText());
                        }

                        List<String> currentPath = new ArrayList<>();
                        double currentDistance = 0;

                        for (int i = 0; i < wayNodes.size(); i++) {
                            String nodeId = wayNodes.get(i).asText();
                            currentPath.add(nodeId);

                            if (i > 0) {
                                GraphNode prev = nodes.get(wayNodes.get(i - 1).asText());
                                GraphNode curr = nodes.get(nodeId);
                                if (prev != null && curr != null) {
                                    currentDistance += calculateDistance(prev.getLatitude(), prev.getLongitude(), curr.getLatitude(), curr.getLongitude());
                                }
                            }

                            // A node is significant if it's the start/end of a way, an intersection, or a turn
                            boolean isSignificant = (i == 0 || i == wayNodes.size() - 1 || nodeUsage.getOrDefault(nodeId, 0) > 1);
                            
                            // Check for turn
                            if (!isSignificant && i > 0 && i < wayNodes.size() - 1) {
                                isSignificant = isTurn(wayNodes.get(i - 1).asText(), nodeId, wayNodes.get(i + 1).asText());
                            }

                            if (isSignificant && currentPath.size() > 1) {
                                String sourceId = currentPath.get(0);
                                String targetId = nodeId;
                                
                                double baseTravelTime = currentDistance * 2.0; // 30km/h
                                
                                double finalWeight = baseTravelTime;
                                if (random.nextDouble() < 0.05) {
                                    finalWeight = - (random.nextDouble() * 5.0 + 1.0); // -1.0 to -6.0
                                }

                                edges.add(new GraphEdge(sourceId, targetId, finalWeight, 1.0));
                                if (!oneway) {
                                    edges.add(new GraphEdge(targetId, sourceId, finalWeight, 1.0));
                                }
                                
                                // Reset for next segment
                                currentPath.clear();
                                currentPath.add(nodeId);
                                currentDistance = 0;
                            }
                        }
                    }
                }
            }
            
            // Add aliases for frontend specific IDs
            addAlias("H1", "6051423911", "Panabo Polymedic Hospital");
            addAlias("H2", "1621665249", "Good Shepherd Hospital");
            addAlias("H3", "1621650390", "Rivera Medical Center");
            addAlias("CITY_HALL", "12743750784", "Panabo City Hall");
            addAlias("AMB_1", "1621665322", "Ambulance Station Alpha");
            addAlias("AMB_2", "968789442", "Ambulance Station Bravo");
            addAlias("AMB_3", "928071323", "Ambulance Station Charlie");
            addAlias("AMB_4", "1298161645", "Downtown Ambulance Stand");

            seedDatabase();
            System.out.println("Loaded " + nodes.size() + " nodes and " + edges.size() + " edges from OSM.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seedDatabase() {
        if (userRepository.count() == 0) {
            userRepository.save(new com.example.bellmanford.model.User("dispatcher1", "password", com.example.bellmanford.model.User.UserRole.DISPATCHER));
            userRepository.save(new com.example.bellmanford.model.User("driver1", "password", com.example.bellmanford.model.User.UserRole.DRIVER));
            userRepository.save(new com.example.bellmanford.model.User("driver2", "password", com.example.bellmanford.model.User.UserRole.DRIVER));
            System.out.println("Seeded initial users.");
        }

        if (hospitalRepository.count() == 0) {
            createHospital("H1", "Panabo Polymedic Hospital");
            createHospital("H2", "Good Shepherd Hospital");
            createHospital("H3", "Rivera Medical Center");
            System.out.println("Seeded initial hospitals.");
        }

        if (ambulanceRepository.count() == 0) {
            seedAmbulance("AMB_1", "H1");
            seedAmbulance("AMB_2", "H1");
            seedAmbulance("AMB_3", "H2");
            seedAmbulance("AMB_4", "H3");
            System.out.println("Seeded initial ambulances.");
        }
    }

    private void createHospital(String id, String name) {
        GraphNode node = nodes.get(id);
        if (node != null) {
            com.example.bellmanford.model.Location location = new com.example.bellmanford.model.Location(
                id, name, node.getLatitude(), node.getLongitude(), 
                com.example.bellmanford.model.Location.LocationType.HOSPITAL
            );
            locationRepository.save(location);
            hospitalRepository.save(new com.example.bellmanford.model.Hospital(location, name));
        }
    }

    private void seedAmbulance(String ambId, String hospAlias) {
        hospitalRepository.findAll().stream()
            .filter(h -> h.getName().equals(nodes.get(hospAlias).getName()))
            .findFirst()
            .ifPresent(h -> {
                ambulanceRepository.save(new com.example.bellmanford.model.Ambulance(
                    ambId, h, com.example.bellmanford.model.Ambulance.AmbulanceStatus.AVAILABLE
                ));
            });
    }
    
    private void addAlias(String alias, String originalId, String aliasName) {
        GraphNode original = nodes.get(originalId);
        if (original != null) {
            GraphNode aliasNode = new GraphNode(alias, original.getLatitude(), original.getLongitude());
            aliasNode.setName(aliasName != null ? aliasName : original.getName());
            nodes.put(alias, aliasNode);
            
            // Re-route edges pointing to/from original to also point to/from alias
            // Actually, simpler to just add 0-weight edges between alias and original
            edges.add(new GraphEdge(alias, originalId, 0.0, 1.0));
            edges.add(new GraphEdge(originalId, alias, 0.0, 1.0));
        }
    }

    private boolean isTurn(String prevId, String currId, String nextId) {
        GraphNode p = nodes.get(prevId);
        GraphNode c = nodes.get(currId);
        GraphNode n = nodes.get(nextId);
        if (p == null || c == null || n == null) return false;

        double angle1 = Math.atan2(c.getLatitude() - p.getLatitude(), c.getLongitude() - p.getLongitude());
        double angle2 = Math.atan2(n.getLatitude() - c.getLatitude(), n.getLongitude() - c.getLongitude());
        double diff = Math.abs(angle1 - angle2);
        if (diff > Math.PI) diff = 2 * Math.PI - diff;
        
        return diff > (Math.PI / 12); // > 15 degrees
    }

    public List<GraphNode> getSignificantNodes() {
        Set<String> significantIds = new HashSet<>();
        for (GraphEdge edge : edges) {
            significantIds.add(edge.getSource());
            significantIds.add(edge.getTarget());
        }
        List<GraphNode> result = new ArrayList<>();
        for (String id : significantIds) {
            GraphNode node = nodes.get(id);
            if (node != null) {
                result.add(node);
            }
        }
        return result;
    }

    // Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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
