package dev.nipafx.args;

import java.util.Optional;
import java.util.Set;

import static dev.nipafx.args.Check.internalErrorOnNull;
import static dev.nipafx.args.Check.internalErrorOnNullOrBlank;
import static dev.nipafx.args.Check.internalErrorOnNullOrEmpty;
import static java.util.stream.Collectors.joining;

/**
 * A warning or error resulting from faulty command line arguments.
 */
public sealed interface ArgsMessage {

	/**
	 * Returns a textual description of the warning/error that can be shown to the user.
	 *
	 * @return a textual description of the warning/error
	 */
	String toMessage();

	/**
	 * If the warning/error was caused by an exception, it is returned.
	 *
	 * @return the cause of the warning/error
	 */
	default Optional<Throwable> cause() {
		return Optional.empty();
	}

	/**
	 * A warning resulting from faulty command line arguments.
	 */
	sealed interface ArgsWarningMessage extends ArgsMessage { }

	/**
	 * Indicates that an argument couldn't be mapped to a record component.
	 *
	 * @param argumentName the name of the argument (which doesn't include "--")
	 */
	record UnknownArgument(String argumentName) implements ArgsWarningMessage {

		/**
		 * Creates a message indicating that an argument couldn't be mapped to a record component.
		 *
		 * @param argumentName the name of the argument (which doesn't include "--")
		 */
		public UnknownArgument {
			internalErrorOnNullOrBlank(argumentName);
		}

		@Override
		public String toMessage() {
			return "The provided argument '--%s' is unknown.".formatted(argumentName);
		}

	}

	/**
	 * An error resulting from faulty command line arguments.
	 */
	sealed interface ArgsErrorMessage extends ArgsMessage { }

	/**
	 * Indicates that a required (i.e. non-container) argument is missing from the argument array.
	 *
	 * @param argumentName the name of the argument (which doesn't include "--")
	 */
	record MissingArgument(String argumentName) implements ArgsErrorMessage {

		/**
		 * Creates a message indicating that a required (i.e. non-container) argument is missing from the argument array.
		 *
		 * @param argumentName the name of the argument (which doesn't include "--")
		 */
		public MissingArgument {
			internalErrorOnNullOrBlank(argumentName);
		}

		@Override
		public String toMessage() {
			return "The required argument '--%s' was not provided.".formatted(argumentName);
		}

	}

	/**
	 * Indicates that a required (i.e. non-container) argument was provided but no value was specified.
	 *
	 * @param argumentName the name of the argument (which doesn't include "--")
	 */
	record MissingValue(String argumentName) implements ArgsErrorMessage {

		/**
		 * Creates a message indicating that a required (i.e. non-container) argument was provided but no value was specified.
		 *
		 * @param argumentName the name of the argument (which doesn't include "--")
		 */
		public MissingValue {
			internalErrorOnNullOrBlank(argumentName);
		}

		@Override
		public String toMessage() {
			return "No value was provided for the argument '--%s'.".formatted(argumentName);
		}

	}

	/**
	 * Indicates that the argument array was empty and no action was selected
	 *
	 * @param allowedValues the allowed values
	 */
	record MissingAction(Set<String> allowedValues) implements ArgsErrorMessage {

		/**
		 * Creates a message indicating that the argument array was empty and no action was selected
		 *
		 * @param allowedValues the allowed values
		 */
		public MissingAction {
			internalErrorOnNullOrEmpty(allowedValues);
			allowedValues = Set.copyOf(allowedValues);
		}

		@Override
		public String toMessage() {
			var allowedNames = allowedValues.stream()
					.sorted()
					.collect(joining("', '", "'", "'"));
			return """
					No arguments were provided but at least one is required. \
					The first argument must be one of [ %s ]."""
					.formatted(allowedNames);
		}

	}

	/**
	 * Indicates that the value provided for the argument could not be parsed.
	 *
	 * @param argumentName the argument for which the error occurred (which doesn't include "--")
	 * @param argumentType the type of the argument
	 * @param value the value provided for the argument
	 * @param parseError the exception indicates the parse error
	 */
	record IllegalValue(String argumentName, Class<?> argumentType, String value, Throwable parseError) implements ArgsErrorMessage {

