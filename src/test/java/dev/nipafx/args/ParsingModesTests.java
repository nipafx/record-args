package dev.nipafx.args;

import dev.nipafx.args.Records.AnotherWithString;
import dev.nipafx.args.Records.Mode;
import dev.nipafx.args.Records.ModeArgs;
import dev.nipafx.args.Records.SubtypesWithOverlappingComponents;
import dev.nipafx.args.Records.Type;
import dev.nipafx.args.Records.WithInteger;
import dev.nipafx.args.Records.WithMap;
import dev.nipafx.args.Records.WithString;
import dev.nipafx.args.Records.WithStringArgs;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
class ParsingModesTests {

	@Test
	void singleMode_missingSelection_missingArgumentError() {
		String[] args = { "--stringArg", "string" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, Mode.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.MissingArgument("mode"));
	}

	@Test
	void singleMode_incorrectSelection_illegalModeValueError() {
		String[] args = { "--mode", "withStringies", "--stringArg", "string" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, Mode.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.IllegalModeValue("mode", Set.of("withString", "withList"), "withStringies"));
	}

	@Test
	void singleMode_correctSelectionButValuesForWrongSubtype_unknownArgumentError() {
		String[] args = { "--mode", "withList", "--stringArg", "string" };
		var exception = assertThrows(ArgsParseException.class, () -> Args.parse(args, Mode.class));
		assertThat(exception.errors())
				.containsExactlyInAnyOrder(new ArgsMessage.UnknownArgument("stringArg"));
	}

	@Test
	void singleMode_correctSelectionAndValuesInOrder_parses() throws ArgsParseException {
		String[] args = { "--mode", "withString", "--stringArg", "string" };
		var parsed = Args.parse(args, Mode.class);

		assertThat(parsed).isInstanceOf(WithString.class);
		var withString = (WithString) parsed;
		assertThat(withString.stringArg()).isEqualTo("string");
	}

	@Test
	void singleModeWithArgsName_correctSelectionAndValuesInOrder_parses() throws ArgsParseException {
		String[] args = { "--mode", "withString", "--stringArg", "string" };
		var parsed = Args.parse(args, ModeArgs.class);

		assertThat(parsed).isInstanceOf(WithStringArgs.class);
		var withString = (WithStringArgs) parsed;
		assertThat(withString.stringArg()).isEqualTo("string");
	}

	@Test
	void singleMode_correctSelectionAndValuesOutOfOrder_parses() throws ArgsParseException {
		String[] args = { "--stringArg", "string", "--mode", "withString" };
		var parsed = Args.parse(args, Mode.class);

		assertThat(parsed).isInstanceOf(WithString.class);
		var withString = (WithString) parsed;
		assertThat(withString.stringArg()).isEqualTo("string");
	}

	@Test
	void singleModePlusOneRecord_correctSelectionAndValuesInOrder_parses() throws ArgsParseException {
		String[] args = { "--mode", "withString", "--stringArg", "string", "--intArg", "42" };
		var parsed = Args.parse(args, Mode.class, WithInteger.class);

		assertThat(parsed.first()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.first();
		assertThat(withString.stringArg()).isEqualTo("string");

		assertThat(parsed.second().intArg()).isEqualTo(42);
	}

	@Test
	void singleModePlusOneRecord_correctSelectionAndValuesOutOfOrder_parses() throws ArgsParseException {
		String[] args = { "--stringArg", "string", "--intArg", "42", "--mode", "withString" };
		var parsed = Args.parse(args, Mode.class, WithInteger.class);

		assertThat(parsed.first()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.first();
		assertThat(withString.stringArg()).isEqualTo("string");

		assertThat(parsed.second().intArg()).isEqualTo(42);
	}

	@Test
	void twoModes_correctSelectionAndValuesInOrder_parses() throws ArgsParseException {
		String[] args = { "--mode", "withString", "--stringArg", "string", "--type", "withMap", "--mapArgs", "1=one" };
		var parsed = Args.parse(args, Mode.class, Type.class);

		assertThat(parsed.first()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.first();
		assertThat(withString.stringArg()).isEqualTo("string");

		assertThat(parsed.second()).isInstanceOf(WithMap.class);
		var withMap = (WithMap) parsed.second();
		assertThat(withMap.mapArgs()).isEqualTo(Map.of(1, "one"));
	}

	@Test
	void twoModes_correctSelectionAndValuesOutOfOrder_parses() throws ArgsParseException {
		String[] args = { "--stringArg", "string", "--type", "withMap", "--mode", "withString", "--mapArgs", "1=one" };
		var parsed = Args.parse(args, Mode.class, Type.class);

		assertThat(parsed.first()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.first();
		assertThat(withString.stringArg()).isEqualTo("string");

		assertThat(parsed.second()).isInstanceOf(WithMap.class);
		var withMap = (WithMap) parsed.second();
		assertThat(withMap.mapArgs()).isEqualTo(Map.of(1, "one"));
	}

	@Test
	void singleModeWithOverlappingComponents_correctSelectionAndValues_parses() throws ArgsParseException {
		String[] args = { "--subtypesWithOverlappingComponents", "anotherWithString", "--stringArg", "string" };
		var parsed = Args.parse(args, SubtypesWithOverlappingComponents.class);

		assertThat(parsed).isInstanceOf(AnotherWithString.class);
		var withString = (AnotherWithString) parsed;
		assertThat(withString.stringArg()).isEqualTo("string");
	}

}
