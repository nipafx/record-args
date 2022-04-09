package dev.nipafx.args;

/**
 * Result of parsing arguments to two args records with {@link Args#parse(String[], Class, Class)}.
 *
 * @param first instance of the first args record type passed to {@code Args::parse}
 * @param second instance of the second args record type passed to {@code Args::parse}
 * @param <ARGS_RECORD_1> first args record type passed to {@code Args::parse}
 * @param <ARGS_RECORD_2> second args record type passed to {@code Args::parse}
 */
public record Parsed2<ARGS_RECORD_1 extends Record, ARGS_RECORD_2 extends Record>(
		ARGS_RECORD_1 first,
		ARGS_RECORD_2 second) { }
