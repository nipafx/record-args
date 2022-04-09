package dev.nipafx.args;

/**
 * Result of parsing arguments to three args records with {@link Args#parse(String[], Class, Class, Class)}.
 *
 * @param first instance of the first args record type passed to {@code Args::parse}
 * @param second instance of the second args record type passed to {@code Args::parse}
 * @param third instance of the third args record type passed to {@code Args::parse}
 * @param <ARGS_RECORD_1> first args record type passed to {@code Args::parse}
 * @param <ARGS_RECORD_2> second args record type passed to {@code Args::parse}
 * @param <ARGS_RECORD_3> third args record type passed to {@code Args::parse}
 */
public record Parsed3<
		ARGS_RECORD_1 extends Record,
		ARGS_RECORD_2 extends Record,
		ARGS_RECORD_3 extends Record>(
		ARGS_RECORD_1 first, ARGS_RECORD_2 second, ARGS_RECORD_3 third) { }
