# RecordArgs

RecordArgs is a simple command-line argument parser for Java applications that relies on records and sealed interfaces:

```java
record ServerArgs(String url, int port) { }
	
// launch command: "java [...] --url localhost --port 8080"
public static void main(String[] args) throws ArgsParseException {
	ServerArgs serverArgs = Args.parse(args, ServerArgs.class);
}
```

RecordArgs uses records' component names to identify command line arguments, their canonical constructors to create instances, and their immutability to let you freely pass them around without fear of unwanted changes.
It uses sealed interfaces to model mutually exclusive sets of arguments, so-called "modes" or "actions".

* [Getting started](#getting-started)
* [Arguments](#arguments)
	* [Argument names](#argument-names)
	* [Simple arguments](#simple-arguments)
		* [Boolean arguments](#boolean-arguments)
	* [Container arguments](#container-arguments)
		* [Optional arguments](#optional-arguments)
		* [List arguments](#list-arguments)
      * [Map arguments](#map-arguments)
* [Args records](#args-records)
	* [Validation](#validation)
	* [Parsing multiple args records](#parsing-multiple-args-records)
* [Args interfaces and mutually exclusive arguments](#args-interfaces-and-mutually-exclusive-arguments)
	* [Modes](#modes)
		* [Branching execution](#branching-execution)
		* [Overlapping components, order, multiple args records](#overlapping-components--order--multiple-args-records)
		* [Ignoring `…Args`](#ignoring---args-)
	* [Actions](#actions)
* [Error Handling](#error-handling)

## Getting started

0. Use your favorite build tool to pull RecordArgs in:
	* group ID: `dev.nipafx.args`
	* artifact ID: `record-args`
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

In most cases, the passed arguments must alternate between an argument's name (prefixed by `--`) and its value (e.g. `--port 8080`) but the order of these pairs can be arbitrary (e.g. first `--url localhost` then `--port 8080` or the other way around).

A value must be defined for all arguments that aren't of a container type (see below), so for the args record…

```java
record ServerArgs(String url, int port) { }
```

…the following command would lead to an exception because `port` has no value:

```
java [...] --url localhost
```

## Arguments

### Argument names

The args record's component names define the argument names.
So for a record like the following…

```java
record ServerArgs(String url, int port) { }
```

…the arguments `--url` and `--port` are parsed.

Camel-cased component names are interpreted as is, i.e. they are _not_ kebap-cased.
So a component `remoteUrl` would be mapped to the argument name `--remoteUrl`, not `--remote-url`. 

### Simple arguments

An argument name must in most cases be followed by exactly one value that can be parsed to the argument's type.
Supported simple types are:

* `String`, `Path`
* `Integer`, `int`, `Long`, `long`, `Float`, `float`, `Double`, `double`
* `Boolean`, `boolean` (only values "true" and "false")

#### Boolean arguments

The boolean types are an exception to the rule that an argument name must always be followed by a type.
If no value is given after the argument, `true` is assumed.

For example, in this situation…

```java
record ServerArgs(String url, boolean createLog) { }
```

…the following arguments could be parsed…

```
java [...] --url localhost:8080 --createLog
java [...] --createLog --url localhost:8080
```

…and `serverArgs.createLog()` would return `true`.

Note that like all non-container arguments, boolean arguments _must_ be present.
To turn them into classic flags instead, where absence means `false` and presence means `true`, use `Optional<Boolean>` as component type (see below for details) and access their value with `orElse(false)`.

### Container arguments

Beyond simple types, the following container types are supported:

* `Optional<VALUE>`, where `VALUE` is any of the simple types above  (`OptionalInt`, `OptionalLong`, `OptionalDouble` aren't supported, use `Optional<Integer>` etc. instead)
* `List<VALUE>`, where `VALUE` is any of the simple types above
* `Map<KEY, VALUE>`, where `KEY` and `VALUE` are any of the simple types above

Container types are always optional.

#### Optional arguments

Arguments of type `Optional` are optional (talk about good naming!).

For `port` to be optional, `ServerArgs` must be defined as follows:

```java
record ServerArgs(String url, Optional<Integer> port) { }
```

Then this command can be successfully parsed:

```
java [...] --url localhost
```

#### List arguments

Arguments of type `List` accept one or more arguments.
If not mentioned, they are empty, which makes them optional as well.
That means for the following args record…

```java
record ServerArgs(List<String> urls, boolean createLog) { }
```

…any of the following command lines are acceptable:

```
java [...] --createLog
java [...] --createLog --urls localhost
java [...] --createLog --urls localhost 127.0.0.1
java [...] --urls localhost 127.0.0.1 --createLog
```

While just mentioning a list argument without providing a value…

```
java [...] --urls --createLog
```

…could be parsed to the empty list, this non-sensical command is instead interpreted as a mistake and leads to an exception.

List instances are unmodifiable, just like those created with `List::of` and `List::copyOf`.

#### Map arguments

Arguments of type `Map` accept one or more key-value pair of the form `key=value` (there must be no additional `=` in the argument).
If not mentioned, they are empty, which makes them optional as well.
That means for the following args record…

```java
record ServerArgs(Map<Integer, String> numbers, boolean createLog) { }
```

…any of the following command lines are acceptable:

```
java [...] --createLog
java [...] --createLog --numbers 1=one
java [...] --createLog --numbers 1=one 2=two 3=three
java [...] --numbers 1=one 2=two 3=three --createLog
```

While just mentioning a map argument without providing a value…

```
java [...] --numbers --createLog
```

…could be parsed to the empty map, this non-sensical command is instead interpreted as a mistake and leads to an exception.

Map instances are unmodifiable, just like those created with `Map::of`, `Map::ofEntries`, and `Map::copyOf`.


## Args records

### Validation

RecordArgs calls a record's canonical constructor and it is advisable to implement all suitable argument verification in there - whether it's ranges for numerical values, existence of files and folders, or number of list elements.
Exceptions thrown by the constructor are surfaced by the error-handling mechanism (see below).

### Parsing multiple args records

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

The records must not have components of the same name or `Args::parse` throws an exception.


## Args interfaces and mutually exclusive arguments

If an application provides diverse features that take distinct execution paths, it might need argument sets for each path that have little to no overlap.
Instead of parsing the arguments to one large or several small args record where most components are optional and then dealing with many absent arguments, consider using "modes" or an "action".

### Modes

A _mode_ is a sealed interface that permits only record implementations:

```java
sealed interface Mode permits Client, Server { }
record Client(int port) implements Mode { }
record Server(String url, int port) implements Mode { }
```

When such an interface is passed to `Args::parse`, an argument with its name and a value that is one of the implementing records' names (always first letter in lower case, e.g. `--mode client`, and without a potential `Args` suffix - more on that below) is used to determine which args record to fill and instantiate (this is called _mode selection_).
Hence, `Args::parse` returns an instance of one of the records implementing the mode as chosen by the command line arguments.

For the types above, here's what `main`…

```java
public static void main(String[] args) throws ArgsParseException{
	var arguments = Args.parse(args, Mode.class);
}
```

…and a successful invocation…

```
java [...] --mode client --port 8080
```

…would look like.
In this case, `arguments` would be of type `Client`.

#### Branching execution

A good way to branch execution based on the specific type is with pattern matching over the returned instance:

```java
public static void main(String[] args) throws ArgsParseException{
	var arguments = Args.parse(args, Mode.class);

	switch(arguments) {
		case Client config -> spawnClient(config);
		case Server config -> spawnServer(config);
	}
}
```

That means a command line with `--mode client` would trigger execution of `spawnClient`.

#### Overlapping components, order, multiple args records

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

#### Ignoring `…Args`

When exclusively dealing with args records, the names of the arguments are determined solely by component names.
But as described above, when using modes (and/or an action - see below), the _type_ names also determine some argument names and values.
Type names should be descriptive (which just `Client` might not be) and command line arguments should be succinct (which `clientArgs` wouldn't be), though, which can lead to tension.
To offer a degree of freedom for resolving this tension, a type name suffix of `Args` is ignored and hence not included in the argument names created for the type:

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

An _action_ is a special mode (i.e. everything stated for them that is not contradicted here applies) where the selection is not done by a pair of arguments (e.g. `... --mode client ...`) but by having just the value as the first argument in the array (e.g. `client ...`).
This interpretation is automatically and exclusively applied to interfaces with the simple name `Action` or `ActionArgs`, e.g.:

```java
sealed interface Action permits Create, Copy, Move { }
record Create(Path path) implements Action { }
record Copy(Path from, Path to) implements Action { }
record Move(Path from, Path to) implements Action { }

// java [...] copy --from… --to…
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

Due to its positional nature, there can only be one action, but it can be combined with modes and other args records.


## Error Handling

`Args:parse` throws four kinds of exceptions:

* `IllegalArgumentException` (unchecked) when you pass an illegal argument, most likely `null`, to `parse`.
  This indicates a user error that can be avoided in production - make sure to check the instances you pass to `parse`.
* `ArgsDefinitionException` (unchecked) when the types passed to `parse` are not valid args types.
  This indicates a user error that can be avoided in production - check the error code and message for details and define your args types accordingly.
  Two example errors (and their codes):
	* `UNSUPPORTED_ARGUMENT_TYPE` when an args record component has a type that is not listed under simple or container types
    * `DUPLICATE_ARGUMENT_DEFINITION` when two args records have a component of the same name  
	* for all possible errors, check `ArgsDefinitionErrorCode`
* `ArgsParseException` (checked) when `String[] args` can't be correctly parsed.
  This error must be expected in production as the arguments are human input and may be faulty.
  Familiarize yourself with the error codes and messages to provide useful feedbacks to said humans.
  Two example errors (and their codes):
    * `MISSING_ARGUMENT` when the argument array did not define a value for a non-container component 
	* `CONSTRUCTOR_EXCEPTION` when the record constructor throws an exception
    * for all possible errors, check `ArgsParseErrorCode`
* `IllegalStateException` when an unexpected internal state is encountered.
  This is not supposed to happen at all - if it does, it is likely a bug.
