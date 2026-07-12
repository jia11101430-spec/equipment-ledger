package com.jia.equipmentledger.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringDataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void connectsToEquipmentLedgerThroughSpring() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getCatalog()).isEqualTo("equipment_ledger");
        }
    }
}