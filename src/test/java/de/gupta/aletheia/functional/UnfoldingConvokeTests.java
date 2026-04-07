package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UnfoldingConvokeTests
{
	@Nested
	@DisplayName("Tests for convoke() method")
	final class ConvokeTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentConvokeTestCases")
		@DisplayName("should convoke omens into a coherent unfolding")
		<T, R, A> void shouldConvokeOmensIntoAConsistentUnfolding(final String description,
		                                                          final Unfolding<T> source,
		                                                          final Collection<Function<? super T, ? extends R>> omens,
		                                                          final Function<? super Collection<? extends R>, ? extends A> oracle,
		                                                          final Unfolding<A> expectedResult)
		{
			assertThat(source.convoke(omens, oracle))
					.as("convoke() for %s should yield %s", source, expectedResult)
					.isEqualTo(expectedResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("presentGuardTestCases")
		@DisplayName("should reject invalid present arguments")
		void shouldRejectInvalidPresentArguments(final String description,
		                                         final Invocation invocation,
		                                         final String expectedMessage)
		{
			assertThatThrownBy(invocation::invoke)
					.as("convoke() should reject invalid present arguments")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining(expectedMessage);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("shellIdentityTestCases")
		@DisplayName("should preserve shell for all omen and oracle shapes")
		void shouldPreserveShellForAllOmenAndOracleShapes(final String description,
		                                                  final Collection<Function<? super String, ? extends String>> omens,
		                                                  final Function<? super Collection<? extends String>, ? extends String> oracle)
		{
			final Unfolding<String> shell = Unfolding.chaos();

			assertThat(shell.convoke(omens, oracle))
					.as("convoke() on shell should return the same shell instance")
					.isSameAs(shell);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("shellLazinessTestCases")
		@DisplayName("should not evaluate omens or oracle for shell")
		void shouldNotEvaluateOmensOrOracleForShell(final String description,
		                                            final Collection<Function<? super String, ? extends String>> omens,
		                                            final Function<? super Collection<? extends String>, ? extends String> oracle,
		                                            final AtomicBoolean omenInvoked,
		                                            final AtomicBoolean oracleInvoked)
		{
			final Unfolding<String> shell = Unfolding.chaos();

			assertThat(shell.convoke(omens, oracle))
					.as("convoke() on shell should remain shell")
					.isSameAs(shell);
			assertThat(omenInvoked.get())
					.as("convoke() on shell should not invoke omens")
					.isFalse();
			assertThat(oracleInvoked.get())
					.as("convoke() on shell should not invoke the oracle")
					.isFalse();
		}

		private static Stream<Arguments> presentConvokeTestCases()
		{
			return Stream.of(
								 new PresentConvokeTestCase<>(
										 "When the omens speak in sequence, the oracle should hear them in that same order",
										 Unfolding.beckon("orpheus"),
										 List.<Function<? super String, ? extends String>>of(
												 String::toUpperCase,
												 value -> value.substring(0, 3),
												 value -> Integer.toString(value.length())),
										 parts -> String.join(" | ", parts),
										 Unfolding.beckon("ORPHEUS | orp | 7")),
								 new PresentConvokeTestCase<>(
										 "When the omens are arithmetical, the oracle should still pronounce over their gathered signs",
										 Unfolding.beckon(6),
										 List.<Function<? super Integer, ? extends Integer>>of(
												 value -> value * 2,
												 value -> value * value,
												 value -> value - 1),
										 parts -> parts.stream().map(Object::toString).toList().toString(),
										 Unfolding.beckon("[12, 36, 5]")),
								 new PresentConvokeTestCase<>(
										 "When no omens are summoned, the oracle should still be able to pronounce over the silence",
										 Unfolding.beckon("echo"),
										 List.<Function<? super String, ? extends String>>of(),
										 parts -> "voices=" + parts.size(),
										 Unfolding.beckon("voices=0")),
								 new PresentConvokeTestCase<>(
										 "When one omen falls silent, the oracle should still receive that silence among the signs",
										 Unfolding.beckon("echo"),
										 List.<Function<? super String, ? extends String>>of(
												 String::toUpperCase,
												 _ -> null,
												 value -> value + "!"),
										 parts -> parts.stream().map(part -> part == null ? "silence" : part).toList().toString(),
										 Unfolding.beckon("[ECHO, silence, echo!]")),
								 new PresentConvokeTestCase<>(
										 "When many omens fall silent, each silence should remain in the gathered prophecy",
										 Unfolding.beckon("echo"),
										 List.<Function<? super String, ? extends String>>of(
												 _ -> null,
												 String::toUpperCase,
												 _ -> null),
										 parts -> parts.stream().map(part -> part == null ? "silence" : part).toList().toString(),
										 Unfolding.beckon("[silence, ECHO, silence]")),
								 new PresentConvokeTestCase<>(
										 "When twin omens foretell the same sign, the oracle should receive both without purification",
										 Unfolding.beckon("echo"),
										 List.<Function<? super String, ? extends String>>of(
												 String::toUpperCase,
												 String::toUpperCase,
												 value -> value + "!"),
										 Object::toString,
										 Unfolding.beckon("[ECHO, ECHO, echo!]")),
								 new PresentConvokeTestCase<>(
										 "When the oracle returns no final prophecy, the convocation should dissolve into chaos",
										 Unfolding.beckon("eurydice"),
										 List.<Function<? super String, ? extends String>>of(String::toUpperCase),
										 _ -> null,
										 Unfolding.chaos()))
			             .map(tc -> Arguments.of(tc.description(), tc.source(), tc.omens(), tc.oracle(),
								 tc.expectedResult()));
		}

		private static Stream<Arguments> presentGuardTestCases()
		{
			return Stream.of(
								 new PresentGuardTestCase(
										 "Null omens should be rejected",
										 () -> Unfolding.beckon("test").convoke(null, Collection::size),
										 "omens may not be null"),
								 new PresentGuardTestCase(
										 "Null omen member should be rejected",
										 () -> Unfolding.beckon("test").convoke(Arrays.asList(String::trim, null), parts -> parts),
										 "omen may not be null"),
								 new PresentGuardTestCase(
										 "Null oracle should be rejected",
										 () -> Unfolding.beckon("test").convoke(List.of(String::trim), null),
										 "oracle may not be null"))
			             .map(tc -> Arguments.of(tc.description(), tc.invocation(), tc.expectedMessage()));
		}

		private static Stream<Arguments> shellIdentityTestCases()
		{
			return Stream.of(
								 new ShellIdentityTestCase(
										 "Concrete omens and oracle should leave shell untouched",
										 List.of(String::trim, String::toUpperCase),
										 parts -> String.join(",", parts)),
								 new ShellIdentityTestCase(
										 "Null omens should still leave shell untouched",
										 null,
										 parts -> String.join(",", parts)),
								 new ShellIdentityTestCase(
										 "Null oracle should still leave shell untouched",
										 List.of(String::trim),
										 null),
								 new ShellIdentityTestCase(
										 "Null omens and oracle should still leave shell untouched",
										 null,
										 null))
			             .map(tc -> Arguments.of(tc.description(), tc.omens(), tc.oracle()));
		}

		private static Stream<Arguments> shellLazinessTestCases()
		{
			return Stream.of(
								 new ShellLazinessTestCase(
										 "Shell should skip a single eager omen and oracle",
										 new AtomicBoolean(false),
										 new AtomicBoolean(false)),
								 new ShellLazinessTestCase(
										 "Shell should skip many eager omens and the oracle",
										 new AtomicBoolean(false),
										 new AtomicBoolean(false)))
			             .map(tc ->
						 {
							 final Collection<Function<? super String, ? extends String>> omens =
									 tc.description().contains("many")
											 ? List.of(
											 value ->
											 {
												 tc.omenInvoked().set(true);
												 return value.trim();
											 },
											 value ->
											 {
												 tc.omenInvoked().set(true);
												 return value.toUpperCase();
											 })
											 : List.of(value ->
									 {
										 tc.omenInvoked().set(true);
										 return value.trim();
									 });

							 final Function<? super Collection<? extends String>, ? extends String> oracle = parts ->
							 {
								 tc.oracleInvoked().set(true);
								 return String.join(",", parts);
							 };

							 return Arguments.of(tc.description(), omens, oracle, tc.omenInvoked(), tc.oracleInvoked());
						 });
		}

		@FunctionalInterface
		private interface Invocation
		{
			void invoke();
		}

		private record PresentConvokeTestCase<T, R, A>(String description,
		                                               Unfolding<T> source,
		                                               Collection<Function<? super T, ? extends R>> omens,
		                                               Function<? super Collection<? extends R>, ? extends A> oracle,
		                                               Unfolding<A> expectedResult)
		{
		}

		private record PresentGuardTestCase(String description, Invocation invocation, String expectedMessage)
		{
		}

		private record ShellIdentityTestCase(String description,
		                                     Collection<Function<? super String, ? extends String>> omens,
		                                     Function<? super Collection<? extends String>, ? extends String> oracle)
		{
		}

		private record ShellLazinessTestCase(String description, AtomicBoolean omenInvoked, AtomicBoolean oracleInvoked)
		{
		}
	}
}