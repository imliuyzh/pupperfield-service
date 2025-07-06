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

/**
 * A global exception handler that handles and logs various exceptions thrown by the application,
 * returning HTTP status codes and error messages in the process.
 */
@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {
    /**
     * Handles cases when the request body cannot be processed.
     *
     * @param exception the exception thrown
     * @return a HTTP 400 response
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<String> failedHttpMessageConversionHandler
        (HttpMessageConversionException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles cases when the request link does not exist.
     *
     * @param exception the exception thrown
     * @return a HTTP 404 response
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<String> notFoundHandler
        (ServletException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles cases when an unsupported HTTP method is used.
     *
     * @param exception the exception thrown
     * @return a HTTP 405 response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> httpMethodNotSupportedHandler
        (HttpRequestMethodNotSupportedException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles cases when the request media type is not supported.
     *
     * @param exception the exception thrown
     * @return a HTTP 415 response
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> invalidMediaTypeHandler
        (HttpMediaTypeNotSupportedException exception) {
        log.info(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(),
            HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handles some cases when request validation failed.
     *
     * @param exception the exception thrown
     * @return a HTTP 422 response
     */
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

    /**
     * Handles some cases when request validation failed.
     *
     * @param exception the exception thrown
     * @return a HTTP 422 response
     */
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

    /**
     * Handles some cases when request validation failed.
     *
     * @param exception the exception thrown
     * @return a HTTP 422 response
     */
    @ExceptionHandler({HandlerMethodValidationException.class, MethodValidationException.class})
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

    /**
     * Handles some cases when request validation failed.
     *
     * @param exception the exception thrown
     * @return a HTTP 422 response
     */
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

    /**
     * A fallback handler for other types of exception.
     *
     * @param exception the exception thrown
     * @return a HTTP 500 response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> baseExceptionHandler(Exception exception) {
        log.error(ExceptionUtils.getStackTrace(exception));
        return new ResponseEntity<>(
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
