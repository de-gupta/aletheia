package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade ordain(comparator) tests")
final class CascadeOrdainComparatorTest
{
	@Nested
	@DisplayName("Aspects of explicit order")
	final class ExplicitOrderTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("ordainCases")
		@DisplayName("should sort by provided comparator")
		void shouldSortByProvidedComparator(final String as, final OrdainCase tc)
		{
			var result = tc.source().ordain(tc.comparator());

			assertThat(result.summon())
					.as("ordain(comparator) should follow given order for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> ordainCases()
		{
			return Stream.of(
					new OrdainCase(
							"Length-first ordering blesses short names first",
							Cascade.beckon("atlas", "ra", "apollo"),
							Comparator.comparingInt(String::length),
							List.of("ra", "atlas", "apollo")
					),
					new OrdainCase(
							"Reverse lexicographic decree",
							Cascade.beckon("b", "a", "c"),
							Comparator.reverseOrder(),
							List.of("c", "b", "a")
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record OrdainCase(String as, Cascade<String> source,
		                          Comparator<String> comparator,
		                          List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of guards")
	final class GuardTests
	{
		@Test
		@DisplayName("should reject null comparator on brook")
		void shouldRejectNullComparatorOnBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon("a").ordain(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("order may not be null");
		}

		@Test
		@DisplayName("should remain nadir for abyss source")
		void shouldRemainNadirForAbyssSource()
		{
			assertThat(Cascade.<String>abyss().ordain(String::compareTo)).isSameAs(Cascade.abyss());
		}
	}
}