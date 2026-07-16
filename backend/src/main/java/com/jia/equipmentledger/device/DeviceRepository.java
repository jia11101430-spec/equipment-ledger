package com.jia.equipmentledger.device;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;

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

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> mapRow(resultSet));
    }

    public DeviceResponse create(DeviceRequest request) {
        String sql = """
                INSERT INTO devices (code, name, model, factory_date, purchase_price,
                                     workshop_id, responsible_person, status, description)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setParameters(statement, request);
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建设备后未获取到 ID");
        }
        return findById(key.longValue());
    }

    public DeviceResponse findById(long id) {
        String sql = """
                SELECT id, code, name, model, factory_date, purchase_price,
                       workshop_id, responsible_person, status, description
                FROM devices WHERE id = ?
                """;
        List<DeviceResponse> devices = jdbcTemplate.query(sql, (resultSet, rowNum) -> mapRow(resultSet), id);
        return devices.isEmpty() ? null : devices.getFirst();
    }

    public boolean workshopExists(long workshopId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workshops WHERE id = ?", Integer.class, workshopId);
        return count != null && count > 0;
    }

    public DeviceResponse update(long id, DeviceRequest request) {
        String sql = """
                UPDATE devices
                SET code = ?, name = ?, model = ?, factory_date = ?, purchase_price = ?,
                    workshop_id = ?, responsible_person = ?, status = ?, description = ?
                WHERE id = ?
                """;
        int updated = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql);
            setParameters(statement, request);
            statement.setLong(10, id);
            return statement;
        });
        return updated == 0 ? null : findById(id);
    }

    public boolean deleteById(long id) {
        return jdbcTemplate.update("DELETE FROM devices WHERE id = ?", id) > 0;
    }

    private void setParameters(PreparedStatement statement, DeviceRequest request) throws java.sql.SQLException {
        statement.setString(1, request.code());
        statement.setString(2, request.name());
        statement.setString(3, request.model());
        statement.setObject(4, request.factoryDate());
        statement.setBigDecimal(5, request.purchasePrice());
        if (request.workshopId() == null) {
            statement.setObject(6, null);
        } else {
            statement.setLong(6, request.workshopId());
        }
        statement.setString(7, request.responsiblePerson());
        statement.setString(8, request.status());
        statement.setString(9, request.description());
    }

    private DeviceResponse mapRow(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        return new DeviceResponse(
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
        );
    }
}
