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

@DisplayName("Cascade#forsake")
final class CascadeForsakeTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("skipsFirstNElementsCases")
		@DisplayName("returns the cascade without the first n elements")
		void skipsFirstNElements(final String as, final ForsakeCase tc)
		{
			assertThat(tc.source().forsake(tc.n()).summon())
					.as(as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("skip() delegates to forsake()")
		void skipDelegatesToForsake()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3, 4);

			assertThat(cascade.skip(2).summon())
					.as("skip() must equal forsake()")
					.containsExactlyElementsOf(cascade.forsake(2).summon());
		}

		@Test
		@DisplayName("forsake(0) returns the full cascade unchanged")
		void forsakeZeroReturnsFullCascade()
		{
			assertThat(Cascade.beckon(1, 2, 3).forsake(0).summon())
					.as("forsake(0) should return all elements")
					.containsExactly(1, 2, 3);
		}

		@Test
		@DisplayName("forsake(n) beyond size returns empty cascade")
		void forsakeMoreThanSizeReturnsEmpty()
		{
			assertThat(Cascade.beckon(1, 2, 3).forsake(10).summon())
					.as("forsake beyond size should return empty")
					.isEmpty();
		}

		private static Stream<Arguments> skipsFirstNElementsCases()
		{
			return Stream.of(
					new ForsakeCase("skip 1 of 4", Cascade.beckon(1, 2, 3, 4), 1, List.of(2, 3, 4)),
					new ForsakeCase("skip 2 of 5", Cascade.beckon(1, 2, 3, 4, 5), 2, List.of(3, 4, 5)),
					new ForsakeCase("skip all but last", Cascade.beckon(10, 20, 30), 2, List.of(30)),
					new ForsakeCase("skip none", Cascade.beckon(7, 8, 9), 0, List.of(7, 8, 9))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record ForsakeCase(String as, Cascade<Integer> source, int n, List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns empty regardless of n")
		void returnsEmptyRegardlessOfN()
		{
			assertThat(Cascade.abyss().forsake(5))
					.as("forsake on empty cascade")
					.isSameAs(Cascade.abyss());
		}
	}

	@Nested
	@DisplayName("forsake(Predicate) — removes elements matching predicate")
	final class ForsakeByPredicate
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("removesMatchingElementsCases")
			@DisplayName("removes elements where predicate holds, keeps the rest")
			void removesMatchingElements(final String as, final ForsakePredicateCase tc)
			{
				assertThat(tc.source().forsake(tc.judgement()).summon())
						.as(as)
						.containsExactlyElementsOf(tc.expected());
			}

			@Test
			@DisplayName("removes all elements when all match predicate")
			void removesAllWhenAllMatch()
			{
				assertThat(Cascade.beckon(2, 4, 6).forsake(n -> n % 2 == 0).summon())
						.as("all even — all removed")
						.isEmpty();
			}

			@Test
			@DisplayName("removes no elements when none match predicate")
			void removesNoneWhenNoneMatch()
			{
				assertThat(Cascade.beckon(1, 3, 5).forsake(n -> n % 2 == 0).summon())
						.as("no evens — nothing removed")
						.containsExactly(1, 3, 5);
			}

			@Test
			@DisplayName("is the exact complement of discern with same predicate")
			void isComplementOfDiscern()
			{
				final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 4, 5, 6);
				final Predicate<Integer> even = n -> n % 2 == 0;

				final var kept = source.discern(even).summon().stream().toList();
				final var removed = source.forsake(even).summon().stream().toList();

				assertThat(Stream.concat(kept.stream(), removed.stream()).sorted().toList())
						.as("discern(pred) + forsake(pred) together restore all original elements")
						.containsExactlyElementsOf(source.summon().stream().sorted().toList());
			}

			@Test
			@DisplayName("result is chainable after forsake(Predicate)")
			void resultIsChainableAfterForsake()
			{
				assertThat(Cascade.beckon(1, 2, 3, 4, 5)
				                  .forsake(n -> n % 2 == 0)
				                  .metamorphose(n -> n * 10)
				                  .summon())
						.as("odd elements transformed after removing evens")
						.containsExactly(10, 30, 50);
			}

			@Test
			@DisplayName("removeIf() delegates to forsake(Predicate)")
			void removeIfDelegatesToForsake()
			{
				final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 4, 5);

				assertThat(source.removeIf(n -> n % 2 == 0).summon())
						.as("removeIf() must equal forsake(Predicate)")
						.containsExactlyElementsOf(source.forsake(n -> n % 2 == 0).summon());
			}

			@Test
			@DisplayName("throws when judgement is null")
			void throwsWhenJudgementIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon(1).forsake(null))
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("judgement may not be null");
			}

			private static Stream<Arguments> removesMatchingElementsCases()
			{
				return Stream.of(
						new ForsakePredicateCase("removes even numbers, keeps odd",
								Cascade.beckon(1, 2, 3, 4, 5), n -> n % 2 == 0, List.of(1, 3, 5)),
						new ForsakePredicateCase("removes negatives, keeps non-negative",
								Cascade.beckon(-1, 2, -3, 4), n -> n < 0, List.of(2, 4)),
						new ForsakePredicateCase("removes single matching element",
								Cascade.beckon(1, 99, 2, 3), n -> n == 99, List.of(1, 2, 3)),
						new ForsakePredicateCase("removes elements above threshold",
								Cascade.beckon(1, 5, 2, 8, 3), n -> n > 4, List.of(1, 2, 3))
				).map(tc -> Arguments.of(tc.as(), tc));
			}

			private record ForsakePredicateCase(String as, Cascade<Integer> source,
			                                    Predicate<Integer> judgement, List<Integer> expected)
			{
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns absent cascade")
			void returnsAbsent()
			{
				assertThat(Cascade.<Integer>abyss().forsake(_ -> true).sterile())
						.as("forsake(Predicate) on absent cascade stays absent")
						.isTrue();
			}
		}
	}
}