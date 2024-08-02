package dev.nipafx.args;

import dev.nipafx.args.Records.WithOptional;
import dev.nipafx.args.Records.WithOptionalBoolean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
class ParsingOptionalValuesTests {

	@Test
	void withoutArgs_programWithOptionalArg_parses() throws ArgsParseException {
		String[] args = { };
		WithOptional parsed = Args.parse(args, WithOptional.class);

		assertThat(parsed.optionalArg()).isEmpty();
	}

	@Test
	void withArg_programWithOptionalArg_parses() throws ArgsParseException {
		String[] args = { "--optionalArg", "string" };
		WithOptional parsed = Args.parse(args, WithOptional.class);

		assertThat(parsed.optionalArg()).contains("string");
	}

	@Test
	void withoutArgs_programWithOptionalBooleanArg_parses() throws ArgsParseException {
		String[] args = { };
		WithOptionalBoolean parsed = Args.parse(args, WithOptionalBoolean.class);

		assertThat(parsed.optionalArg()).isEmpty();
	}

	@Test
	void withFalseArg_programWithOptionalBooleanArg_parses() throws ArgsParseException {
		String[] args = { "--optionalArg", "false" };
		WithOptionalBoolean parsed = Args.parse(args, WithOptionalBoolean.class);

		assertThat(parsed.optionalArg()).contains(false);
	}

	@Test
	void withTrueArg_programWithOptionalBooleanArg_parses() throws ArgsParseException {
		String[] args = { "--optionalArg", "true" };
		WithOptionalBoolean parsed = Args.parse(args, WithOptionalBoolean.class);

		assertThat(parsed.optionalArg()).contains(true);
	}

	@Test
	void withArgButWithoutValue_programWithOptionalBooleanArg_parses() throws ArgsParseException {
		String[] args = { "--optionalArg" };
		WithOptionalBoolean parsed = Args.parse(args, WithOptionalBoolean.class);

		assertThat(parsed.optionalArg()).contains(true);
	}

}
