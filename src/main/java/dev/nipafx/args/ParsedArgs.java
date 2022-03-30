package dev.nipafx.args;

import java.util.List;

record ParsedArgs(List<Arg<?>> args, List<ArgsMessage> errors, List<ArgsMessage> warnings) { }
