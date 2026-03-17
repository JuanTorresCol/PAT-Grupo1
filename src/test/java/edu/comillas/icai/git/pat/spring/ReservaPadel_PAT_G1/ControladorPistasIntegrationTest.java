package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControladorPistasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @WithMockUser(roles = "ADMIN")
    public void creaPistaOk () throws Exception {
        String pista = "{\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @Order(2)
    @WithMockUser(roles = "ADMIN")
    public void creaPistaRepetidaError409 () throws Exception {
        String pista2 = "{\"nombre\":\"pista1\",\"ubicacion\":\"alberto\",\"precioHora\":1.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista2))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    @Order(3)
    @WithMockUser(roles = "ADMIN")
    public void creaPistaErrorBadRequest () throws Exception {
        String pista1 = "{\"idPista\":,\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista1))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Order(4)
    public void listarpistasOK () throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(5)
    public void listarpistasOKconFiltro () throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts")
                        .param("filtro", "true"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(6)
    public void verDetalleOK () throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts/pista1"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(7)
    public void verDetalleKO () throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts/pistaNoExiste"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @Order(8)
    @WithMockUser(roles = "ADMIN")
    public void actualizarPistaOK() throws Exception {
        String update = "{\"nombre\":\"pistaNueva\",\"activa\":true}";

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/pistaPadel/courts/pista1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nombre").value("pistaNueva"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.activa").value(true));
    }

    @Test
    @Order(9)
    @WithMockUser(roles = "ADMIN")
    public void actualizarPistaNotFound() throws Exception {
        String update = "{\"nombre\":\"nuevo\"}";

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/pistaPadel/courts/noExiste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @Order(10)
    @WithMockUser(roles = "ADMIN")
    public void actualizarPistaBadRequest() throws Exception {
        String update = "{}";

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/pistaPadel/courts/pistaNueva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Order(11)
    @WithMockUser(roles = "ADMIN")
    public void deletePistaOK() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/pistaPadel/courts/pistaNueva"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(12)
    @WithMockUser(roles = "ADMIN")
    public void deletePistaNotFound() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/pistaPadel/courts/noExiste"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @Order(13)
    @WithMockUser(roles = "ADMIN")
    public void availabilityGeneralOK() throws Exception {
        String fecha = LocalDate.now().plusDays(1).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/pistaPadel/availability")
                        .param("date", fecha))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(14)
    public void availabilityFechaPasada() throws Exception {
        String fecha = LocalDate.now().minusDays(1).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/pistaPadel/availability")
                        .param("date", fecha))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Order(15)
    public void availabilityCourtNotFound() throws Exception {
        String fecha = LocalDate.now().plusDays(1).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/pistaPadel/availability")
                        .param("date", fecha)
                        .param("nombre", "noExiste"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @Order(16)
    public void availabilityPorPistaNotFound() throws Exception {
        String fecha = LocalDate.now().plusDays(1).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/pistaPadel/courts/noExiste/availability")
                        .param("date", fecha))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}