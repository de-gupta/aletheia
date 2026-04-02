package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.functional.EmptyUnfoldingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade cleave tests")
final class CascadeCleaveTest
{
	@Nested
	@DisplayName("Aspects of branching current")
	final class BranchingCurrentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("cleaveCases")
		@DisplayName("should apply reward or punishment per element")
		void shouldApplyRewardOrPunishmentPerElement(final String as, final CleaveCase tc)
		{
			var result = tc.source().cleave(tc.judgement(), tc.reward(), tc.punishment());

			assertThat(result.summon())
					.as("cleave should branch elementwise for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> cleaveCases()
		{
			return Stream.of(
					new CleaveCase(
							"Even heroes are crowned, odd are exiled",
							Cascade.beckon(1, 2, 3, 4),
							n -> n % 2 == 0,
							n -> "crown-" + n,
							n -> "exile-" + n,
							List.of("exile-1", "crown-2", "exile-3", "crown-4")
					),
					new CleaveCase(
							"Both branches can carry distinct outcomes",
							Cascade.beckon(1, 2, 3),
							n -> n % 2 == 0,
							n -> "even-" + n,
							n -> "odd-" + n,
							List.of("odd-1", "even-2", "odd-3")
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record CleaveCase(String as, Cascade<Integer> source,
		                          Predicate<Integer> judgement,
		                          Function<Integer, String> reward,
		                          Function<Integer, String> punishment,
		                          List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of branch laziness")
	final class BranchLazinessTests
	{
		@Test
		@DisplayName("should never invoke punishment when judgement always true")
		void shouldNeverInvokePunishmentWhenJudgementAlwaysTrue()
		{
			var result = Cascade.beckon(1, 2)
			                    .cleave(_ -> true, n -> "reward-" + n, _ ->
								{
									throw new AssertionError("punishment should not be invoked");
								});

			assertThat(result.summon()).containsExactly("reward-1", "reward-2");
		}

		@Test
		@DisplayName("should never invoke reward when judgement always false")
		void shouldNeverInvokeRewardWhenJudgementAlwaysFalse()
		{
			var result = Cascade.beckon(1, 2)
			                    .cleave(_ -> false, _ ->
								{
									throw new AssertionError("reward should not be invoked");
								}, n -> "punish-" + n);

			assertThat(result.summon()).containsExactly("punish-1", "punish-2");
		}

		@Test
		@DisplayName("should throw when selected branch returns null")
		void shouldThrowWhenSelectedBranchReturnsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1, 2)
			                                .cleave(n -> n % 2 == 0, n -> "even-" + n, _ -> null)
			                                .summon())
					.isInstanceOf(EmptyUnfoldingException.class);
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
			assertThatThrownBy(() -> Cascade.beckon(1).cleave(null, Object::toString, Object::toString))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		@Test
		@DisplayName("should reject null reward for brook")
		void shouldRejectNullRewardForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).cleave(_ -> true, null, Object::toString))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("reward may not be null");
		}

		@Test
		@DisplayName("should reject null punishment for brook")
		void shouldRejectNullPunishmentForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).cleave(_ -> true, Object::toString, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("punishment may not be null");
		}

		@Test
		@DisplayName("should tolerate null arguments for abyss")
		void shouldTolerateNullArgumentsForAbyss()
		{
			assertThat(Cascade.<Integer>abyss().cleave(null, null, null))
					.as("nadir cleave should remain nadir without guard checks")
					.isSameAs(Cascade.abyss());
		}
	}
}