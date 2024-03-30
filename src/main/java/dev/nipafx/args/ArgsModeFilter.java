package dev.nipafx.args;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static dev.nipafx.args.ArgsDefinitionErrorCode.ILL_DEFINED_ARGS_TYPE;
import static dev.nipafx.args.ArgsDefinitionErrorCode.MULTIPLE_ACTIONS;
import static dev.nipafx.args.Check.internalErrorOnNull;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

class ArgsModeFilter {

	private final List<String> argList;
	private final List<ArgsMessage> errors;
	private final List<Class<? extends Record>> recordTypes;

	private boolean actionFound;

	public ArgsModeFilter() {
		this.argList = new ArrayList<>();
		this.errors = new ArrayList<>();
		this.recordTypes = new ArrayList<>();
	}

	public ArgsAndTypes processModes(String[] argStrings, Class<?>[] types) {
		internalErrorOnNull(argStrings);
		internalErrorOnNull(types);

		argList.addAll(List.of(argStrings));

		for (Class<?> type : types) {
			if (type.isRecord())
				processRecord(type);
			else if (type.isInterface() && type.isSealed()) {
				processSealedInterface(type);
			} else {
				var message = "Types must be records or sealed interfaces with exclusively record implementations, but '%s' isn't.";
				throw new ArgsDefinitionException(ILL_DEFINED_ARGS_TYPE, message.formatted(type));
			}
		}

		var argsAndTypes = new ArgsAndTypes(List.copyOf(argList), List.copyOf(recordTypes), List.copyOf(errors));
		argList.clear();
		recordTypes.clear();
		errors.clear();
		actionFound = false;
		return argsAndTypes;
	}

	@SuppressWarnings("unchecked")
	private void processRecord(Class<?> type) {
		recordTypes.add((Class<? extends Record>) type);
	}

	private void processSealedInterface(Class<?> type) {
		if (Set.of("Action", "ActionArgs").contains(type.getSimpleName()))
			processAction(type);
		else
			processMode(type);
	}

	private void processAction(Class<?> type) {
		if (actionFound) {
			var message = "There can only be one action, but %s is the second such interface.".formatted(type);
			throw new ArgsDefinitionException(MULTIPLE_ACTIONS, message);
		}
		actionFound = true;

		var valueTypesByName = createValuesByTypeName(type);
		if (argList.isEmpty())
			errors.add(new ArgsMessage.MissingAction(valueTypesByName.keySet()));
		else {
			var value = argList.getFirst();
			var valueType = valueTypesByName.get(value);

			if (valueType == null)
				errors.add(new ArgsMessage.UnknownAction(valueTypesByName.keySet(), value));
			else {
				argList.removeFirst();
				recordTypes.add(valueType);
			}
		}
	}

	private void processMode(Class<?> type) {
		// create names of argument and values
		var modeName = createArgumentName(type);
		var valueTypesByName = createValuesByTypeName(type);

		// detect and remove arguments
		var argumentIndex = argList.indexOf("--" + modeName);
		if (argumentIndex == -1)
			errors.add(new ArgsMessage.MissingArgument(modeName));
		else {
			var value = argList.get(argumentIndex + 1);
			var valueType = valueTypesByName.get(value);

			if (valueType == null)
				errors.add(new ArgsMessage.IllegalModeValue(modeName, valueTypesByName.keySet(), value));
			else {
				argList.remove(argumentIndex + 1);
				argList.remove(argumentIndex);
				recordTypes.add(valueType);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Class<? extends Record>> createValuesByTypeName(Class<?> type) {
		return stream(type.getPermittedSubclasses())
				.map(subtype -> {
					if (subtype.isRecord())
						return (Class<? extends Record>) subtype;
					else {
						var message = "Types must be records or sealed interfaces with exclusively record implementations, but '%s' isn't.";
						throw new ArgsDefinitionException(ILL_DEFINED_ARGS_TYPE, message.formatted(subtype));
					}
				})
				.collect(toMap(ArgsModeFilter::createArgumentName, identity()));
	}

	private static String createArgumentName(Class<?> type) {
		var originalName = type.getSimpleName();
		var argsLessName = originalName.endsWith("Args")
				? originalName.substring(0, originalName.length() - 4)
				: originalName;
		return argsLessName.substring(0, 1).toLowerCase(Locale.US) + argsLessName.substring(1);
	}

}
