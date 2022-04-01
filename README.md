# RecordArgs

A simple command-line argument parser for Java applications that relies on records.
Specifically, it uses their component names to parse command line arguments, their canonical constructors to create instances, and their immutability to let you freely pass them around without fear of unwanted changes.

## Getting started

1. Create a record like this one:
	```java
	record ServerArgs(String url, int port) { }
	```
2. Make the record public and export its package or keep it encapsulated and open the package (optionally just to _dev.nipafx.args_).
3. Pass command-line arguments in this form:
	```
	java [...] --url localhost --port 8080
	```
4. Call `Args`:
	```java
	public static void main(String[] args) throws ArgsException {
		ServerArgs serverArgs = Args.parse(args, ServerArgs.class);
	}
   ```

In most cases, the passed arguments must alternative between an argument's name (prefixed by `--`) and its value.
A value must be defined for all arguments (i.e. there are no optional arguments).

## Argument names

The record component names define the argument names.
So for a record like the following ...

```java
record ServerArgs(String url, int port) { }
```

... the arguments `--url` and `--port` are parsed.

## Argument values (and types)

An argument name must in most cases be followed by exactly one value that can be parsed to the argument's type.
Supported types are:

* `String`, `Path`
* `Integer`, `int`, `Long`, `long`
* `Float`, `float`, `Double`, `double`
* `Boolean`, `boolean` (only values "true" and "false")

The boolean types are an exception to the rule that an argument name must always be followed by a type.
If no value is given, `true` is assumed.

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
