package dev.nipafx.args;

/**
 * Thrown when parsing command line arguments fails.
 */
public class ArgsDefinitionException extends RuntimeException {

	private final ArgsDefinitionErrorCode errorCode;

	ArgsDefinitionException(ArgsDefinitionErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	ArgsDefinitionException(ArgsDefinitionErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	/**
	 * @return describes the error that caused the exception
	 */
	public ArgsDefinitionErrorCode errorCode() {
		return errorCode;
	}

}
