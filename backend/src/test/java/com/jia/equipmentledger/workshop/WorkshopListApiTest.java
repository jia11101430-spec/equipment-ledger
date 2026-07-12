package com.jia.equipmentledger.workshop;

import static org.hamcrest.Matchers.hasItems;
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
class WorkshopListApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsPersistedWorkshops() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String firstName = "接口测试车间-" + suffix + "-A";
        String secondName = "接口测试车间-" + suffix + "-B";

        jdbcTemplate.update(
                "INSERT INTO workshops (name, manager_name, location) VALUES (?, ?, ?)",
                firstName, "王工", "一号厂房"
        );
        jdbcTemplate.update(
                "INSERT INTO workshops (name, manager_name, location) VALUES (?, ?, ?)",
                secondName, "李工", "二号厂房"
        );

        mockMvc.perform(get("/api/workshops"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$..name", hasItems(firstName, secondName)))
                .andExpect(jsonPath("$..managerName", hasItems("王工", "李工")));
    }
}