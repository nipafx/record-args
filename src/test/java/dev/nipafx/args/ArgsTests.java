package dev.nipafx.args;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.nipafx.args.ArgsCode.MISSING_ARGUMENT;
import static dev.nipafx.args.ArgsCode.MISSING_VALUE;
import static dev.nipafx.args.ArgsCode.UNEXPECTED_VALUE;
import static dev.nipafx.args.ArgsCode.UNKNOWN_ARGUMENT;
import static dev.nipafx.args.ArgsCode.UNPARSEABLE_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgsTests {

	@Nested
	class ParsingErrors {

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
		void multipleRecords_sameComponents_duplicateArgError() throws ArgsException {
			String[] args = { };
			var exception = assertThrows(
					IllegalArgumentException.class, () -> Args.parse(args, WithMany.class, WithString.class));
			assertThat(exception)
					.hasMessageMatching("Duplicate arg.*stringArg.*")
					.hasMessageContaining("WithMany")
					.hasMessageContaining("WithString");
		}

	}

	@Nested
	class ParsingSingleValue {

		@Test
		void withoutArgs_programWithoutArgs_parses() throws ArgsException {
			String[] args = { };
			None parsed = Args.parse(args, None.class);

			assertThat(parsed).isNotNull();
		}

		@Test
		void withStringArg_programWithStringArg_parses() throws ArgsException {
			String[] args = { "--stringArg", "foobar" };
			WithString parsed = Args.parse(args, WithString.class);

			assertThat(parsed.stringArg()).isEqualTo("foobar");
		}

		@Test
		void withPathArg_programWithPathArg_parses() throws ArgsException {
			String[] args = { "--pathArg", "/tmp" };
			WithPath parsed = Args.parse(args, WithPath.class);

			assertThat(parsed.pathArg()).isEqualTo(Path.of("/tmp"));
		}

		@Test
		void withIntegerArg_programWithIntegerArg_parses() throws ArgsException {
			String[] args = { "--intArg", "5" };
			WithInteger parsed = Args.parse(args, WithInteger.class);

			assertThat(parsed.intArg()).isEqualTo(5);
		}

		@Test
		void withLongArg_programWithLongArg_parses() throws ArgsException {
			String[] args = { "--longArg", "5" };
			WithLong parsed = Args.parse(args, WithLong.class);

			assertThat(parsed.longArg()).isEqualTo(5L);
		}

		@Test
		void withFloatArg_programWithFloatArg_parses() throws ArgsException {
			String[] args = { "--floatArg", "5.5" };
			WithFloat parsed = Args.parse(args, WithFloat.class);

			assertThat(parsed.floatArg()).isEqualTo(5.5f);
		}

		@Test
		void withDoubleArg_programWithDoubleArg_parses() throws ArgsException {
			String[] args = { "--doubleArg", "5.5" };
			WithDouble parsed = Args.parse(args, WithDouble.class);

			assertThat(parsed.doubleArg()).isEqualTo(5.5d);
		}

		@Test
		void withBooleanFalseArg_programWithBooleanArg_parses() throws ArgsException {
			String[] args = { "--booleanArg", "false" };
			WithBoolean parsed = Args.parse(args, WithBoolean.class);

			assertThat(parsed.booleanArg()).isFalse();
		}

		@Test
		void withTrueBooleanArg_programWithBooleanArg_parses() throws ArgsException {
			String[] args = { "--booleanArg", "true" };
			WithBoolean parsed = Args.parse(args, WithBoolean.class);

			assertThat(parsed.booleanArg()).isTrue();
		}

		@Test
		void withBooleanArgWithoutValue_programWithBooleanArg_parses() throws ArgsException {
			String[] args = { "--booleanArg" };
			WithBoolean parsed = Args.parse(args, WithBoolean.class);

			assertThat(parsed.booleanArg()).isTrue();
		}

	}

	@Nested
	class ParsingOptionalValues {

		@Test
		void withoutArgs_programWithOptionalArg_parses() throws ArgsException {
			String[] args = { };
			WithOptional parsed = Args.parse(args, WithOptional.class);

			assertThat(parsed.optionalArg()).isEmpty();
		}

		@Test
		void withArg_programWithOptionalArg_parses() throws ArgsException {
			String[] args = { "--optionalArg", "string" };
			WithOptional parsed = Args.parse(args, WithOptional.class);

			assertThat(parsed.optionalArg()).contains("string");
		}

	}

	@Nested
	class ParsingListValues {

		@Test
		void withoutArgs_programWithListArg_parses() throws ArgsException {
			String[] args = { };
			WithList parsed = Args.parse(args, WithList.class);

			assertThat(parsed.stringArgs()).isEmpty();
		}

		@Test
		void withOneArg_programWithListArg_parses() throws ArgsException {
			String[] args = { "--stringArgs", "string" };
			WithList parsed = Args.parse(args, WithList.class);

			assertThat(parsed.stringArgs()).containsExactly("string");
		}

		@Test
		void withListArgs_programWithListArg_parses() throws ArgsException {
			String[] args = { "--stringArgs", "string 1", "string 2", "string 3" };
			WithList parsed = Args.parse(args, WithList.class);

			assertThat(parsed.stringArgs()).containsExactly("string 1", "string 2", "string 3");
		}

		@Test
		void withListArgsFollowedByOtherArg_programWithListArg_parses() throws ArgsException {
			String[] args = { "--stringArgs", "string 1", "string 2", "string 3", "--booleanArg" };
			WithListAndMore parsed = Args.parse(args, WithListAndMore.class);

			assertThat(parsed.stringArgs()).containsExactly("string 1", "string 2", "string 3");
		}

	}

	@Nested
	class ParsingMapValues {

		@Test
		void withoutArgs_programWithMapArg_parses() throws ArgsException {
			String[] args = { };
			WithMap parsed = Args.parse(args, WithMap.class);

			assertThat(parsed.mapArgs()).isEmpty();
		}

		@Test
		void withOneArgPair_programWithMapArg_parses() throws ArgsException {
			String[] args = { "--mapArgs", "1=one" };
			WithMap parsed = Args.parse(args, WithMap.class);

			assertThat(parsed.mapArgs()).isEqualTo(Map.of(1, "one"));
		}

		@Test
		void withMultipleArgPairs_programWithMapArg_parses() throws ArgsException {
			String[] args = { "--mapArgs", "1=one", "2=two", "3=three" };
			WithMap parsed = Args.parse(args, WithMap.class);

			assertThat(parsed.mapArgs()).isEqualTo(Map.of(
					1, "one",
					2, "two",
					3, "three"));
		}

		@Test
		void withMultipleArgPairsFollowedByOtherArg_programWithMapArg_parses() throws ArgsException {
			String[] args = { "--mapArgs", "1=one", "2=two", "3=three", "--booleanArg" };
			WithMapAndMore parsed = Args.parse(args, WithMapAndMore.class);

			assertThat(parsed.mapArgs()).isEqualTo(Map.of(
					1, "one",
					2, "two",
					3, "three"));
			assertThat(parsed.booleanArg()).isTrue();
		}

	}

	@Nested
	class ParsingMultipleValues {

		@Test
		void multipleArgs_correctValuesInOrder_parses() throws ArgsException {
			String[] args = { "--stringArg", "string", "--pathArg", "/tmp", "--intArg", "5", "--floatArg", "5.5", "--booleanArg", "true", "--numberArgs", "42", "63" };
			WithMany parsed = Args.parse(args, WithMany.class);

			assertThat(parsed.stringArg()).isEqualTo("string");
			assertThat(parsed.pathArg()).contains(Path.of("/tmp"));
			assertThat(parsed.intArg()).isEqualTo(5);
			assertThat(parsed.floatArg()).isEqualTo(5.5f);
			assertThat(parsed.booleanArg()).isTrue();
			assertThat(parsed.numberArgs()).containsExactly(42, 63);
		}

		@Test
		void multipleArgs_correctValuesOutOfOrder_parses() throws ArgsException {
			String[] args = { "--intArg", "5", "--numberArgs", "42", "63", "--booleanArg", "--stringArg", "string", "--floatArg", "5.5" };
			WithMany parsed = Args.parse(args, WithMany.class);

			assertThat(parsed.stringArg()).isEqualTo("string");
			assertThat(parsed.pathArg()).isEmpty();
			assertThat(parsed.intArg()).isEqualTo(5);
			assertThat(parsed.floatArg()).isEqualTo(5.5f);
			assertThat(parsed.booleanArg()).isTrue();
			assertThat(parsed.numberArgs()).containsExactly(42, 63);
		}

	}

	@Nested
	class ParsingMultipleRecords {

		@Test
		void multipleArgsTwoRecords_correctValuesInOrder_parses() throws ArgsException {
			String[] args = { "--stringArg", "string", "--pathArg", "/tmp" };
			var parsed = Args.parse(args, WithString.class, WithPath.class);

			assertThat(parsed.first().stringArg()).isEqualTo("string");
			assertThat(parsed.second().pathArg()).isEqualTo(Path.of("/tmp"));
		}

		@Test
		void multipleArgsTwoRecords_correctValuesOutOfOrder_parses() throws ArgsException {
			String[] args = { "--pathArg", "/tmp", "--stringArg", "string" };
			var parsed = Args.parse(args, WithString.class, WithPath.class);

			assertThat(parsed.first().stringArg()).isEqualTo("string");
			assertThat(parsed.second().pathArg()).isEqualTo(Path.of("/tmp"));
		}

		@Test
		void multipleArgsThreeRecords_correctValuesInOrder_parses() throws ArgsException {
			String[] args = {
					"--intArg", "5", "--numberArgs", "42", "63", "--booleanArg", "--stringArg", "string", "--floatArg", "5.5",
					"--longArg", "5921650832",
					"--mapArgs", "1=one", "2=two", "3=three"
			};
			var parsed = Args.parse(args, WithMany.class, WithLong.class, WithMap.class);

			assertThat(parsed.first().stringArg()).isEqualTo("string");
			assertThat(parsed.first().pathArg()).isEmpty();
			assertThat(parsed.first().intArg()).isEqualTo(5);
			assertThat(parsed.first().floatArg()).isEqualTo(5.5f);
			assertThat(parsed.first().booleanArg()).isTrue();
			assertThat(parsed.first().numberArgs()).containsExactly(42, 63);
			assertThat(parsed.second().longArg()).isEqualTo(5_921_650_832L);
			assertThat(parsed.third().mapArgs()).isEqualTo(Map.of(
					1, "one",
					2, "two",
					3, "three"));
		}

		@Test
		void multipleArgsThreeRecords_correctValuesOutOfOrder_parses() throws ArgsException {
			String[] args = {
					 "--floatArg", "5.5", "--intArg", "5", "--numberArgs", "42", "63",
					"--mapArgs", "1=one", "2=two", "3=three", "--booleanArg",
					"--longArg", "5921650832", "--stringArg", "string",
			};
			var parsed = Args.parse(args, WithMany.class, WithLong.class, WithMap.class);

			assertThat(parsed.first().stringArg()).isEqualTo("string");
			assertThat(parsed.first().pathArg()).isEmpty();
			assertThat(parsed.first().intArg()).isEqualTo(5);
			assertThat(parsed.first().floatArg()).isEqualTo(5.5f);
			assertThat(parsed.first().booleanArg()).isTrue();
			assertThat(parsed.first().numberArgs()).containsExactly(42, 63);
			assertThat(parsed.second().longArg()).isEqualTo(5_921_650_832L);
			assertThat(parsed.third().mapArgs()).isEqualTo(Map.of(
					1, "one",
					2, "two",
					3, "three"));
		}

	}

	record None() { }

	record WithString(String stringArg) { }
	record WithPath(Path pathArg) { }
	record WithInteger(int intArg) { }
	record WithLong(long longArg) { }
	record WithFloat(float floatArg) { }
	record WithDouble(double doubleArg) { }
	record WithBoolean(boolean booleanArg) { }

	record WithOptional(Optional<String> optionalArg) { }
	record WithList(List<String> stringArgs) { }
	record WithListAndMore(List<String> stringArgs, boolean booleanArg) { }
	record WithMap(Map<Integer, String> mapArgs) { }
	record WithMapAndMore(Map<Integer, String> mapArgs, boolean booleanArg) { }

	record WithStringArray(String[] stringsArg) { }
	record WithMany(
			String stringArg, Optional<Path> pathArg,
			int intArg, float floatArg, boolean booleanArg,
			List<Integer> numberArgs) { }

}
