package dev.nipafx.args;

import java.util.Collection;
import java.util.List;

class InternalArgsException extends RuntimeException {

	private final List<ArgsMessage> errors;

	InternalArgsException(ArgsMessage error) {
		this(List.of(error));
	}

	InternalArgsException(Collection<ArgsMessage> errors) {
		this.errors = List.copyOf(errors);
	}

	InternalArgsException(ArgsMessage error, Throwable cause) {
		this(List.of(error), cause);
	}

	InternalArgsException(Collection<ArgsMessage> errors, Throwable cause) {
		this.errors = List.copyOf(errors);
	}

	public List<ArgsMessage> errors() {
		return errors;
	}

}
