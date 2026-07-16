package com.jia.equipmentledger.device;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class DeviceListApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsPersistedDevices() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String code = "DEV-" + suffix;
        jdbcTemplate.update("""
                INSERT INTO devices (code, name, model, purchase_price, status)
                VALUES (?, ?, ?, ?, ?)
                """, code, "数控车床", "CNC-01", 125000.00, "IN_USE");

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$..code", hasItem(code)))
                .andExpect(jsonPath("$..name", hasItem("数控车床")))
                .andExpect(jsonPath("$..purchasePrice", hasItem(125000.00)))
                .andExpect(jsonPath("$..status", hasItem("IN_USE")));
    }
}
