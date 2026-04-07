package de.gupta.aletheia.trials;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests for Portents.redeem()")
final class PortentsRedeemTest
{
	@SafeVarargs
	private static <R> List<Portent<R>> portents(final Portent<R>... portents)
	{
		return List.of(portents);
	}

	private static List<Portent<String>> brokenPortents()
	{
		return Collections.singletonList(null);
	}

	@FunctionalInterface
	private interface Invocation
	{
		void invoke();
	}

	@Nested
	@DisplayName("Tests for redemptive portents")
	final class RedemptivePortentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("redemptionCases")
		@DisplayName("should redeem fury according to portent order")
		void shouldRedeemFuryAccordingToPortentOrder(final String description,
		                                             final Exception fury,
		                                             final List<Portent<String>> portents,
		                                             final Fallible<String> expected)
		{
			assertThat(Portents.redeem(fury, portents)).as("redeem should heed %s", description)
			                                           .isEqualTo(expected);
		}

		private static Stream<Arguments> redemptionCases()
		{
			return Stream.of(
								 new RedemptionCase(
										 "the first broader portent should prevail",
										 new IllegalArgumentException("too soon"),
										 portents(
												 Portent.foretell(RuntimeException.class, _ -> "general runtime"),
												 Portent.foretell(IllegalArgumentException.class, _ -> "specific argument")),
										 Fallible.beckon("general runtime")),
								 new RedemptionCase(
										 "the first specific portent should prevail",
										 new IllegalArgumentException("too soon"),
										 portents(
												 Portent.foretell(IllegalArgumentException.class, _ -> "specific argument"),
												 Portent.foretell(RuntimeException.class, _ -> "general runtime")),
										 Fallible.beckon("specific argument")))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.fury,
								 testCase.portents,
								 testCase.expected));
		}

		private record RedemptionCase(String description,
		                              Exception fury,
		                              List<Portent<String>> portents,
		                              Fallible<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Tests for unredeemed fury")
	final class UnredeemedFuryTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("unredeemedCases")
		@DisplayName("should preserve fury when no portent matches")
		void shouldPreserveFuryWhenNoPortentMatches(final String description,
		                                            final Exception fury,
		                                            final List<Portent<String>> portents,
		                                            final String expectedMessage)
		{
			Fallible<String> result = Portents.redeem(fury, portents);

			assertThat(result).as("redeem should preserve fury for %s", description)
			                  .isInstanceOf(Fury.class);
			assertThat(result.coronate(value -> value, Exception::getMessage))
					.as("unredeemed fury should preserve its message")
					.isEqualTo(expectedMessage);
		}

		private static Stream<Arguments> unredeemedCases()
		{
			return Stream.of(
								 new UnredeemedCase(
										 "silence among the portents should preserve the fury",
										 new IllegalArgumentException("broken"),
										 List.of(),
										 "broken"),
								 new UnredeemedCase(
										 "an unrelated portent should also preserve the fury",
										 new IllegalArgumentException("broken"),
										 portents(Portent.foretell(IllegalStateException.class, _ -> "state")),
										 "broken"))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.fury,
								 testCase.portents,
								 testCase.expectedMessage));
		}

		private record UnredeemedCase(String description,
		                              Exception fury,
		                              List<Portent<String>> portents,
		                              String expectedMessage)
		{
		}
	}

	@Nested
	@DisplayName("Tests for invalid arguments")
	final class InvalidArgumentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidRedeemCases")
		@DisplayName("should reject invalid redeem arguments")
		void shouldRejectInvalidRedeemArguments(final String description,
		                                        final Invocation invocation,
		                                        final String expectedMessage)
		{
			assertThatThrownBy(invocation::invoke)
					.as("redeem should reject invalid arguments for %s", description)
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining(expectedMessage);
		}

		private static Stream<Arguments> invalidRedeemCases()
		{
			return Stream.of(
								 new InvalidRedeemCase(
										 "a null fury should be rejected",
										 () -> Portents.redeem(null, List.of()),
										 "fury may not be null"),
								 new InvalidRedeemCase(
										 "a null portent collection should be rejected",
										 () -> Portents.redeem(new IllegalArgumentException("broken"), null),
										 "portents may not be null"),
								 new InvalidRedeemCase(
										 "a null portent within the collection should be rejected",
										 () -> Portents.redeem(new IllegalArgumentException("broken"), brokenPortents()),
										 "portents may not contain null"))
			             .map(testCase -> Arguments.of(testCase.description,
								 testCase.invocation,
								 testCase.expectedMessage));
		}

		private record InvalidRedeemCase(String description, Invocation invocation, String expectedMessage)
		{
		}
	}
}