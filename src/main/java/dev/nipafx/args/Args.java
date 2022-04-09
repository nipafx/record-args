package dev.nipafx.args;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nipafx.args.ArgsCode.ILLEGAL_ACCESS;
import static dev.nipafx.args.ArgsCode.MISSING_ARGUMENT;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

/**
 * Parses command-line arguments to an args record - call {@link Args#parse(String[], Class) parse}
 * or one of its overloads (depending on how many args record types are involved).
 */
public class Args {

	/**
	 * Parses the specified string array to create an instance of the specified type.
	 */
	@SuppressWarnings("unchecked")
	public static <ARGS_RECORD extends Record> ARGS_RECORD parse(
			String[] argStrings, Class<ARGS_RECORD> type) throws ArgsException {
		RecordPackager<ARGS_RECORD> packager = records -> (ARGS_RECORD) records.get(type);
		return parse(argStrings, packager, type);
	}

	/**
	 * Parses the specified string array to create instances of the specified types.
	 */
	@SuppressWarnings("unchecked")
	public static <ARGS_RECORD_1 extends Record, ARGS_RECORD_2 extends Record> Parsed2<ARGS_RECORD_1, ARGS_RECORD_2> parse(
			String[] argStrings, Class<ARGS_RECORD_1> type1, Class<ARGS_RECORD_2> type2) throws ArgsException {
		RecordPackager<Parsed2<ARGS_RECORD_1, ARGS_RECORD_2>> packager = records
				-> new Parsed2<>((ARGS_RECORD_1) records.get(type1), (ARGS_RECORD_2) records.get(type2));
		return parse(argStrings, packager, type1, type2);
	}

	/**
	 * Parses the specified string array to create instances of the specified types.
	 */
	@SuppressWarnings("unchecked")
	public static <ARGS_RECORD_1 extends Record, ARGS_RECORD_2 extends Record, ARGS_RECORD_3 extends Record> Parsed3<ARGS_RECORD_1, ARGS_RECORD_2, ARGS_RECORD_3> parse(
			String[] argStrings, Class<ARGS_RECORD_1> type1, Class<ARGS_RECORD_2> type2, Class<ARGS_RECORD_3> type3) throws ArgsException {
		RecordPackager<Parsed3<ARGS_RECORD_1, ARGS_RECORD_2, ARGS_RECORD_3>> packager = records
				-> new Parsed3<>((ARGS_RECORD_1) records.get(type1), (ARGS_RECORD_2) records.get(type2), (ARGS_RECORD_3) records.get(type3));
		return parse(argStrings, packager, type1, type2, type3);
	}

	private interface RecordPackager<T> extends Function<Map<Class<? extends Record>, Record>, T> { }

	@SafeVarargs
	private static <T> T parse(String[] argStrings, RecordPackager<T> packager, Class<? extends Record>... types)
			throws ArgsException {
		var args = inferArgs(types);
		var messages = ArgsParser
				.forArgs(args.all().toList())
				.parse(argStrings);
		throwOnErrors(messages.errors(), messages.warnings(), argStrings, types);

		var constructorArguments = prepareConstructions(args);
		var constructorErrors = constructorArguments.stream()
						.flatMap(constrArg -> constrArg.errors.stream())
						.toList();
		throwOnErrors(constructorErrors, List.of(), argStrings, types);

		var records = constructArgTypes(argStrings, constructorArguments);
		return packager.apply(records);
	}

	@SafeVarargs
	private static InferredArgs inferArgs(Class<? extends Record>... types) {
		Map<Class<? extends Record>, List<Arg<?>>> argsByType = stream(types)
				.map(type -> Map.entry(
						type,
						stream(type.getRecordComponents())
								.<Arg<?>> map(Args::readComponent)
								.toList()
						))
				.collect(toMap(Entry::getKey, Entry::getValue));
		ensureArgUniqueness(argsByType);
		return new InferredArgs(argsByType);
	}

