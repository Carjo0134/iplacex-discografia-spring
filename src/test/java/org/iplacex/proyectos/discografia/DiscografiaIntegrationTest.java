package org.iplacex.proyectos.discografia;

import org.iplacex.proyectos.discografia.artistas.IArtistaRepository;
import org.iplacex.proyectos.discografia.discos.IDiscoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/?serverSelectionTimeoutMS=5000",
        "spring.data.mongodb.database=discografia-test"
})
@AutoConfigureMockMvc
class DiscografiaIntegrationTest {

    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired IArtistaRepository artistas;
    @Autowired IDiscoRepository discos;

    @AfterEach
    void limpiarDatosDePrueba() {
        discos.deleteAll();
        artistas.deleteAll();
    }

    private String crearArtista(String nombre) throws Exception {
        String cuerpo = json.writeValueAsString(java.util.Map.of(
                "nombre", nombre, "estilos", java.util.List.of("Rock"),
                "anioFundacion", 1965, "estaActivo", false));
        String respuesta = http.perform(post("/api/artista").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated()).andExpect(jsonPath("_id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("_id").asText();
    }

    @Test
    void crearConsultarActualizarYEliminarArtista() throws Exception {
        String id = crearArtista("Los Jaivas");
        assertThat(artistas.existsById(id)).isTrue();
        http.perform(get("/api/artistas")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Los Jaivas"));
        http.perform(get("/api/artista/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("estilos[0]").value("Rock"));
        http.perform(put("/api/artista/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"_id":"otro-id","nombre":"Los Jaivas","estilos":["Rock progresivo"],
                                 "anioFundacion":1963,"estaActivo":true}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("_id").value(id))
                .andExpect(jsonPath("anioFundacion").value(1963));
        assertThat(artistas.findById(id).orElseThrow().estaActivo).isTrue();
        assertThat(artistas.existsById("otro-id")).isFalse();
        http.perform(delete("/api/artista/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("mensaje").value("Artista eliminado"));
        assertThat(artistas.existsById(id)).isFalse();
        http.perform(get("/api/artista/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void discosSeGuardanYSeFiltranPorArtista() throws Exception {
        String id = crearArtista("Los Prisioneros");
        String otro = crearArtista("Congreso");
        String cuerpo = json.writeValueAsString(java.util.Map.of("idArtista", id,
                "nombre", "La voz de los 80", "anioLanzamiento", 1984,
                "canciones", java.util.List.of("La voz de los 80", "Sexo")));
        String respuesta = http.perform(post("/api/disco").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo)).andExpect(status().isCreated())
                .andExpect(jsonPath("_id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        JsonNode disco = json.readTree(respuesta);
        String discoId = disco.get("_id").asText();
        assertThat(discos.findById(discoId).orElseThrow().idArtista).isEqualTo(id);
        http.perform(get("/api/discos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        http.perform(get("/api/disco/{id}", discoId)).andExpect(status().isOk())
                .andExpect(jsonPath("canciones[1]").value("Sexo"));
        http.perform(get("/api/artista/{id}/discos", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0]._id").value(discoId));
        http.perform(get("/api/artista/{id}/discos", otro)).andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void recursosInexistentesDevuelven404() throws Exception {
        String id = "000000000000000000000000";
        http.perform(get("/api/artista/{id}", id)).andExpect(status().isNotFound());
        http.perform(put("/api/artista/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Inexistente\"}")).andExpect(status().isNotFound());
        http.perform(delete("/api/artista/{id}", id)).andExpect(status().isNotFound());
        http.perform(get("/api/disco/{id}", id)).andExpect(status().isNotFound());
        http.perform(post("/api/disco").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idArtista\":\"" + id + "\",\"nombre\":\"Sin artista\"}"))
                .andExpect(status().isNotFound());
        assertThat(discos.count()).isZero();
    }

    @Test
    void jsonIncorrectoYDiscoSinArtistaDevuelven400() throws Exception {
        http.perform(post("/api/artista").contentType(MediaType.APPLICATION_JSON)
                .content("{incorrecto")).andExpect(status().isBadRequest());
        http.perform(post("/api/disco").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Sin artista\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void permitePeticionesDeOtroOrigen() throws Exception {
        http.perform(options("/api/artista").header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}
