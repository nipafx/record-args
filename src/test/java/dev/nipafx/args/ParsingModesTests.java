package dev.nipafx.args;

import dev.nipafx.args.Records.AnotherWithString;
import dev.nipafx.args.Records.Mode;
import dev.nipafx.args.Records.SubtypesWithOverlappingComponents;
import dev.nipafx.args.Records.Type;
import dev.nipafx.args.Records.WithInteger;
import dev.nipafx.args.Records.WithMap;
import dev.nipafx.args.Records.WithString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.nipafx.args.ArgsCode.MISSING_ARGUMENT;
import static dev.nipafx.args.ArgsCode.UNKNOWN_ARGUMENT;
import static dev.nipafx.args.ArgsCode.UNPARSEABLE_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
class ParsingModesTests {

	@Test
	void singleSealedInterface_missingSelection_missingArgumentError() {
		String[] args = { "--stringArg", "string" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, Mode.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(MISSING_ARGUMENT);
	}

	@Test
	void singleSealedInterface_incorrectSelection_unparseableValueError() {
		String[] args = { "--mode", "withStringies", "--stringArg", "string" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, Mode.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNPARSEABLE_VALUE);
	}

	@Test
	void singleSealedInterface_correctSelectionButValuesForWrongSubtype_unknownArgumentError() {
		String[] args = { "--mode", "withList", "--stringArg", "string" };
		var exception = assertThrows(ArgsException.class, () -> Args.parse(args, Mode.class));
		assertThat(exception.errors())
				.map(ArgsMessage::code)
				.containsExactlyInAnyOrder(UNKNOWN_ARGUMENT);
	}

	@Test
	void singleSealedInterface_correctSelectionAndValuesInOrder_parses() throws ArgsException {
		String[] args = { "--mode", "withString", "--stringArg", "string" };
		var parsed = Args.parse(args, Mode.class);

		assertThat(parsed).isInstanceOf(WithString.class);
		var withString = (WithString) parsed;
		assertThat(withString.stringArg()).isEqualTo("string");
	}

	@Test
	void singleSealedInterface_correctSelectionAndValuesOutOfOrder_parses() throws ArgsException {
		String[] args = { "--stringArg", "string", "--mode", "withString" };
		var parsed = Args.parse(args, Mode.class);

		assertThat(parsed).isInstanceOf(WithString.class);
		var withString = (WithString) parsed;
		assertThat(withString.stringArg()).isEqualTo("string");
	}

	@Test
	void singleSealedInterfacePlusOneRecord_correctSelectionAndValuesInOrder_parses() throws ArgsException {
		String[] args = { "--mode", "withString", "--stringArg", "string", "--intArg", "42" };
		var parsed = Args.parse(args, Mode.class, WithInteger.class);

		assertThat(parsed.first()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.first();
		assertThat(withString.stringArg()).isEqualTo("string");

		assertThat(parsed.second().intArg()).isEqualTo(42);
	}

	@Test
	void singleSealedInterfacePlusOneRecord_correctSelectionAndValuesOutOfOrder_parses() throws ArgsException {
		String[] args = { "--stringArg", "string", "--intArg", "42", "--mode", "withString" };
		var parsed = Args.parse(args, Mode.class, WithInteger.class);

		assertThat(parsed.first()).isInstanceOf(WithString.class);
		var withString = (WithString) parsed.first();
		assertThat(withString.stringArg()).isEqualTo("string");

		assertThat(parsed.second().intArg()).isEqualTo(42);
	}

	@Test
	void twoSealedInterfaces_correctSelectionAndValuesInOrder_parses() throws ArgsException {
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
	void twoSealedInterfaces_correctSelectionAndValuesOutOfOrder_parses() throws ArgsException {
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
	void singleSealedInterfaceWithOverlappingComponents_correctSelectionAndValues_parses() throws ArgsException {
		String[] args = { "--subtypesWithOverlappingComponents", "anotherWithString", "--stringArg", "string" };
		var parsed = Args.parse(args, SubtypesWithOverlappingComponents.class);

		assertThat(parsed).isInstanceOf(AnotherWithString.class);
		var withString = (AnotherWithString) parsed;
		assertThat(withString.stringArg()).isEqualTo("string");
	}

}
