package dev.nipafx.args;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static dev.nipafx.args.ArgsDefinitionErrorCode.ILL_DEFINED_ARGS_TYPE;
import static dev.nipafx.args.ArgsDefinitionErrorCode.MULTIPLE_ACTIONS;
import static dev.nipafx.args.ArgsParseErrorCode.FAULTY_ACTION;
import static dev.nipafx.args.ArgsParseErrorCode.MISSING_ARGUMENT;
import static dev.nipafx.args.ArgsParseErrorCode.UNPARSEABLE_VALUE;
import static dev.nipafx.args.Check.internalErrorOnNull;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
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
		var allowedValues = valueTypesByName
				.keySet().stream()
				.collect(joining("', '", "'", "'"));

		if (argList.size() == 0) {
			String message = "No arguments provided - first argument must be one of [ %s ].".formatted(allowedValues);
			errors.add(new ArgsMessage(FAULTY_ACTION, message));
		} else {
			var value = argList.get(0);
			var valueType = valueTypesByName.get(value);

			if (valueType == null) {
				var message = "First argument '%s' did not match any of the allowed values: [ %s ].".formatted(value, allowedValues);
				errors.add(new ArgsMessage(FAULTY_ACTION, message));
			} else {
				argList.remove(0);
				recordTypes.add(valueType);
			}
		}
	}

	private void processMode(Class<?> type) {
		// create names of argument and values
		var argumentName = "--" + createArgumentName(type);
		var valueTypesByName = createValuesByTypeName(type);

		// detect and remove arguments
		var argumentIndex = argList.indexOf(argumentName);
		if (argumentIndex == -1) {
			String message = "No value for required argument '%s'.".formatted(argumentName);
			errors.add(new ArgsMessage(MISSING_ARGUMENT, message));
		} else if (argumentIndex == argList.size()) {
			String message = "No value for required argument '%s'.".formatted(argumentName);
			errors.add(new ArgsMessage(MISSING_ARGUMENT, message));
		} else {
			var value = argList.get(argumentIndex + 1);
			var valueType = valueTypesByName.get(value);

			if (valueType == null) {
				var allowedValues = valueTypesByName
						.keySet().stream()
						.collect(joining("', '", "'", "'"));
				var message = "Value '%s' did not match any of the values allowed for '%s': [ %s ]."
						.formatted(value, argumentName, allowedValues);
				errors.add(new ArgsMessage(UNPARSEABLE_VALUE, message));
			} else {
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
