package com.jia.equipmentledger.workshop;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class WorkshopRepository {

    private final JdbcTemplate jdbcTemplate;

    public WorkshopRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public WorkshopResponse create(CreateWorkshopRequest request) {
        String sql = """
                INSERT INTO workshops (name, manager_name, location, phone, description)
                VALUES (?, ?, ?, ?, ?)
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.name());
            statement.setString(2, request.managerName());
            statement.setString(3, request.location());
            statement.setString(4, request.phone());
            statement.setString(5, request.description());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建车间后未获取到 ID");
        }

        return new WorkshopResponse(
                key.longValue(),
                request.name(),
                request.managerName(),
                request.location(),
                request.phone(),
                request.description()
        );
    }

    public List<WorkshopResponse> findAll() {
        String sql = """
                SELECT id, name, manager_name, location, phone, description
                FROM workshops
                ORDER BY id ASC
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new WorkshopResponse(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("manager_name"),
                resultSet.getString("location"),
                resultSet.getString("phone"),
                resultSet.getString("description")
        ));
    }
}
