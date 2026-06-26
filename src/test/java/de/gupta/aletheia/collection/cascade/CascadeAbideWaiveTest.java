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

@DisplayName("Cascade#abide and Cascade#waive")
final class CascadeAbideWaiveTest
{
	@Nested
	@DisplayName("abide — takes elements while predicate holds")
	final class Abide
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("takesWhileCases")
			@DisplayName("takes elements from head while predicate holds, stops at first failure")
			void takesWhilePredicateHolds(final String as, final AbideCase tc)
			{
				assertThat(tc.source().abide(tc.judgement()).summon())
						.as(as)
						.containsExactlyElementsOf(tc.expected());
			}

			@Test
			@DisplayName("stops immediately when first element fails predicate")
			void stopsImmediatelyWhenFirstElementFails()
			{
				assertThat(Cascade.beckon(1, 2, 3, 4).abide(n -> n > 5).summon())
						.as("first element fails — result is empty")
						.isEmpty();
			}

			@Test
			@DisplayName("takes all elements when all satisfy predicate")
			void takesAllWhenAllSatisfy()
			{
				assertThat(Cascade.beckon(2, 4, 6, 8).abide(n -> n % 2 == 0).summon())
						.as("all even — all taken")
						.containsExactly(2, 4, 6, 8);
			}

			@Test
			@DisplayName("stops mid-sequence and does not resume after a later match")
			void doesNotResumeAfterFirstFailure()
			{
				assertThat(Cascade.beckon(1, 2, 3, 10, 4, 5).abide(n -> n < 5).summon())
						.as("stops at 10, does not take 4 and 5 even though they match")
						.containsExactly(1, 2, 3);
			}

			@Test
			@DisplayName("takeWhile() delegates to abide()")
			void takeWhileDelegatesToAbide()
			{
				final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 4, 5);

				assertThat(source.takeWhile(n -> n < 4).summon())
						.as("takeWhile() must equal abide()")
						.containsExactlyElementsOf(source.abide(n -> n < 4).summon());
			}

			private static Stream<Arguments> takesWhileCases()
			{
				return Stream.of(
						new AbideCase("takes prefix while positive",
								Cascade.beckon(1, 2, 3, -1, 4), n -> n > 0, List.of(1, 2, 3)),
						new AbideCase("takes single element when second fails",
								Cascade.beckon(1, 5, 2), n -> n < 3, List.of(1)),
						new AbideCase("takes none when first fails",
								Cascade.beckon(10, 1, 2), n -> n < 5, List.of()),
						new AbideCase("takes all when all pass",
								Cascade.beckon(1, 2, 3), n -> n > 0, List.of(1, 2, 3))
				).map(tc -> Arguments.of(tc.as(), tc));
			}

			private record AbideCase(String as, Cascade<Integer> source,
			                         Predicate<Integer> judgement, List<Integer> expected)
			{
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns absent Cascade")
			void returnsAbsent()
			{
				assertThat(Cascade.<Integer>abyss().abide(n -> n > 0).sterile())
						.as("abide on absent cascade stays absent")
						.isTrue();
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
				assertThatThrownBy(() -> Cascade.beckon(1).abide(null))
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("judgement may not be null");
			}
		}
	}

	@Nested
	@DisplayName("waive — drops elements while predicate holds, keeps rest")
	final class Waive
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("dropsWhileCases")
			@DisplayName("drops elements from head while predicate holds, keeps rest")
			void dropsWhilePredicateHolds(final String as, final WaiveCase tc)
			{
				assertThat(tc.source().waive(tc.judgement()).summon())
						.as(as)
						.containsExactlyElementsOf(tc.expected());
			}

			@Test
			@DisplayName("keeps all when first element fails predicate")
			void keepsAllWhenFirstElementFails()
			{
				assertThat(Cascade.beckon(10, 1, 2, 3).waive(n -> n < 5).summon())
						.as("first element fails — nothing dropped")
						.containsExactly(10, 1, 2, 3);
			}

			@Test
			@DisplayName("drops all when all elements satisfy predicate")
			void dropsAllWhenAllSatisfy()
			{
				assertThat(Cascade.beckon(2, 4, 6).waive(n -> n % 2 == 0).summon())
						.as("all even — all dropped")
						.isEmpty();
			}

			@Test
			@DisplayName("keeps elements after first failure even when they would match")
			void keepsElementsAfterFirstFailureEvenIfTheyMatch()
			{
				assertThat(Cascade.beckon(1, 2, 10, 3, 4).waive(n -> n < 5).summon())
						.as("drops 1 and 2, stops at 10, keeps 10, 3, 4 regardless")
						.containsExactly(10, 3, 4);
			}

			@Test
			@DisplayName("dropWhile() delegates to waive()")
			void dropWhileDelegatesToWaive()
			{
				final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 4, 5);

				assertThat(source.dropWhile(n -> n < 4).summon())
						.as("dropWhile() must equal waive()")
						.containsExactlyElementsOf(source.waive(n -> n < 4).summon());
			}

			@Test
			@DisplayName("abide and waive together cover the full cascade")
			void abideAndWaiveTogetherCoverFullCascade()
			{
				final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 10, 11);
				final Predicate<Integer> small = n -> n < 5;

				final var taken = source.abide(small).summon().stream().toList();
				final var dropped = source.waive(small).summon().stream().toList();

				assertThat(Stream.concat(taken.stream(), dropped.stream()).toList())
						.as("abide prefix + waive suffix = full cascade")
						.containsExactly(1, 2, 3, 10, 11);
			}

			private static Stream<Arguments> dropsWhileCases()
			{
				return Stream.of(
						new WaiveCase("drops prefix while positive, keeps rest",
								Cascade.beckon(1, 2, 3, -1, 4), n -> n > 0, List.of(-1, 4)),
						new WaiveCase("drops single element, keeps rest",
								Cascade.beckon(1, 5, 2), n -> n < 3, List.of(5, 2)),
						new WaiveCase("drops none when first fails",
								Cascade.beckon(10, 1, 2), n -> n < 5, List.of(10, 1, 2)),
						new WaiveCase("drops all when all pass",
								Cascade.beckon(1, 2, 3), n -> n > 0, List.of())
				).map(tc -> Arguments.of(tc.as(), tc));
			}

			private record WaiveCase(String as, Cascade<Integer> source,
			                         Predicate<Integer> judgement, List<Integer> expected)
			{
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns absent Cascade")
			void returnsAbsent()
			{
				assertThat(Cascade.<Integer>abyss().waive(n -> n > 0).sterile())
						.as("waive on absent cascade stays absent")
						.isTrue();
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
				assertThatThrownBy(() -> Cascade.beckon(1).waive(null))
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("judgement may not be null");
			}
		}
	}
}