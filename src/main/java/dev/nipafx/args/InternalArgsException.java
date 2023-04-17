package dev.nipafx.args;

import java.util.Collection;
import java.util.List;

import static dev.nipafx.args.Check.internalErrorOnNull;

class InternalArgsException extends RuntimeException {

	private final List<ArgsMessage> errors;

	InternalArgsException(ArgsMessage error) {
		this(List.of(internalErrorOnNull(error)));
	}

	InternalArgsException(Collection<ArgsMessage> errors) {
		this.errors = List.copyOf(internalErrorOnNull(errors));
	}

	InternalArgsException(ArgsMessage error, Throwable cause) {
		this(List.of(internalErrorOnNull(error)), internalErrorOnNull(cause));
	}

	InternalArgsException(Collection<ArgsMessage> errors, Throwable cause) {
		super(internalErrorOnNull(cause));
		this.errors = List.copyOf(errors);
	}

	public List<ArgsMessage> errors() {
		return errors;
	}

}
