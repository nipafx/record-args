# RecordArgs

A simple command-line argument parser for Java applications that relies on records and sealed interfaces.
Specifically, it uses record component names to parse command line arguments, their canonical constructors to create instances, and their immutability to let you freely pass them around without fear of unwanted changes.
It uses sealed interfaces to model mutually exclusive sets of arguments, so-called "modes".

## Getting started

1. Create a record like this one (called an _args record_):
	```java
	record ServerArgs(String url, int port) { }
	```
2. Make the record public and export its package or keep it encapsulated and open the package (optionally just to _dev.nipafx.args_).
3. Pass command-line arguments in this form:
	```
	java [...] --url localhost --port 8080
	```
4. Call `Args::parse`:
	```java
	public static void main(String[] args) throws ArgsParseException {
		ServerArgs serverArgs = Args.parse(args, ServerArgs.class);
	}
   ```

In most cases, the passed arguments must alternate between an argument's name (prefixed by `--`) and its value but the order of these pairs can be arbitrary.
A value must be defined for all arguments that aren't of a container type (see below).

## Argument names

The args record's component names define the argument names.
So for a record like the following ...

```java
record ServerArgs(String url, int port) { }
```

... the arguments `--url` and `--port` are parsed.

## Simple arguments

An argument name must in most cases be followed by exactly one value that can be parsed to the argument's type.
Supported simple types are:

* `String`, `Path`
* `Integer`, `int`, `Long`, `long`
* `Float`, `float`, `Double`, `double`
* `Boolean`, `boolean` (only values "true" and "false")

### Boolean arguments

The boolean types are an exception to the rule that an argument name must always be followed by a type.
If no value is given after the argument, `true` is assumed.

For example, in this situation ...

```java
record ServerArgs(String url, boolean createLog) { }
```

... the following arguments could be parsed ...

```
java [...] --url localhost:8080 --createLog
java [...] --createLog --url localhost:8080
```

... and `serverArgs.createLog()` would return `true`.

## Container arguments

Beyond that, the following container types are supported:

