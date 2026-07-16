package com.jia.equipmentledger.maintenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MaintenanceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsAndListsMaintenanceRecord() throws Exception {
        long deviceId = insertDevice();

        MvcResult result = mockMvc.perform(post("/api/maintenance-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": %d,
                                  "maintenanceTime": "2026-07-16T09:30:00",
                                  "faultType": "机械故障",
                                  "faultDescription": "主轴异响",
                                  "repairDescription": "更换轴承",
                                  "maintenanceCost": 680.50,
                                  "operatorName": "李工"
                                }
                                """.formatted(deviceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId").value(deviceId))
                .andExpect(jsonPath("$.maintenanceCost").value(680.50))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("id").asLong()).isPositive();

        mockMvc.perform(get("/api/maintenance-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value(deviceId))
                .andExpect(jsonPath("$[0].faultType").value("机械故障"));
    }

    @Test
    void rejectsInvalidMaintenanceRecord() throws Exception {
        mockMvc.perform(post("/api/maintenance-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceId":999999999,"maintenanceTime":"2026-07-16T09:30:00","maintenanceCost":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("设备不存在"));
    }

    private long insertDevice() {
        String code = "MNT-" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update("""
                INSERT INTO devices (code, name, purchase_price, status)
                VALUES (?, '维修测试设备', 0, 'IN_USE')
                """, code);
        return jdbcTemplate.queryForObject("SELECT id FROM devices WHERE code = ?", Long.class, code);
    }
}
