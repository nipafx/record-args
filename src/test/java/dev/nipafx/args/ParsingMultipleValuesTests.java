package dev.nipafx.args;

import dev.nipafx.args.Records.WithMany;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
class ParsingMultipleValuesTests {

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
