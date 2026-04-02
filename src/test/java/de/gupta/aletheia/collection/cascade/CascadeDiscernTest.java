package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade discern tests")
final class CascadeDiscernTest
{
	@Nested
	@DisplayName("Aspects of judgement filtering")
	final class JudgementFilteringTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("discernCases")
		@DisplayName("should keep only judged elements")
		void shouldKeepOnlyJudgedElements(final String as, final DiscernCase tc)
		{
			var result = tc.source().discern(tc.judgement());

			assertThat(result.summon())
					.as("discern should preserve only approved current for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> discernCases()
		{
			return Stream.of(
					new DiscernCase(
							"Only even runes remain in the river",
							Cascade.beckon(1, 2, 3, 4),
							n -> n % 2 == 0,
							List.of(2, 4)
					),
					new DiscernCase(
							"When all are worthy, all remain",
							Cascade.beckon(5, 7),
							_ -> true,
							List.of(5, 7)
					),
					new DiscernCase(
							"When none are worthy, brook falls to abyss",
							Cascade.beckon(5, 7),
							_ -> false,
							List.of()
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record DiscernCase(String as, Cascade<Integer> source,
		                           Predicate<Integer> judgement,
		                           List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of guards and abyss")
	final class GuardAndAbyssTests
	{
		@Test
		@DisplayName("should reject null judgement for brook")
		void shouldRejectNullJudgementForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).discern(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		@Test
		@DisplayName("should tolerate null judgement for abyss")
		void shouldTolerateNullJudgementForAbyss()
		{
			assertThat(Cascade.<Integer>abyss().discern(null))
					.as("nadir discern should remain nadir even when judgement is null")
					.isSameAs(Cascade.abyss());
		}
	}
}