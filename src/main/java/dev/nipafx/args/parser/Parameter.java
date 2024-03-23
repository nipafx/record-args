package dev.nipafx.args.parser;

public record Parameter<T>(Aliases aliases, ArgumentConverter<T> converter) {

	public Parameter {
		Check.nonNull("aliases", aliases);
		Check.nonNull("converter", converter);
	}

	public static Parameter<String> ofString(Aliases aliases) {
		return new Parameter<>(aliases, stringValue -> stringValue);
	}

}
