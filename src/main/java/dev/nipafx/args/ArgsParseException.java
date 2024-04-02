package dev.nipafx.args;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static dev.nipafx.args.Check.internalErrorOnNull;
import static java.util.stream.Collectors.joining;

/**
 * Thrown when parsing command line arguments fails.
 */
public class ArgsParseException extends Exception {

	@SuppressWarnings("doclint:missing") private final String[] args;
	@SuppressWarnings("doclint:missing") private final List<? extends Class<?>> types;
	@SuppressWarnings("doclint:missing") private final List<ArgsMessage> errors;

	ArgsParseException(String[] args, Collection<? extends Class<?>> types, InternalArgsException cause) {
		super(combineErrors(cause.errors()), cause.getCause());
		this.args = internalErrorOnNull(args);
		this.types = List.copyOf(types);
		this.errors = List.copyOf(cause.errors());
	}

	private static String combineErrors(Collection<ArgsMessage> errors) {
		return errors.stream()
				.map(ArgsMessage::toMessage)
				.collect(joining(" "));
	}

	/**
	 * Returns the argument array passed to {@link Args}.
	 *
	 * @return the argument array passed to {@link Args}
	 */
	public String[] args() {
		return args;
	}

	/**
	 * Returns the args types that were supposed to be created.
	 *
	 * @return the args types that were supposed to be created
	 */
	public List<? extends Class<?>> types() {
		return types;
	}

	/**
	 * Returns {@link ArgsMessage}s that describe the errors.
	 *
	 * @return {@link ArgsMessage}s that describe the errors
	 */
	public Stream<ArgsMessage> errors() {
		return errors.stream();
	}

}
