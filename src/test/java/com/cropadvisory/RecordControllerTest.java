package com.cropadvisory;

import com.cropadvisory.repository.RecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecordRepository recordRepository;

    @Test
    void recordsPageRequiresLogin() throws Exception {
        mockMvc.perform(get("/records")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "farmer1", roles = "FARMER")
    void farmerCanOpenNewQueryForm() throws Exception {
        mockMvc.perform(get("/records/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("records/form"));
    }

    @Test
    @WithMockUser(username = "expert1", roles = "EXPERT")
    void expertCannotOpenNewQueryForm() throws Exception {
        mockMvc.perform(get("/records/new")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "expert1", roles = "EXPERT")
    void expertCanSeeRecordList() throws Exception {
        mockMvc.perform(get("/records"))
                .andExpect(status().isOk())
                .andExpect(view().name("records/list"));
    }

    @Test
    @WithMockUser(username = "farmer1", roles = "FARMER")
    void farmerCanSubmitQuery() throws Exception {
        long before = recordRepository.count();

        mockMvc.perform(post("/records").with(csrf())
                        .param("village", "Rampur")
                        .param("crop", "Rice")
                        .param("problemTitle", "Brown spots on leaves")
                        .param("description", "Spots started after rain."))
                .andExpect(status().is3xxRedirection());

        assertEquals(before + 1, recordRepository.count());
    }

    @Test
    @WithMockUser(username = "farmer1", roles = "FARMER")
    void emptyVillageShowsValidationError() throws Exception {
        mockMvc.perform(post("/records").with(csrf())
                        .param("village", "")
                        .param("crop", "Rice")
                        .param("problemTitle", "Brown spots on leaves"))
                .andExpect(status().isOk())
                .andExpect(view().name("records/form"))
                .andExpect(model().attributeHasFieldErrors("form", "village"));
    }
}
