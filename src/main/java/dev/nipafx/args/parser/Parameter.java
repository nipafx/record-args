package dev.nipafx.args.parser;

public record Parameter(Aliases aliases) {

	public Parameter {
		Check.nonNull("aliases", aliases);
	}

}
