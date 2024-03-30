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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
class ParsingErrorsTests {

	@Test
	void withoutArgs_expectedProgramArgs_missingArgumentError() {
		String[] args = { };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.MissingArgument("stringArg"));
	}

	@Test
	void withoutArgs_programWithBooleanArg_missingArgumentError() {
		String[] args = { };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithBoolean.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.MissingArgument("booleanArg"));
	}

	@Test
	void mentionsListArgWithoutValue_missingValueError() {
		String[] args = { "--stringArgs" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithList.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.MissingValue("stringArgs"));
	}

	@Test
	void mentionsMapArgWithoutValue_missingValueError() {
		String[] args = { "--mapArgs" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithMap.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.MissingValue("mapArgs"));
	}

	@Test
	void mapArgWithIncompleteValue_illegalValueError() {
		String[] args = { "--mapArgs", "one" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithMap.class));
		assertThat(exception.errors())
				.hasSize(1)
				.allMatch(msg -> msg instanceof ArgsMessage.IllegalValue(var argName, var argType, var value, var __)
								 && argName.equals("mapArgs") && argType == Map.class && value.equals("one"));
	}

	@Test
	void expectedArgWithoutValue_missingArgumentError() {
		String[] args = { "--stringArg" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.MissingValue("stringArg"));
	}

	@Test
	void unexpectedArgWithoutValue_unknownArgumentError() {
		String[] args = { "--unexpected" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, None.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownArgument("unexpected"));
	}

	@Test
	void unexpectedArgWithValue_unknownArgumentError() {
		String[] args = { "--unexpected", "should_be_ignored" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, None.class));
		// this asserts the presence of the message about the argument _and_ the absence of a message about its value
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownArgument("unexpected"));
	}

	@Test
	void expectedArgWithMultipleValues_unexpectedValueError() {
		String[] args = { "--stringArg", "one", "two" };
		var exception = assertThrows(
				ArgsParseException.class, () -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnexpectedValue("two"));
	}

	@Test
	void expectedArgWithValueOfWrongType_illegalValueError() {
		String[] args = { "--intArg", "five" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithInteger.class));
		assertThat(exception.errors())
				.hasSize(1)
				.allMatch(msg -> msg instanceof ArgsMessage.IllegalValue(var argName, var argType, var value, var __)
								 && argName.equals("intArg") && argType == int.class && value.equals("five"));
	}

	@Test
	void constructorThrowsException_constructorError() {
		String[] args = { };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, WithConstructorException.class));
		assertThat(exception.errors())
				.hasSize(1)
				.allMatch(msg -> msg instanceof ArgsMessage.FailedConstruction(var ex) && ex.getClass() == IllegalArgumentException.class);
	}

}
