package de.gupta.aletheia.trials;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests for Fallible.metamorphose()")
final class FallibleMetamorphoseTest
{
	@SafeVarargs
	private static <R> List<Portent<R>> portents(final Portent<R>... portents)
	{
		return List.of(portents);
	}

	@FunctionalInterface
	private interface Invocation
	{
		void invoke();
	}

	@Nested
	@DisplayName("Tests for triumph paths")
	final class TriumphPaths
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("successfulMetamorphosisCases")
		@DisplayName("should transfigure a triumph when the ordeal completes")
		void shouldTransfigureATriumphWhenTheOrdealCompletes(final String description,
		                                                     final Fallible<String> source,
		                                                     final Ordeal<? super String, ? extends Integer> ordeal,
		                                                     final List<Portent<Integer>> portents,
		                                                     final Fallible<Integer> expected)
		{
			Fallible<Integer> result = source.metamorphose(ordeal, portents);

			assertThat(result).as("successful metamorphosis for %s should remain triumph", description)
			                  .isEqualTo(expected);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("recoveryCases")
		@DisplayName("should heed the first matching fury in declared order")
		void shouldHeedTheFirstMatchingFuryInDeclaredOrder(final String description,
		                                                   final List<Portent<String>> portents,
		                                                   final Fallible<String> expected)
		{
			Fallible<String> result = Fallible.beckon("Eurydice")
			                                  .metamorphose(_ ->
											  {
												  throw new IllegalArgumentException("too soon");
											  }, portents);

			assertThat(result).as("recovery for %s should respect declared portent order", description)
			                  .isEqualTo(expected);
		}

		private static Stream<Arguments> successfulMetamorphosisCases()
		{
			return Stream.of(
								 new SuccessfulMetamorphosisCase(
										 "string length should emerge as triumph",
										 Fallible.beckon("Orpheus"),
										 String::length,
										 List.of(),
										 Fallible.beckon(7)),
								 new SuccessfulMetamorphosisCase(
										 "arithmetical ordeal should also emerge as triumph",
										 Fallible.beckon("7"),
										 Integer::parseInt,
										 List.of(),
										 Fallible.beckon(7)))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.source,
								 testCase.ordeal,
								 testCase.portents,
								 testCase.expected));
		}

		private static Stream<Arguments> recoveryCases()
		{
			return Stream.of(
								 new RecoveryCase(
										 "a broader runtime portent declared first should prevail",
										 portents(
												 Portent.foretell(RuntimeException.class, _ -> "general runtime"),
												 Portent.foretell(IllegalArgumentException.class, _ -> "specific argument")),
										 Fallible.beckon("general runtime")),
								 new RecoveryCase(
										 "a specific illegal argument portent declared first should prevail",
										 portents(
												 Portent.foretell(IllegalArgumentException.class, _ -> "specific argument"),
												 Portent.foretell(RuntimeException.class, _ -> "general runtime")),
										 Fallible.beckon("specific argument")))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.portents,
								 testCase.expected));
		}

		private record SuccessfulMetamorphosisCase(String description,
		                                           Fallible<String> source,
		                                           Ordeal<? super String, ? extends Integer> ordeal,
		                                           List<Portent<Integer>> portents,
		                                           Fallible<Integer> expected)
		{
		}

		private record RecoveryCase(String description,
		                            List<Portent<String>> portents,
		                            Fallible<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Tests for fury paths")
	final class FuryPaths
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("unmatchedFuryCases")
		@DisplayName("should become fury when no recovery portent matches")
		void shouldBecomeFuryWhenNoRecoveryPortentMatches(final String description,
		                                                  final List<Portent<String>> portents,
		                                                  final String expectedMessage)
		{
			Fallible<String> result = Fallible.beckon("echo")
			                                  .metamorphose(_ ->
											  {
												  throw new IllegalArgumentException("broken");
											  }, portents);

			assertThat(result).as("unmatched exception for %s should remain fury", description)
			                  .isInstanceOf(Fury.class);

			assertThat(result.coronate(value -> value, Exception::getMessage))
					.as("fury should preserve the original exception")
					.isEqualTo(expectedMessage);
		}

		private static Stream<Arguments> unmatchedFuryCases()
		{
			return Stream.of(
								 new UnmatchedFuryCase(
										 "an unrelated portent should leave the fury untouched",
										 portents(Portent.foretell(IllegalStateException.class, _ -> "state")),
										 "broken"),
								 new UnmatchedFuryCase(
										 "silence among the portents should also leave the fury untouched",
										 List.of(),
										 "broken"))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.portents,
								 testCase.expectedMessage));
		}

		private record UnmatchedFuryCase(String description,
		                                 List<Portent<String>> portents,
		                                 String expectedMessage)
		{
		}
	}

	@Nested
	@DisplayName("Tests for edge cases")
	final class EdgeCaseTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("nullTriumphCases")
		@DisplayName("should preserve null triumphs when the ordeal returns null")
		void shouldPreserveNullTriumphsWhenTheOrdealReturnsNull(final String description,
		                                                        final Fallible<String> source,
		                                                        final Ordeal<? super String, ? extends String> ordeal,
		                                                        final List<Portent<String>> portents,
		                                                        final Fallible<String> expected)
		{
			assertThat(source.metamorphose(ordeal, portents))
					.as("null-producing metamorphosis for %s should remain a triumph", description)
					.isEqualTo(expected);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("furyPropagationCases")
		@DisplayName("should preserve fury without invoking later ordeals")
		void shouldPreserveFuryWithoutInvokingLaterOrdeals(final String description,
		                                                   final Fallible<String> source,
		                                                   final Ordeal<? super String, ? extends String> ordeal,
		                                                   final List<Portent<String>> portents,
		                                                   final AtomicBoolean ordealInvoked,
		                                                   final String expectedMessage)
		{
			Fallible<String> result = source.metamorphose(ordeal, portents);

			assertThat(result).as("existing fury for %s should remain fury", description)
			                  .isInstanceOf(Fury.class);
			assertThat(ordealInvoked.get()).as("later ordeal should not be invoked for fury")
			                               .isFalse();
			assertThat(result.coronate(value -> value, Exception::getMessage))
					.as("original fury should be preserved")
					.isEqualTo(expectedMessage);
		}

		private static Stream<Arguments> nullTriumphCases()
		{
			return Stream.of(
								 new NullTriumphCase(
										 "silence from the ordeal should be borne as a triumph",
										 Fallible.beckon("echo"),
										 _ -> null,
										 List.of(),
										 Fallible.beckon(null)))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.source,
								 testCase.ordeal,
								 testCase.portents,
								 testCase.expected));
		}

		private static Stream<Arguments> furyPropagationCases()
		{
			AtomicBoolean ordealInvoked = new AtomicBoolean(false);

			return Stream.of(
								 new FuryPropagationCase(
										 "a fury should not entertain further metamorphoses",
										 Fallible.beckon("echo").metamorphose(_ ->
										 {
											 throw new IllegalArgumentException("broken");
										 }, List.of()),
										 boon ->
										 {
											 ordealInvoked.set(true);
											 return boon.toUpperCase();
										 },
										 List.of(),
										 ordealInvoked,
										 "broken"))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.source,
								 testCase.ordeal,
								 testCase.portents,
								 testCase.ordealInvoked,
								 testCase.expectedMessage));
		}

		private record NullTriumphCase(String description,
		                               Fallible<String> source,
		                               Ordeal<? super String, ? extends String> ordeal,
		                               List<Portent<String>> portents,
		                               Fallible<String> expected)
		{
		}

		private record FuryPropagationCase(String description,
		                                   Fallible<String> source,
		                                   Ordeal<? super String, ? extends String> ordeal,
		                                   List<Portent<String>> portents,
		                                   AtomicBoolean ordealInvoked,
		                                   String expectedMessage)
		{
		}
	}

	@Nested
	@DisplayName("Tests for invalid arguments")
	final class InvalidArgumentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidArgumentCases")
		@DisplayName("should reject invalid metamorphosis arguments")
		void shouldRejectInvalidMetamorphosisArguments(final String description,
		                                               final Invocation invocation,
		                                               final String expectedMessage)
		{
			assertThatThrownBy(invocation::invoke)
					.as("metamorphose should reject invalid arguments for %s", description)
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining(expectedMessage);
		}

		private static Stream<Arguments> invalidArgumentCases()
		{
			return Stream.of(
								 new InvalidArgumentCase(
										 "a null ordeal should be rejected",
										 () -> Fallible.beckon("echo").metamorphose(null, List.of()),
										 "ordeal may not be null"),
								 new InvalidArgumentCase(
										 "a null portent collection should be rejected",
										 () -> Fallible.beckon("echo").metamorphose(String::length, null),
										 "portents may not be null"),
								 new InvalidArgumentCase(
										 "a null portent within the collection should be rejected when fury seeks redemption",
										 () -> Fallible.beckon("echo").metamorphose(_ ->
										 {
											 throw new IllegalArgumentException("broken");
										 }, brokenPortents()),
										 "portents may not contain null"))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.invocation,
								 testCase.expectedMessage));
		}

		private static List<Portent<String>> brokenPortents()
		{
			return Collections.singletonList(null);
		}

		private record InvalidArgumentCase(String description,
		                                   Invocation invocation,
		                                   String expectedMessage)
		{
		}
	}
}