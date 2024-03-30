package dev.nipafx.args;

/**
 * Thrown when the types provided to {@link Args#parse(String[], Class) Args::parse}
 * are ill-defined.
 */
public class ArgsDefinitionException extends RuntimeException {

	@SuppressWarnings("doclint:missing")
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
	 * Returns an error code that describes the cause of the exception.
	 *
	 * @return describes the error that caused the exception
	 */
	public ArgsDefinitionErrorCode errorCode() {
		return errorCode;
	}

}
