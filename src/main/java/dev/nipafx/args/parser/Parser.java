package dev.nipafx.args.parser;

import java.util.*;
import java.util.function.BinaryOperator;

public class Parser {

	public Parsed parseFirst(String[] args, List<Parameter> parameters) throws ParseException {
		return parseNext(args, 0, parameters);
	}

	public Parsed parseNext(String[] args, int startIndex, List<Parameter> parameters) throws ParseException {
		Check.nonNullNorEmpty("parameters", parameters);
		Check.nonNullNorContainsNull("parameters", parameters);

		var parameter = parameters.stream()
				.filter(param -> param.aliases().matches(args[startIndex]))
				.reduce(toOnlyParameter())
				.orElseThrow(() -> {
					var message = "Argument array %s[%d] ('%s') is no parameter alias"
							.formatted(Arrays.toString(args), startIndex, args[startIndex]);
					return new ParseException(message);
				});
		var argument = parseArgument(args, startIndex, parameter);

		return new Parsed(startIndex + argument.length(), argument);
	}

	private BinaryOperator<Parameter> toOnlyParameter() {
		return (p1, p2) -> {
			var message = "No two parameters must have aliases with the same short or long name: %s and %s".formatted(p1, p2);
			throw new IllegalStateException(message);
		};
	}

	private static Argument parseArgument(String[] args, int index, Parameter parameter) {
		var value = args[index + 1];
		return new Argument(value, parameter, List.of(args), index, 2);
	}

}
