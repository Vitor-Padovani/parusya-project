package com.parusya.infra.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }

    // ─── Códigos de erro mapeados ao contrato de API ──────────────────────────

    public enum ErrorCode {

        // 400
        INVALID_QR_CODE(HttpStatus.BAD_REQUEST, "QR Code não corresponde a nenhum Participant cadastrado"),
        VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos"),

        // 401
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Token não fornecido ou expirado"),
        INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos"),

        // 403
        FORBIDDEN(HttpStatus.FORBIDDEN, "Sem permissão para acessar este recurso"),
        WRONG_ROLE(HttpStatus.FORBIDDEN, "Utilize o endpoint correto para seu perfil"),

        // 404
        EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Evento não encontrado ou não pertence ao seu Grupo"),
        RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Recurso não encontrado"),

        // 409
        DUPLICATE_CHECKIN(HttpStatus.CONFLICT, "Participant já realizou check-in neste evento"),
        DUPLICATE_EMAIL(HttpStatus.CONFLICT, "E-mail já em uso"),
        DUPLICATE_PHONE(HttpStatus.CONFLICT, "Telefone já em uso"),
        ORGANIZER_ALREADY_IN_GROUP(HttpStatus.CONFLICT, "Este e-mail já está associado a um Grupo"),

        // 422
        EVENT_INACTIVE(HttpStatus.UNPROCESSABLE_ENTITY, "Não é possível realizar check-in em evento inativo"),

        // 500
        INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");

        private final HttpStatus status;
        private final String message;

        ErrorCode(HttpStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public HttpStatus getStatus() { return status; }
        public String getMessage()    { return message; }
    }
}