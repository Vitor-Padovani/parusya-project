package com.parusya.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erros de negócio com código mapeado
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        var body = new ErrorResponse(
                ex.getErrorCode().name(),
                ex.getMessage()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    // Erros de validação de Bean Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Inválido",
                        (a, b) -> a  // mantém a primeira mensagem em caso de campo com múltiplos erros
                ));

        var body = new ValidationErrorResponse(
                BusinessException.ErrorCode.VALIDATION_ERROR.name(),
                "Dados de entrada inválidos",
                fields
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // Fallback para erros não mapeados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        var body = new ErrorResponse(
                BusinessException.ErrorCode.INTERNAL_ERROR.name(),
                "Erro interno do servidor"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ─── Response bodies ─────────────────────────────────────────────────────

    public record ErrorResponse(
            String error,
            String message
    ) {}

    public record ValidationErrorResponse(
            String error,
            String message,
            Map<String, String> fields
    ) {}
}