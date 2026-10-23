package com.ewit.airshare.controller;

import com.ewit.airshare.dto.AddRiderRequest;
import com.ewit.airshare.repository.HighwayCorridorRepository;
import com.ewit.airshare.repository.RideRepository;
import com.ewit.airshare.service.ClusteringEngineService;
import com.ewit.airshare.service.FlightSimulatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows local HTML frontend on any port/file without CORS issues
public class AirShareController {

    private final RideRepository rideRepo;
    private final HighwayCorridorRepository corridorRepo;
    private final ClusteringEngineService clusteringService;
    private final FlightSimulatorService simulatorService;

    public AirShareController(RideRepository rideRepo,
                              HighwayCorridorRepository corridorRepo,
                              ClusteringEngineService clusteringService,
                              FlightSimulatorService simulatorService) {
        this.rideRepo = rideRepo;
        this.corridorRepo = corridorRepo;
        this.clusteringService = clusteringService;
        this.simulatorService = simulatorService;
    }

    /**
     * Health check and engine configuration summary
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("project", "AirShare - Dual-Capacity Corridor Clustering Engine");
        status.put("status", "ACTIVE");
        status.put("database", "PostgreSQL 16 + PostGIS 3.4");
        status.put("maxSeatsPerVehicle", 6);
        status.put("maxLuggageUnitsPerVehicle", 6);
        status.put("tollFeeINR", 120.0);
        status.put("kmRateINR", 22.0);
        return ResponseEntity.ok(status);
    }

    /**
     * Returns the 3 arterial highway corridor spines with PostGIS GeoJSON geometries
     */
    @GetMapping("/corridors")
    public ResponseEntity<List<Map<String, Object>>> getCorridors() {
        return ResponseEntity.ok(corridorRepo.findCorridorsWithGeoJSON());
    }

    /**
     * Injects a burst of synthetic passengers arriving at terminal curb
     */
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateArrivals(
            @RequestParam(name = "count", defaultValue = "25") int count) {
        int generated = simulatorService.simulateBurstArrivals(count);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Simulated " + generated + " curbside passenger arrivals across 3 highway corridors");
        resp.put("count", generated);
        return ResponseEntity.ok(resp);
    }

    /**
     * Ingests a single custom rider readiness event (e.g. from the judge panel or passenger booking form)
     */
    @PostMapping("/add-rider")
    public ResponseEntity<Map<String, Object>> addRider(@RequestBody AddRiderRequest req) {
        rideRepo.insertCurbsideRider(
            req.getPassengerName(),
            req.getFlightNo() != null ? req.getFlightNo() : "6E-CUSTOM",
            req.getSeatsNeeded(),
            req.getCabinBags(),
            req.getCheckinBags(),
            req.getDropName(),
            req.getLon(),
            req.getLat()
        );

        int totalUnits = req.getCabinBags() + (req.getCheckinBags() * 2);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Passenger ingested at curbside: " + req.getPassengerName() + " (" + totalUnits + " luggage units)");
        return ResponseEntity.ok(resp);
    }

    /**
     * Inspects waiting riders in a corridor pre-sorted by PostGIS ST_LineLocatePoint progress
     */
    @GetMapping("/candidates/{corridorId}")
    public ResponseEntity<List<Map<String, Object>>> getCandidates(@PathVariable int corridorId) {
        return ResponseEntity.ok(rideRepo.findCorridorCandidates(corridorId));
    }

    /**
     * Executes Stage 3 of the pipeline: 2D Knapsack Matcher + Proportional Distance Fare Allocation
     */
    @PostMapping("/match")
    public ResponseEntity<Map<String, Object>> executeClustering() {
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> fleet = clusteringService.executeCorridorPooling();
        long executionTimeMs = System.currentTimeMillis() - startTime;

        int totalDispatchedRiders = 0;
        int totalSavedFare = 0;

        for (Map<String, Object> v : fleet) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> manifest = (List<Map<String, Object>>) v.get("manifest");
            totalDispatchedRiders += manifest.size();
            for (Map<String, Object> r : manifest) {
                int solo = ((Number) r.get("soloFare")).intValue();
                int pooled = ((Number) r.get("pooledFare")).intValue();
                totalSavedFare += Math.max(0, solo - pooled);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("executionTimeMs", executionTimeMs);
        result.put("vehiclesDispatched", fleet.size());
        result.put("passengersClustered", totalDispatchedRiders);
        result.put("totalPassengerSavingsINR", totalSavedFare);
        result.put("fleet", fleet);

        return ResponseEntity.ok(result);
    }

    /**
     * Resets waiting pool for clean demo runs
     */
    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPool() {
        rideRepo.clearWaitingPool();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Waiting ride pool reset.");
        return ResponseEntity.ok(resp);
    }
}
