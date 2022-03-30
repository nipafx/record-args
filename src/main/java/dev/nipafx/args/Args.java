package dev.nipafx.args;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

import static dev.nipafx.args.ArgsCode.ILLEGAL_ACCESS;
import static dev.nipafx.args.ArgsCode.MISSING_ARGUMENT;
import static java.util.Arrays.stream;

/**
 * Handles parsing the command-line arguments - call {@link Args#parse(String[], Class) parse}.
 */
public class Args {

	/**
	 * Parses the specified string array to create an instance of the specified type.
	 */
	public static <T extends Record> T parse(String[] argStrings, Class<T> type) throws ArgsException {
		List<Arg<?>> args = inferArgs(type);
		var parsedArgs = ArgsParser.forArgs(args).parse(argStrings);
		throwOnErrors(argStrings, type, parsedArgs.errors(), parsedArgs.warnings());

		var constructorArguments = prepareConstruction(parsedArgs.args());
		throwOnErrors(argStrings, type, constructorArguments.errors(), List.of());

		return constructArgType(argStrings, type, constructorArguments.parameters(), constructorArguments.arguments());
	}

	private static <T extends Record> List<Arg<?>> inferArgs(Class<T> type) {
		return stream(type.getRecordComponents())
				.<Arg<?>> map(Args::readComponent)
				.toList();
	}

	private static Arg<?> readComponent(RecordComponent component) {
		return new Arg<>(component.getName(), component.getType());
	}

	private record ConstructorArguments(Class<?>[] parameters, Object[] arguments, List<ArgsMessage> errors) { }

	private static ConstructorArguments prepareConstruction(List<Arg<?>> args) {
		List<ArgsMessage> errors = new ArrayList<>();
		Class<?>[] parameters = args.stream()
				.map(Arg::type)
				.toArray(Class<?>[]::new);
		Object[] arguments = args.stream()
				.peek(arg -> {
					if (arg.value().isEmpty()) {
						String message = "No value for required argument '%s'.".formatted(arg.name());
						errors.add(new ArgsMessage(MISSING_ARGUMENT, message));
					}
				})
				.map(arg -> arg.value().orElse(null))
				.toArray(Object[]::new);
		return new ConstructorArguments(parameters, arguments, errors);
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

	private static <T extends Record> void throwOnErrors(
			String[] argStrings, Class<T> type, List<ArgsMessage> errors, List<ArgsMessage> warnings) throws ArgsException {
		if (errors.isEmpty() && warnings.isEmpty())
			return;

		List<ArgsMessage> messages = new ArrayList<>(errors);
		messages.addAll(warnings);
		throw new ArgsException(argStrings, type, messages);
	}

}
