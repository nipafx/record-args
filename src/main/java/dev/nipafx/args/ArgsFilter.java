package dev.nipafx.args;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.nipafx.args.ArgsCode.FAULTY_ACTION;
import static dev.nipafx.args.ArgsCode.MISSING_ARGUMENT;
import static dev.nipafx.args.ArgsCode.UNPARSEABLE_VALUE;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

class ArgsFilter {

	private final List<String> argList;
	private final List<ArgsMessage> errors;
	private final List<Class<? extends Record>> recordTypes;

	public ArgsFilter() {
		this.argList = new ArrayList<>();
		this.errors = new ArrayList<>();
		this.recordTypes = new ArrayList<>();

	}

	public ArgsAndTypes processModes(String[] argStrings, Class<?>[] types) {
		argList.addAll(List.of(argStrings));

		for (Class<?> type : types) {
			if (type.isRecord())
				processRecord(type);
			else if (type.isInterface() && type.isSealed()) {
				processSealedInterface(type);
			} else {
				var message = "Types must be records or sealed interfaces with exclusively record implementations, but '%s' isn't.";
				throw new IllegalArgumentException(message.formatted(type));
			}
		}

		var argsAndTypes = new ArgsAndTypes(List.copyOf(argList), List.copyOf(recordTypes), List.copyOf(errors));
		argList.clear();
		recordTypes.clear();
		errors.clear();
		return argsAndTypes;
	}

	@SuppressWarnings("unchecked")
	private void processRecord(Class<?> type) {
		recordTypes.add((Class<? extends Record>) type);
	}

	private void processSealedInterface(Class<?> type) {
		if (type.getSimpleName().equals("Action"))
			processAction(type);
		else
			processMode(type);
	}

	private void processAction(Class<?> type) {
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
		var argumentName = "--" + lowerCaseFirstLetter(type);
		var valueTypesByName = createValuesByTypeName(type);

		// detect and remove arguments
		var argumentIndex = argList.indexOf(argumentName);
		if (argumentIndex == -1){
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
						throw new IllegalArgumentException(message.formatted(subtype));
					}
				})
				.collect(toMap(ArgsFilter::lowerCaseFirstLetter, identity()));
	}

	private static String lowerCaseFirstLetter(Class<?> type) {
		return type.getSimpleName().substring(0, 1).toLowerCase(Locale.US) + type.getSimpleName().substring(1);
	}

}
