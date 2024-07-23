package dev.nipafx.args;

import dev.nipafx.args.Records.WithBoolean;
import dev.nipafx.args.Records.WithInteger;
import dev.nipafx.args.Records.WithString;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LenientParsingTests {

	@Test
	void parseOneRecord_unknownArguments_exception() {
		String[] args = { "--stringArg", "string", "--unknown", "unknownValue" };
		var exception = assertThrows(
				ArgsParseException.class,
				() -> Args.parse(args, WithString.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownArgument("unknown"));
	}

	@Test
	void parseOneRecordLeniently_unknownArguments_parses() throws ArgsParseException {
		String[] args = { "--stringArg", "string", "--unknown", "unknownValue" };
		var parsed = Args.parseLeniently(args, WithString.class);

		assertThat(parsed.stringArg()).isEqualTo("string");
	}

	@Test
	void parseTwoRecords_unknownArguments_exception() {
		String[] args = { "--stringArg", "string", "--intArg", "42", "--unknown", "unknownValue" };
		var exception = assertThrows(
				ArgsParseException.class,
				() -> Args.parse(args, WithString.class, WithInteger.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownArgument("unknown"));
	}

	@Test
	void parseTwoRecordsLeniently_unknownArguments_parses() throws ArgsParseException {
		String[] args = { "--stringArg", "string", "--intArg", "42", "--unknown", "unknownValue" };
		var parsed = Args.parseLeniently(args, WithString.class, WithInteger.class);

		assertThat(parsed.first().stringArg()).isEqualTo("string");
		assertThat(parsed.second().intArg()).isEqualTo(42);
	}

	@Test
	void parseThreeRecords_unknownArguments_exception() {
		String[] args = { "--stringArg", "string", "--intArg", "42", "--booleanArg", "true", "--unknown", "unknownValue" };
		var exception = assertThrows(
				ArgsParseException.class,
				() -> Args.parse(args, WithString.class, WithInteger.class, WithBoolean.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownArgument("unknown"));
	}

	@Test
	void parseThreeRecordsLeniently_unknownArguments_parses() throws ArgsParseException {
		String[] args = { "--stringArg", "string", "--intArg", "42", "--booleanArg", "true", "--unknown", "unknownValue" };
		var parsed = Args.parseLeniently(args, WithString.class, WithInteger.class, WithBoolean.class);

		assertThat(parsed.first().stringArg()).isEqualTo("string");
		assertThat(parsed.second().intArg()).isEqualTo(42);
		assertThat(parsed.third().booleanArg()).isTrue();
	}

}
