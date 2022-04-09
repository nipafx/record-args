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
	private final List<Class<?>> types;
	private final List<ArgsMessage> errors;

	ArgsException(String[] args, Collection<Class<?>> types, Collection<ArgsMessage> errors) {
		super(combineErrors(errors));
		this.args = nonNull(args);
		this.types = List.copyOf(types);
		this.errors = List.copyOf(errors);
	}

	ArgsException(String[] args, List<Class<?>> types, Collection<ArgsMessage> errors, Throwable cause) {
		super(combineErrors(errors), cause);
		this.args = nonNull(args);
		this.types = List.copyOf(types);
		this.errors = List.copyOf(errors);
	}

	ArgsException(String[] args, Class<?> type, Collection<ArgsMessage> errors) {
		this(args, List.of(type), errors);
	}

	ArgsException(String[] args, Class<?> type, Collection<ArgsMessage> errors, Throwable cause) {
		this(args, List.of(type), errors, cause);
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
	 * @return the types of args records that were supposed to be created
	 */
	public List<Class<?>> types() {
		return types;
	}

	/**
	 * @return unmodifiable list of {@link ArgsMessage}s
	 */
	public List<ArgsMessage> errors() {
		return errors;
	}

}
