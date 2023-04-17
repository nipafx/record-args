package dev.nipafx.args;

import java.util.List;

import static dev.nipafx.args.Check.internalErrorOnNull;

record ArgsMessages(List<ArgsMessage> errors, List<ArgsMessage> warnings) {

	ArgsMessages {
		errors = List.copyOf(internalErrorOnNull(errors));
		warnings = List.copyOf(internalErrorOnNull(warnings));
	}

}
