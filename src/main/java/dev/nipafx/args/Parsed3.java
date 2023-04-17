package dev.nipafx.args;

import static dev.nipafx.args.Check.internalErrorOnNull;

/**
 * Result of parsing arguments to three args records with {@link Args#parse(String[], Class, Class, Class)}.
 *
 * @param first instance of the first args type passed to {@code Args::parse}
 * @param second instance of the second args type passed to {@code Args::parse}
 * @param third instance of the third args type passed to {@code Args::parse}
 * @param <ARGS_TYPE_1> first args type passed to {@code Args::parse}
 * @param <ARGS_TYPE_2> second args type passed to {@code Args::parse}
 * @param <ARGS_TYPE_3> third args type passed to {@code Args::parse}
 */
public record Parsed3<ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3>(
		ARGS_TYPE_1 first, ARGS_TYPE_2 second, ARGS_TYPE_3 third) {

	public Parsed3 {
		internalErrorOnNull(first);
		internalErrorOnNull(second);
		internalErrorOnNull(third);
	}

}