		/**
		 * Creates a message indicating that the value provided for the argument could not be parsed.
		 *
		 * @param argumentName the argument for which the error occurred (which doesn't include "--")
		 * @param argumentType the type of the argument
		 * @param value the value provided for the argument
		 * @param parseError the exception indicates the parse error
		 */
		public IllegalValue {
			internalErrorOnNullOrBlank(argumentName);
			internalErrorOnNull(argumentType);
			internalErrorOnNullOrBlank(value);
			internalErrorOnNull(parseError);
		}

		@Override
		public String toMessage() {
			return "The value '%s' for argument '--%s' could not be parsed to type '%s': \"%s\" (%s)"
					.formatted(value, argumentName, argumentType.getSimpleName(), parseError.getMessage(), parseError.getClass().getSimpleName());
		}

	}

	/**
	 * Indicates that, at that position in the argument array, an argument was expected but instead a value was encountered.
	 *
	 * @param value the encountered value
	 */
	record UnexpectedValue(String value) implements ArgsErrorMessage {

		/**
		 * Creates a message indicating that, at that position in the argument array, an argument was expected but instead a value was encountered.
		 *
		 * @param value the encountered value
		 */
		public UnexpectedValue {
			internalErrorOnNullOrBlank(value);
		}

		@Override
		public String toMessage() {
			return "At the position of '%s' an argument was expected instead of a value.".formatted(value);
		}

	}

	/**
	 * Indicates that the value provided for the mode was unknown.
	 *
	 * @param modeName the name of the mode (which doesn't include "--")
	 * @param allowedValues the allowed values
	 * @param actualValue the actual value provided for the mode
	 */
	record IllegalModeValue(String modeName, Set<String> allowedValues, String actualValue) implements ArgsErrorMessage {

		/**
		 * Creates a message indicating that the value provided for the mode was unknown.
		 *
		 * @param modeName the name of the mode (which doesn't include "--")
		 * @param allowedValues the allowed values
		 * @param actualValue the actual value provided for the mode
		 */
		public IllegalModeValue {
			internalErrorOnNullOrBlank(modeName);
			internalErrorOnNullOrEmpty(allowedValues);
			allowedValues = Set.copyOf(allowedValues);
			internalErrorOnNullOrBlank(actualValue);
		}

		@Override
		public String toMessage() {
			var allowedNames = allowedValues.stream()
					.sorted()
					.collect(joining("', '", "'", "'"));
			return "The value '%s' for argument '--%s' did not match any of the allowed values: [ %s ]"
					.formatted(actualValue, modeName, allowedNames);
		}

	}

	/**
	 * Indicates that the first argument was not a known value for the action.
	 *
	 * @param allowedValues the allowed values
	 * @param actualValue the first encountered value in the argument array
	 */
	record UnknownAction(Set<String> allowedValues, String actualValue) implements ArgsErrorMessage {

		/**
		 * Creates a message indicating that the first argument was not a known value for the action.
		 *
		 * @param allowedValues the allowed values
		 * @param actualValue the first encountered value in the argument array
		 */
		public UnknownAction {
			internalErrorOnNullOrEmpty(allowedValues);
			allowedValues = Set.copyOf(allowedValues);
			internalErrorOnNullOrBlank(actualValue);
		}

		@Override
		public String toMessage() {
			var allowedNames = allowedValues.stream()
					.sorted()
					.collect(joining("', '", "'", "'"));
			return "The first provided value '%s' did not match any of the allowed values: [ %s ]."
					.formatted(actualValue, allowedNames);
		}

	}

	/**
	 * Indicates that an args record constructor threw an exception.
	 *
	 * @param exception the exception thrown by the args record constructor
	 */
	record FailedConstruction(Throwable exception) implements ArgsErrorMessage {

		/**
		 * Creates a message indicating that an args record constructor threw an exception.
		 *
		 * @param exception the exception thrown by the args record constructor
		 */
		public FailedConstruction {
			internalErrorOnNull(exception);
		}

		@Override
		public String toMessage() {
			return "Argument validation failed with an error: \"%s\" (%s)"
					.formatted(exception.getMessage(), exception.getClass().getSimpleName());
		}

		@Override
		public Optional<Throwable> cause() {
			return Optional.of(exception);
		}

	}

}
