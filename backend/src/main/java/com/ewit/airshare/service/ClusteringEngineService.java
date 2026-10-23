package com.ewit.airshare.service;

import com.ewit.airshare.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClusteringEngineService {

    private final RideRepository repo;

    // Dual-Capacity Knapsack Bounds for Airport MP
    // Vs (Maruti Ertiga / Toyota Innova)
    private static final int MAX_SEATS = 6;
    private static final int MAX_LUGGAGE = 6; // usable trunk capacity units (CNG loss accounted for)

    // Fair Cost Allocation Parameters
    private static final double TOLL = 120.0; // Fixed airport toll + expressway surcharge (INR)
    private static final double KM_RATE = 22.0; // Running rate per km (INR)

    public ClusteringEngineService(RideRepository repo) {
        this.repo = repo;
    }

    /**
     * Executes Stage 3 of the AirShare pipeline:
     * Iterates through all 3 Bengaluru highway corridors, fetches waiting riders
     * pre-sorted
     * by PostGIS ST_LineLocatePoint progress, and applies a 2D 0/1 Knapsack
     * matcher.
     */
    public List<Map<String, Object>> executeCorridorPooling() {
        List<Map<String, Object>> fleet = new ArrayList<>();
        int vehicleCounter = 101;

        // Corridors: 1 = North-South (Silk Board), 2 = East (Whitefield/ORR), 3 = West
        // (Kengeri)
        for (int corridorId : new int[] { 1, 2, 3 }) {
            List<Map<String, Object>> queue = new ArrayList<>(repo.findCorridorCandidates(corridorId));

            while (!queue.isEmpty()) {
                List<Map<String, Object>> cabRiders = new ArrayList<>();
                int seatsOccupied = 0;
                int luggageOccupied = 0;
                double maxRouteKm = 0.0;
                double sumDistances = 0.0;

                Iterator<Map<String, Object>> it = queue.iterator();
                while (it.hasNext()) {
                    Map<String, Object> candidate = it.next();
                    int riderSeats = ((Number) candidate.get("seats_needed")).intValue();
                    int riderLuggage = ((Number) candidate.get("luggage_units")).intValue();
                    double riderKm = ((Number) candidate.get("dist_km")).doubleValue();

                    // 2D Knapsack Feasibility Check: simultaneously verify seats and boot capacity
                    if ((seatsOccupied + riderSeats <= MAX_SEATS) && (luggageOccupied + riderLuggage <= MAX_LUGGAGE)) {
                        seatsOccupied += riderSeats;
                        luggageOccupied += riderLuggage;
                        maxRouteKm = Math.max(maxRouteKm, riderKm);
                        sumDistances += riderKm;

                        cabRiders.add(candidate);
                        it.remove(); // Remove from waiting queue
                    }
                }

                if (cabRiders.isEmpty()) {
                    break;
                }

                // Stage 3.2: Compute Proportional Distance-Split Fares
                int riderCount = cabRiders.size();
                double totalRunningCost = maxRouteKm * KM_RATE;
                double totalCabCost = totalRunningCost + TOLL;
                double equalTollShare = TOLL / (double) riderCount;

                List<Map<String, Object>> riderManifest = new ArrayList<>();
                List<Long> dispatchedIds = new ArrayList<>();

                for (Map<String, Object> r : cabRiders) {
                    Long riderId = ((Number) r.get("id")).longValue();
                    dispatchedIds.add(riderId);

                    double distKm = ((Number) r.get("dist_km")).doubleValue();
                    double distanceProportion = (sumDistances > 0) ? (distKm / sumDistances) : (1.0 / riderCount);

                    // Proportional Fare Split Formula
                    int pooledFare = (int) Math.round(equalTollShare + (distanceProportion * totalRunningCost));
                    int soloFare = (int) Math.round(120.0 + (distKm * 28.0)); // Commercial solo cab baseline
                    int savingsPercent = (soloFare > pooledFare)
                            ? (int) Math.round(((soloFare - pooledFare) / (double) soloFare) * 100)
                            : 0;

                    Map<String, Object> riderInfo = new HashMap<>(r);
                    riderInfo.put("pooledFare", pooledFare);
                    riderInfo.put("soloFare", soloFare);
                    riderInfo.put("savingsPercent", savingsPercent);
                    riderManifest.add(riderInfo);

                    // Mark rider as DISPATCHED in database
                    repo.updateRideStatus(riderId, "DISPATCHED");
                }

                // Construct Dispatched Vehicle Record
                Map<String, Object> vehicle = new HashMap<>();
                vehicle.put("vehicleId", "MPV-" + vehicleCounter++);
                vehicle.put("vehicleModel", "Maruti Suzuki Ertiga (Smart Hybrid)");
                vehicle.put("corridorId", corridorId);
                vehicle.put("corridorName", getCorridorName(corridorId));
                vehicle.put("seatsOccupied", seatsOccupied);
                vehicle.put("maxSeats", MAX_SEATS);
                vehicle.put("luggageOccupied", luggageOccupied);
                vehicle.put("maxLuggage", MAX_LUGGAGE);
                vehicle.put("bootFullPercentage", (int) Math.round((luggageOccupied / (double) MAX_LUGGAGE) * 100));
                vehicle.put("seatFullPercentage", (int) Math.round((seatsOccupied / (double) MAX_SEATS) * 100));
                vehicle.put("maxDistanceKm", maxRouteKm);
                vehicle.put("totalCabFare", (int) Math.round(totalCabCost));
                vehicle.put("passengerCount", riderCount);
                vehicle.put("manifest", riderManifest);

                fleet.add(vehicle);
            }
        }

        return fleet;
    }

    private String getCorridorName(int id) {
        return switch (id) {
            case 1 -> "KIA to Silk Board via Hebbal & MG Road (North-South)";
            case 2 -> "KIA to Electronic City via Outer Ring Road (East)";
            case 3 -> "KIA to Kengeri via Bellary Rd & West ORR (North-West)";
            default -> "Corridor " + id;
        };
    }
}
