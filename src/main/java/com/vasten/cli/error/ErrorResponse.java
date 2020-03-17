package com.vasten.cli.error;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class ErrorResponse {

	@JsonInclude(Include.NON_NULL)
	private List<ValidationError> errors; // To throw multiple error

	public ErrorResponse(List<ValidationError> errors) {
		this.errors = errors;
	}

	public List<ValidationError> getErrors() {
		return errors;
	}

	public void setErrors(List<ValidationError> errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		StringBuilder errorString = new StringBuilder();

		if (null != this.errors) {
			errorString.append(", errors : { " + this.errors.toString());
		}
		errorString.append(" }");
		return errorString.toString();
	}
}
