package com.ewit.airshare.repository;

import com.ewit.airshare.model.HighwayCorridor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface HighwayCorridorRepository extends JpaRepository<HighwayCorridor, Integer> {

    @Query(value = """
        SELECT id, corridor_name, ST_AsGeoJSON(spine_path::geometry) as geojson
        FROM highway_corridors
        ORDER BY id ASC
        """, nativeQuery = true)
    List<Map<String, Object>> findCorridorsWithGeoJSON();
}
