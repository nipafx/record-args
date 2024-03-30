package dev.nipafx.args;

/**
 * Encodes various definition errors that the developer(s) using RecordArgs must fix.
 */
public enum ArgsDefinitionErrorCode {

	/** An args types was neither a record nor a sealed interfaces with exclusively record implementations. */
	ILL_DEFINED_ARGS_TYPE,

	/** An args record had an unsupported component type. */
	UNSUPPORTED_ARGUMENT_TYPE,

	/**
	 * {@link dev.nipafx.args.Args#parse(java.lang.String[], java.lang.Class, java.lang.Class) Args::parse}
	 * was called with at least two records that have components of the same name.
	 */
	DUPLICATE_ARGUMENT_DEFINITION,

	/** RecordArgs has no reflective access to all args types. */
	ILLEGAL_ACCESS,

	/** An args record's static initializer threw an error. */
	FAULTY_STATIC_INITIALIZER,

	/** At most one sealed interface may be called {@code Action} or {@code ActionArgs}. */
	MULTIPLE_ACTIONS

}
