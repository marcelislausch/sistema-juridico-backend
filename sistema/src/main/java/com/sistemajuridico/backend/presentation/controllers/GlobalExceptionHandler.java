package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        Map<String, Object> corpoErro = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value(),
                "erro", "Not Found",
                "mensagem", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpoErro);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleRegraNegocio(RegraNegocioException ex) {
        Map<String, Object> corpoErro = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "erro", "Unprocessable Entity",
                "mensagem", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(corpoErro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> camposErros = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            camposErros.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> corpoErro = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "erro", "Bad Request",
                "mensagem", "Erro de validação nos campos informados.",
                "erros", camposErros
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpoErro);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> corpoErro = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.CONFLICT.value(),
                "erro", "Conflict",
                "mensagem", "Já existe um registro com este valor único no sistema."
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpoErro);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String mensagem = ex.getMessage() != null ? ex.getMessage() : "Erro interno no servidor";
        Map<String, Object> corpoErro = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "erro", "Bad Request",
                "mensagem", mensagem
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpoErro);
    }
}
