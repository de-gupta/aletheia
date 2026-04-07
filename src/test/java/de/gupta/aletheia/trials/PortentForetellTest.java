package de.gupta.aletheia.trials;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests for Portent.foretell() static method")
final class PortentForetellTest
{
	@FunctionalInterface
	private interface Invocation
	{
		void invoke();
	}

	@Nested
	@DisplayName("Tests for matching omens")
	final class MatchingOmenTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("matchingCases")
		@DisplayName("should heed matching fury according to the foretold omen")
		void shouldHeedMatchingFuryAccordingToTheForetoldOmen(final String description,
		                                                      final Portent<String> portent,
		                                                      final Exception fury,
		                                                      final Optional<String> expected)
		{
			assertThat(portent.heed(fury)).as("matching portent for %s should yield redemption", description)
			                              .isEqualTo(expected);
		}

		private static Stream<Arguments> matchingCases()
		{
			return Stream.of(
								 new MatchingCase(
										 "an exact omen should be heeded",
										 Portent.foretell(IllegalArgumentException.class, IllegalArgumentException::getMessage),
										 new IllegalArgumentException("too soon"),
										 Optional.of("too soon")),
								 new MatchingCase(
										 "a broader omen should also be heeded",
										 Portent.foretell(RuntimeException.class, RuntimeException::getMessage),
										 new IllegalArgumentException("general runtime"),
										 Optional.of("general runtime")))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.portent,
								 testCase.fury,
								 testCase.expected));
		}

		private record MatchingCase(String description,
		                            Portent<String> portent,
		                            Exception fury,
		                            Optional<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Tests for unheeded omens")
	final class UnheededOmenTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("unmatchedCases")
		@DisplayName("should ignore fury that does not match the omen")
		void shouldIgnoreFuryThatDoesNotMatchTheOmen(final String description,
		                                             final Portent<String> portent,
		                                             final Exception fury)
		{
			assertThat(portent.heed(fury)).as("non-matching portent for %s should remain silent", description)
			                              .isEmpty();
		}

		private static Stream<Arguments> unmatchedCases()
		{
			return Stream.of(
								 new UnmatchedCase(
										 "an unrelated checked fury should be ignored",
										 Portent.foretell(IllegalArgumentException.class, IllegalArgumentException::getMessage),
										 new IllegalStateException("other")))
			             .map(testCase -> Arguments.of(testCase.description, testCase.portent, testCase.fury));
		}

		private record UnmatchedCase(String description, Portent<String> portent, Exception fury)
		{
		}
	}

	@Nested
	@DisplayName("Tests for invalid factory arguments")
	final class InvalidFactoryArgumentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidForetellCases")
		@DisplayName("should reject invalid foretell arguments")
		void shouldRejectInvalidForetellArguments(final String description,
		                                          final Invocation invocation,
		                                          final String expectedMessage)
		{
			assertThatThrownBy(invocation::invoke)
					.as("foretell should reject invalid arguments for %s", description)
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining(expectedMessage);
		}

		private static Stream<Arguments> invalidForetellCases()
		{
			return Stream.of(
								 new InvalidForetellCase(
										 "a null omen should be rejected",
										 () -> Portent.foretell(null, IllegalArgumentException::getMessage),
										 "omen may not be null"),
								 new InvalidForetellCase(
										 "a null prophecy should be rejected",
										 () -> Portent.foretell(IllegalArgumentException.class, null),
										 "prophecy may not be null"))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.invocation,
								 testCase.expectedMessage));
		}

		private record InvalidForetellCase(String description, Invocation invocation, String expectedMessage)
		{
		}
	}
}