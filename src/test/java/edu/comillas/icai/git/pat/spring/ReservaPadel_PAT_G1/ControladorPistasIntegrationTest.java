 package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

@WebMvcTest(ControladorPistas.class)
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ControladorPistasIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void creaPistaOk () throws Exception {
        String pista = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(pista));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void creaPistaRepetidaError409 () throws Exception {
        String pista1 = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":false}";
        String pista2 = "{\"idPista\":\"pista1\",\"nombre\":\"pista\",\"ubicacion\":\"alberto\",\"precioHora\":1.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista1)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(pista1));
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista2)
                )
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void creaPistaErrorBadRequest () throws Exception {
        String pista1 = "{\"idPista\":,\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(pista1))

                // Then
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void listarpistasOK () throws Exception {
        String pista1 = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":false}";
        String pista2 = "{\"idPista\":\"pista2\",\"nombre\":\"pista2\",\"ubicacion\":\"alberto\",\"precioHora\":1.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista1)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(pista1));
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista2)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(pista2));
        this.mockMvc // expected pista1 and pista2
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void listarpistasOKconFiltro () throws Exception {
        String pista1 = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":true}";
        String pista2 = "{\"idPista\":\"pista2\",\"nombre\":\"pista2\",\"ubicacion\":\"alberto\",\"precioHora\":1.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista1)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(pista1));
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista2)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(pista2));
        this.mockMvc // expected pista1 ONLY
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts")
                        .contentType(MediaType.ALL)
                        .param("filtro", "true"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void listarpistasKO () throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void verDetalleOK () throws Exception {
        String pista1 = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista1)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(pista1));
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts/pista1"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void verDetalleKO () throws Exception {
        String pista1 = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto aguilera\",\"precioHora\":10.0,\"activa\":false}";
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista1)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(pista1));
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/pistaPadel/courts/pista2")) //no existe una pista con ese ID
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void actualizarPistaOK() throws Exception {

        String pista = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto\",\"precioHora\":10.0,\"activa\":false}";
        String update = "{\"nombre\":\"pistaNueva\",\"activa\":true}";

        this.mockMvc.perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/pistaPadel/courts/pista1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nombre").value("pistaNueva"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.activa").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void actualizarPistaNotFound() throws Exception {

        String update = "{\"nombre\":\"nuevo\"}";

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/pistaPadel/courts/noExiste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void actualizarPistaBadRequest() throws Exception {

        String pista = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto\",\"precioHora\":10.0,\"activa\":false}";
        String update = "{}";

        this.mockMvc.perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/pistaPadel/courts/pista1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deletePistaOK() throws Exception {

        String pista = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto\",\"precioHora\":10.0,\"activa\":false}";

        this.mockMvc.perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/pistaPadel/courts/pista1"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deletePistaNotFound() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/pistaPadel/courts/noExiste"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void availabilityGeneralOK() throws Exception {

        String pista = "{\"idPista\":\"pista1\",\"nombre\":\"pista1\",\"ubicacion\":\"alberto\",\"precioHora\":10.0,\"activa\":true}";
        String fecha = LocalDate.now().plusDays(1).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.post("/pistaPadel/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pista))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        this.mockMvc.perform(MockMvcRequestBuilders.get("/pistaPadel/availability")
                        .param("date", fecha))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void availabilityFechaPasada() throws Exception {

        String fecha = LocalDate.now().minusDays(1).toString();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/pistaPadel/availability")
                        .param("date", fecha))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

     @Test
     @WithMockUser(roles = "USER")
     public void availabilityCourtNotFound() throws Exception {

         String fecha = LocalDate.now().plusDays(1).toString();

         this.mockMvc.perform(MockMvcRequestBuilders.get("/pistaPadel/availability")
                         .param("date", fecha)
                         .param("courtId", "noExiste"))
                 .andExpect(MockMvcResultMatchers.status().isNotFound());
     }

     @Test
     @WithMockUser(roles = "USER")
     public void availabilityPorPistaNotFound() throws Exception {

         String fecha = LocalDate.now().plusDays(1).toString();

         this.mockMvc.perform(MockMvcRequestBuilders.get("/pistaPadel/courts/noExiste/availability")
                         .param("date", fecha))
                 .andExpect(MockMvcResultMatchers.status().isNotFound());
     }
 }
