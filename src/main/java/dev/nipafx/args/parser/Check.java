package dev.nipafx.args.parser;

import java.util.Collection;

class Check {

	static <T> T nonNull(String name, T object) {
		if (object == null)
			throw new IllegalArgumentException("Parameter '" + name + "' must not be null.");
		return object;
	}

	static String nonNullNorBlank(String name, String string) {
		nonNull(name, string);
		if (string.isBlank())
			throw new IllegalArgumentException("Parameter '" + name + "' must not be blank");
		return string;
	}

	static <T> Collection<T> nonNullNorEmpty(String name, Collection<T> collection) {
		nonNull(name, collection);
		if (collection.isEmpty())
			throw new IllegalArgumentException("Parameter '" + name + "' must not be empty.");
		return collection;
	}

	static <T> Collection<T> nonNullNorContainsNull(String name, Collection<T> collection) {
		nonNull(name, collection);
		for (T element : collection)
			if (element == null)
				throw new IllegalArgumentException("Parameter '" + name + "' must not contain null: " + collection);
		return collection;
	}

}
