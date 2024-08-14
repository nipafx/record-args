package dev.nipafx.args;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static dev.nipafx.args.Check.internalErrorOnNull;

class ArgsParser {

	private State state;

	// used by `State` implementations
	private final List<Arg<?>> args;
	private final List<ArgsMessage> mutableErrors;
	private final List<ArgsMessage> mutableWarnings;

	private ArgsParser(List<Arg<?>> args) {
		this.args = List.copyOf(internalErrorOnNull(args));
		this.mutableErrors = new ArrayList<>();
		this.mutableWarnings = new ArrayList<>();

		this.state = new ExpectingName();
	}

	/**
	 * @param args list of {@link Arg}s, which will be mutated according to the args string
	 */
	static ArgsParser forArgs(List<Arg<?>> args) {
		return new ArgsParser(args);
	}

	public ArgsMessages parse(List<String> argStrings) {
		internalErrorOnNull(argStrings);

		for (String argString : argStrings)
			state = state.transition(argString);
		state.finish();
		return new ArgsMessages(List.copyOf(mutableErrors), List.copyOf(mutableWarnings));
	}

	/*
	 * STATE MACHINE
	 */

	private interface State {

		State transition(String argString);

		void finish();

	}

	private abstract class GeneralState implements State {

		protected final Optional<String> asArgName(String argString) {
			return argString.startsWith("--")
					? Optional.of(argString.substring(2))
					: Optional.empty();
		}

		protected final Optional<Arg<?>> findArgument(String argName) {
			return args.stream()
					.filter(arg -> arg.name().equals(argName))
					.findFirst();
		}

		protected final State transitionToArgumentState(
				String argName,
				Function<Arg<?>, ? extends State> createStateForArg,
				Function<String, ? extends State> createStateForUnknownArg) {
			return findArgument(argName)
					.<State> map(createStateForArg)
					.orElseGet(() -> {
						mutableWarnings.add(new ArgsMessage.UnknownArgument(argName));
						return createStateForUnknownArg.apply(argName);
					});
		}

		protected final State transition(
				String argString,
				Function<Arg<?>, ? extends State> createStateForArg,
				Function<String, ? extends State> createStateForUnknownArg,
				Function<String, ? extends State> createStateForValue) {
			return asArgName(argString)
					.map(argName -> transitionToArgumentState(argName, createStateForArg, createStateForUnknownArg))
					.orElseGet(() -> createStateForValue.apply(argString));
		}

		protected final void setValue(Arg<?> arg, String argString) {
			try {
				arg.setValue(argString);
			} catch (IllegalArgumentException ex) {
				mutableErrors.add(new ArgsMessage.IllegalValue(arg.name(), arg.type(), argString, ex));
			}
		}

		@Override
		public void finish() {
		}

	}

	private final class ExpectingName extends GeneralState {

		@Override
		public State transition(String argString) {
			return transition(
					argString,
					ExpectingValue::new,
					unknownArgName -> new IgnoringValue(),
					string -> {
						mutableErrors.add(new ArgsMessage.UnexpectedValue(string));
						return this;
					});
		}

	}

	private final class ExpectingValue extends GeneralState {

		private final Arg<?> currentArg;

		ExpectingValue(Arg<?> currentArg) {
			this.currentArg = internalErrorOnNull(currentArg);
		}

		@Override
		public State transition(String argString) {
			return transition(
					argString,
					arg -> {
						processMissingValue();
						return new ExpectingValue(arg);
					},
					unknownArgName -> new IgnoringValue(),
					string -> {
						setValue(currentArg, string);
						return currentArg.type() == List.class | currentArg.type() == Map.class
								? new ExpectingNameOrAdditionalValue(currentArg)
								: new ExpectingName();
					});
		}

		private void processMissingValue() {
			Class<?> type = currentArg.type();
			var isBoolean = type == Boolean.class || type == boolean.class
					|| currentArg instanceof OptionalArg<?> opt && opt.valueType() == Boolean.class;
			if (isBoolean)
				setValue(currentArg, "true");
			else
				mutableErrors.add(new ArgsMessage.MissingValue(currentArg.name()));
		}

		@Override
		public void finish() {
			processMissingValue();
		}

	}

	private final class ExpectingNameOrAdditionalValue extends GeneralState {

		private final Arg<?> currentArg;

		ExpectingNameOrAdditionalValue(Arg<?> currentArg) {
			this.currentArg = internalErrorOnNull(currentArg);
		}

		@Override
		public State transition(String argString) {
			return transition(
					argString,
					ExpectingValue::new,
					unknownArgName -> new IgnoringValue(),
					string -> {
						setValue(currentArg, string);
						return this;
					});
		}

	}

	private final class IgnoringValue extends GeneralState {

		@Override
		public State transition(String argString) {
			return transition(
					argString,
					ExpectingValue::new,
					unknownArgName -> new IgnoringValue(),
					ignoredValue -> this);
		}

	}

}
