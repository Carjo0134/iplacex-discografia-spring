package org.iplacex.proyectos.discografia;

import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErroresController {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> jsonIncorrecto() {
        return new ResponseEntity<>(Map.of("mensaje", "Revisa el cuerpo JSON de la solicitud"),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> errorBaseDeDatos() {
        return new ResponseEntity<>(Map.of("mensaje", "No se pudo completar la operación en la base de datos"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
