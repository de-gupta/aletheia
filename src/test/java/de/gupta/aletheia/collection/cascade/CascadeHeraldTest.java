package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.functional.Unfolding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cascade#herald")
final class CascadeHeraldTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("returnsFirstElementCases")
		@DisplayName("returns the first element as a present Unfolding")
		void returnsFirstElement(final String as, final HeraldCase tc)
		{
			assertThat(tc.source().herald())
					.as(as)
					.isEqualTo(tc.expected());
		}

		@Test
		@DisplayName("first() delegates to herald()")
		void firstDelegatesToHerald()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3);

			assertThat(cascade.first())
					.as("first() must equal herald()")
					.isEqualTo(cascade.herald());
		}

		@Test
		@DisplayName("returns first element after discern narrows the cascade")
		void returnsFirstElementAfterDiscern()
		{
			assertThat(Cascade.beckon(1, 2, 3, 4, 5)
			                  .discern(n -> n % 2 == 0)
			                  .herald())
					.as("first even number in [1,2,3,4,5]")
					.isEqualTo(Unfolding.beckon(2));
		}

		@Test
		@DisplayName("returns empty when discern eliminates all elements")
		void returnsEmptyWhenDiscernEliminatesAll()
		{
			assertThat(Cascade.beckon(1, 3, 5)
			                  .discern(n -> n % 2 == 0)
			                  .herald())
					.as("no even numbers — herald should be empty")
					.isEqualTo(Unfolding.chaos());
		}

		private static Stream<Arguments> returnsFirstElementCases()
		{
			return Stream.of(
					new HeraldCase("single element",
							Cascade.beckon(42),
							Unfolding.beckon(42)),
					new HeraldCase("first of many integers",
							Cascade.beckon(1, 2, 3),
							Unfolding.beckon(1)),
					new HeraldCase("first of many strings",
							Cascade.beckon("alpha", "beta", "gamma"),
							Unfolding.beckon("alpha")),
					new HeraldCase("first from collection",
							Cascade.beckon(List.of(10, 20, 30)),
							Unfolding.beckon(10))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record HeraldCase(String as, Cascade<?> source, Unfolding<?> expected)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns empty Unfolding")
		void returnsEmptyUnfolding()
		{
			assertThat(Cascade.abyss().herald())
					.as("abyss herald should be empty")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("first() returns empty Unfolding")
		void firstReturnsEmptyUnfolding()
		{
			assertThat(Cascade.<Integer>abyss().first())
					.as("abyss first() should be empty")
					.isEqualTo(Unfolding.chaos());
		}
	}
}