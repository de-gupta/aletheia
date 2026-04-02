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

@DisplayName("Cascade ordain tests")
final class CascadeOrdainTest
{
	@Nested
	@DisplayName("Aspects of natural order")
	final class NaturalOrderTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("naturalOrderCases")
		@DisplayName("should sort by natural ordering")
		void shouldSortByNaturalOrdering(final String as, final NaturalOrderCase tc)
		{
			var result = tc.source().ordain();

			assertThat(result.summon())
					.as("ordain() should follow natural order for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> naturalOrderCases()
		{
			return Stream.of(
					new NaturalOrderCase(
							"Numbers are lined by numeric order",
							Cascade.beckon(9, 1, 4, 2),
							List.of(1, 2, 4, 9)
					),
					new NaturalOrderCase(
							"Duplicates keep sorted multiplicity",
							Cascade.beckon(3, 2, 3, 1),
							List.of(1, 2, 3, 3)
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record NaturalOrderCase(String as, Cascade<Integer> source, List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of comparability")
	final class ComparabilityTests
	{
		@Test
		@DisplayName("should fail on non-comparable elements")
		void shouldFailOnNonComparableElements()
		{
			record Rune(int value)
			{
			}

			assertThatThrownBy(() -> Cascade.beckon(new Rune(1), new Rune(2)).ordain().summon())
					.isInstanceOf(ClassCastException.class);
		}

		@Test
		@DisplayName("should stay nadir for abyss source")
		void shouldStayNadirForAbyssSource()
		{
			assertThat(Cascade.abyss().ordain()).isSameAs(Cascade.abyss());
		}
	}
}