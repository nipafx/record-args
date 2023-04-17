package dev.nipafx.args;

import java.util.Optional;

import static dev.nipafx.args.Check.internalErrorOnNull;

/**
 * A warning or error that occurred while parsing command line arguments.
 *
 * @param code the warning/error code for this message
 * @param message a textual description of the warning/error
 * @param cause the {@link Exception} that caused the error (if available)
 */
public record ArgsMessage(ArgsParseErrorCode code, String message, Optional<Exception> cause) {

	public ArgsMessage {
		internalErrorOnNull(code);
		internalErrorOnNull(message);
		internalErrorOnNull(cause);
	}

	ArgsMessage(ArgsParseErrorCode code, String message) {
		this(code, message, Optional.empty());
	}

	ArgsMessage(ArgsParseErrorCode code, String message, Exception cause) {
		this(code, message, Optional.of(cause));
	}

	@Override
	public String toString() {
		var string = "%s: %s".formatted(code, message);
		if (cause.isPresent())
			string += " (%s)".formatted(cause.get().getMessage());
		return string;
	}

}
