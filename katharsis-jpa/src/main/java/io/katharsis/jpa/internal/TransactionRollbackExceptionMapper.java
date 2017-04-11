package io.katharsis.jpa.internal;

import javax.transaction.RollbackException;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.module.Module;
import io.katharsis.module.Module.ModuleContext;
import io.katharsis.repository.response.HttpStatus;
import io.katharsis.utils.Optional;

/**
 * RollbackException can hide the more interesting causes.
 */
public class TransactionRollbackExceptionMapper implements ExceptionMapper<RollbackException> {

	private ModuleContext context;

	public TransactionRollbackExceptionMapper(Module.ModuleContext context) {
		this.context = context;
	}

	@Override
	public ErrorResponse toErrorResponse(RollbackException exception) {
		Throwable cause = exception.getCause();
		if (cause != null) {
			Optional<JsonApiExceptionMapper> mapper = context.getExceptionMapperRegistry().findMapperFor(cause.getClass());
			if (mapper.isPresent()) {
				return mapper.get().toErrorResponse(cause);
			}
		}
		// no mapper found, return default error
		ErrorData errorData = ErrorData.builder().setStatus(Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR_500)).setTitle(exception.getMessage()).build();
		return ErrorResponse.builder().setSingleErrorData(errorData).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500).build();
	}

	@Override
	public RollbackException fromErrorResponse(ErrorResponse errorResponse) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return false;
	}

}
