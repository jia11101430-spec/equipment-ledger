package com.jia.equipmentledger.workshop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkshopCreateApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsWorkshopAndReturnsIt() throws Exception {
        String name = "接口创建车间-" + UUID.randomUUID();
        String managerName = "王工";
        String location = "一号厂房";
        String phone = "13800138000";
        String description = "用于接口创建测试";

        MvcResult result = mockMvc.perform(post("/api/workshops")
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
                .andExpect(jsonPath("$.description").value(description))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        long responseId = response.get("id").longValue();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workshops WHERE name = ?", Integer.class, name
        );
        Long persistedId = jdbcTemplate.queryForObject(
                "SELECT id FROM workshops WHERE name = ?", Long.class, name
        );

        assertThat(count).isEqualTo(1);
        assertThat(responseId).isEqualTo(persistedId);
    }

    @Test
    void rejectsBlankWorkshopName() throws Exception {
        mockMvc.perform(post("/api/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("车间名称不能为空"));
    }

    @Test
    void rejectsMissingWorkshopName() throws Exception {
        mockMvc.perform(post("/api/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("车间名称不能为空"));
    }

    @Test
    void rejectsNullWorkshopRequestBody() throws Exception {
        mockMvc.perform(post("/api/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"message":"车间名称不能为空"}
                        """));
    }

    @Test
    void rejectsEmptyWorkshopRequestBody() throws Exception {
        mockMvc.perform(post("/api/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {"message":"车间名称不能为空"}
                        """));
    }

    @Test
    void trimsWorkshopNameInResponseAndPersistence() throws Exception {
        String rawName = "  空格车间-" + UUID.randomUUID() + "  ";
        String trimmedName = rawName.trim();

        mockMvc.perform(post("/api/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s"
                                }
                                """.formatted(rawName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(trimmedName));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workshops WHERE name = ?", Integer.class, trimmedName
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateWorkshopName() throws Exception {
        String name = "重复车间-" + UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO workshops (name) VALUES (?)", name);

        mockMvc.perform(post("/api/workshops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s"
                                }
                                """.formatted(name)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("车间名称已存在"));
    }
}
