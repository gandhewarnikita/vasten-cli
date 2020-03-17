package com.vasten.cli.error.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.vasten.cli.error.ErrorResponse;
import com.vasten.cli.exception.CliBadRequestException;
import com.vasten.cli.exception.CliNotFoundException;

@RestControllerAdvice
@RestController

public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(CliNotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public final ErrorResponse handleNotFoundException(CliNotFoundException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ex.getValidateErrors());
		return errorResponse;
	}

	@ExceptionHandler(CliBadRequestException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ErrorResponse handleBadRequestException(CliBadRequestException ex) {
		ErrorResponse errorResponse = new ErrorResponse(ex.getValidateErrors());
		return errorResponse;
	}
}
