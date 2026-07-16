package com.jia.equipmentledger.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeviceCrudApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsUpdatesAndDeletesDevice() throws Exception {
        String code = "CRUD-" + UUID.randomUUID().toString().substring(0, 8);
        String body = """
                {
                  "code": "%s",
                  "name": "数控车床",
                  "model": "CNC-01",
                  "purchasePrice": 125000.00,
                  "status": "IN_USE"
                }
                """.formatted(code);

        MvcResult createResult = mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.status").value("IN_USE"))
                .andReturn();
        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long id = created.get("id").longValue();

        mockMvc.perform(put("/api/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("数控车床", "磨床").replace("IN_USE", "MAINTENANCE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("磨床"))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        mockMvc.perform(delete("/api/devices/{id}", id))
                .andExpect(status().isNoContent());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM devices WHERE id = ?", Integer.class, id);
        assertThat(count).isZero();
    }

    @Test
    void rejectsInvalidDeviceInput() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"", "name":"设备", "purchasePrice":-1, "status":"UNKNOWN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("设备编号不能为空"));
    }

    @Test
    void returnsNotFoundWhenUpdatingOrDeletingMissingDevice() throws Exception {
        mockMvc.perform(put("/api/devices/999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"MISSING", "name":"设备"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/devices/999999999"))
                .andExpect(status().isNotFound());
    }
}
