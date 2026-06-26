package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.functional.Unfolding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cascade#at")
final class CascadeAtTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("returnsElementAtIndexCases")
		@DisplayName("returns element at valid index as present Unfolding")
		void returnsElementAtIndex(final String as, final AtCase tc)
		{
			assertThat(tc.source().at(tc.index()))
					.as(as)
					.isEqualTo(tc.expected());
		}

		@Test
		@DisplayName("returns empty for index equal to size")
		void returnsEmptyWhenIndexEqualsSize()
		{
			assertThat(Cascade.beckon("a", "b", "c").at(3))
					.as("index 3 on size-3 cascade — out of bounds")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("returns empty for index greater than size")
		void returnsEmptyWhenIndexExceedsSize()
		{
			assertThat(Cascade.beckon(1, 2).at(10))
					.as("index far beyond size")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("returns empty for negative index")
		void returnsEmptyForNegativeIndex()
		{
			assertThat(Cascade.beckon(1, 2, 3).at(-1))
					.as("negative index is invalid")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("returns empty for large negative index")
		void returnsEmptyForLargeNegativeIndex()
		{
			assertThat(Cascade.beckon(1, 2, 3).at(Integer.MIN_VALUE))
					.as("MIN_VALUE index is invalid")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("at() respects ordering after discern")
		void respectsOrderingAfterDiscern()
		{
			assertThat(Cascade.beckon(1, 2, 3, 4, 5).discern(n -> n % 2 == 0).at(1))
					.as("second even number in [1,2,3,4,5] is 4")
					.isEqualTo(Unfolding.beckon(4));
		}

		@Test
		@DisplayName("get() delegates to at()")
		void getDelegatesToAt()
		{
			final Cascade<String> source = Cascade.beckon("x", "y", "z");

			assertThat(source.get(1))
					.as("get() must equal at()")
					.isEqualTo(source.at(1));
		}

		private static Stream<Arguments> returnsElementAtIndexCases()
		{
			return Stream.of(
					new AtCase("index 0 — first element", Cascade.beckon("a", "b", "c"), 0, Unfolding.beckon("a")),
					new AtCase("index 1 — middle element", Cascade.beckon("a", "b", "c"), 1, Unfolding.beckon("b")),
					new AtCase("index 2 — last element", Cascade.beckon("a", "b", "c"), 2, Unfolding.beckon("c")),
					new AtCase("single element at index 0", Cascade.beckon(42), 0, Unfolding.beckon(42)),
					new AtCase("integer cascade middle", Cascade.beckon(10, 20, 30, 40), 2, Unfolding.beckon(30))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record AtCase(String as, Cascade<?> source, int index, Unfolding<?> expected)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns empty Unfolding for any index")
		void returnsEmptyForAnyIndex()
		{
			assertThat(Cascade.<String>abyss().at(0))
					.as("at(0) on empty cascade")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("returns empty Unfolding for negative index")
		void returnsEmptyForNegativeIndex()
		{
			assertThat(Cascade.<String>abyss().at(-1))
					.as("at(-1) on empty cascade")
					.isEqualTo(Unfolding.chaos());
		}
	}
}