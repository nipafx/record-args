package dev.nipafx.args;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class Records {

	record None() { }

	record WithString(String stringArg) implements Mode, SubtypesWithOverlappingComponents { }
	record WithPath(Path pathArg) implements Type { }
	record WithInteger(int intArg) implements Action { }
	record WithLong(long longArg) { }
	record WithFloat(float floatArg) { }
	record WithDouble(double doubleArg) { }
	record WithBoolean(boolean booleanArg) { }

	record WithOptional(Optional<String> optionalArg) implements Action { }
	record WithList(List<String> stringArgs) implements Mode { }
	record WithListAndMore(List<String> stringArgs, boolean booleanArg) { }
	record WithMap(Map<Integer, String> mapArgs) implements Type { }
	record WithMapAndMore(Map<Integer, String> mapArgs, boolean booleanArg) { }

	record WithConstructorException() {

		WithConstructorException {
			throw new IllegalArgumentException();
		}

	}

	record WithInitializerException() {

		static {
			//noinspection ConstantValue
			if (true)
				throw new IllegalArgumentException();
		}

	}

	record WithStringArray(String[] stringsArg) { }
	record WithMany(
			String stringArg, Optional<Path> pathArg,
			int intArg, float floatArg, boolean booleanArg,
			List<Integer> numberArgs) { }

	static class Class { }
	interface Interface { }

	sealed interface Mode permits WithString, WithList { }
	sealed interface Type permits WithPath, WithMap { }
	sealed interface Action permits WithInteger, WithOptional { }

	sealed interface SubtypesWithOverlappingComponents permits WithString, AnotherWithString { }
	record AnotherWithString(String stringArg) implements SubtypesWithOverlappingComponents { }
}
