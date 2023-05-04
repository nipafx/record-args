package dev.nipafx.args.parser;

public record Parsed(int nextArgumentIndex, Argument argument) {

	public Parsed {
		Check.nonNull("argument", argument);
	}

}
