package org.iplacex.proyectos.discografia.discos;

import java.util.List;
import java.util.Map;
import org.iplacex.proyectos.discografia.artistas.IArtistaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class DiscoController {

    private final IDiscoRepository discoRepository;
    private final IArtistaRepository artistaRepository;

    public DiscoController(IDiscoRepository discoRepository, IArtistaRepository artistaRepository) {
        this.discoRepository = discoRepository;
        this.artistaRepository = artistaRepository;
    }

    @PostMapping(value = "/disco", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> HandlePostDiscoRequest(@RequestBody Disco disco) {
        if (disco.idArtista == null || disco.idArtista.isBlank()) {
            return new ResponseEntity<>(Map.of("mensaje", "Falta indicar idArtista"), HttpStatus.BAD_REQUEST);
        }
        if (!artistaRepository.existsById(disco.idArtista)) {
            return new ResponseEntity<>(Map.of("mensaje", "Artista no encontrado"), HttpStatus.NOT_FOUND);
        }
        disco._id = null;
        return new ResponseEntity<>(discoRepository.insert(disco), HttpStatus.CREATED);
    }

    @GetMapping(value = "/discos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Disco>> HandleGetDiscosRequest() {
        return new ResponseEntity<>(discoRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/disco/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> HandleGetDiscoRequest(@PathVariable("id") String id) {
        Disco disco = discoRepository.findById(id).orElse(null);
        if (disco == null) {
            return new ResponseEntity<>(Map.of("mensaje", "Disco no encontrado"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(disco, HttpStatus.OK);
    }

    @GetMapping(value = "/artista/{id}/discos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Disco>> HandleGetDiscosByArtistaRequest(@PathVariable("id") String id) {
        return new ResponseEntity<>(discoRepository.findDiscosByIdArtista(id), HttpStatus.OK);
    }
}
