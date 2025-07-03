package com.pupperfield.backend.exception;

import com.pupperfield.backend.model.InvalidRequestResponseDto;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {
    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<String> failedHttpMessageConversionHandler
        (HttpMessageConversionException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            null,
            HttpStatus.BAD_REQUEST.value()
        );
    }

    @ExceptionHandler({
        NoHandlerFoundException.class,
        NoResourceFoundException.class
    })
    public ResponseEntity<String> notFoundHandler
        (ServletException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            null,
            HttpStatus.NOT_FOUND.value()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> httpMethodNotSupportedHandler
        (HttpRequestMethodNotSupportedException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
            null,
            HttpStatus.METHOD_NOT_ALLOWED.value()
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> invalidMediaTypeHandler
        (HttpMediaTypeNotSupportedException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(),
            null,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<InvalidRequestResponseDto> failedValidationHandler1
        (MethodArgumentNotValidException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            new InvalidRequestResponseDto(
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                exception.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toSet())
            ),
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<InvalidRequestResponseDto> failedValidationHandler2
        (ConstraintViolationException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            new InvalidRequestResponseDto(
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                List.of(exception.getMessage())
            ),
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler({
        HandlerMethodValidationException.class,
        MethodValidationException.class
    })
    public ResponseEntity<InvalidRequestResponseDto> failedValidationHandler3
        (MethodValidationResult exception) {
        log.info(ExceptionUtils.getStackTrace((RuntimeException) exception));
        return new ResponseEntity<>(
            new InvalidRequestResponseDto(
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                exception.getAllErrors()
                    .stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toSet())
            ),
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<InvalidRequestResponseDto> failedValidationHandler4
        (MethodArgumentTypeMismatchException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        var targetType = exception.getRequiredType();
        return new ResponseEntity<>(
            new InvalidRequestResponseDto(
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                List.of(String.format(
                    "%s should be %s",
                    exception.getPropertyName(),
                    targetType != null ? targetType.getSimpleName() : "null"
                ))
            ),
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> baseExceptionHandler(Exception exception) {
        log.error(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            null,
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }
}
