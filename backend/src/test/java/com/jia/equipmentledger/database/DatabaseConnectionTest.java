package com.jia.equipmentledger.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class DatabaseConnectionTest {

    @Test
    void connectsToEquipmentLedger() throws SQLException {
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        assertThat(username).isEqualTo("root");
        assertThat(password).isNotBlank();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/equipment_ledger"
                        + "?useUnicode=true&characterEncoding=utf8"
                        + "&serverTimezone=Asia/Shanghai",
                username,
                password)) {
            assertThat(connection.getCatalog()).isEqualTo("equipment_ledger");
        }
    }
}