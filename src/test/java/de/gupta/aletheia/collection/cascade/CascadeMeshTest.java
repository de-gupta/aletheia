package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.Dyad;
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

@DisplayName("Cascade#mesh")
final class CascadeMeshTest
{
	@Nested
	@DisplayName("when both Cascades are present")
	final class WhenBothPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("pairsElementsByPositionCases")
		@DisplayName("pairs elements by position — sinister from self, dexter from other")
		void pairsElementsByPosition(final String as, final MeshCase tc)
		{
			assertThat(tc.source().mesh(tc.other()).summon())
					.as(as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("works when both cascades have the same element type")
		void worksWithSameElementType()
		{
			assertThat(Cascade.beckon(10, 20, 30).mesh(Cascade.beckon(1, 2, 3)).summon())
					.as("integer meshed with integer — Dyad<Integer, Integer>")
					.containsExactly(Dyad.of(10, 1), Dyad.of(20, 2), Dyad.of(30, 3));
		}

		@Test
		@DisplayName("left cascade longer — truncates to right (shorter) length")
		void leftLongerTruncatesToRight()
		{
			assertThat(Cascade.beckon(1, 2, 3, 4).mesh(Cascade.beckon("a", "b")).summon())
					.as("left has 4, right has 2 — result has 2 pairs")
					.containsExactly(Dyad.of(1, "a"), Dyad.of(2, "b"));
		}

		@Test
		@DisplayName("right cascade longer — truncates to left (shorter) length")
		void rightLongerTruncatesToLeft()
		{
			assertThat(Cascade.beckon(1, 2).mesh(Cascade.beckon("a", "b", "c", "d")).summon())
					.as("left has 2, right has 4 — result has 2 pairs")
					.containsExactly(Dyad.of(1, "a"), Dyad.of(2, "b"));
		}

		@Test
		@DisplayName("sinister is element from self, dexter is element from other")
		void sinisterFromSelfDexterFromOther()
		{
			final var result = Cascade.beckon("left").mesh(Cascade.beckon("right")).summon().stream().toList();

			assertThat(result.getFirst().sinister()).as("sinister from self").isEqualTo("left");
			assertThat(result.getFirst().dexter()).as("dexter from other").isEqualTo("right");
		}

		@Test
		@DisplayName("result is chainable as Cascade<Dyad<E,F>>")
		void resultIsChainable()
		{
			assertThat(Cascade.beckon(1, 2, 3).mesh(Cascade.beckon("a", "b", "c"))
			                  .metamorphose(pair -> pair.sinister() + ":" + pair.dexter())
			                  .summon())
					.as("pairs transformed to formatted strings")
					.containsExactly("1:a", "2:b", "3:c");
		}

		@Test
		@DisplayName("zip() delegates to mesh()")
		void zipDelegatesToMesh()
		{
			final Cascade<Integer> source = Cascade.beckon(1, 2, 3);
			final Cascade<String> other = Cascade.beckon("a", "b", "c");

			assertThat(source.zip(other).summon())
					.as("zip() must equal mesh()")
					.containsExactlyElementsOf(source.mesh(other).summon());
		}

		@Test
		@DisplayName("throws when other is null")
		void throwsWhenOtherIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).mesh(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("other may not be null");
		}

		private static Stream<Arguments> pairsElementsByPositionCases()
		{
			return Stream.of(
					new MeshCase("equal length — three pairs",
							Cascade.beckon(1, 2, 3),
							Cascade.beckon("a", "b", "c"),
							List.of(Dyad.of(1, "a"), Dyad.of(2, "b"), Dyad.of(3, "c"))),
					new MeshCase("single element each — one pair",
							Cascade.beckon(42),
							Cascade.beckon("x"),
							List.of(Dyad.of(42, "x")))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record MeshCase(String as, Cascade<Integer> source, Cascade<String> other,
		                        List<Dyad<Integer, String>> expected)
		{
		}
	}

	@Nested
	@DisplayName("when one or both Cascades are empty")
	final class WhenEmptyCascadeInvolved
	{
		@Test
		@DisplayName("self is empty — result is empty")
		void selfEmptyResultIsEmpty()
		{
			assertThat(Cascade.<Integer>abyss().mesh(Cascade.beckon("a", "b")).sterile())
					.as("empty self meshed with present other — result absent")
					.isTrue();
		}

		@Test
		@DisplayName("other is empty — result is empty")
		void otherEmptyResultIsEmpty()
		{
			assertThat(Cascade.beckon(1, 2, 3).mesh(Cascade.<String>abyss()).summon())
					.as("present self meshed with empty other — result empty")
					.isEmpty();
		}

		@Test
		@DisplayName("both empty — result is empty")
		void bothEmptyResultIsEmpty()
		{
			assertThat(Cascade.<Integer>abyss().mesh(Cascade.<String>abyss()).sterile())
					.as("both absent — result absent")
					.isTrue();
		}
	}
}