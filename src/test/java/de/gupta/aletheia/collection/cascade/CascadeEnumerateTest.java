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

@DisplayName("Cascade#enumerate")
final class CascadeEnumerateTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("pairsWithZeroBasedIndexCases")
		@DisplayName("pairs each element with its zero-based position index")
		void pairsWithZeroBasedIndex(final String as, final EnumerateCase tc)
		{
			assertThat(tc.source().enumerate().summon())
					.as(as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("indices start at zero")
		void indicesStartAtZero()
		{
			final var result = Cascade.beckon("first", "second", "third").enumerate().summon().stream().toList();

			assertThat(result.getFirst().sinister())
					.as("first element has index 0")
					.isEqualTo(0);
		}

		@Test
		@DisplayName("indices are sequential with no gaps")
		void indicesAreSequential()
		{
			final var indices = Cascade.beckon("a", "b", "c", "d")
			                           .enumerate()
			                           .summon().stream()
			                           .map(Dyad::sinister)
			                           .toList();

			assertThat(indices)
					.as("indices must be 0, 1, 2, 3")
					.containsExactly(0, 1, 2, 3);
		}

		@Test
		@DisplayName("original values are preserved in dexter")
		void originalValuesPreservedInDexter()
		{
			final var values = Cascade.beckon(10, 20, 30)
			                          .enumerate()
			                          .summon().stream()
			                          .map(Dyad::dexter)
			                          .toList();

			assertThat(values)
					.as("dexter of each pair must be original element")
					.containsExactly(10, 20, 30);
		}

		@Test
		@DisplayName("enumerate after discern — indices restart from zero for remaining elements")
		void enumerateAfterDiscernRestartsIndices()
		{
			final var result = Cascade.beckon(1, 2, 3, 4, 5)
			                          .discern(n -> n % 2 == 0)
			                          .enumerate()
			                          .summon().stream().toList();

			assertThat(result)
					.as("even elements [2, 4] indexed from 0")
					.containsExactly(Dyad.of(0, 2), Dyad.of(1, 4));
		}

		@Test
		@DisplayName("enumerate result is chainable for further transformation")
		void enumerateResultIsChainable()
		{
			final var result = Cascade.beckon("a", "b", "c")
			                          .enumerate()
			                          .metamorphose(pair -> pair.sinister() + ":" + pair.dexter())
			                          .summon();

			assertThat(result)
					.as("indexed pairs transformed to formatted strings")
					.containsExactly("0:a", "1:b", "2:c");
		}

		@Test
		@DisplayName("indexed() delegates to enumerate()")
		void indexedDelegatesToEnumerate()
		{
			final Cascade<String> source = Cascade.beckon("x", "y", "z");

			assertThat(source.indexed().summon())
					.as("indexed() must equal enumerate()")
					.containsExactlyElementsOf(source.enumerate().summon());
		}

		@Test
		@DisplayName("works with Integer elements")
		void worksWithIntegerElements()
		{
			assertThat(Cascade.beckon(10, 20, 30).enumerate().summon())
					.as("integer elements paired with indices")
					.containsExactly(Dyad.of(0, 10), Dyad.of(1, 20), Dyad.of(2, 30));
		}

		private static Stream<Arguments> pairsWithZeroBasedIndexCases()
		{
			return Stream.of(
					new EnumerateCase("three strings",
							Cascade.beckon("a", "b", "c"),
							List.of(Dyad.of(0, "a"), Dyad.of(1, "b"), Dyad.of(2, "c"))),
					new EnumerateCase("single string",
							Cascade.beckon("only"),
							List.of(Dyad.of(0, "only"))),
					new EnumerateCase("two strings",
							Cascade.beckon("x", "y"),
							List.of(Dyad.of(0, "x"), Dyad.of(1, "y")))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record EnumerateCase(String as, Cascade<String> source, List<Dyad<Integer, String>> expected)
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
			assertThat(Cascade.abyss().enumerate().sterile())
					.as("enumerate on absent cascade stays absent")
					.isTrue();
		}
	}
}