package com.gp.radioregistry.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EntityNotFoundException.class)
	public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
		log.warn("Entity not found: {}", ex.getMessage());

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problemDetail.setTitle("Resource not found");
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}

	@ExceptionHandler(ResourceAlreadyExistsException.class)
	public ProblemDetail handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
		log.warn("Resource already exists: {}", ex.getMessage());

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problemDetail.setTitle("Resource already exists");
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		log.warn("Validation failed: {}", ex.getMessage());

		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			String message = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value";
			errors.merge(
				fieldError.getField(),
				message,
				(existing, incoming) -> existing + "; " + incoming);
		}
		ex.getBindingResult().getGlobalErrors().forEach(error ->
			errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
			HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
		problemDetail.setTitle("Validation error");
		problemDetail.setProperty("timestamp", Instant.now());
		problemDetail.setProperty("errors", errors);
		return problemDetail;
	}
}
