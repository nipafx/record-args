package dev.nipafx.args;

import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nipafx.args.ArgsDefinitionErrorCode.DUPLICATE_ARGUMENT_DEFINITION;
import static dev.nipafx.args.ArgsDefinitionErrorCode.FAULTY_STATIC_INITIALIZER;
import static dev.nipafx.args.ArgsDefinitionErrorCode.ILLEGAL_ACCESS;
import static dev.nipafx.args.Check.internalErrorOnNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

/**
 * Parses command-line arguments to args records - call {@link Args#parse(String[], Class) parse}
 * or one of its overloads (depending on how many args types are involved).
 */
public class Args {

	private Args() {
		// private constructor to prevent initialization
	}

	/**
	 * Parses the specified string array to create an instance of the specified type.
	 *
	 * @param argStrings the string array to be parsed - usually {@code String[] args} as passed to {@code main}
	 * @param type the args type to be created - must be a record or a sealed interface with record implementations
	 * @return an instance of {@code type}, populated with values from {@code argStrings}
	 * @param <ARGS_TYPE> the args type to be created - must be a record or a sealed interface with record implementations
	 * @throws ArgsParseException when the specified argument array can't be correctly parsed
	 * @throws ArgsDefinitionException when the specified type is not a valid args type
	 * @throws IllegalArgumentException when an illegal argument was passed to {@code parse} (it was likely {@code null} as other cases are covered by other exceptions)
	 * @throws IllegalStateException when an unexpected internal state is encountered - this is likely a bug
	 */
	public static <ARGS_TYPE> ARGS_TYPE parse(
			String[] argStrings, Class<ARGS_TYPE> type) throws ArgsParseException {
		throwIfAnyIsNull(argStrings, type);
		RecordPackager<ARGS_TYPE> packager = types -> getFromInstanceMap(types, type);
		return parse(argStrings, packager, type);
	}

	/**
	 * Parses the specified string array to create instances of the specified types.
	 *
	 * @param argStrings the string array to be parsed - usually {@code String[] args} as passed to {@code main}
	 * @param type1 one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @param type2 one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @return a pair of {@code [type1, type2]}, populated with values from {@code argStrings}
	 * @param <ARGS_TYPE_1> one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @param <ARGS_TYPE_2> one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @throws ArgsParseException when the specified argument array can't be correctly parsed
	 * @throws ArgsDefinitionException when not all specified types are valid args types
	 * @throws IllegalArgumentException when an illegal argument was passed to {@code parse} (it was likely {@code null} as other cases are covered by other exceptions)
	 * @throws IllegalStateException when an unexpected internal state is encountered - this is likely a bug
	 */
	public static <ARGS_TYPE_1, ARGS_TYPE_2> Parsed2<ARGS_TYPE_1, ARGS_TYPE_2> parse(
			String[] argStrings, Class<ARGS_TYPE_1> type1, Class<ARGS_TYPE_2> type2) throws ArgsParseException {
		throwIfAnyIsNull(argStrings, type1, type2);
		RecordPackager<Parsed2<ARGS_TYPE_1, ARGS_TYPE_2>> packager = types -> new Parsed2<>(
				getFromInstanceMap(types, type1),
				getFromInstanceMap(types, type2));
		return parse(argStrings, packager, type1, type2);
	}

	/**
	 * Parses the specified string array to create instances of the specified types.
	 *
	 * @param argStrings the string array to be parsed - usually {@code String[] args} as passed to {@code main}
	 * @param type1 one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @param type2 one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @param type3 one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @return a triple of {@code [type1, type2, type3]}, populated with values from {@code argStrings}
	 * @param <ARGS_TYPE_1> one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @param <ARGS_TYPE_2> one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @param <ARGS_TYPE_3> one of the args types to be created - must be a record or a sealed interface with record implementations
	 * @throws ArgsParseException when the specified argument array can't be correctly parsed
	 * @throws ArgsDefinitionException when not all specified types are valid args types
	 * @throws IllegalArgumentException when an illegal argument was passed to {@code parse} (it was likely {@code null} as other cases are covered by other exceptions)
	 * @throws IllegalStateException when an unexpected internal state is encountered - this is likely a bug
	 */
	public static <ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3> Parsed3<ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3> parse(
			String[] argStrings, Class<ARGS_TYPE_1> type1, Class<ARGS_TYPE_2> type2, Class<ARGS_TYPE_3> type3) throws ArgsParseException {
		throwIfAnyIsNull(argStrings, type1, type2, type3);
		RecordPackager<Parsed3<ARGS_TYPE_1, ARGS_TYPE_2, ARGS_TYPE_3>> packager = types -> new Parsed3<>(
				getFromInstanceMap(types, type1),
				getFromInstanceMap(types, type2),
				getFromInstanceMap(types, type3));
		return parse(argStrings, packager, type1, type2, type3);
	}

