package dev.nipafx.args;

import java.util.List;

import static dev.nipafx.args.Check.internalErrorOnNull;

record ArgsAndTypes(List<String> argsStrings, List<Class<? extends Record>> types, List<ArgsMessage> errors) {

	ArgsAndTypes {
		internalErrorOnNull(argsStrings);
		internalErrorOnNull(types);
		internalErrorOnNull(errors);
	}

}
