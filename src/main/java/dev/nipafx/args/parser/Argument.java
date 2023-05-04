package dev.nipafx.args.parser;

import java.util.List;

public record Argument(String value, Parameter parameter, List<String> args, int index, int length) {

	public Argument {
		Check.nonNull("value", value);
		Check.nonNull("parameter", parameter);
		Check.nonNullNorEmpty("args", args);
		if (index < 0 || index >= args.size()) {
			var message = "Parameter 'index' must be an index into 'args': %d in %s".formatted(index, args);
			throw new IllegalArgumentException(message);
		}
		if (length <= 0 || index + length > args.size()) {
			var message = "Parameter 'length' must be positive and if added to 'index' an index into 'args': [%d-%d] in %s"
					.formatted(index, index + length, args);
			throw new IllegalArgumentException(message);
		}
	}

}
