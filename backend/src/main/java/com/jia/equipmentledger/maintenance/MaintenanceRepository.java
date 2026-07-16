package com.jia.equipmentledger.maintenance;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class MaintenanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaintenanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MaintenanceResponse create(MaintenanceRequest request) {
        String sql = """
                INSERT INTO maintenance_records
                    (device_id, maintenance_time, fault_type, fault_description,
                     repair_description, maintenance_cost, operator_name)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, request.deviceId());
            statement.setObject(2, request.maintenanceTime());
            statement.setString(3, request.faultType());
            statement.setString(4, request.faultDescription());
            statement.setString(5, request.repairDescription());
            statement.setBigDecimal(6, request.maintenanceCost());
            statement.setString(7, request.operatorName());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建维修记录后未获取到 ID");
        }
        return findById(key.longValue());
    }

    public List<MaintenanceResponse> findAll() {
        String sql = """
                SELECT id, device_id, maintenance_time, fault_type, fault_description,
                       repair_description, maintenance_cost, operator_name
                FROM maintenance_records
                ORDER BY maintenance_time DESC, id DESC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> mapRow(resultSet));
    }

    public MaintenanceResponse findById(long id) {
        String sql = """
                SELECT id, device_id, maintenance_time, fault_type, fault_description,
                       repair_description, maintenance_cost, operator_name
                FROM maintenance_records WHERE id = ?
                """;
        List<MaintenanceResponse> records = jdbcTemplate.query(sql,
                (resultSet, rowNum) -> mapRow(resultSet), id);
        return records.isEmpty() ? null : records.getFirst();
    }

    public boolean deviceExists(long deviceId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM devices WHERE id = ?", Integer.class, deviceId);
        return count != null && count > 0;
    }

    private MaintenanceResponse mapRow(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        return new MaintenanceResponse(
                resultSet.getLong("id"),
                resultSet.getLong("device_id"),
                resultSet.getObject("maintenance_time", java.time.LocalDateTime.class),
                resultSet.getString("fault_type"),
                resultSet.getString("fault_description"),
                resultSet.getString("repair_description"),
                resultSet.getBigDecimal("maintenance_cost"),
                resultSet.getString("operator_name")
        );
    }
}
