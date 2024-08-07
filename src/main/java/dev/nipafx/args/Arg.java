package dev.nipafx.args;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static dev.nipafx.args.ArgsDefinitionErrorCode.UNSUPPORTED_ARGUMENT_TYPE;
import static dev.nipafx.args.Check.internalErrorOnNull;
import static dev.nipafx.args.Check.internalErrorOnNullOrBlank;

/**
 * An argument as defined by an args record component, possibly holding a value.
 *
 * <p>Two {@code Arg} instances are {@link Object#equals(Object) equal} if they have the same name.</p>
 *
 * @param <T> the type of the component
 */
sealed interface Arg<T> {

	/**
	 * @throws IllegalArgumentException if the argument {@code type} is not supported
	 */
	static <T> Arg<T> of(String name, Type type) throws IllegalArgumentException {
		return AbstractArg.of(name, type);
	}

	String name();

	Class<T> type();

	Optional<T> value();

	/**
	 * @throws IllegalArgumentException if {@code value} couldn't be parsed to the argument's type
	 */
	void setValue(String value) throws IllegalArgumentException;

}

abstract class AbstractArg<T> {

	private static final Set<Class<?>> SUPPORTED_TYPES = Set.of(
			String.class, Path.class,
			Integer.class, int.class, Long.class, long.class,
			Float.class, float.class, Double.class, double.class,
			Boolean.class, boolean.class);

	private final String name;
	private final Class<T> type;

	protected AbstractArg(String name, Class<T> type) {
		this.name = internalErrorOnNullOrBlank(name);
		this.type = internalErrorOnNull(type);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T> Arg<T> of(String name, Type type) {
		return switch (type) {
			case Class classType -> new SimpleArg<>(name, assertSupported((Class<T>) classType));
			case ParameterizedType paramType -> switch (paramType.getRawType().getTypeName()) {
				case "java.util.Optional" -> {
					var valueType = paramType.getActualTypeArguments()[0];
					if (valueType instanceof Class classType)
						yield new OptionalArg<>(name, assertSupported(classType));
					else
						throw unexpectedArgumentException(type);
				}
				case "java.util.List" -> {
					var valueType = paramType.getActualTypeArguments()[0];
					if (valueType instanceof Class classType)
						yield new ListArg<>(name, assertSupported(classType));
					else
						throw unexpectedArgumentException(type);
				}
				case "java.util.Map" -> {
					var keyType = paramType.getActualTypeArguments()[0];
					var valueType = paramType.getActualTypeArguments()[1];
					if (keyType instanceof Class keyClass && valueType instanceof Class valueClass)
						yield new MapArg<>(name, assertSupported(keyClass), assertSupported(valueClass));
					else
						throw unexpectedArgumentException(type);
				}
				default -> throw unexpectedArgumentException(type);
			};
			case null, default -> throw unexpectedArgumentException(type);
		};
	}

	private static IllegalArgumentException unexpectedArgumentException(Type type) {
		String message = "Unexpected argument type '%s'.".formatted(type);
		return new IllegalArgumentException(message);
	}

	private static <T> Class<T> assertSupported(Class<T> type) {
		if (!SUPPORTED_TYPES.contains(type)) {
			String message = "Argument type %s is not supported.".formatted(type.getSimpleName());
			throw new ArgsDefinitionException(UNSUPPORTED_ARGUMENT_TYPE, message);
		}
		return type;
	}

	@SuppressWarnings("unchecked")
	protected static <T> T parseValueToType(String value, Class<T> type) {
		return (T) switch (type.getSimpleName()) {
			case "String" -> value;
			case "Path" -> Path.of(value);
			case "Integer", "int" -> Integer.parseInt(value);
			case "Long", "long" -> Long.parseLong(value);
			case "Float", "float" -> Float.parseFloat(value);
			case "Double", "double" -> Double.parseDouble(value);
			case "Boolean", "boolean" -> switch (value) {
				case "true" -> true;
				case "false" -> false;
				default -> throw new IllegalArgumentException("Only 'true' and 'false' allowed for boolean args.");
			};
			default -> {
				var message = "The unsupported argument type %s (with value '%s') was encountered after those should already have been detected.";
				throw new IllegalStateException(message.formatted(type, value));
			}
		};
	}

	public String name() {
		return name;
	}

	public Class<T> type() {
		return type;
	}

	@Override
	public final boolean equals(Object other) {
		return this == other
				|| other instanceof Arg<?> arg && name.equals(arg.name());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(name);
	}

}

final class SimpleArg<T> extends AbstractArg<T> implements Arg<T> {

	private Optional<T> value;

	SimpleArg(String name, Class<T> type) {
		super(name, type);
		this.value = Optional.empty();
	}

	public void setValue(String value) throws IllegalArgumentException {
		this.value = Optional.of(parseValueToType(value, type()));
	}

	@Override
	public Optional<T> value() {
		return value;
	}

}

@SuppressWarnings("rawtypes")
final class OptionalArg<T> extends AbstractArg<Optional> implements Arg<Optional> {

	private final Class<T> valueType;
	private Optional<T> value;

	OptionalArg(String name, Class<T> valueType) {
		super(name, Optional.class);
		this.valueType = valueType;
		this.value = Optional.empty();
	}

	public void setValue(String value) throws IllegalArgumentException {
		this.value = Optional.of(parseValueToType(value, valueType));
	}

	@Override
	public Optional<Optional> value() {
		return Optional.of(value);
	}

	public Class<T> valueType() {
		return valueType;
	}

}

@SuppressWarnings("rawtypes")
final class ListArg<T> extends AbstractArg<List> implements Arg<List> {

	private final Class<T> valueType;
	private final List<T> values;

	ListArg(String name, Class<T> valueType) {
		super(name, List.class);
		this.valueType = valueType;
		this.values = new ArrayList<>();
	}

	public void setValue(String value) throws IllegalArgumentException {
		this.values.add(parseValueToType(value, valueType));
	}

	@Override
	public Optional<List> value() {
		return Optional.of(List.copyOf(values));
	}

}

@SuppressWarnings("rawtypes")
final class MapArg<K, V> extends AbstractArg<Map> implements Arg<Map> {

	private final Class<K> keyType;
	private final Class<V> valueType;
	private final Map<K, V> values;

	MapArg(String name, Class<K> keyType, Class<V> valueType) {
		super(name, Map.class);
		this.keyType = keyType;
		this.valueType = valueType;
		this.values = new HashMap<>();
	}

	public void setValue(String keyValue) throws IllegalArgumentException {
		var pair = keyValue.split("=");
		if (pair.length == 1) {
			String message = "Map argument '%s' is no valid 'key=value' pair - it has no value.".formatted(keyValue);
			throw new IllegalArgumentException(message);
		} else if (pair.length > 2) {
			String message = "Map argument '%s' is no valid 'key=value' pair - it has more than one equal sign.".formatted(keyValue);
			throw new IllegalArgumentException(message);
		}
		var key = parseValueToType(pair[0], keyType);
		var value = parseValueToType(pair[1], valueType);
		this.values.put(key, value);
	}

	@Override
	public Optional<Map> value() {
		return Optional.of(Map.copyOf(values));
	}

}
