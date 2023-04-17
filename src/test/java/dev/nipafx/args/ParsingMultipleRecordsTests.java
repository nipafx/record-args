package dev.nipafx.args;

import dev.nipafx.args.Records.WithLong;
import dev.nipafx.args.Records.WithMany;
import dev.nipafx.args.Records.WithMap;
import dev.nipafx.args.Records.WithPath;
import dev.nipafx.args.Records.WithString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
class ParsingMultipleRecordsTests {

	@Test
	void multipleArgsTwoRecords_correctValuesInOrder_parses() throws ArgsParseException {
		String[] args = { "--stringArg", "string", "--pathArg", "/tmp" };
		var parsed = Args.parse(args, WithString.class, WithPath.class);

		assertThat(parsed.first().stringArg()).isEqualTo("string");
		assertThat(parsed.second().pathArg()).isEqualTo(Path.of("/tmp"));
	}

	@Test
	void multipleArgsTwoRecords_correctValuesOutOfOrder_parses() throws ArgsParseException {
		String[] args = { "--pathArg", "/tmp", "--stringArg", "string" };
		var parsed = Args.parse(args, WithString.class, WithPath.class);

		assertThat(parsed.first().stringArg()).isEqualTo("string");
		assertThat(parsed.second().pathArg()).isEqualTo(Path.of("/tmp"));
	}

	@Test
	void multipleArgsThreeRecords_correctValuesInOrder_parses() throws ArgsParseException {
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
	void multipleArgsThreeRecords_correctValuesOutOfOrder_parses() throws ArgsParseException {
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
