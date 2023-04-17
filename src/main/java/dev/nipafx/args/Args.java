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

import static dev.nipafx.args.ArgsCode.MISSING_ARGUMENT;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

/**
 * Parses command-line arguments to args records - call {@link Args#parse(String[], Class) parse}
 * or one of its overloads (depending on how many args types are involved).
 */
public class Args {

	/**
	 * Parses the specified string array to create an instance of the specified type.
	 */
	public static <ARGS_TYPE> ARGS_TYPE parse(
			String[] argStrings, Class<ARGS_TYPE> type) throws ArgsException {
		RecordPackager<ARGS_TYPE> packager = types -> getFromInstanceMap(types, type);
		return parse(argStrings, packager, type);
	}

	/**
	 * Parses the specified string array to create instances of the specified types.
	 */
	public static <ARGS_TYPE_1, ARGS_TYPE_2> Parsed2<ARGS_TYPE_1, ARGS_TYPE_2> parse(
			String[] argStrings, Class<ARGS_TYPE_1> type1, Class<ARGS_TYPE_2> type2) throws ArgsException {
		RecordPackager<Parsed2<ARGS_TYPE_1, ARGS_TYPE_2>> packager = types -> new Parsed2<>(
				getFromInstanceMap(types, type1),
				getFromInstanceMap(types, type2));
		return parse(argStrings, packager, type1, type2);
	}

	/**
	 * Parses the specified string array to create instances of the specified types.
	 */
	public static <ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3> Parsed3<ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3> parse(
			String[] argStrings, Class<ARGS_TYPE_1> type1, Class<ARGS_TYPE_2> type2, Class<ARGS_TYPE_3> type3) throws ArgsException {
		RecordPackager<Parsed3<ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3>> packager = types -> new Parsed3<>(
				getFromInstanceMap(types, type1),
				getFromInstanceMap(types, type2),
				getFromInstanceMap(types, type3));
		return parse(argStrings, packager, type1, type2, type3);
	}

	private static <T> T parse(String[] argStrings, RecordPackager<T> packager, Class<?>... types) throws ArgsException {
		try {
			var argsAndTypes = new ArgsFilter().processModes(argStrings, types);
			throwOnErrors(argsAndTypes.errors(), List.of());

			var args = inferArgs(argsAndTypes.types());
			var messages = ArgsParser
					.forArgs(args.all().toList())
					.parse(argsAndTypes.argsStrings());
			throwOnErrors(messages.errors(), messages.warnings());

			var constructorArguments = prepareConstructions(args);
			var constructorErrors = constructorArguments.stream()
					.flatMap(constrArg -> constrArg.errors.stream())
					.toList();
			throwOnErrors(constructorErrors, List.of());

			var records = constructArgTypes(constructorArguments);
			return packager.apply(records);
		} catch (InternalArgsException ex) {
			throw new ArgsException(argStrings, List.of(types), ex);
		}
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

	private static Map<Class<? extends Record>, Record> constructArgTypes(List<ConstructorArguments> constructors) {
		var argTypes = new HashMap<Class<? extends Record>, Record>();
		for (var constr : constructors) {
			var argType = constructArgType(constr.argType(), constr.parameters(), constr.arguments());
			argTypes.put(constr.argType(), argType);
		}
		return argTypes;
	}

	private static <T extends Record> T constructArgType(Class<T> type, Class<?>[] parameters, Object[] arguments) {
		try {
			Constructor<T> canonicalConstructor = type.getDeclaredConstructor(parameters);
			canonicalConstructor.setAccessible(true);
			return canonicalConstructor.newInstance(arguments);
		} catch (IllegalAccessException ex) {
			String message = "Make sure Args has reflective access to the argument record, e.g. with an `opens ... to ...` directive.";
			throw new IllegalStateException(message, ex);
		} catch (ReflectiveOperationException ex) {
			String message = "There was an unexpected error while creating the argument record.";
			throw new IllegalStateException(message, ex);
		}
	}

	private static void throwOnErrors(List<ArgsMessage> errors, List<ArgsMessage> warnings) {
		if (errors.isEmpty() && warnings.isEmpty())
			return;

		List<ArgsMessage> messages = new ArrayList<>(errors);
		messages.addAll(warnings);
		throw new InternalArgsException(messages);
	}

	@SuppressWarnings("unchecked")
	private static <ARGS_TYPE> ARGS_TYPE getFromInstanceMap(Map<Class<? extends Record>, Record> instanceMap, Class<ARGS_TYPE> type) {
		if (instanceMap.containsKey(type))
			return (ARGS_TYPE) instanceMap.get(type);

		//noinspection SuspiciousMethodCalls
		return stream(type.getPermittedSubclasses())
				.filter(instanceMap::containsKey)
				.map(instanceMap::get)
				.map(instance -> (ARGS_TYPE) instance)
				.findAny()
				.orElseThrow(() -> new IllegalStateException("There should've been an instance of a subtype of '%s'. 🤔".formatted(type)));
	}

	/*
	 * INNER TYPES
	 */

	private interface RecordPackager<T> extends Function<Map<Class<? extends Record>, Record>, T> { }

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
