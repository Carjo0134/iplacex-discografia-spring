package org.iplacex.proyectos.discografia.artistas;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ArtistaController {

    private final IArtistaRepository artistaRepository;

    public ArtistaController(IArtistaRepository artistaRepository) {
        this.artistaRepository = artistaRepository;
    }

    @PostMapping(value = "/artista", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> HandleInsertArtistaRequest(@RequestBody Artista artista) {
        artista._id = null;
        Artista nuevo = artistaRepository.insert(artista);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping(value = "/artistas", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Artista>> HandleGetAristasRequest() {
        return new ResponseEntity<>(artistaRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/artista/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> HandleGetArtistaRequest(@PathVariable("id") String id) {
        Artista artista = artistaRepository.findById(id).orElse(null);
        if (artista == null) {
            return new ResponseEntity<>(Map.of("mensaje", "Artista no encontrado"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(artista, HttpStatus.OK);
    }

    @PutMapping(value = "/artista/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> HandleUpdateArtistaRequest(
            @PathVariable("id") String id, @RequestBody Artista artista) {
        if (!artistaRepository.existsById(id)) {
            return new ResponseEntity<>(Map.of("mensaje", "Artista no encontrado"), HttpStatus.NOT_FOUND);
        }
        artista._id = id;
        return new ResponseEntity<>(artistaRepository.save(artista), HttpStatus.OK);
    }

    @DeleteMapping(value = "/artista/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> HandleDeleteArtistaRequest(@PathVariable("id") String id) {
        if (!artistaRepository.existsById(id)) {
            return new ResponseEntity<>(Map.of("mensaje", "Artista no encontrado"), HttpStatus.NOT_FOUND);
        }
        artistaRepository.deleteById(id);
        return new ResponseEntity<>(Map.of("mensaje", "Artista eliminado"), HttpStatus.OK);
    }
}
