package com.ewit.airshare.repository;

import com.ewit.airshare.model.RideRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface RideRepository extends JpaRepository<RideRequest, Long> {

    /**
     * Native PostGIS query: Filters waiting passengers within 3,500m of the highway corridor
     * and projects their drop-off coordinates onto the highway polyline via ST_LineLocatePoint.
     * Returned riders are pre-sorted monotonically (0.0 to 1.0 progress) with zero zigzagging.
     */
    @Query(value = """
        SELECT r.id, 
               r.passenger_name, 
               r.flight_no, 
               r.seats_needed, 
               r.luggage_units,
               r.drop_name,
               ST_Y(r.drop_point::geometry) AS lat, 
               ST_X(r.drop_point::geometry) AS lon,
               ST_LineLocatePoint(c.spine_path::geometry, r.drop_point::geometry) AS progress,
               ROUND((ST_Distance(r.drop_point, ST_GeogFromText('POINT(77.7068 13.1989)')) / 1000.0)::numeric, 1) AS dist_km
        FROM ride_requests r 
        JOIN highway_corridors c ON c.id = :corridorId
        WHERE r.status = 'WAITING' 
          AND ST_DWithin(r.drop_point, c.spine_path, 3500)
        ORDER BY progress ASC
        """, nativeQuery = true)
    List<Map<String, Object>> findCorridorCandidates(@Param("corridorId") int corridorId);

    /**
     * Ingests a new passenger readiness event directly into PostGIS using ST_MakePoint.
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO ride_requests 
        (passenger_name, flight_no, seats_needed, cabin_bags, checkin_bags, drop_name, drop_point, status)
        VALUES 
        (:name, :flightNo, :seats, :cabin, :checkin, :dropName, 
         ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, 'WAITING')
        """, nativeQuery = true)
    void insertCurbsideRider(
        @Param("name") String name,
        @Param("flightNo") String flightNo,
        @Param("seats") int seats,
        @Param("cabin") int cabin,
        @Param("checkin") int checkin,
        @Param("dropName") String dropName,
        @Param("lon") double lon,
        @Param("lat") double lat
    );

    @Modifying
    @Transactional
    @Query("UPDATE RideRequest r SET r.status = :status WHERE r.id = :id")
    void updateRideStatus(@Param("id") Long id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ride_requests WHERE status = 'WAITING'", nativeQuery = true)
    void clearWaitingPool();
}
