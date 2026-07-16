package com.jia.equipmentledger.device;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DeviceRepository {

    private final JdbcTemplate jdbcTemplate;

    public DeviceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DeviceResponse> findAll() {
        String sql = """
                SELECT id, code, name, model, factory_date, purchase_price,
                       workshop_id, responsible_person, status, description
                FROM devices
                ORDER BY id ASC
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new DeviceResponse(
                resultSet.getLong("id"),
                resultSet.getString("code"),
                resultSet.getString("name"),
                resultSet.getString("model"),
                resultSet.getObject("factory_date", java.time.LocalDate.class),
                resultSet.getBigDecimal("purchase_price"),
                resultSet.getObject("workshop_id", Long.class),
                resultSet.getString("responsible_person"),
                resultSet.getString("status"),
                resultSet.getString("description")
        ));
    }
}
