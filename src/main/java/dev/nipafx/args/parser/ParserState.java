package dev.nipafx.args.parser;

import java.util.List;

public record ParserState(List<String> parsedArgs, List<String> remainingArgs, List<Argument> parsed, List<Parameter> unparsed) {

	public ParserState {
		Check.nonNullNorContainsNull("parsedArgs", parsedArgs);
		Check.nonNullNorContainsNull("remainingArgs", remainingArgs);
		Check.nonNullNorContainsNull("parsed", parsed);
		Check.nonNullNorContainsNull("unparsed", unparsed);
	}

}
