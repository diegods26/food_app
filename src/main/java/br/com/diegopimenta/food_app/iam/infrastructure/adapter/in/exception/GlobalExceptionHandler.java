package br.com.diegopimenta.food_app.iam.infrastructure.adapter.in.exception;

import br.com.diegopimenta.food_app.iam.domain.exception.ConflictException;
import br.com.diegopimenta.food_app.iam.domain.exception.InvalidAccountStateException;
import br.com.diegopimenta.food_app.iam.domain.exception.InvalidValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidValueException.class)
    ProblemDetail handleInvalidValue(InvalidValueException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(InvalidAccountStateException.class)
    ProblemDetail handleInvalidState(InvalidAccountStateException ex) {
        return problem(HttpStatus.CONFLICT, ex.code(), ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                            HttpHeaders headers,
                                                            HttpStatusCode status,
                                                            WebRequest request) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation_error", "Dados inválidos");
        Map<String, String> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(f -> f.getField(),
                        f -> String.valueOf(f.getDefaultMessage()), (a, b) -> a));
        pd.setProperty("fields", fields);
        return handleExceptionInternal(ex, pd, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Erro inesperado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", "Erro inesperado");
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setProperty("code", code);
        return pd;
    }
}
