package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Unfolding#interdict(Predicate, Supplier)")
final class UnfoldingInterdictTest
{
	@Nested
	@DisplayName("when condition holds")
	final class WhenConditionHolds
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("throwsWrathWhenConditionIsTrueCases")
		@DisplayName("throws wrath when condition is satisfied by value")
		void throwsWrathWhenConditionIsTrue(final String as, final Unfolding<Integer> source,
		                                    final Predicate<Integer> condition)
		{
			assertThatThrownBy(() -> source.interdict(condition, () -> new IllegalStateException("forbidden")))
					.as(as)
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("forbidden");
		}

		private static Stream<Arguments> throwsWrathWhenConditionIsTrueCases()
		{
			return Stream.of(
					Arguments.of("positive value matches positive predicate",
							Unfolding.beckon(5), (Predicate<Integer>) n -> n > 0),
					Arguments.of("zero matches zero predicate",
							Unfolding.beckon(0), (Predicate<Integer>) n -> n == 0),
					Arguments.of("negative value matches negative predicate",
							Unfolding.beckon(-3), (Predicate<Integer>) n -> n < 0)
			);
		}
	}

	@Nested
	@DisplayName("when condition does not hold")
	final class WhenConditionDoesNotHold
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("continuesChainWhenConditionIsFalseCases")
		@DisplayName("returns original Unfolding and continues the chain")
		void continuesChainWhenConditionIsFalse(final String as, final Unfolding<Integer> source,
		                                        final Predicate<Integer> condition,
		                                        final Unfolding<Integer> expected)
		{
			assertThat(source.interdict(condition, () -> new IllegalStateException("should not throw")))
					.as(as)
					.isEqualTo(expected);
		}

		@Test
		@DisplayName("result is chainable when condition is not satisfied")
		void resultIsChainableWhenConditionIsNotSatisfied()
		{
			final String result = Unfolding.beckon(5)
			                               .interdict(n -> n < 0, IllegalStateException::new)
			                               .metamorphose(n -> "value=" + n)
			                               .summon();

			assertThat(result).as("chain continues after non-triggering interdict").isEqualTo("value=5");
		}

		private static Stream<Arguments> continuesChainWhenConditionIsFalseCases()
		{
			return Stream.of(
					Arguments.of("positive value does not match negative predicate",
							Unfolding.beckon(5), (Predicate<Integer>) n -> n < 0, Unfolding.beckon(5)),
					Arguments.of("zero does not match positive predicate",
							Unfolding.beckon(0), (Predicate<Integer>) n -> n > 0, Unfolding.beckon(0)),
					Arguments.of("negative value does not match positive predicate",
							Unfolding.beckon(-3), (Predicate<Integer>) n -> n > 0, Unfolding.beckon(-3))
			);
		}
	}

	@Nested
	@DisplayName("when Unfolding is empty")
	final class WhenUnfoldingIsEmpty
	{
		@Test
		@DisplayName("does nothing and returns empty without evaluating condition")
		void doesNothingAndReturnsEmpty()
		{
			final Unfolding<Integer> result = Unfolding.<Integer>chaos()
			                                           .interdict(_ ->
															   {
																   throw new AssertionError(
																		   "condition must not be evaluated on empty");
															   },
															   IllegalStateException::new);

			assertThat(result).as("empty source stays empty").isEqualTo(Unfolding.chaos());
		}
	}

	@Nested
	@DisplayName("with null arguments")
	final class WithNullArguments
	{
		@Test
		@DisplayName("throws when judgement is null")
		void throwsWhenJudgementIsNull()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).interdict(null, IllegalStateException::new))
					.as("null judgement on present source")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");
		}

		@Test
		@DisplayName("throws when wrath is null")
		void throwsWhenWrathIsNull()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).interdict(n -> n > 0, null))
					.as("null wrath on present source")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("wrath may not be null");
		}
	}
}