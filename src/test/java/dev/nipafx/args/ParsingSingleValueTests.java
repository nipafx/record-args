package dev.nipafx.args;

import dev.nipafx.args.Records.None;
import dev.nipafx.args.Records.WithBoolean;
import dev.nipafx.args.Records.WithDouble;
import dev.nipafx.args.Records.WithFloat;
import dev.nipafx.args.Records.WithInteger;
import dev.nipafx.args.Records.WithLong;
import dev.nipafx.args.Records.WithPath;
import dev.nipafx.args.Records.WithString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
class ParsingSingleValueTests {

	@Test
	void withoutArgs_programWithoutArgs_parses() throws ArgsParseException {
		String[] args = { };
		None parsed = Args.parse(args, None.class);

		assertThat(parsed).isNotNull();
	}

	@Test
	void withStringArg_programWithStringArg_parses() throws ArgsParseException {
		String[] args = { "--stringArg", "foobar" };
		WithString parsed = Args.parse(args, WithString.class);

		assertThat(parsed.stringArg()).isEqualTo("foobar");
	}

	@Test
	void withPathArg_programWithPathArg_parses() throws ArgsParseException {
		String[] args = { "--pathArg", "/tmp" };
		WithPath parsed = Args.parse(args, WithPath.class);

		assertThat(parsed.pathArg()).isEqualTo(Path.of("/tmp"));
	}

	@Test
	void withIntegerArg_programWithIntegerArg_parses() throws ArgsParseException {
		String[] args = { "--intArg", "5" };
		WithInteger parsed = Args.parse(args, WithInteger.class);

		assertThat(parsed.intArg()).isEqualTo(5);
	}

	@Test
	void withLongArg_programWithLongArg_parses() throws ArgsParseException {
		String[] args = { "--longArg", "5" };
		WithLong parsed = Args.parse(args, WithLong.class);

		assertThat(parsed.longArg()).isEqualTo(5L);
	}

	@Test
	void withFloatArg_programWithFloatArg_parses() throws ArgsParseException {
		String[] args = { "--floatArg", "5.5" };
		WithFloat parsed = Args.parse(args, WithFloat.class);

		assertThat(parsed.floatArg()).isEqualTo(5.5f);
	}

	@Test
	void withDoubleArg_programWithDoubleArg_parses() throws ArgsParseException {
		String[] args = { "--doubleArg", "5.5" };
		WithDouble parsed = Args.parse(args, WithDouble.class);

		assertThat(parsed.doubleArg()).isEqualTo(5.5d);
	}

	@Test
	void withBooleanFalseArg_programWithBooleanArg_parses() throws ArgsParseException {
		String[] args = { "--booleanArg", "false" };
		WithBoolean parsed = Args.parse(args, WithBoolean.class);

		assertThat(parsed.booleanArg()).isFalse();
	}

	@Test
	void withTrueBooleanArg_programWithBooleanArg_parses() throws ArgsParseException {
		String[] args = { "--booleanArg", "true" };
		WithBoolean parsed = Args.parse(args, WithBoolean.class);

		assertThat(parsed.booleanArg()).isTrue();
	}

	@Test
	void withBooleanArgWithoutValue_programWithBooleanArg_parses() throws ArgsParseException {
		String[] args = { "--booleanArg" };
		WithBoolean parsed = Args.parse(args, WithBoolean.class);

		assertThat(parsed.booleanArg()).isTrue();
	}

}
