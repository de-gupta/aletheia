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

@DisplayName("Cascade#temper")
final class CascadeTemperTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("limitsToFirstNElementsCases")
		@DisplayName("returns at most the first n elements")
		void limitsToFirstNElements(final String as, final TemperCase tc)
		{
			assertThat(tc.source().temper(tc.n()).summon())
					.as(as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("limit() delegates to temper()")
		void limitDelegatesToTemper()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3, 4);

			assertThat(cascade.limit(2).summon())
					.as("limit() must equal temper()")
					.containsExactlyElementsOf(cascade.temper(2).summon());
		}

		@Test
		@DisplayName("temper(0) returns an empty cascade")
		void temperZeroReturnsEmpty()
		{
			assertThat(Cascade.beckon(1, 2, 3).temper(0).summon())
					.as("temper(0) should return no elements")
					.isEmpty();
		}

		@Test
		@DisplayName("temper(n) beyond size returns all elements")
		void temperMoreThanSizeReturnsAll()
		{
			assertThat(Cascade.beckon(1, 2, 3).temper(10).summon())
					.as("temper beyond size should return all elements")
					.containsExactly(1, 2, 3);
		}

		private static Stream<Arguments> limitsToFirstNElementsCases()
		{
			return Stream.of(
					new TemperCase("limit to 1 of 4", Cascade.beckon(1, 2, 3, 4), 1, List.of(1)),
					new TemperCase("limit to 3 of 5", Cascade.beckon(1, 2, 3, 4, 5), 3, List.of(1, 2, 3)),
					new TemperCase("limit to all", Cascade.beckon(7, 8, 9), 3, List.of(7, 8, 9)),
					new TemperCase("limit to 0", Cascade.beckon(10, 20, 30), 0, List.of())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record TemperCase(String as, Cascade<Integer> source, int n, List<Integer> expected)
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
			assertThat(Cascade.abyss().temper(5))
					.as("temper on empty cascade")
					.isSameAs(Cascade.abyss());
		}
	}
}