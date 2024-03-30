package dev.nipafx.args;

import java.util.Collection;

class Check {

	static <T> T internalErrorOnNull(T object) {
		if (object == null)
			throw new IllegalStateException("An argument was null when it was not supposed to.");
		return object;
	}

	static String internalErrorOnNullOrBlank(String string) {
		if (string == null || string.isBlank())
			throw new IllegalStateException("A string argument was null or blank when it was not supposed to.");
		return string;
	}

	static <E, T extends Collection<E>> T internalErrorOnNullOrEmpty(T collection) {
		if (collection == null || collection.isEmpty())
			throw new IllegalStateException("A collection was null or empty when it was not supposed to.");
		return collection;
	}

}
