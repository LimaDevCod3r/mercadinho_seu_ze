package com.diego.lima.dev.startup.Exceptions;

import com.diego.lima.dev.startup.Exceptions.Category.ConflictCategoryException;
import com.diego.lima.dev.startup.Exceptions.Category.NotFoundCategoryException;
import com.diego.lima.dev.startup.Exceptions.Product.ConflictProductException;
import com.diego.lima.dev.startup.Exceptions.Response.ErrorApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ConflictProductException.class)
    public ResponseEntity<ErrorApiResponse> handlerConflitProductException(ConflictProductException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(

                new ErrorApiResponse(
                        ex.getMessage(),
                        409,
                        request.getRequestURI(),
                        LocalDateTime.now()
                )
        );
    }


    // Category Custom Exception
    @ExceptionHandler(NotFoundCategoryException.class)
    public ResponseEntity<ErrorApiResponse> handlerNotFoundCategoryException(NotFoundCategoryException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(

                new ErrorApiResponse(
                        ex.getMessage(),
                        404,
                        request.getRequestURI(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(ConflictCategoryException.class)
    public ResponseEntity<ErrorApiResponse> handlerConflitCategoryException(ConflictCategoryException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(

                new ErrorApiResponse(
                        ex.getMessage(),
                        409,
                        request.getRequestURI(),
                        LocalDateTime.now()
                )
        );

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonErrors(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        Map<String, String> fieldErrors = new HashMap<>();

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidEx) {

            invalidEx.getPath().forEach(reference -> {
                String fieldName = reference.getPropertyName();
                fieldErrors.put(fieldName, "Valor inválido para este campo");
            });
        } else {
            fieldErrors.put("body", "JSON inválido");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Erro de Conversão de Tipo");
        body.put("path", request.getRequestURI());
        body.put("fields", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format(
                "O parâmetro '%s' deve ser do tipo %s",
                ex.getName(),
                ex.getRequiredType().getSimpleName()
        );
        return ResponseEntity.badRequest().body(
                new ErrorApiResponse(
                        message,
                        400,
                        request.getRequestURI(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> fieldsErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldsErrors.put(error.getField(), error.getDefaultMessage());
        });

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Erro de Validação");
        body.put("path", request.getRequestURI());
        body.put("fields", fieldsErrors);

        return ResponseEntity.badRequest().body(body);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorApiResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorApiResponse(
                        "ERRO INTERNO DO SERVIDOR",
                        500,
                        request.getRequestURI(),
                        LocalDateTime.now()
                )
        );
    }
}
