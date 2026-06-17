package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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
}
