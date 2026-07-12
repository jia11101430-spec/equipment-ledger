package com.jia.equipmentledger.workshop;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WorkshopRepository {

    private final JdbcTemplate jdbcTemplate;

    public WorkshopRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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