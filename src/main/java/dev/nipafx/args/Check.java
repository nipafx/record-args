package dev.nipafx.args;

class Check {

	static <T> T internalErrorOnNull(T object) {
		if (object == null)
			throw new IllegalStateException("An argument was null when it was not supposed to.");
		return object;
	}

}
