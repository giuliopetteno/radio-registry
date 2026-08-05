package com.gp.radioregistry.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

	@ExceptionHandler(InvalidEntityStateException.class)
	public ProblemDetail handleInvalidEntityState(InvalidEntityStateException ex) {
		log.warn("Invalid entity state: {}", ex.getMessage());

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problemDetail.setTitle("Invalid entity state");
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

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		log.warn("Data integrity violation: {}", ex.getMessage());

		String detail = "The request violates a data integrity constraint";
		String rootMessage = ex.getMostSpecificCause().getMessage();

		if (rootMessage != null) {
			if (rootMessage.contains("fk_device_organization")) {
				detail = "The specified organization does not exist";
			} else if (rootMessage.contains("fk_device_department")) {
				detail = "The specified department does not exist";
			} else if (rootMessage.contains("fk_device_type")) {
				detail = "The specified device type does not exist";
			} else if (rootMessage.contains("fk_department_organization")) {
				detail = "The specified organization does not exist";
			} else if (rootMessage.contains("fk_department_parent")) {
				detail = "The specified parent department does not exist";
			} else if (rootMessage.contains("chk_device_parent_structure")) {
				detail = "Either an organization or a department must be specified, not both";
			} else if (rootMessage.contains("chk_device_decommission_date")) {
				detail = "A decommission date is required for DECOMMISSIONED or PENDING_DECOMMISSIONING status, and must be omitted otherwise";
			} else if (rootMessage.contains("chk_department_parent_structure")) {
				detail = "Either an organization or a parent department must be specified, not both";
			}
		}

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, detail);
		problemDetail.setTitle("Invalid reference or constraint violation");
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}
}
