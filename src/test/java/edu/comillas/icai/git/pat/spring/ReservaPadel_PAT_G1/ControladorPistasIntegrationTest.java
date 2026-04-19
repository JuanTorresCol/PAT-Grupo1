package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControladorPistasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String loginAdminYObtenerToken() throws Exception {
        String cred = "{\"email\":\"admin@padel.com\",\"password\":\"1234\"}";

        return mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cred))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .trim();
    }

    private String loginUserYObtenerToken() throws Exception {
        String cred = "{\"email\":\"user@padel.com\",\"password\":\"1234\"}";

        return mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cred))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .trim();
    }

    @Test
    @Order(0)
    public void login() throws Exception {
        String cred = "{\"email\":\"admin@padel.com\",\"password\":\"1234\"}";

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cred))
                .andExpect(status().isOk());
    }

    @Test
    @Order(1)
    public void creaPistaOk() throws Exception {
        String token = loginAdminYObtenerToken();

        String pista = """
                {
                  "nombre":"pista1",
                  "ubicacion":"alberto aguilera",
                  "precioHora":10.0,
                  "activa":false
                }
                """;

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(pista))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    public void creaPistaRepetidaError409() throws Exception {
        String token = loginAdminYObtenerToken();

        String pista2 = """
                {
                  "nombre":"pista1",
                  "ubicacion":"alberto",
                  "precioHora":1.0,
                  "activa":false
                }
                """;

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(pista2))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    public void creaPistaErrorBadRequest() throws Exception {
        String token = loginAdminYObtenerToken();

        String pista1 = "{\"idPista\":,\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":false}";

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(pista1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    public void listarPistasOK() throws Exception {
        String token = loginUserYObtenerToken();
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    public void listarPistasOKconFiltro() throws Exception {
        String token = loginUserYObtenerToken();
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts")
                        .param("filtro", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    public void verDetalleOK() throws Exception {
        String token = loginUserYObtenerToken();
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts/pista1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    public void verDetalleKO() throws Exception {
        String token = loginUserYObtenerToken();
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts/pistaNoExiste")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    public void actualizarPistaOK() throws Exception {
        String token = loginAdminYObtenerToken();

        String update = """
                {
                  "nombre":"pistaNueva",
                  "activa":true
                }
                """;

        this.mockMvc
                .perform(MockMvcRequestBuilders.patch("/pistaPadel/courts/pista1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("pistaNueva"))
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    @Order(9)
    public void actualizarPistaNotFound() throws Exception {
        String token = loginAdminYObtenerToken();

        String update = """
                {
                  "nombre":"nuevo"
                }
                """;

        this.mockMvc
                .perform(MockMvcRequestBuilders.patch("/pistaPadel/courts/noExiste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(update))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    public void actualizarPistaBadRequest() throws Exception {
        String token = loginAdminYObtenerToken();

        String update = "{}";

        this.mockMvc
                .perform(MockMvcRequestBuilders.patch("/pistaPadel/courts/pistaNueva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(update))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    public void deletePistaOK() throws Exception {
        String token = loginAdminYObtenerToken();

        this.mockMvc
                .perform(MockMvcRequestBuilders.delete("/pistaPadel/courts/pistaNueva")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    public void deletePistaNotFound() throws Exception {
        String token = loginAdminYObtenerToken();

        this.mockMvc
                .perform(MockMvcRequestBuilders.delete("/pistaPadel/courts/noExiste")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(13)
    public void availabilityGeneralOK() throws Exception {
        String token = loginUserYObtenerToken();
        String fecha = LocalDate.now().plusDays(1).toString();

        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/availability")
                        .param("date", fecha)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    public void availabilityFechaPasada() throws Exception {
        String token = loginUserYObtenerToken();
        String fecha = LocalDate.now().minusDays(1).toString();

        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/availability")
                        .param("date", fecha)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(15)
    public void availabilityCourtNotFound() throws Exception {
        String token = loginUserYObtenerToken();
        String fecha = LocalDate.now().plusDays(1).toString();

        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/availability")
                        .param("date", fecha)
                        .param("nombre", "noExiste")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(16)
    public void availabilityPorPistaNotFound() throws Exception {
        String token = loginUserYObtenerToken();
        String fecha = LocalDate.now().plusDays(1).toString();

        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts/noExiste/availability")
                        .param("date", fecha)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}