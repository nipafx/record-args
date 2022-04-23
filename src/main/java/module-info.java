/**
 * A simple command-line argument parser for Java applications that relies on records.
 *
 * <p>Specifically, it uses their component names to parse command line arguments,
 * their canonical constructors to create instances, and their immutability to let you
 * freely pass them around without fear of unwanted changes.</p>
 *
 * <p>To use it, create a record, make it public and export its package or keep it
 * package-private and open the package to this module, and call
 * {@link dev.nipafx.args.Args#parse(String[], Class) Args.parse(args, ArgsRecord.class)}</p>
 * to parse the string array {@code args} to an instance of your record ({@code ArgsRecord},
 * in this example).
 */
module dev.nipafx.args {
	exports dev.nipafx.args;
}