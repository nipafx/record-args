package dev.nipafx.args;

/**
 * Encodes various parsing errors.
 */
public enum ArgsParseErrorCode {
	// warnings
	UNKNOWN_ARGUMENT,

	// argument errors
	FAULTY_ACTION, MISSING_ARGUMENT, MISSING_VALUE, UNPARSEABLE_VALUE, UNEXPECTED_VALUE, CONSTRUCTOR_EXCEPTION
}
