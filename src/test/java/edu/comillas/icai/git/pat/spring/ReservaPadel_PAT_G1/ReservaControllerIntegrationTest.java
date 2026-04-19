package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
    void listarReservasOk() throws Exception {
        String token = loginUserYObtenerToken();
        mockMvc.perform(get("/pistaPadel/reservations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void buscarReservaNotFound() throws Exception {
        String token = loginUserYObtenerToken();
        mockMvc.perform(get("/pistaPadel/reservations/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void modificarReservaBadRequest() throws Exception {
        String token = loginUserYObtenerToken();
        mockMvc.perform(patch("/pistaPadel/reservations/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelarReservaNotFound() throws Exception {
        String token = loginUserYObtenerToken();
        mockMvc.perform(delete("/pistaPadel/reservations/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }


    @Test
    void adminReservationsUserForbidden() throws Exception {
        String token = loginUserYObtenerToken();
        mockMvc.perform(get("/pistaPadel/admin/reservations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminReservationsAdminOk() throws Exception {
        String token = loginAdminYObtenerToken();
        mockMvc.perform(get("/pistaPadel/admin/reservations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

}