package dev.nipafx.args;

import dev.nipafx.args.Records.None;
import dev.nipafx.args.Records.WithBoolean;
import dev.nipafx.args.Records.WithInteger;
import dev.nipafx.args.Records.WithList;
import dev.nipafx.args.Records.WithMany;
import dev.nipafx.args.Records.WithMap;
import dev.nipafx.args.Records.WithString;
import dev.nipafx.args.Records.WithStringArray;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.args.ArgsCode.MISSING_ARGUMENT;
import static dev.nipafx.args.ArgsCode.MISSING_VALUE;
import static dev.nipafx.args.ArgsCode.UNEXPECTED_VALUE;
import static dev.nipafx.args.ArgsCode.UNKNOWN_ARGUMENT;
import static dev.nipafx.args.ArgsCode.UNPARSEABLE_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
class ParsingErrorsTests {

	@Test
	void withoutArgs_expectedProgramArgs_missingArgumentError() {
		String[] args = { };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_ARGUMENT);
	}

	@Test
	void withoutArgs_programWithBooleanArg_missingArgumentError() {
		String[] args = { };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, WithBoolean.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_ARGUMENT);
	}

	@Test
	void mentionsListArgWithoutValue_missingValueError() {
		String[] args = { "--stringArgs" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, WithList.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_VALUE);
	}

	@Test
	void mentionsMapArgWithoutValue_missingValueError() {
		String[] args = { "--mapArgs" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, WithMap.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_VALUE);
	}

	@Test
	void mapArgWithIncompleteValue_missingValueError() {
		String[] args = { "--mapArgs", "one" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, WithMap.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNPARSEABLE_VALUE);
	}

	@Test
	void expectedArgWithoutValue_missingArgumentError() {
		String[] args = { "--stringArg" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_VALUE);
	}

	@Test
	void unexpectedArgWithoutValue_unknownArgumentError() {
		String[] args = { "--unexpected" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, None.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNKNOWN_ARGUMENT);
	}

	@Test
	void unexpectedArgWithValue_unknownArgumentError() {
		String[] args = { "--unexpected", "should_be_ignored" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, None.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNKNOWN_ARGUMENT);
	}

	@Test
	void expectedArgWithMultipleValues_unexpectedValueError() {
		String[] args = { "--stringArg", "one", "two" };
		var exception = assertThrows(
				ArgsException.class, () -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNEXPECTED_VALUE);
	}

	@Test
	void expectedArgOfUnsupportedType_unparseableError() {
		String[] args = { "--stringsArg", "{ one, two, three }" };
		var exception = assertThrows(
				IllegalArgumentException.class, () -> Args.parse(args, WithStringArray.class));
		assertThat(exception)
				.hasMessageContaining(String[].class.getSimpleName())
				.hasMessageContaining("not supported");
	}

	@Test
	void expectedArgWithValueOfWrongType_unparseableError() {
		String[] args = { "--intArg", "five" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, WithInteger.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNPARSEABLE_VALUE);
	}

	@Test
	void multipleRecords_sameComponents_duplicateArgError() {
		String[] args = { };
		var exception = assertThrows(
				IllegalArgumentException.class, () -> Args.parse(args, WithMany.class, WithString.class));
		assertThat(exception)
				.hasMessageMatching("Duplicate arg.*stringArg.*")
				.hasMessageContaining("WithMany")
				.hasMessageContaining("WithString");
	}

}