* `Optional<VALUE>`, where `VALUE` is any of the simple types above  (`OptionalInt`, `OptionalLong`, `OptionalDouble` aren't supported, use `Optional<Integer>` etc. instead)
* `List<VALUE>`, where `VALUE` is any of the simple types above
* `Map<KEY, VALUE>`, where `KEY` and `VALUE` are any of the simple types above

Container types are always optional.

### Optional arguments

Arguments of type `Optional` are optional (talk about good naming!).
Given the following command ...

```
java [...] --url localhost
```

... this args record would lead to an exception during parsing because `port` wasn't defined:

```java
record ServerArgs(String url, int port) { }
```

To allow `port` to be optional, use `Optional`:

```java
record ServerArgs(String url, Optional<Integer> port) { }
```

### List arguments

Arguments of type `List` accept one or more arguments.
If not mentioned, they are empty, which makes them optional as well.
That means for the following args record ...

```java
record ServerArgs(List<String> urls, boolean createLog) { }
```

... any of the following command lines are acceptable:

```
java [...] --createLog
java [...] --createLog --urls localhost
java [...] --createLog --urls localhost 127.0.0.1
java [...] --urls localhost 127.0.0.1 --createLog
```

While just mentioning a list argument without providing a value ...

```
java [...] --urls --createLog
```

... could be parsed to the empty list, this non-sensical command is instead interpreted as a mistake and leads to an exception.

List instances are unmodifiable, just like those created with `List::of` and `List::copyOf`.

### Map arguments

Arguments of type `Map` accept one or more key-value pair of the form `key=value`.
If not mentioned, they are empty, which makes them optional as well.
That means for the following args record ...

```java
record ServerArgs(Map<Integer, String> numbers, boolean createLog) { }
```

... any of the following command lines are acceptable:

```
java [...] --createLog
java [...] --createLog --numbers 1=one
java [...] --createLog --numbers 1=one 2=two 3=three
java [...] --numbers 1=one 2=two 3=three --createLog
```

While just mentioning a map argument without providing a value ...

```
java [...] --numbers --createLog
```

... could be parsed to the empty map, this non-sensical command is instead interpreted as a mistake and leads to an exception.

Map instances are unmodifiable, just like those created with `Map::of`, `Map::ofEntries`, and `Map::copyOf`.

## Parsing multiple args records

It is possible to parse command line arguments to up to three args records with overloads of `Args::parse`.
These overloads return instances of `Parsed2` or `Parsed3` that have accessors `first()`, `second()`, and maybe `third()` to access the parsed args record instances:

```java
// args records
record LogArgs(int logLevel) { }
record ServerArgs(String url, int port) { }

// parsing arguments
public static void main(String[] args) throws ArgsParseException {
	Parsed2 parsed = Args.parse(args, LogArgs.class, ServerArgs.class);
	LogArgs logArgs = parsed.first();
	ServerArgs serverArgs = parsed.second();
}
```

The records must not have components of the same name or `Args::parse` throws an `IllegalArgumentException`.

##  Parsing mutually exclusive arguments with "modes" and "actions"

If an application provides diverse features that take distinct execution paths, it might need argument sets for each path that have little to no overlap.
Instead of parsing the arguments to one large or several small args record and then dealing with most arguments being absent, consider using "modes" or an "action".

### Modes

A _mode_ is a sealed interface that permits only record implementations:

```java
sealed interface Mode permits Client, Server { }
record Client(int port) implements Mode { }
record Server(String url, int port) implements Mode { }
```

When such an interface is passed to `parse` an argument with its name and a value that is one of the implementing records' names (always first letter in lower case, e.g. `--mode client`, and without a potential `Args` suffix - more on that below) is used to determine which args record to fill and instantiate (this is called _mode selection_).
That means the command line ...

```
java [...] --mode client --port 8080
```

... is best handled as follows:

```java
public static void main(String[] args) throws ArgsParseException{
	var arguments = Args.parse(args,Mode.class);

	switch(arguments) {
		// this path is taken
		case Client config -> spawnClient(config);
		case Server config -> spawnServer(config);
	}
}
```

The non-selected args records are ignored and no values are expected or allowed for them.
This also means that, as in the example above, alternative args records can have components with the same name.

Just as with all other values, mode selection can happen anywhere within the `args` array.
And it is possible to parse multiple modes as well as modes mixed with regular args records, e.g.:

```java
sealed interface Mode permits Client, Server { }
record Client(int port) implements Mode { }
record Server(String url, int port) implements Mode { }
record LogArgs(int logLevel) { }

// java [...] --port 8080 --logLevel 3 --mode server --url localhost
public static void main(String[] args) throws ArgsParseException {
	var arguments = Args.parse(args, Mode.class, LogArgs.class);

	Logging.configure(arguments.second());
	switch(arguments.first()) {
		case Client config -> spawnClient(config);
		// this path is taken
		case Server config -> spawnServer(config);
	}
}
```

When exclusively dealing with args records, the names of the arguments are determined solely by component names.
But as described above, when using modes (and/or actions - see below), the _type_ names determine some argument names and values.
To offer a degree of freedom when finding names that suit the types within the application as well as the argument on the command line, a type name suffix of `Args` is ignored and hence not included in the argument created for the type:

```java
// maps to `--mode`
sealed interface ModeArgs permits Client, Server { }
// maps to `client`
record ClientArgs(int port) implements Mode { }
// maps to `server`
record ServerArgs(String url, int port) implements Mode { }

// java [...] --mode server --port 8080 --url localhost
public static void main(String[] args) throws ArgsParseException {
	// successfully parsed
	var arguments = Args.parse(args, ModeArgs.class);
}
```

### Actions

An _action_ is a special mode where the selection is not done by a pair of arguments (e.g. `... --mode client ...`) but by having just the value as the first argument in the array (e.g. `client ...`).
This interpretation is automatically and exclusively applied to interfaces with the simple name `Action` or `ActionArgs`, e.g.:

```java
sealed interface Action permits Create, Copy, Move { }
record Create(Path path) implements Action { }
record Copy(Path from, Path to) implements Action { }
record Move(Path from, Path to) implements Action { }

// java [...] copy --from ... --to ...
public static void main(String[] args) throws ArgsParseException {
	var arguments = Args.parse(args, Action.class);

	switch(arguments) {
		case Create args -> create(args);
		// this path is taken
		case Copy args -> copy(args);
		case Move args -> move(args);
	}
}
```
