package dev.nipafx.args;

import java.util.List;

record ArgsAndTypes(List<String> argsStrings, List<Class<? extends Record>> types, List<ArgsMessage> errors) { }
