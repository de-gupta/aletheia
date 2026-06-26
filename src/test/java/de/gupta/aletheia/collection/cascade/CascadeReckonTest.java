package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade#reckon")
final class CascadeReckonTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("countsElementsCases")
		@DisplayName("returns the number of elements")
		void returnsNumberOfElements(final String as, final Cascade<?> source, final long expected)
		{
			assertThat(source.reckon())
					.as(as)
					.isEqualTo(expected);
		}

		@Test
		@DisplayName("tally() delegates to reckon()")
		void tallyDelegatesToReckon()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3);

			assertThat(cascade.tally())
					.as("tally() must equal reckon()")
					.isEqualTo(cascade.reckon());
		}

		@Test
		@DisplayName("reflects count after discern narrows the cascade")
		void reflectsCountAfterDiscern()
		{
			assertThat(Cascade.beckon(1, 2, 3, 4, 5).discern(n -> n % 2 == 0).reckon())
					.as("even numbers in [1,2,3,4,5]")
					.isEqualTo(2L);
		}

		private static Stream<Arguments> countsElementsCases()
		{
			return Stream.of(
					Arguments.of("single element", Cascade.beckon(42), 1L),
					Arguments.of("three elements", Cascade.beckon(1, 2, 3), 3L),
					Arguments.of("five strings", Cascade.beckon("a", "b", "c", "d", "e"), 5L)
			);
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns zero")
		void returnsZero()
		{
			assertThat(Cascade.abyss().reckon())
					.as("empty cascade count")
					.isEqualTo(0L);
		}
	}

	@Nested
	@DisplayName("reckon(Predicate) — count matching elements")
	final class ReckonWithPredicate
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("countsMatchingElementsCases")
			@DisplayName("returns the count of elements satisfying the predicate")
			void countsMatchingElements(final String as, final Cascade<Integer> source,
			                            final java.util.function.Predicate<Integer> judgement, final long expected)
			{
				assertThat(source.reckon(judgement)).as(as).isEqualTo(expected);
			}

			@Test
			@DisplayName("returns zero when no elements match")
			void returnsZeroWhenNoMatch()
			{
				assertThat(Cascade.beckon(1, 3, 5).reckon(n -> n % 2 == 0))
						.as("no even numbers")
						.isEqualTo(0L);
			}

			@Test
			@DisplayName("returns full count when all elements match")
			void returnsFullCountWhenAllMatch()
			{
				assertThat(Cascade.beckon(2, 4, 6).reckon(n -> n % 2 == 0))
						.as("all even numbers")
						.isEqualTo(3L);
			}

			@Test
			@DisplayName("counts correctly after discern narrows the cascade")
			void countsCorrectlyAfterDiscern()
			{
				assertThat(Cascade.beckon(1, 2, 3, 4, 5, 6)
				                  .discern(n -> n % 2 == 0)
				                  .reckon(n -> n > 3))
						.as("even numbers above 3 in [1..6]")
						.isEqualTo(2L);
			}

			@Test
			@DisplayName("tally(Predicate) delegates to reckon(Predicate)")
			void tallyDelegatesToReckon()
			{
				final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3, 4, 5);

				assertThat(cascade.tally(n -> n % 2 == 0))
						.as("tally(Predicate) must equal reckon(Predicate)")
						.isEqualTo(cascade.reckon(n -> n % 2 == 0));
			}

			@Test
			@DisplayName("throws when judgement is null")
			void throwsWhenJudgementIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon(1).reckon(null))
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("judgement may not be null");
			}

			private static Stream<Arguments> countsMatchingElementsCases()
			{
				return Stream.of(
						Arguments.of("count even numbers", Cascade.beckon(1, 2, 3, 4, 5),
								(java.util.function.Predicate<Integer>) n -> n % 2 == 0, 2L),
						Arguments.of("count positives", Cascade.beckon(-1, 2, -3, 4),
								(java.util.function.Predicate<Integer>) n -> n > 0, 2L),
						Arguments.of("count above threshold", Cascade.beckon(5, 10, 15, 20),
								(java.util.function.Predicate<Integer>) n -> n > 10, 2L),
						Arguments.of("count single match", Cascade.beckon(7),
								(java.util.function.Predicate<Integer>) n -> n == 7, 1L)
				);
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns zero regardless of predicate")
			void returnsZeroRegardlessOfPredicate()
			{
				assertThat(Cascade.<Integer>abyss().reckon(n -> true))
						.as("reckon with always-true predicate on empty cascade")
						.isEqualTo(0L);
			}
		}
	}
}
