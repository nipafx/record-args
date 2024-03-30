package dev.nipafx.args;

import dev.nipafx.args.Records.Action;
import dev.nipafx.args.Records.ActionArgs;
import dev.nipafx.args.Records.Mode;
import dev.nipafx.args.Records.WithInteger;
import dev.nipafx.args.Records.WithPath;
import dev.nipafx.args.Records.WithPathArgs;
import dev.nipafx.args.Records.WithString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
class ParsingActionTests {

	@Test
	void justAction_emptyArgs_missingActionError() {
		String[] args = { };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, Action.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.MissingAction(Set.of("withInteger", "withOptional")));
	}

	@Test
	void justAction_missingSelection_unknownActionError() {
		String[] args = { "--intArg", "42" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, Action.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownAction(Set.of("withInteger", "withOptional"), "--intArg"));
	}

	@Test
	void justAction_incorrectSelection_unknownActionError() {
		String[] args = { "withInt", "--intArg", "42" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, Action.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownAction(Set.of("withInteger", "withOptional"), "withInt"));
	}

	@Test
	void justAction_correctSelectionButValuesForWrongSubtype_unknownArgumentError() {
		String[] args = { "withInteger", "--optionalArg", "string" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, Action.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownArgument("optionalArg"));
	}

	@Test
	void justAction_correctSelectionButActionOutOfOrder_unknownActionError() throws ArgsParseException {
		String[] args = { "--intArg", "42", "withString" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, Action.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownAction(Set.of("withInteger", "withOptional"), "--intArg"));
	}

	@Test
	void justAction_correctSelectionAndValuesInOrder_parses() throws ArgsParseException {
		String[] args = { "withInteger", "--intArg", "42" };
		var parsed = Args.parse(args, Action.class);

		assertThat(parsed).isInstanceOf(WithInteger.class);
		var withInteger = (WithInteger) parsed;
		assertThat(withInteger.intArg()).isEqualTo(42);
	}

	@Test
	void actionWithArgsName_correctSelectionAndValuesInOrder_parses() throws ArgsParseException {
		String[] args = { "withPath", "--pathArg", "/tmp" };
		var parsed = Args.parse(args, ActionArgs.class);

		assertThat(parsed).isInstanceOf(WithPathArgs.class);
		var withPath = (WithPathArgs) parsed;
		assertThat(withPath.pathArg()).isEqualTo(Path.of("/tmp"));
	}

	@Test
	void actionPlusOneRecord_correctSelectionAndValuesInOrder_parses() throws ArgsParseException {
		String[] args = { "withInteger", "--intArg", "42", "--stringArg", "string" };
		var parsed = Args.parse(args, Action.class, WithString.class);

		assertThat(parsed.first()).isInstanceOf(WithInteger.class);
		var withInteger = (WithInteger) parsed.first();
		assertThat(withInteger.intArg()).isEqualTo(42);

		assertThat(parsed.second().stringArg()).isEqualTo("string");
	}

	@Test
	void actionPlusOneRecord_correctSelectionAndValuesOutOfOrder_parses() throws ArgsParseException {
		String[] args = { "withInteger", "--intArg", "42", "--stringArg", "string" };
		var parsed = Args.parse(args, Action.class, WithString.class);

		assertThat(parsed.first()).isInstanceOf(WithInteger.class);
		var withInteger = (WithInteger) parsed.first();
		assertThat(withInteger.intArg()).isEqualTo(42);

		assertThat(parsed.second().stringArg()).isEqualTo("string");
	}

	@Test
	void actionPlusMode_correctSelectionAndValuesInOrder_parses() throws ArgsParseException {
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
	void actionPlusMode_correctSelectionAndValuesOutOfOrder_parses() throws ArgsParseException {
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
	void actionPlusModePlusRecord_correctSelectionAndValuesInOrder_parses() throws ArgsParseException {
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
	void actionPlusModePlusRecord_correctSelectionAndValuesOutOfOrder_parses() throws ArgsParseException {
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
