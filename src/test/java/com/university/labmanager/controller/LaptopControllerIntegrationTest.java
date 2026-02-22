package com.university.labmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.labmanager.model.Laptop;
import com.university.labmanager.model.enums.LaptopStatus;
import com.university.labmanager.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
public class LaptopControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Laptop sampleLaptop;

    @BeforeEach
    void setUp() {
        sampleLaptop = new Laptop();
        sampleLaptop.setId(1L);
        sampleLaptop.setSerialNumber("SN-999");
        sampleLaptop.setModel("Lenovo ThinkPad");
        sampleLaptop.setStatus(LaptopStatus.AVAILABLE);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testGetAllLaptops() throws Exception {
        List<Laptop> laptops = Arrays.asList(sampleLaptop);
        Mockito.when(reservationService.findAllLaptops()).thenReturn(laptops);

        mockMvc.perform(get("/api/laptops/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].model", is("Lenovo ThinkPad")));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testCreateLaptop() throws Exception {
        Mockito.when(reservationService.saveLaptop(Mockito.any(Laptop.class))).thenReturn(sampleLaptop);

        mockMvc.perform(post("/api/laptops")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleLaptop)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber", is("SN-999")));
    }
}