	private static void ensureArgUniqueness(Map<Class<? extends Record>, List<Arg<?>>> argsByType) {
		record ArgAndRecordType(String name, Class<? extends Record> recordType) { }
		var allArgs = argsByType
				.entrySet().stream()
				.flatMap(recordWithArgs -> recordWithArgs
						.getValue().stream()
						.map(arg -> new ArgAndRecordType(arg.name(), recordWithArgs.getKey())))
				.toList();
		var uniqueArgs = new HashMap<String, ArgAndRecordType>();
		var errors = new ArrayList<String>();

		for (var arg : allArgs) {
			if (uniqueArgs.containsKey(arg.name())) {
				var message = "Duplicate arg '%s' in types '%s' and '%s'.".formatted(
						arg.name(),
						uniqueArgs.get(arg.name()).recordType().getName(),
						arg.recordType().getName());
				errors.add(message);
			} else
				uniqueArgs.put(arg.name(), arg);
		}
		if (!errors.isEmpty())
			throw new IllegalArgumentException(String.join("\n", errors));
	}

	private static Arg<?> readComponent(RecordComponent component) {
		return Arg.of(component.getName(), component.getGenericType());
	}

	private static List<ConstructorArguments> prepareConstructions(InferredArgs args) {
		return args
				.allByType()
				.map(entry -> prepareConstruction(entry.getKey(), entry.getValue()))
				.toList();
	}

	private static ConstructorArguments prepareConstruction(Class<? extends Record> type, List<Arg<?>> args) {
		List<ArgsMessage> errors = new ArrayList<>();
		Class<?>[] parameters = args.stream()
				.map(Arg::type)
				.toArray(Class<?>[]::new);
		Object[] arguments = args.stream()
				.map(arg -> {
					if (arg.value().isEmpty()) {
						String message = "No value for required argument '%s'.".formatted(arg.name());
						errors.add(new ArgsMessage(MISSING_ARGUMENT, message));
						return null;
					} else
						return arg.value().get();
				})
				.toArray(Object[]::new);
		return new ConstructorArguments(type, parameters, arguments, errors);
	}

	private static Map<Class<? extends Record>, Record> constructArgTypes(
			String[] argStrings, List<ConstructorArguments> constructors) throws ArgsException {
		var argTypes = new HashMap<Class<? extends Record>, Record>();
		for (var constr : constructors) {
			var argType = constructArgType(argStrings, constr.argType(), constr.parameters(), constr.arguments());
			argTypes.put(constr.argType(), argType);
		}
		return argTypes;
	}

	private static <T extends Record> T constructArgType(
			String[] argStrings, Class<T> type, Class<?>[] parameters, Object[] arguments) throws ArgsException {
		try {
			Constructor<T> canonicalConstructor = type.getDeclaredConstructor(parameters);
			canonicalConstructor.setAccessible(true);
			return canonicalConstructor.newInstance(arguments);
		} catch (IllegalAccessException ex) {
			String message = "Make sure Args has reflective access to the argument record, e.g. with an `opens ... to ...` directive.";
			throw new ArgsException(argStrings, type, List.of(new ArgsMessage(ILLEGAL_ACCESS, message)), ex);
		} catch (ReflectiveOperationException ex) {
			String message = "There was an unexpected error while creating the argument record.";
			throw new ArgsException(argStrings, type, List.of(new ArgsMessage(ILLEGAL_ACCESS, message)), ex);
		}
	}

	@SafeVarargs
	private static void throwOnErrors(
			List<ArgsMessage> errors, List<ArgsMessage> warnings, String[] argStrings, Class<? extends Record>... types) throws ArgsException {
		if (errors.isEmpty() && warnings.isEmpty())
			return;

		List<ArgsMessage> messages = new ArrayList<>(errors);
		messages.addAll(warnings);
		throw new ArgsException(argStrings, List.of(types), messages);
	}

	/*
	 * INNER TYPES
	 */

	private record ConstructorArguments(
			Class<? extends Record> argType, Class<?>[] parameters, Object[] arguments, List<ArgsMessage> errors) { }

	private static class InferredArgs {

		private final Map<Class<? extends Record>, List<Arg<?>>> argsByType;

		private InferredArgs(Map<Class<? extends Record>, List<Arg<?>>> argsByType) {
			this.argsByType = argsByType;
		}

		public Stream<Arg<?>> all() {
			return argsByType.values().stream().flatMap(List::stream);
		}

		public Stream<Entry<Class<? extends Record>, List<Arg<?>>>> allByType() {
			return argsByType.entrySet().stream();
		}

	}

}
