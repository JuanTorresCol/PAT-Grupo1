package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "maria@test.com", roles = "USER")
    void listarReservasOk() throws Exception {
        mockMvc.perform(get("/pistaPadel/reservations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "maria@test.com", roles = "USER")
    void buscarReservaNotFound() throws Exception {
        mockMvc.perform(get("/pistaPadel/reservations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "maria@test.com", roles = "USER")
    void modificarReservaBadRequest() throws Exception {
        mockMvc.perform(patch("/pistaPadel/reservations/1")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "maria@test.com", roles = "USER")
    void cancelarReservaNotFound() throws Exception {
        mockMvc.perform(delete("/pistaPadel/reservations/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void adminReservationsUserForbidden() throws Exception {
        mockMvc.perform(get("/pistaPadel/admin/reservations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void adminReservationsAdminOk() throws Exception {
        mockMvc.perform(get("/pistaPadel/admin/reservations"))
                .andExpect(status().isOk());
    }

}