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
 * Parses command-line arguments to args types - call {@link Args#parse(String[], Class) parse}
 * or one of its overloads (depending on how many args types are involved).
 */
public class Args {

	/**
	 * Parses the specified string array to create an instance of the specified type.
	 */
	@SuppressWarnings("unchecked")
	public static <ARGS_TYPE> ARGS_TYPE parse(
			String[] argStrings, Class<ARGS_TYPE> type) throws ArgsException {
		RecordPackager<ARGS_TYPE> packager = types -> (ARGS_TYPE) types.get(type);
		return parse(argStrings, packager, type);
	}

	/**
	 * Parses the specified string array to create instances of the specified types.
	 */
	@SuppressWarnings("unchecked")
	public static <ARGS_TYPE_1, ARGS_TYPE_2> Parsed2<ARGS_TYPE_1, ARGS_TYPE_2> parse(
			String[] argStrings, Class<ARGS_TYPE_1> type1, Class<ARGS_TYPE_2> type2) throws ArgsException {
		RecordPackager<Parsed2<ARGS_TYPE_1, ARGS_TYPE_2>> packager = types
				-> new Parsed2<>((ARGS_TYPE_1) types.get(type1), (ARGS_TYPE_2) types.get(type2));
		return parse(argStrings, packager, type1, type2);
	}

	/**
	 * Parses the specified string array to create instances of the specified types.
	 */
	@SuppressWarnings("unchecked")
	public static <ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3> Parsed3<ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3> parse(
			String[] argStrings, Class<ARGS_TYPE_1> type1, Class<ARGS_TYPE_2> type2, Class<ARGS_TYPE_3> type3) throws ArgsException {
		RecordPackager<Parsed3<ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3>> packager = types
				-> new Parsed3<>((ARGS_TYPE_1) types.get(type1), (ARGS_TYPE_2) types.get(type2), (ARGS_TYPE_3) types.get(type3));
		return parse(argStrings, packager, type1, type2, type3);
	}

	private static <T> T parse(String[] argStrings, RecordPackager<T> packager, Class<?>... types)
			throws ArgsException {
		var argsAndTypes = determineArgsAndTypes(argStrings, types);
		var args = inferArgs(argsAndTypes.types());
		var messages = ArgsParser
				.forArgs(args.all().toList())
				.parse(argsAndTypes.argsStrings());
		throwOnErrors(messages.errors(), messages.warnings(), argStrings, List.of(types));

		var constructorArguments = prepareConstructions(args);
		var constructorErrors = constructorArguments.stream()
						.flatMap(constrArg -> constrArg.errors.stream())
						.toList();
		throwOnErrors(constructorErrors, List.of(), argStrings, List.of(types));

		var records = constructArgTypes(argStrings, constructorArguments);
		return packager.apply(records);
	}

	private static ArgsAndTypes determineArgsAndTypes(String[] argStrings, Class<?>[] types) {
		@SuppressWarnings("unchecked")
		var recordTypes = stream(types)
				.<Class<? extends Record>> map(type -> {
					if (type.isRecord())
						return (Class<? extends Record>) type;
					else
						throw new IllegalArgumentException("Types must be records, but '%s' isn't.".formatted(type));
				})
				.toList();
		return new ArgsAndTypes(List.of(argStrings), recordTypes);
	}

	private static InferredArgs inferArgs(List<Class<? extends Record>> types) {
		Map<Class<? extends Record>, List<Arg<?>>> argsByType = types.stream()
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
			String[] argStringsForError, List<ConstructorArguments> constructors) throws ArgsException {
		var argTypes = new HashMap<Class<? extends Record>, Record>();
		for (var constr : constructors) {
			var argType = constructArgType(argStringsForError, constr.argType(), constr.parameters(), constr.arguments());
			argTypes.put(constr.argType(), argType);
		}
		return argTypes;
	}

	private static <T extends Record> T constructArgType(
			String[] argStringsForError, Class<T> type, Class<?>[] parameters, Object[] arguments) throws ArgsException {
		try {
			Constructor<T> canonicalConstructor = type.getDeclaredConstructor(parameters);
			canonicalConstructor.setAccessible(true);
			return canonicalConstructor.newInstance(arguments);
		} catch (IllegalAccessException ex) {
			String message = "Make sure Args has reflective access to the argument record, e.g. with an `opens ... to ...` directive.";
			throw new ArgsException(argStringsForError, type, List.of(new ArgsMessage(ILLEGAL_ACCESS, message)), ex);
		} catch (ReflectiveOperationException ex) {
			String message = "There was an unexpected error while creating the argument record.";
			throw new ArgsException(argStringsForError, type, List.of(new ArgsMessage(ILLEGAL_ACCESS, message)), ex);
		}
	}

	private static void throwOnErrors(
			List<ArgsMessage> errors, List<ArgsMessage> warnings, String[] argStringsForError, List<Class<?>> typesForError) throws ArgsException {
		if (errors.isEmpty() && warnings.isEmpty())
			return;

		List<ArgsMessage> messages = new ArrayList<>(errors);
		messages.addAll(warnings);
		throw new ArgsException(argStringsForError, typesForError, messages);
	}

	/*
	 * INNER TYPES
	 */

	private interface RecordPackager<T> extends Function<Map<Class<? extends Record>, Record>, T> { }

	private record ArgsAndTypes(List<String> argsStrings, List<Class<? extends Record>> types) { }

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
