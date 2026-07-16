package com.jia.equipmentledger.statistics;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StatisticsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void countsDevicesByStatus() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("INSERT INTO devices (code, name, purchase_price, status) VALUES (?, ?, 0, 'RETIRED')",
                "STAT-" + suffix, "统计设备");

        mockMvc.perform(get("/api/statistics/devices/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.status == 'RETIRED')].count").isNotEmpty());
    }
}
