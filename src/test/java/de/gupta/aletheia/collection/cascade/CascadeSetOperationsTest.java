package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade set operations — union, intersect, subtract")
final class CascadeSetOperationsTest
{
	@Nested
	@DisplayName("union — combines both cascades, deduplicates")
	final class Union
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("unionCases")
		@DisplayName("returns all distinct elements from both cascades")
		void returnsAllDistinctElements(final String as, final SetCase tc)
		{
			assertThat(tc.left().union(tc.right()).summon())
					.as(as)
					.containsExactlyInAnyOrderElementsOf(tc.expected());
		}

		@Test
		@DisplayName("self-union returns same distinct elements")
		void selfUnionReturnsDistinctElements()
		{
			assertThat(Cascade.beckon(1, 2, 2, 3).union(Cascade.beckon(1, 2, 3)).summon())
					.as("union with self deduplicates")
					.containsExactlyInAnyOrder(1, 2, 3);
		}

		@Test
		@DisplayName("union with empty cascade returns original (deduplicated)")
		void unionWithEmptyReturnsOriginal()
		{
			assertThat(Cascade.beckon(1, 2, 3).union(Cascade.abyss()).summon())
					.as("union with empty = self")
					.containsExactlyInAnyOrder(1, 2, 3);
		}

		@Test
		@DisplayName("empty union with non-empty returns the other")
		void emptyUnionWithNonEmpty()
		{
			assertThat(Cascade.<Integer>abyss().union(Cascade.beckon(1, 2, 3)).summon())
					.as("empty union other = other")
					.containsExactlyInAnyOrder(1, 2, 3);
		}

		@Test
		@DisplayName("throws when other is null")
		void throwsWhenOtherIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).union(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("other may not be null");
		}

		private static Stream<Arguments> unionCases()
		{
			return Stream.of(
					new SetCase("disjoint sets — all elements included",
							Cascade.beckon(1, 2, 3), Cascade.beckon(4, 5, 6),
							List.of(1, 2, 3, 4, 5, 6)),
					new SetCase("overlapping sets — duplicates removed",
							Cascade.beckon(1, 2, 3), Cascade.beckon(2, 3, 4),
							List.of(1, 2, 3, 4)),
					new SetCase("identical sets — same elements returned",
							Cascade.beckon(1, 2, 3), Cascade.beckon(1, 2, 3),
							List.of(1, 2, 3))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record SetCase(String as, Cascade<Integer> left, Cascade<Integer> right, List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("intersect — keeps only elements present in both")
	final class Intersect
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("intersectCases")
		@DisplayName("retains only elements that appear in the other cascade")
		void retainsOnlySharedElements(final String as, final SetCase tc)
		{
			assertThat(tc.left().intersect(tc.right()).summon())
					.as(as)
					.containsExactlyInAnyOrderElementsOf(tc.expected());
		}

		@Test
		@DisplayName("intersection with empty cascade is always empty")
		void intersectWithEmptyIsEmpty()
		{
			assertThat(Cascade.beckon(1, 2, 3).intersect(Cascade.abyss()).summon())
					.as("intersect with empty = empty")
					.isEmpty();
		}

		@Test
		@DisplayName("empty intersection with non-empty stays absent")
		void emptyIntersectWithNonEmptyIsEmpty()
		{
			assertThat(Cascade.<Integer>abyss().intersect(Cascade.beckon(1, 2, 3)).sterile())
					.as("empty intersect other = absent")
					.isTrue();
		}

		@Test
		@DisplayName("self-intersection preserves all elements")
		void selfIntersectionPreservesAll()
		{
			assertThat(Cascade.beckon(1, 2, 3).intersect(Cascade.beckon(1, 2, 3)).summon())
					.as("intersect with self = self")
					.containsExactlyInAnyOrder(1, 2, 3);
		}

		@Test
		@DisplayName("throws when other is null")
		void throwsWhenOtherIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).intersect(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("other may not be null");
		}

		private static Stream<Arguments> intersectCases()
		{
			return Stream.of(
					new SetCase("fully overlapping — all shared",
							Cascade.beckon(1, 2, 3), Cascade.beckon(1, 2, 3),
							List.of(1, 2, 3)),
					new SetCase("partially overlapping — shared subset",
							Cascade.beckon(1, 2, 3, 4), Cascade.beckon(2, 4, 6),
							List.of(2, 4)),
					new SetCase("disjoint — no shared elements",
							Cascade.beckon(1, 2, 3), Cascade.beckon(4, 5, 6),
							List.of())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record SetCase(String as, Cascade<Integer> left, Cascade<Integer> right, List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("subtract — removes elements present in other")
	final class Subtract
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("subtractCases")
		@DisplayName("removes elements that appear in the other cascade")
		void removesElementsInOther(final String as, final SetCase tc)
		{
			assertThat(tc.left().subtract(tc.right()).summon())
					.as(as)
					.containsExactlyInAnyOrderElementsOf(tc.expected());
		}

		@Test
		@DisplayName("subtract empty cascade leaves original unchanged")
		void subtractEmptyLeavesOriginal()
		{
			assertThat(Cascade.beckon(1, 2, 3).subtract(Cascade.abyss()).summon())
					.as("subtract empty = self")
					.containsExactlyInAnyOrder(1, 2, 3);
		}

		@Test
		@DisplayName("subtract self leaves empty")
		void subtractSelfLeavesEmpty()
		{
			assertThat(Cascade.beckon(1, 2, 3).subtract(Cascade.beckon(1, 2, 3)).summon())
					.as("subtract self = empty")
					.isEmpty();
		}

		@Test
		@DisplayName("union, intersect, subtract are consistent — A∪B minus A∩B = symmetric difference")
		void unionMinusIntersectEqualsSymmetricDifference()
		{
			final Cascade<Integer> a = Cascade.beckon(1, 2, 3, 4);
			final Cascade<Integer> b = Cascade.beckon(3, 4, 5, 6);

			final var unionMinusIntersect = a.union(b).subtract(a.intersect(b)).summon();

			assertThat(unionMinusIntersect)
					.as("symmetric difference = elements in exactly one of A or B")
					.containsExactlyInAnyOrder(1, 2, 5, 6);
		}

		@Test
		@DisplayName("throws when other is null")
		void throwsWhenOtherIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).subtract(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("other may not be null");
		}

		private static Stream<Arguments> subtractCases()
		{
			return Stream.of(
					new SetCase("remove subset — remaining elements kept",
							Cascade.beckon(1, 2, 3, 4, 5), Cascade.beckon(2, 4),
							List.of(1, 3, 5)),
					new SetCase("remove superset — all removed",
							Cascade.beckon(1, 2), Cascade.beckon(1, 2, 3, 4),
							List.of()),
					new SetCase("remove disjoint — nothing removed",
							Cascade.beckon(1, 2, 3), Cascade.beckon(4, 5, 6),
							List.of(1, 2, 3))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record SetCase(String as, Cascade<Integer> left, Cascade<Integer> right, List<Integer> expected)
		{
		}
	}
}