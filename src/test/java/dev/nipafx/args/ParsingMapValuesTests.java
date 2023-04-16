package dev.nipafx.args;

import dev.nipafx.args.Records.WithMap;
import dev.nipafx.args.Records.WithMapAndMore;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
class ParsingMapValuesTests {

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
