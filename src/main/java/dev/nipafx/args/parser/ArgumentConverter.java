package dev.nipafx.args.parser;

@FunctionalInterface
public interface ArgumentConverter<T> {

	T convert(String value) throws Exception;

}
