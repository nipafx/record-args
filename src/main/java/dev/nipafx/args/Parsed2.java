package dev.nipafx.args;

import static dev.nipafx.args.Check.internalErrorOnNull;

/**
 * Result of parsing arguments to two args records with {@link Args#parse(String[], Class, Class)}.
 *
 * @param first instance of the first args type passed to {@code Args::parse}
 * @param second instance of the second args type passed to {@code Args::parse}
 * @param <ARGS_TYPE_1> first args type passed to {@code Args::parse}
 * @param <ARGS_TYPE_2> second args type passed to {@code Args::parse}
 */
public record Parsed2<ARGS_TYPE_1, ARGS_TYPE_2>(
		ARGS_TYPE_1 first,
		ARGS_TYPE_2 second) {

	/**
	 * Creates a new parsing result.
	 *
	 * @param first an instance of the first type passed to {@code Args::parse}
	 * @param second an instance of the second type passed to {@code Args::parse}
	 */
	public Parsed2 {
		internalErrorOnNull(first);
		internalErrorOnNull(second);
	}

}
