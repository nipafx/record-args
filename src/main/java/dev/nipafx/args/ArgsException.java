package dev.nipafx.args;

import java.util.Collection;
import java.util.List;

import static dev.nipafx.args.Check.nonNull;
import static java.util.stream.Collectors.joining;

/**
 * Thrown when parsing command line arguments fails.
 */
public class ArgsException extends Exception {

	private final String[] args;
	private final Class<?> type;
	private final List<ArgsMessage> errors;

	ArgsException(String[] args, Class<?> type, Collection<ArgsMessage> errors) {
		super(combineErrors(errors));
		this.args = nonNull(args);
		this.type = nonNull(type);
		this.errors = List.copyOf(errors);
	}

	ArgsException(String[] args, Class<?> type, Collection<ArgsMessage> errors, Throwable cause) {
		super(combineErrors(errors), cause);
		this.args = nonNull(args);
		this.type = nonNull(type);
		this.errors = List.copyOf(errors);
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
	 * @return the type of the args record that was supposed to be created
	 */
	public Class<?> type() {
		return type;
	}

	/**
	 * @return unmodifiable list of {@link ArgsMessage}s
	 */
	public List<ArgsMessage> errors() {
		return errors;
	}

}
