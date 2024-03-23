package dev.nipafx.args.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTests {

	private final Parser parser = new Parser();

	@Test
	void parseOneOfOneParameter() throws ParseException {
		String[] args = { "--name", "value" };
		var parameter = Parameter.ofString(Aliases.withLongName("name"));

		var parsed = parser.parseFirst(args, List.of(parameter));

		assertThat(parsed.nextArgumentIndex()).isEqualTo(2);
		assertThat(parsed.argument().parameter()).isSameAs(parameter);
		assertThat(parsed.argument().value()).isEqualTo("value");
		assertThat(parsed.argument().index()).isEqualTo(0);
		assertThat(parsed.argument().length()).isEqualTo(2);
	}

	@Test
	void parseFirstOfTwoParameters() throws ParseException {
		String[] args = { "--name", "value" };
		var parameter = Parameter.ofString(Aliases.withLongName("name"));
		var otherParameter = Parameter.ofString(Aliases.withLongName("other-name"));

		var parsed = parser.parseFirst(args, List.of(parameter, otherParameter));

		assertThat(parsed.nextArgumentIndex()).isEqualTo(2);
		assertThat(parsed.argument().parameter()).isSameAs(parameter);
		assertThat(parsed.argument().value()).isEqualTo("value");
		assertThat(parsed.argument().index()).isEqualTo(0);
		assertThat(parsed.argument().length()).isEqualTo(2);
	}

	@Test
	void parseSecondOfTwoParameters() throws ParseException {
		String[] args = { "--other-name", "other value" };
		var parameter = Parameter.ofString(Aliases.withLongName("name"));
		var otherParameter = Parameter.ofString(Aliases.withLongName("other-name"));

		var parsed = parser.parseFirst(args, List.of(parameter, otherParameter));

		assertThat(parsed.nextArgumentIndex()).isEqualTo(2);
		assertThat(parsed.argument().parameter()).isSameAs(otherParameter);
		assertThat(parsed.argument().value()).isEqualTo("other value");
		assertThat(parsed.argument().index()).isEqualTo(0);
		assertThat(parsed.argument().length()).isEqualTo(2);
	}

	@Test
	void parseTwoOfTwoParametersInOrder() throws ParseException {
		String[] args = { "--name", "value", "--other-name", "other value" };
		var parameter = Parameter.ofString(Aliases.withLongName("name"));
		var otherParameter = Parameter.ofString(Aliases.withLongName("other-name"));
		var parameters = List.of(parameter, otherParameter);

		var parsedFirst = parser.parseFirst(args, parameters);
		var parsedSecond = parser.parseNext(args, parsedFirst.nextArgumentIndex(), parameters);

		assertThat(parsedFirst.nextArgumentIndex()).isEqualTo(2);
		assertThat(parsedFirst.argument().parameter()).isSameAs(parameter);
		assertThat(parsedFirst.argument().value()).isEqualTo("value");
		assertThat(parsedFirst.argument().index()).isEqualTo(0);
		assertThat(parsedFirst.argument().length()).isEqualTo(2);

		assertThat(parsedSecond.nextArgumentIndex()).isEqualTo(4);
		assertThat(parsedSecond.argument().parameter()).isSameAs(otherParameter);
		assertThat(parsedSecond.argument().value()).isEqualTo("other value");
		assertThat(parsedSecond.argument().index()).isEqualTo(2);
		assertThat(parsedSecond.argument().length()).isEqualTo(2);
	}

	@Test
	void parseTwoOfTwoParametersOutOfOrder() throws ParseException {
		String[] args = { "--other-name", "other value", "--name", "value" };
		var parameter = Parameter.ofString(Aliases.withLongName("name"));
		var otherParameter = Parameter.ofString(Aliases.withLongName("other-name"));
		var parameters = List.of(parameter, otherParameter);

		var parsedFirst = parser.parseFirst(args, parameters);
		var parsedSecond = parser.parseNext(args, parsedFirst.nextArgumentIndex(), parameters);

		assertThat(parsedFirst.nextArgumentIndex()).isEqualTo(2);
		assertThat(parsedFirst.argument().parameter()).isSameAs(otherParameter);
		assertThat(parsedFirst.argument().value()).isEqualTo("other value");
		assertThat(parsedFirst.argument().index()).isEqualTo(0);
		assertThat(parsedFirst.argument().length()).isEqualTo(2);

		assertThat(parsedSecond.nextArgumentIndex()).isEqualTo(4);
		assertThat(parsedSecond.argument().parameter()).isSameAs(parameter);
		assertThat(parsedSecond.argument().value()).isEqualTo("value");
		assertThat(parsedSecond.argument().index()).isEqualTo(2);
		assertThat(parsedSecond.argument().length()).isEqualTo(2);
	}

}
