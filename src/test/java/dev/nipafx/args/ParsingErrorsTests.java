package dev.nipafx.args;

import dev.nipafx.args.Records.None;
import dev.nipafx.args.Records.WithBoolean;
import dev.nipafx.args.Records.WithConstructorException;
import dev.nipafx.args.Records.WithInteger;
import dev.nipafx.args.Records.WithList;
import dev.nipafx.args.Records.WithMap;
import dev.nipafx.args.Records.WithString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.args.ArgsParseErrorCode.CONSTRUCTOR_EXCEPTION;
import static dev.nipafx.args.ArgsParseErrorCode.MISSING_ARGUMENT;
import static dev.nipafx.args.ArgsParseErrorCode.MISSING_VALUE;
import static dev.nipafx.args.ArgsParseErrorCode.UNEXPECTED_VALUE;
import static dev.nipafx.args.ArgsParseErrorCode.UNKNOWN_ARGUMENT;
import static dev.nipafx.args.ArgsParseErrorCode.UNPARSEABLE_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
class ParsingErrorsTests {

	@Test
	void withoutArgs_expectedProgramArgs_missingArgumentError() {
		String[] args = { };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_ARGUMENT);
	}

	@Test
	void withoutArgs_programWithBooleanArg_missingArgumentError() {
		String[] args = { };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithBoolean.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_ARGUMENT);
	}

	@Test
	void mentionsListArgWithoutValue_missingValueError() {
		String[] args = { "--stringArgs" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithList.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_VALUE);
	}

	@Test
	void mentionsMapArgWithoutValue_missingValueError() {
		String[] args = { "--mapArgs" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithMap.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_VALUE);
	}

	@Test
	void mapArgWithIncompleteValue_missingValueError() {
		String[] args = { "--mapArgs", "one" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithMap.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNPARSEABLE_VALUE);
	}

	@Test
	void expectedArgWithoutValue_missingArgumentError() {
		String[] args = { "--stringArg" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_VALUE);
	}

	@Test
	void unexpectedArgWithoutValue_unknownArgumentError() {
		String[] args = { "--unexpected" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, None.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNKNOWN_ARGUMENT);
	}

	@Test
	void unexpectedArgWithValue_unknownArgumentError() {
		String[] args = { "--unexpected", "should_be_ignored" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, None.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNKNOWN_ARGUMENT);
	}

	@Test
	void expectedArgWithMultipleValues_unexpectedValueError() {
		String[] args = { "--stringArg", "one", "two" };
		var exception = assertThrows(
				ArgsParseException.class, () -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNEXPECTED_VALUE);
	}

	@Test
	void expectedArgWithValueOfWrongType_unparseableError() {
		String[] args = { "--intArg", "five" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithInteger.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNPARSEABLE_VALUE);
	}

	@Test
	void constructorThrowsException_constructorError() {
		String[] args = { };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithConstructorException.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(CONSTRUCTOR_EXCEPTION);
	}

}
