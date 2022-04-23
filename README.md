# RecordArgs

A simple command-line argument parser for Java applications that relies on records.
Specifically, it uses their component names to parse command line arguments, their canonical constructors to create instances, and their immutability to let you freely pass them around without fear of unwanted changes.

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
	public static void main(String[] args) throws ArgsException {
		ServerArgs serverArgs = Args.parse(args, ServerArgs.class);
	}
   ```

In most cases, the passed arguments must alternate between an argument's name (prefixed by `--`) and its value.
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