package com.jia.equipmentledger.statistics;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StatisticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public StatisticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DeviceStatusCount> countDevicesByStatus() {
        return jdbcTemplate.query("""
                SELECT status, COUNT(*) AS device_count
                FROM devices
                GROUP BY status
                ORDER BY status ASC
                """, (resultSet, rowNum) -> new DeviceStatusCount(
                resultSet.getString("status"), resultSet.getLong("device_count")));
    }
}
