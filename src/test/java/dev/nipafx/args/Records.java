package dev.nipafx.args;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class Records {

	record None() { }

	record WithString(String stringArg) { }
	record WithPath(Path pathArg) { }
	record WithInteger(int intArg) { }
	record WithLong(long longArg) { }
	record WithFloat(float floatArg) { }
	record WithDouble(double doubleArg) { }
	record WithBoolean(boolean booleanArg) { }

	record WithOptional(Optional<String> optionalArg) { }
	record WithList(List<String> stringArgs) { }
	record WithListAndMore(List<String> stringArgs, boolean booleanArg) { }
	record WithMap(Map<Integer, String> mapArgs) { }
	record WithMapAndMore(Map<Integer, String> mapArgs, boolean booleanArg) { }

	record WithStringArray(String[] stringsArg) { }
	record WithMany(
			String stringArg, Optional<Path> pathArg,
			int intArg, float floatArg, boolean booleanArg,
			List<Integer> numberArgs) { }

	static class Class { }

}
