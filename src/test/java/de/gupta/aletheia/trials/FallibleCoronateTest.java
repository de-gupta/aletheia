package de.gupta.aletheia.trials;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests for Fallible.coronate()")
final class FallibleCoronateTest
{
	private static Fallible<String> brokenFury(final String message)
	{
		return Fallible.beckon("echo")
		               .metamorphose(_ ->
					   {
						   throw new IllegalArgumentException(message);
					   }, List.of());
	}

	@FunctionalInterface
	private interface Invocation
	{
		void invoke();
	}

	@Nested
	@DisplayName("Tests for triumph coronations")
	final class TriumphCoronationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("triumphCoronationCases")
		@DisplayName("should conclude through the triumph branch")
		void shouldConcludeThroughTheTriumphBranch(final String description,
		                                           final Fallible<String> source,
		                                           final Function<? super String, ? extends String> triumph,
		                                           final AtomicBoolean furyInvoked,
		                                           final String expected)
		{
			String result = source.coronate(triumph, _ ->
			{
				furyInvoked.set(true);
				return "wrath";
			});

			assertThat(result).as("triumph coronation for %s should yield the triumph conclusion", description)
			                  .isEqualTo(expected);
			assertThat(furyInvoked.get()).as("fury branch should remain untouched")
			                             .isFalse();
		}

		private static Stream<Arguments> triumphCoronationCases()
		{
			AtomicBoolean upperCaseWrath = new AtomicBoolean(false);
			AtomicBoolean nullWrath = new AtomicBoolean(false);

			return Stream.of(
								 new TriumphCoronationCase(
										 "a triumphant string should be transformed",
										 Fallible.beckon("orpheus"),
										 String::toUpperCase,
										 upperCaseWrath,
										 "ORPHEUS"),
								 new TriumphCoronationCase(
										 "a null triumph should still be concluded through triumph",
										 Fallible.beckon(null),
										 value -> value == null ? "silence" : value,
										 nullWrath,
										 "silence"))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.source,
								 testCase.triumph,
								 testCase.furyInvoked,
								 testCase.expected));
		}

		private record TriumphCoronationCase(String description,
		                                     Fallible<String> source,
		                                     Function<? super String, ? extends String> triumph,
		                                     AtomicBoolean furyInvoked,
		                                     String expected)
		{
		}
	}

	@Nested
	@DisplayName("Tests for fury coronations")
	final class FuryCoronationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("furyCoronationCases")
		@DisplayName("should conclude through the fury branch")
		void shouldConcludeThroughTheFuryBranch(final String description,
		                                        final Fallible<String> source,
		                                        final AtomicBoolean triumphInvoked,
		                                        final String expected)
		{
			String result = source.coronate(value ->
			{
				triumphInvoked.set(true);
				return value;
			}, Exception::getMessage);

			assertThat(result).as("fury coronation for %s should yield the fury conclusion", description)
			                  .isEqualTo(expected);
			assertThat(triumphInvoked.get()).as("triumph branch should remain untouched")
			                                .isFalse();
		}

		private static Stream<Arguments> furyCoronationCases()
		{
			AtomicBoolean brokenTriumph = new AtomicBoolean(false);
			AtomicBoolean cursedTriumph = new AtomicBoolean(false);

			return Stream.of(
								 new FuryCoronationCase(
										 "an unrecovered fury should yield its message",
										 brokenFury("broken"),
										 brokenTriumph,
										 "broken"),
								 new FuryCoronationCase(
										 "another fury should still shun the triumph branch",
										 brokenFury("cursed"),
										 cursedTriumph,
										 "cursed"))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.source,
								 testCase.triumphInvoked,
								 testCase.expected));
		}

		private record FuryCoronationCase(String description,
		                                  Fallible<String> source,
		                                  AtomicBoolean triumphInvoked,
		                                  String expected)
		{
		}
	}

	@Nested
	@DisplayName("Tests for invalid arguments")
	final class InvalidArgumentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidCoronationCases")
		@DisplayName("should reject invalid coronation arguments")
		void shouldRejectInvalidCoronationArguments(final String description,
		                                            final Invocation invocation,
		                                            final String expectedMessage)
		{
			assertThatThrownBy(invocation::invoke)
					.as("coronate should reject invalid arguments for %s", description)
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining(expectedMessage);
		}

		private static Stream<Arguments> invalidCoronationCases()
		{
			return Stream.of(
								 new InvalidCoronationCase(
										 "a triumph coronation requires a triumph conclusion",
										 () -> Fallible.beckon("echo").coronate(null, Exception::getMessage),
										 "triumph may not be null"),
								 new InvalidCoronationCase(
										 "a triumph coronation requires a fury conclusion",
										 () -> Fallible.beckon("echo").coronate(String::toUpperCase, null),
										 "fury may not be null"),
								 new InvalidCoronationCase(
										 "a fury coronation also requires a triumph conclusion",
										 () -> brokenFury("broken").coronate(null, Exception::getMessage),
										 "triumph may not be null"),
								 new InvalidCoronationCase(
										 "a fury coronation also requires a fury conclusion",
										 () -> brokenFury("broken").coronate(String::toUpperCase, null),
										 "fury may not be null"))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.invocation,
								 testCase.expectedMessage));
		}

		private record InvalidCoronationCase(String description, Invocation invocation, String expectedMessage)
		{
		}
	}
}