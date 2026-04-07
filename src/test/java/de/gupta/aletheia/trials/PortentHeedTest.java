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

@DisplayName("Tests for Portent.heed()")
final class PortentHeedTest
{
	@FunctionalInterface
	private interface Invocation
	{
		void invoke();
	}

	@Nested
	@DisplayName("Tests for matching furies")
	final class MatchingFuryTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("matchingCases")
		@DisplayName("should yield redemption for matching furies")
		void shouldYieldRedemptionForMatchingFuries(final String description,
		                                            final Portent<String> portent,
		                                            final Exception fury,
		                                            final Optional<String> expected)
		{
			assertThat(portent.heed(fury)).as("heed should redeem %s", description)
			                              .isEqualTo(expected);
		}

		private static Stream<Arguments> matchingCases()
		{
			return Stream.of(
								 new MatchingCase(
										 "an exact omen",
										 Portent.foretell(IllegalArgumentException.class, IllegalArgumentException::getMessage),
										 new IllegalArgumentException("too soon"),
										 Optional.of("too soon")),
								 new MatchingCase(
										 "a broader runtime omen",
										 Portent.foretell(RuntimeException.class, RuntimeException::getMessage),
										 new IllegalArgumentException("runtime"),
										 Optional.of("runtime")))
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
	@DisplayName("Tests for unmatching furies")
	final class UnmatchingFuryTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("unmatchingCases")
		@DisplayName("should remain silent for unmatching furies")
		void shouldRemainSilentForUnmatchingFuries(final String description,
		                                           final Portent<String> portent,
		                                           final Exception fury)
		{
			assertThat(portent.heed(fury)).as("heed should remain silent for %s", description)
			                              .isEmpty();
		}

		private static Stream<Arguments> unmatchingCases()
		{
			return Stream.of(
								 new UnmatchingCase(
										 "an unrelated omen",
										 Portent.foretell(IllegalArgumentException.class, IllegalArgumentException::getMessage),
										 new IllegalStateException("other")))
			             .map(testCase -> Arguments.of(testCase.description, testCase.portent, testCase.fury));
		}

		private record UnmatchingCase(String description, Portent<String> portent, Exception fury)
		{
		}
	}

	@Nested
	@DisplayName("Tests for invalid arguments")
	final class InvalidArgumentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidHeedCases")
		@DisplayName("should reject invalid heed arguments")
		void shouldRejectInvalidHeedArguments(final String description,
		                                      final Invocation invocation,
		                                      final String expectedMessage)
		{
			var assertion = assertThatThrownBy(invocation::invoke)
					.as("heed should reject invalid arguments for %s", description)
					.isInstanceOf(NullPointerException.class);

			if (expectedMessage != null)
			{
				assertion.hasMessageContaining(expectedMessage);
			}
		}

		private static Stream<Arguments> invalidHeedCases()
		{
			return Stream.of(
								 new InvalidHeedCase(
										 "a null fury should be rejected",
										 () -> Portent.foretell(IllegalArgumentException.class, IllegalArgumentException::getMessage)
							                          .heed(null),
										 "fury may not be null"),
								 new InvalidHeedCase(
										 "a null prophecy result should be rejected",
										 () -> Portent.foretell(IllegalArgumentException.class, _ -> null)
							                          .heed(new IllegalArgumentException("void")),
										 null))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.invocation,
								 testCase.expectedMessage));
		}

		private record InvalidHeedCase(String description, Invocation invocation, String expectedMessage)
		{
		}
	}
}