package dev.nipafx.args.parser;

import java.util.List;

public class Aliases {

	private final List<String> names;

	private Aliases(List<String> names) {
		this.names = List.copyOf(names);
	}

	public static Aliases withShortName(String shortName) {
		checkShortName(shortName);
		return new Aliases(List.of("-" + shortName));
	}

	public static Aliases withLongName(String longName) {
		checkLongName(longName);
		return new Aliases(List.of("--" + longName));
	}

	public static Aliases withNames(String longName, String shortName) {
		checkLongName(longName);
		checkShortName(shortName);
		return new Aliases(List.of("--" + longName, "-" + shortName));
	}

	private static void checkLongName(String longName) {
		Check.nonNullNorBlank("longName", longName);
	}

	private static void checkShortName(String shortName) {
		Check.nonNullNorBlank("shortName", shortName);
		if (shortName.length() > 1)
			throw new IllegalArgumentException("Short form aliases must be of length 1: " + shortName);
	}

	public boolean matches(String name) {
		return names.stream().anyMatch(name::equals);
	}

}
