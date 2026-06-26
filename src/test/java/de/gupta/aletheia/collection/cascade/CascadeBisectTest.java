package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.Dyad;
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

@DisplayName("Cascade#bisect")
final class CascadeBisectTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("partitionsCorrectlyCases")
		@DisplayName("splits into matching (sinister) and non-matching (dexter) Cascades")
		void partitionsCorrectly(final String as, final BisectCase tc)
		{
			final Dyad<Cascade<Integer>, Cascade<Integer>> result = tc.source().bisect(tc.judgement());

			assertThat(result.sinister().summon())
					.as(as + " — sinister (matching)")
					.containsExactlyInAnyOrderElementsOf(tc.expectedMatching());

			assertThat(result.dexter().summon())
					.as(as + " — dexter (non-matching)")
					.containsExactlyInAnyOrderElementsOf(tc.expectedNonMatching());
		}

		@Test
		@DisplayName("all elements match — sinister has all, dexter is empty")
		void allMatchSinisterHasAllDexterEmpty()
		{
			final Dyad<Cascade<Integer>, Cascade<Integer>> result =
					Cascade.beckon(2, 4, 6).bisect(n -> n % 2 == 0);

			assertThat(result.sinister().summon())
					.as("sinister — all even numbers")
					.containsExactlyInAnyOrder(2, 4, 6);

			assertThat(result.dexter().summon())
					.as("dexter — empty since all matched")
					.isEmpty();
		}

		@Test
		@DisplayName("no elements match — sinister is empty, dexter has all")
		void noneMatchSinisterEmptyDexterHasAll()
		{
			final Dyad<Cascade<Integer>, Cascade<Integer>> result =
					Cascade.beckon(1, 3, 5).bisect(n -> n % 2 == 0);

			assertThat(result.sinister().summon())
					.as("sinister — empty since none matched")
					.isEmpty();

			assertThat(result.dexter().summon())
					.as("dexter — all odd numbers")
					.containsExactlyInAnyOrder(1, 3, 5);
		}

		@Test
		@DisplayName("sinister and dexter together contain all original elements")
		void sinisterAndDexterContainAllOriginalElements()
		{
			final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 4, 5, 6);
			final Dyad<Cascade<Integer>, Cascade<Integer>> result = source.bisect(n -> n % 2 == 0);

			final var combined = Stream.concat(
					result.sinister().summon().stream(),
					result.dexter().summon().stream()
			).toList();

			assertThat(combined)
					.as("sinister + dexter = full original cascade")
					.containsExactlyInAnyOrderElementsOf(source.summon());
		}

		@Test
		@DisplayName("order is preserved within each partition")
		void orderPreservedWithinEachPartition()
		{
			final Dyad<Cascade<Integer>, Cascade<Integer>> result =
					Cascade.beckon(3, 1, 4, 1, 5, 9, 2, 6).bisect(n -> n % 2 == 0);

			assertThat(result.sinister().summon())
					.as("even numbers in encounter order")
					.containsExactly(4, 2, 6);

			assertThat(result.dexter().summon())
					.as("odd numbers in encounter order")
					.containsExactly(3, 1, 1, 5, 9);
		}

		@Test
		@DisplayName("sinister is chainable as a Cascade")
		void sinisterIsChainable()
		{
			final Dyad<Cascade<Integer>, Cascade<Integer>> result =
					Cascade.beckon(1, 2, 3, 4, 5, 6).bisect(n -> n % 2 == 0);

			assertThat(result.sinister().metamorphose(n -> n * 10).summon())
					.as("sinister cascade can be further transformed")
					.containsExactlyInAnyOrder(20, 40, 60);
		}

		@Test
		@DisplayName("partition() delegates to bisect()")
		void partitionDelegatesToBisect()
		{
			final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 4);
			final Predicate<Integer> even = n -> n % 2 == 0;

			final var via_bisect = source.bisect(even);
			final var via_partition = source.partition(even);

			assertThat(via_partition.sinister().summon())
					.as("partition() sinister must equal bisect() sinister")
					.containsExactlyElementsOf(via_bisect.sinister().summon());

			assertThat(via_partition.dexter().summon())
					.as("partition() dexter must equal bisect() dexter")
					.containsExactlyElementsOf(via_bisect.dexter().summon());
		}

		private static Stream<Arguments> partitionsCorrectlyCases()
		{
			return Stream.of(
					new BisectCase("even/odd split",
							Cascade.beckon(1, 2, 3, 4, 5),
							n -> n % 2 == 0,
							List.of(2, 4), List.of(1, 3, 5)),
					new BisectCase("positive/negative split",
							Cascade.beckon(-2, 1, -3, 4, 0),
							n -> n > 0,
							List.of(1, 4), List.of(-2, -3, 0)),
					new BisectCase("above-threshold split",
							Cascade.beckon(5, 10, 3, 8, 1),
							n -> n > 5,
							List.of(10, 8), List.of(5, 3, 1)),
					new BisectCase("single element matches",
							Cascade.beckon(7),
							n -> n == 7,
							List.of(7), List.of()),
					new BisectCase("single element does not match",
							Cascade.beckon(7),
							n -> n == 0,
							List.of(), List.of(7))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record BisectCase(String as, Cascade<Integer> source, Predicate<Integer> judgement,
		                          List<Integer> expectedMatching, List<Integer> expectedNonMatching)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("both sinister and dexter are empty")
		void bothPartitionsAreEmpty()
		{
			final Dyad<Cascade<Integer>, Cascade<Integer>> result =
					Cascade.<Integer>abyss().bisect(n -> n > 0);

			assertThat(result.sinister().sterile())
					.as("sinister — empty from Nadir")
					.isTrue();

			assertThat(result.dexter().sterile())
					.as("dexter — empty from Nadir")
					.isTrue();
		}
	}

	@Nested
	@DisplayName("with null arguments")
	final class WithNullArguments
	{
		@Test
		@DisplayName("throws when judgement is null on present cascade")
		void throwsWhenJudgementIsNullOnPresentCascade()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).bisect(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");
		}

		@Test
		@DisplayName("throws when judgement is null on empty cascade")
		void throwsWhenJudgementIsNullOnEmptyCascade()
		{
			assertThatThrownBy(() -> Cascade.<Integer>abyss().bisect(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");
		}
	}
}