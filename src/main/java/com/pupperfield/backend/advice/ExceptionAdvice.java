package com.pupperfield.backend.advice;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.pupperfield.backend.model.InvalidRequestResponseDto;

import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {
    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<String> httpMessageConverterExceptionHandler
            (HttpMessageConversionException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            null,
            HttpStatus.BAD_REQUEST.value()
        );
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> authExceptionHandler
            (AuthException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            null,
            HttpStatus.UNAUTHORIZED.value()
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> nohandlerFoundExceptionHandler
            (NoHandlerFoundException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            null,
            HttpStatus.NOT_FOUND.value()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> httpMethodNotSupportedExceptionHandler
            (HttpRequestMethodNotSupportedException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
            null,
            HttpStatus.METHOD_NOT_ALLOWED.value()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<InvalidRequestResponseDto> validationExceptionHandler
            (MethodArgumentNotValidException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            new InvalidRequestResponseDto(
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                exception.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> String.format(
                        "%s %s", error.getField(), error.getDefaultMessage()
                    ))
                    .toList()
            ),
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        log.error(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            null,
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }
}