	private static void throwIfAnyIsNull(String[] argStrings, Class<?>... types) {
		if (argStrings == null)
			throw new IllegalArgumentException("Argument array must not be null.");
		for (int i = 0; i < argStrings.length; i++)
			if (argStrings[i] == null)
				throw new IllegalArgumentException("Argument array must not contain null but does at position %s.".formatted(i));
		for (int i = 0; i < types.length; i++) {
			var indexWord = switch (i) {
				case 0 -> "first";
				case 1 -> "second";
				case 2 -> "third";
				default -> (i - 1) + "th";
			};
			if (types[i] == null)
				throw new IllegalArgumentException("Args type must not be null but %s was.".formatted(indexWord));
		}
	}

	private static <T> T parse(String[] argStrings, RecordPackager<T> packager, Class<?>... types) throws ArgsParseException {
		try {
			var argsAndTypes = new ArgsModeFilter().processModes(argStrings, types);
			throwOnErrors(argsAndTypes.errors());

			var args = inferArgs(argsAndTypes.types());
			var messages = ArgsParser
					.forArgs(args.all().toList())
					.parse(argsAndTypes.argsStrings());
			throwOnErrors(messages.errors(), messages.warnings());

			var constructorArguments = prepareConstructions(args);
			var constructorErrors = constructorArguments.stream()
					.flatMap(constrArg -> constrArg.errors.stream())
					.toList();
			throwOnErrors(constructorErrors);

			var constructions = constructArgTypes(constructorArguments);
			throwOnErrors(constructions.errors());

			return packager.apply(constructions.argInstances());
		} catch (InternalArgsException ex) {
			throw new ArgsParseException(argStrings, List.of(types), ex);
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
			throw new ArgsDefinitionException(DUPLICATE_ARGUMENT_DEFINITION, String.join("\n", errors));
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
						errors.add(new ArgsMessage.MissingArgument(arg.name()));
						return null;
					} else
						return arg.value().get();
				})
				.toArray(Object[]::new);
		return new ConstructorArguments(type, parameters, arguments, errors);
	}

	private static Constructions constructArgTypes(List<ConstructorArguments> constructors) {
		var argInstances = new HashMap<Class<? extends Record>, Record>();
		var errors = new ArrayList<ArgsMessage>();
		for (var constr : constructors) {
			var construction = constructArgType(constr.argType(), constr.parameters(), constr.arguments());
			construction.instance().ifPresent(argInstance -> argInstances.put(constr.argType(), argInstance));
			errors.addAll(construction.errors());
		}
		return new Constructions(argInstances, errors);
	}

	private static <T extends Record> Construction<T> constructArgType(Class<T> type, Class<?>[] parameters, Object[] arguments) {
		try {
			Constructor<T> canonicalConstructor = type.getDeclaredConstructor(parameters);
			canonicalConstructor.setAccessible(true);
			return Construction.successful(canonicalConstructor.newInstance(arguments));
		// errors that should've been avoided by RecordArgs (i.e. likely bugs)
		} catch (NoSuchMethodException ex) {
			var message = "The canonical constructor for %s could not be found - presumably it has these parameters: %s"
					.formatted(type, Arrays.toString(parameters));
			throw new IllegalStateException(message, ex);
		} catch (IllegalArgumentException ex) {
			var message = "Could not invoke the canonical constructor for %s with these arguments: %s"
					.formatted(type, Arrays.toString(arguments));
			throw new IllegalStateException(message, ex);
		} catch (InstantiationException ex) {
			var message = "Apparently, %s is abstract, which should've been caught earlier.".formatted(type);
			throw new IllegalStateException(message, ex);
		// errors that should've been avoided by the caller
		} catch (InaccessibleObjectException | IllegalAccessException ex) {
			var message = "Make sure Args has reflective access to the args record %s, e.g. with an `opens ... to ...` directive."
					.formatted(type);
			throw new ArgsDefinitionException(ILLEGAL_ACCESS, message, ex);
		} catch (ExceptionInInitializerError ex) {
			var message = "Invoking the constructor of %s caused an exception in the static initializer.".formatted(type);
			throw new ArgsDefinitionException(FAULTY_STATIC_INITIALIZER, message, ex);
		// errors from faulty arguments
		} catch (InvocationTargetException ex) {
			return Construction.failed(new ArgsMessage.FailedConstruction(ex.getTargetException()));
		}
	}

	private static void throwOnErrors(List<ArgsMessage> errors) {
		throwOnErrors(errors, List.of());
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

	private record Construction<T extends Record>(Optional<T> instance, List<ArgsMessage> errors) {

		private Construction {
			internalErrorOnNull(instance);
			errors = List.copyOf(internalErrorOnNull(errors));
			if (instance.isEmpty() && errors.isEmpty())
				throw new IllegalStateException("Construction yielded neither instance nor errors.");
		}

		public static <T extends Record> Construction<T> successful(T instance) {
			return new Construction<>(Optional.of(internalErrorOnNull(instance)), List.of());
		}

		public static <T extends Record> Construction<T> failed(ArgsMessage... errors) {
			return new Construction<>(Optional.empty(), List.of(errors));
		}

	}

	private record Constructions(Map<Class<? extends Record>, Record> argInstances, List<ArgsMessage> errors) {

		Constructions {
			argInstances = Map.copyOf(internalErrorOnNull(argInstances));
			errors = List.copyOf(internalErrorOnNull(errors));
		}

	}

}
