package de.gupta.aletheia.collection.cascade;

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

@DisplayName("Cascade#interdict")
final class CascadeInterdictTest
{
	@Nested
	@DisplayName("when a forbidden element is present")
	final class WhenForbiddenElementPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("throwsWhenForbiddenElementFoundCases")
		@DisplayName("throws wrath when any element satisfies the judgement")
		void throwsWhenForbiddenElementFound(final String as, final Cascade<Integer> source,
		                                     final Predicate<Integer> judgement)
		{
			assertThatThrownBy(() -> source.interdict(judgement, () -> new IllegalStateException("forbidden")))
					.as(as)
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("forbidden");
		}

		@Test
		@DisplayName("throws on first element when it matches")
		void throwsOnFirstElementWhenItMatches()
		{
			assertThatThrownBy(() -> Cascade.beckon(10, 1, 2).interdict(n -> n > 5,
					() -> new IllegalArgumentException("value too large")))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("value too large");
		}

		@Test
		@DisplayName("throws when only one element among many matches")
		void throwsWhenOneElementAmongManyMatches()
		{
			assertThatThrownBy(() -> Cascade.beckon(1, 2, 100, 3).interdict(n -> n > 50,
					() -> new IllegalStateException("outlier found")))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("outlier found");
		}

		@Test
		@DisplayName("throws when all elements match")
		void throwsWhenAllElementsMatch()
		{
			assertThatThrownBy(() -> Cascade.beckon(2, 4, 6).interdict(n -> n % 2 == 0,
					() -> new IllegalStateException("evens forbidden")))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("evens forbidden");
		}

		private static Stream<Arguments> throwsWhenForbiddenElementFoundCases()
		{
			return Stream.of(
					Arguments.of("single forbidden element — positive value",
							Cascade.beckon(1, 2, 3), (Predicate<Integer>) n -> n == 2),
					Arguments.of("forbidden predicate — negative value",
							Cascade.beckon(1, -1, 3), (Predicate<Integer>) n -> n < 0),
					Arguments.of("forbidden predicate — all elements match",
							Cascade.beckon(5, 10, 15), (Predicate<Integer>) n -> n > 0)
			);
		}
	}

	@Nested
	@DisplayName("when no forbidden element is present")
	final class WhenNoForbiddenElementPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("passesThroughWhenNoElementMatchesCases")
		@DisplayName("returns the cascade unchanged when no element satisfies the judgement")
		void passesThroughWhenNoElementMatches(final String as, final Cascade<Integer> source,
		                                       final Predicate<Integer> judgement)
		{
			assertThat(source.interdict(judgement, () -> new IllegalStateException("should not throw"))
			                 .summon())
					.as(as)
					.containsExactlyElementsOf(source.summon());
		}

		@Test
		@DisplayName("result is chainable after passing interdict")
		void resultIsChainableAfterPassingInterdict()
		{
			assertThat(Cascade.beckon(1, 2, 3, 4)
			                  .interdict(n -> n > 10, IllegalStateException::new)
			                  .discern(n -> n % 2 == 0)
			                  .summon())
					.as("chain continues after non-triggering interdict")
					.containsExactly(2, 4);
		}

		@Test
		@DisplayName("cascade content is fully preserved when interdict does not trigger")
		void cascadeContentFullyPreservedWhenNoTrigger()
		{
			final var source = Cascade.beckon(3, 1, 4, 1, 5);

			assertThat(source.interdict(n -> n > 100, IllegalStateException::new).summon())
					.as("no forbidden element — all elements preserved in original order")
					.containsExactly(3, 1, 4, 1, 5);
		}

		private static Stream<Arguments> passesThroughWhenNoElementMatchesCases()
		{
			return Stream.of(
					Arguments.of("no negative values — passes",
							Cascade.beckon(1, 2, 3), (Predicate<Integer>) n -> n < 0),
					Arguments.of("no values above threshold — passes",
							Cascade.beckon(1, 2, 3), (Predicate<Integer>) n -> n > 10),
					Arguments.of("single element does not match — passes",
							Cascade.beckon(5), (Predicate<Integer>) n -> n != 5)
			);
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("does nothing and returns absent cascade — no elements to forbid")
		void doesNothingAndReturnsAbsent()
		{
			assertThat(Cascade.<Integer>abyss()
			                  .interdict(_ -> true, () -> new IllegalStateException("should not throw"))
			                  .sterile())
					.as("interdict on absent cascade stays absent without throwing")
					.isTrue();
		}

		@Test
		@DisplayName("does not evaluate wrath when cascade is absent")
		void doesNotEvaluateWrathWhenAbsent()
		{
			final var called = new boolean[]{false};

			Cascade.<Integer>abyss().interdict(_ -> true, () ->
			{
				called[0] = true;
				return new IllegalStateException();
			});

			assertThat(called[0])
					.as("wrath supplier must not be called on absent cascade")
					.isFalse();
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
			assertThatThrownBy(() -> Cascade.beckon(1).interdict(null, IllegalStateException::new))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");
		}

		@Test
		@DisplayName("throws when wrath is null")
		void throwsWhenWrathIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).interdict(n -> n > 0, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("wrath may not be null");
		}
	}
}