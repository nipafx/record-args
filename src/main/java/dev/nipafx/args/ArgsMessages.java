package dev.nipafx.args;

import java.util.List;

record ArgsMessages(List<ArgsMessage> errors, List<ArgsMessage> warnings) { }
