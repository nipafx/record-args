package dev.nipafx.args;

import dev.nipafx.args.Records.WithList;
import dev.nipafx.args.Records.WithListAndMore;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
class ParsingListValuesTests {

	@Test
	void withoutArgs_programWithListArg_parses() throws ArgsParseException {
		String[] args = { };
		WithList parsed = Args.parse(args, WithList.class);

		assertThat(parsed.stringArgs()).isEmpty();
	}

	@Test
	void withOneArg_programWithListArg_parses() throws ArgsParseException {
		String[] args = { "--stringArgs", "string" };
		WithList parsed = Args.parse(args, WithList.class);

		assertThat(parsed.stringArgs()).containsExactly("string");
	}

	@Test
	void withListArgs_programWithListArg_parses() throws ArgsParseException {
		String[] args = { "--stringArgs", "string 1", "string 2", "string 3" };
		WithList parsed = Args.parse(args, WithList.class);

		assertThat(parsed.stringArgs()).containsExactly("string 1", "string 2", "string 3");
	}

	@Test
	void withListArgsFollowedByOtherArg_programWithListArg_parses() throws ArgsParseException {
		String[] args = { "--stringArgs", "string 1", "string 2", "string 3", "--booleanArg" };
		WithListAndMore parsed = Args.parse(args, WithListAndMore.class);

		assertThat(parsed.stringArgs()).containsExactly("string 1", "string 2", "string 3");
	}

}
