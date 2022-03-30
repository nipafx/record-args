package dev.nipafx.args;

import java.util.Optional;
import java.util.Set;

import static dev.nipafx.args.Check.nonNull;

final class Arg<T> {

	private final static Set<Class<?>> SUPPORTED_TYPES = Set.of(
			String.class,
			Integer.class, int.class, Long.class, long.class,
			Float.class, float.class, Double.class, double.class,
			Boolean.class, boolean.class);

	private final String name;
	private final Class<T> type;
	private Optional<T> value;

	Arg(String name, Class<T> type) {
		this.name = nonNull(name);
		this.type = nonNull(type);
		if (!SUPPORTED_TYPES.contains(type)) {
			String message = "Argument type %s is not supported.".formatted(type.getSimpleName());
			throw new IllegalArgumentException(message);
		}
		this.value = Optional.empty();
	}

	public String name() {
		return name;
	}

	public Class<T> type() {
		return type;
	}

	public Optional<T> value() {
		return value;
	}

	/**
	 * @throws IllegalArgumentException if {@code value} couldn't be parsed to the argument's type
	 * @throws IllegalStateException if this Arg's {@link Arg#type()} is not supported
	 */
	public void setValue(String value) throws IllegalArgumentException, IllegalStateException {
		T parsed = (T) switch (type.getSimpleName()) {
			case "String" -> value;
			case "Integer", "int" -> Integer.parseInt(value);
			case "Long", "long" -> Long.parseLong(value);
			case "Float", "float" -> Float.parseFloat(value);
			case "Double", "double" -> Double.parseDouble(value);
			case "Boolean", "boolean" -> switch (value) {
				case "true" -> true;
				case "false" -> false;
				default -> throw new IllegalArgumentException("Only 'true' and 'false' allowed for boolean args.");
			};
			default -> throw new IllegalStateException("This case should've been discovered before... 🤔");
		};
		this.value = Optional.ofNullable(parsed);
	}

}
