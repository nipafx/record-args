package dev.nipafx.args;

class Check {

	static <T> T nonNull(T object) {
		if (object == null)
			throw new IllegalArgumentException("Argument should not have been null.");
		return object;
	}

}
