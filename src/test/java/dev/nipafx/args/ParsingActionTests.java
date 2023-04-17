package dev.nipafx.args;

import dev.nipafx.args.Records.Action;
import dev.nipafx.args.Records.Mode;
import dev.nipafx.args.Records.WithInteger;
import dev.nipafx.args.Records.WithPath;
import dev.nipafx.args.Records.WithString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static dev.nipafx.args.ArgsCode.FAULTY_ACTION;
import static dev.nipafx.args.ArgsCode.UNKNOWN_ARGUMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
class ParsingActionTests {

	@Test
	void justAction_missingSelection_faultyActionError() {
		String[] args = { "--intArg", "42" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, Action.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(FAULTY_ACTION);
	}

	@Test
	void justAction_incorrectSelection_faultyActionError() {
		String[] args = { "withInt", "--intArg", "42" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, Action.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(FAULTY_ACTION);
	}

	@Test
	void justAction_correctSelectionButValuesForWrongSubtype_unknownArgumentError() {
		String[] args = { "withInteger", "--optionalArg", "string" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, Action.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNKNOWN_ARGUMENT);
	}

	@Test
	void justAction_correctSelectionButActionOutOfOrder_faultyActionError() throws ArgsException {
		String[] args = { "--intArg", "42", "withString" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, Action.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(FAULTY_ACTION);
	}

	@Test
	void justAction_correctSelectionAndValuesInOrder_parses() throws ArgsException {
		String[] args = { "withInteger", "--intArg", "42" };
		var parsed = Args.parse(args, Action.class);

		assertThat(parsed).isInstanceOf(WithInteger.class);
		var withString = (WithInteger) parsed;
		assertThat(withString.intArg()).isEqualTo(42);
	}

	@Test
	void actionPlusOneRecord_correctSelectionAndValuesInOrder_parses() throws ArgsException {
		String[] args = { "withInteger", "--intArg", "42", "--stringArg", "string" };
		var parsed = Args.parse(args, Action.class, WithString.class);

		assertThat(parsed.first()).isInstanceOf(WithInteger.class);
		var withString = (WithInteger) parsed.first();
		assertThat(withString.intArg()).isEqualTo(42);

		assertThat(parsed.second().stringArg()).isEqualTo("string");
	}

	@Test
	void actionPlusOneRecord_correctSelectionAndValuesOutOfOrder_parses() throws ArgsException {
		String[] args = { "withInteger", "--intArg", "42", "--stringArg", "string" };
		var parsed = Args.parse(args, Action.class, WithString.class);

		assertThat(parsed.first()).isInstanceOf(WithInteger.class);
		var withString = (WithInteger) parsed.first();
		assertThat(withString.intArg()).isEqualTo(42);

		assertThat(parsed.second().stringArg()).isEqualTo("string");
	}

	@Test
	void actionPlusMode_correctSelectionAndValuesInOrder_parses() throws ArgsException {
		String[] args = { "withInteger", "--intArg", "42", "--mode", "withString", "--stringArg", "string" };
		var parsed = Args.parse(args, Action.class, Mode.class);

		assertThat(parsed.first()).isInstanceOf(WithInteger.class);
		var withInteger = (WithInteger) parsed.first();
		assertThat(withInteger.intArg()).isEqualTo(42);

		assertThat(parsed.second()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.second();
		assertThat(withString.stringArg()).isEqualTo("string");
	}

	@Test
	void actionPlusMode_correctSelectionAndValuesOutOfOrder_parses() throws ArgsException {
		String[] args = { "withInteger", "--mode", "withString", "--intArg", "42", "--stringArg", "string" };
		var parsed = Args.parse(args, Action.class, Mode.class);

		assertThat(parsed.first()).isInstanceOf(WithInteger.class);
		var withInteger = (WithInteger) parsed.first();
		assertThat(withInteger.intArg()).isEqualTo(42);

		assertThat(parsed.second()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.second();
		assertThat(withString.stringArg()).isEqualTo("string");
	}

	@Test
	void actionPlusModePlusRecord_correctSelectionAndValuesInOrder_parses() throws ArgsException {
		String[] args = { "withInteger", "--intArg", "42", "--mode", "withString", "--stringArg", "string", "--pathArg", "/tmp" };
		var parsed = Args.parse(args, Action.class, Mode.class, WithPath.class);

		assertThat(parsed.first()).isInstanceOf(WithInteger.class);
		var withInteger = (WithInteger) parsed.first();
		assertThat(withInteger.intArg()).isEqualTo(42);

		assertThat(parsed.second()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.second();
		assertThat(withString.stringArg()).isEqualTo("string");

		assertThat(parsed.third()).isInstanceOf(WithPath.class);
		var withPath = (WithPath) parsed.third();
		assertThat(withPath.pathArg()).isEqualTo(Path.of("/tmp"));
	}

	@Test
	void actionPlusModePlusRecord_correctSelectionAndValuesOutOfOrder_parses() throws ArgsException {
		String[] args = { "withInteger", "--pathArg", "/tmp", "--stringArg", "string", "--intArg", "42", "--mode", "withString" };
		var parsed = Args.parse(args, Action.class, Mode.class, WithPath.class);

		assertThat(parsed.first()).isInstanceOf(WithInteger.class);
		var withInteger = (WithInteger) parsed.first();
		assertThat(withInteger.intArg()).isEqualTo(42);

		assertThat(parsed.second()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.second();
		assertThat(withString.stringArg()).isEqualTo("string");

		assertThat(parsed.third()).isInstanceOf(WithPath.class);
		var withPath = (WithPath) parsed.third();
		assertThat(withPath.pathArg()).isEqualTo(Path.of("/tmp"));
	}

}
