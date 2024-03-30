package dev.nipafx.args;

import dev.nipafx.args.Records.Action;
import dev.nipafx.args.Records.ActionArgs;
import dev.nipafx.args.Records.Class;
import dev.nipafx.args.Records.Interface;
import dev.nipafx.args.Records.WithInitializerException;
import dev.nipafx.args.Records.WithMany;
import dev.nipafx.args.Records.WithString;
import dev.nipafx.args.Records.WithStringArray;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.args.ArgsDefinitionErrorCode.DUPLICATE_ARGUMENT_DEFINITION;
import static dev.nipafx.args.ArgsDefinitionErrorCode.FAULTY_STATIC_INITIALIZER;
import static dev.nipafx.args.ArgsDefinitionErrorCode.ILL_DEFINED_ARGS_TYPE;
import static dev.nipafx.args.ArgsDefinitionErrorCode.MULTIPLE_ACTIONS;
import static dev.nipafx.args.ArgsDefinitionErrorCode.UNSUPPORTED_ARGUMENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
class DefinitionErrorsTests {

	@Test
	void parseToClass_noRecordError() {
		String[] args = { };
		var exception = assertThrows(ArgsDefinitionException.class, () -> Args.parse(args, Class.class));
		assertThat(exception.errorCode()).isEqualTo(ILL_DEFINED_ARGS_TYPE);
	}

	@Test
	void parseToInterface_noRecordError() {
		String[] args = { };
		var exception = assertThrows(ArgsDefinitionException.class, () -> Args.parse(args, Interface.class));
		assertThat(exception.errorCode()).isEqualTo(ILL_DEFINED_ARGS_TYPE);
	}

	@Test
	void multipleRecords_sameComponents_duplicateArgError() {
		String[] args = { };
		var exception = assertThrows(ArgsDefinitionException.class, () -> Args.parse(args, WithMany.class, WithString.class));
		assertThat(exception.errorCode()).isEqualTo(DUPLICATE_ARGUMENT_DEFINITION);
	}

	@Test
	void expectedArgOfUnsupportedType_unsupportedArgumentTypeError() {
		String[] args = { "--stringsArg", "{ one, two, three }" };
		var exception = assertThrows(ArgsDefinitionException.class, () -> Args.parse(args, WithStringArray.class));
		assertThat(exception.errorCode()).isEqualTo(UNSUPPORTED_ARGUMENT_TYPE);
	}

	@Test
	void constructorThrowsException_initializerError() {
		String[] args = { };
		var exception = assertThrows(ArgsDefinitionException.class, () -> Args.parse(args, WithInitializerException.class));
		assertThat(exception.errorCode()).isEqualTo(FAULTY_STATIC_INITIALIZER);
	}

	@Test
	void multipleActions_multipleActionsError() {
		String[] args = { };
		var exception = assertThrows(ArgsDefinitionException.class, () -> Args.parse(args, Action.class, ActionArgs.class));
		assertThat(exception.errorCode()).isEqualTo(MULTIPLE_ACTIONS);
	}

}
