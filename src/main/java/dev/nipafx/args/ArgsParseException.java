package dev.nipafx.args;

import java.util.Collection;
import java.util.List;

import static dev.nipafx.args.Check.internalErrorOnNull;
import static java.util.stream.Collectors.joining;

/**
 * Thrown when parsing command line arguments fails.
 */
public class ArgsParseException extends Exception {

	private final String[] args;
	private final List<? extends Class<?>> types;
	private final List<? extends ArgsMessage> errors;

	ArgsParseException(String[] args, Collection<? extends Class<?>> types, InternalArgsException cause) {
		super(combineErrors(cause.errors()), cause.getCause());
		this.args = internalErrorOnNull(args);
		this.types = List.copyOf(types);
		this.errors = List.copyOf(cause.errors());
	}

	private static String combineErrors(Collection<ArgsMessage> errors) {
		return errors.stream()
				.map(ArgsMessage::toString)
				.collect(joining("\n"));
	}

	/**
	 * @return the args array passed to {@link Args}
	 */
	public String[] args() {
		return args;
	}

	/**
	 * @return the args types that were supposed to be created
	 */
	public List<? extends Class<?>> types() {
		return types;
	}

	/**
	 * @return {@link ArgsMessage} that describe the errors
	 */
	public List<? extends ArgsMessage> errors() {
		return errors;
	}

}
