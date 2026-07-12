package com.jia.equipmentledger.workshop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkshopCreateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsWorkshopAndReturnsIt() throws Exception {
        String name = "接口创建车间-" + UUID.randomUUID();
        String managerName = "王工";
        String location = "一号厂房";
        String phone = "13800138000";
        String description = "用于接口创建测试";

        mockMvc.perform(post("/api/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "managerName": "%s",
                                  "location": "%s",
                                  "phone": "%s",
                                  "description": "%s"
                                }
                                """.formatted(name, managerName, location, phone, description)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.managerName").value(managerName))
                .andExpect(jsonPath("$.location").value(location))
                .andExpect(jsonPath("$.phone").value(phone))
                .andExpect(jsonPath("$.description").value(description));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workshops WHERE name = ?", Integer.class, name
        );

        assertThat(count).isEqualTo(1);
    }
}
