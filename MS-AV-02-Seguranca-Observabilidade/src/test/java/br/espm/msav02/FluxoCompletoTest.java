package br.espm.msav02;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
@AutoConfigureMockMvc
class FluxoCompletoTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MeterRegistry registry;

    private ResultActions login(String username, String senha) throws Exception {
        return mvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"senha\":\"%s\"}".formatted(username, senha)));
    }

    private void cadastrar(String username, String senha) throws Exception {
        mvc.perform(post("/")
                .with(httpBasic("manut", "manut123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"senha\":\"%s\"}".formatted(username, senha)))
                .andExpect(status().isCreated());
    }

    @Test
    void loginValidoRetorna200EInvalidoRetorna401() throws Exception {
        login("user", "user123").andExpect(status().isOk());
        login("naoexiste", "qualquer").andExpect(status().isUnauthorized());
    }

    @Test
    void segundaFalhaBloqueiaEAdminDesbloqueia() throws Exception {
        cadastrar("aluno1", "abc123");

        login("aluno1", "errada").andExpect(status().isUnauthorized());
        login("aluno1", "errada").andExpect(status().isUnauthorized()); // 2ª falha => bloqueado
        login("aluno1", "abc123").andExpect(status().isUnauthorized()); // senha certa, mas bloqueado

        mvc.perform(get("/").with(httpBasic("manut", "manut123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='aluno1')].locked").value(true));

        double bloqueados = registry.get("usuarios_bloqueados").gauge().value();
        assertTrue(bloqueados >= 1, "gauge usuarios_bloqueados deveria ser >= 1 e foi " + bloqueados);

        // manut não é admin
        mvc.perform(put("/unlock/aluno1").with(httpBasic("manut", "manut123")))
                .andExpect(status().isForbidden());
        mvc.perform(put("/unlock/aluno1").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());

        login("aluno1", "abc123").andExpect(status().isOk());
    }

    @Test
    void metricasContamLoginsValidosEInvalidos() throws Exception {
        double validosAntes = registry.get("logins_validos").counter().count();
        double invalidosAntes = registry.get("logins_invalidos").counter().count();

        login("user", "user123").andExpect(status().isOk());
        login("naoexiste", "x").andExpect(status().isUnauthorized());

        assertEquals(validosAntes + 1, registry.get("logins_validos").counter().count());
        assertEquals(invalidosAntes + 1, registry.get("logins_invalidos").counter().count());
    }

    @Test
    void trocarSenha() throws Exception {
        cadastrar("aluno2", "velha123");

        mvc.perform(put("/trocarsenha/aluno2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"errada\",\"nova\":\"nova123\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(put("/trocarsenha/aluno2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"velha123\",\"nova\":\"nova123\"}"))
                .andExpect(status().isOk());

        login("aluno2", "nova123").andExpect(status().isOk());

        mvc.perform(put("/trocarsenha/fantasma")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"atual\":\"a\",\"nova\":\"b\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rolesProtegemOsEndpoints() throws Exception {
        String body = "{\"username\":\"intruso\",\"senha\":\"abc\"}";

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/").with(httpBasic("user", "user123"))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(get("/").with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/").with(httpBasic("manut", "manut123")))
                .andExpect(status().isOk());
        mvc.perform(put("/unlock/user").with(httpBasic("manut", "manut123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void openApiDocumentaTodosOsEndpoints() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/login")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/trocarsenha/{userid}")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/unlock/{userid}")));
    }
}
