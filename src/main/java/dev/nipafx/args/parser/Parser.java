package dev.nipafx.args.parser;

import java.util.*;
import java.util.function.BinaryOperator;

public class Parser {

	public Parsed parseFirst(String[] args, List<? extends Parameter<?>> parameters) throws ParseException {
		return parseNext(args, 0, parameters);
	}

	public Parsed parseNext(String[] args, int startIndex, List<? extends Parameter<?>> parameters) throws ParseException {
		Check.nonNullNorEmpty("parameters", parameters);
		Check.nonNullNorContainsNull("parameters", parameters);

		Parameter<?> parameter = parameters.stream()
				.filter(param -> param.aliases().matches(args[startIndex]))
				.reduce((p1, p2) -> {
					var message1 = "No two parameters must have aliases with the same short or long name: %s and %s".formatted(p1, p2);
					throw new IllegalStateException(message1);
				})
				.orElseThrow(() -> {
					var message = "Argument array %s[%d] ('%s') is no parameter alias"
							.formatted(Arrays.toString(args), startIndex, args[startIndex]);
					return new ParseException(message);
				});
		var argument = parseArgument(args, startIndex, parameter);

		return new Parsed(startIndex + argument.length(), argument);
	}

	private BinaryOperator<? extends Parameter<?>> toOnlyParameter() {
		return (p1, p2) -> {
			var message = "No two parameters must have aliases with the same short or long name: %s and %s".formatted(p1, p2);
			throw new IllegalStateException(message);
		};
	}

	private static <T> Argument<T> parseArgument(String[] args, int index, Parameter<T> parameter) throws ParseException {
		var stringValue = args[index + 1];
		try {
			var value = parameter.converter().convert(stringValue);
			return new Argument<>(value, parameter, List.of(args), index, 2);
		} catch (Exception ex) {
			var message = "Parsing argument '%s' to parameter %s failed".formatted(stringValue, parameter);
			throw new ParseException(message);
		}
	}

}
