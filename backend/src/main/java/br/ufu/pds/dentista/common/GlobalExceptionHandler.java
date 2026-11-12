package br.ufu.pds.dentista.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Formato único de erro para o frontend:
 * { "status": 400, "mensagem": "...", "campos": { "cpf": "CPF inválido" }, "timestamp": "..." }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErroResponse(int status, String mensagem, Map<String, String> campos, LocalDateTime timestamp) { }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException ex) {
        return erro(HttpStatus.BAD_REQUEST, "JSON inválido ou data fora do formato yyyy-MM-dd", null);
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(NaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException ex) {
        return erro(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /** Rede de segurança: duas requisições simultâneas com o mesmo CPF batem na UNIQUE do banco. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
        return erro(HttpStatus.CONFLICT, "Registro viola uma restrição do banco (CPF duplicado?)", null);
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String msg, Map<String, String> campos) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), msg, campos, LocalDateTime.now()));
    }
}
