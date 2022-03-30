package dev.nipafx.args;

/**
 * Encodes various parsing errors.
 */
public enum ArgsCode {
	// warnings
	UNKNOWN_ARGUMENT,

	// argument errors
	MISSING_ARGUMENT, UNSUPPORTED_ARGUMENT_TYPE, MISSING_VALUE,  UNPARSEABLE_VALUE, UNEXPECTED_VALUE,

	// type errors
	ILLEGAL_ACCESS, REFLECTION_EXCEPTION,
}
