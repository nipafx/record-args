package dev.nipafx.args;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static dev.nipafx.args.ArgsCode.MISSING_VALUE;
import static dev.nipafx.args.ArgsCode.UNEXPECTED_VALUE;
import static dev.nipafx.args.ArgsCode.UNKNOWN_ARGUMENT;
import static dev.nipafx.args.ArgsCode.UNPARSEABLE_VALUE;
import static dev.nipafx.args.Check.nonNull;

class ArgsParser {

	private State state;

	// used by `State` implementations
	private final List<Arg<?>> args;
	private final List<ArgsMessage> mutableErrors;
	private final List<ArgsMessage> mutableWarnings;

	private ArgsParser(List<Arg<?>> args) {
		this.args = List.copyOf(args);
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

	public ArgsMessages parse(String[] argStrings) {
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
						var message = "Encountered name of unknown argument '%s'.".formatted(argName);
						mutableWarnings.add(new ArgsMessage(UNKNOWN_ARGUMENT, message));
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
				var message = "Value '%s' could not be parsed to '%s's type %s."
						.formatted(argString, arg.name(), arg.type().getSimpleName());
				mutableErrors.add(new ArgsMessage(UNPARSEABLE_VALUE, message, ex));
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
						var message = "Expected an option but got argument '%s' instead.".formatted(string);
						mutableErrors.add(new ArgsMessage(UNEXPECTED_VALUE, message));
						return this;
					});
		}

	}

	private final class ExpectingValue extends GeneralState {

		private final Arg<?> currentArg;

		ExpectingValue(Arg<?> currentArg) {
			this.currentArg = nonNull(currentArg);
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
			if (currentArg.type() == Boolean.class || currentArg.type() == boolean.class)
				setValue(currentArg, "true");
			else {
				var message = "No value was assigned to arg '%s'.".formatted(currentArg.name());
				mutableErrors.add(new ArgsMessage(MISSING_VALUE, message));
			}
		}

		@Override
		public void finish() {
			processMissingValue();
		}

	}

	private final class ExpectingNameOrAdditionalValue extends GeneralState {

		private final Arg<?> currentArg;

		ExpectingNameOrAdditionalValue(Arg<?> currentArg) {
			this.currentArg = nonNull(currentArg);
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
			return new ExpectingName();
		}

	}

}
