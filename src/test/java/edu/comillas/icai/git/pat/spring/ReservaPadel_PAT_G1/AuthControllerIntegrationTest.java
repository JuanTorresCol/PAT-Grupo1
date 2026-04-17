package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.LoginRequest;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc //lanzar peticiones HTTP
@Transactional

public class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testFlujoCompletoRegistroYLogin() throws Exception {
        // preparo usuario
        String userJson = """
        {
          "nombre": "Prueba",
          "apellidos": "Integracion",
          "email": "test-integracion@ejemplo.com",
          "password": "password123",
          "rol": "USER",
          "telefono": "111222333"
        }
        """;

        //test registro
        mockMvc.perform(post("/pistaPadel/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)) // Enviamos el String directamente
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test-integracion@ejemplo.com"));;

        //test login (con el user que acabo de crear)
        LoginRequest loginRequest = new LoginRequest("test-integracion@ejemplo.com", "password123");

        mockMvc.perform(post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLoginFallido() throws Exception {
        String loginMalo = """
        {
          "email": "noexiste@ejemplo.com",
          "password": "mala"
        }
        """;

        mockMvc.perform(post("/pistaPadel/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginMalo))
                .andExpect(status().isUnauthorized()); // Ahora sí devolverá 401 gracias al ExceptionHandler
    }

}