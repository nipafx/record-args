package dev.nipafx.args;

import dev.nipafx.args.Records.WithOptional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
class ParsingOptionalValuesTests {

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
