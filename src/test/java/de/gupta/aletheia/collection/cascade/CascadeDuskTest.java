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

@DisplayName("Cascade#dusk")
final class CascadeDuskTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("returnsLastElementCases")
		@DisplayName("returns the last element as a present Unfolding")
		void returnsLastElement(final String as, final DuskCase tc)
		{
			assertThat(tc.source().dusk())
					.as(as)
					.isEqualTo(tc.expected());
		}

		@Test
		@DisplayName("last() delegates to dusk()")
		void lastDelegatesToDusk()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3);

			assertThat(cascade.last())
					.as("last() must equal dusk()")
					.isEqualTo(cascade.dusk());
		}

		@Test
		@DisplayName("returns last element after discern narrows the cascade")
		void returnsLastElementAfterDiscern()
		{
			assertThat(Cascade.beckon(1, 2, 3, 4, 5)
			                  .discern(n -> n % 2 == 0)
			                  .dusk())
					.as("last even number in [1,2,3,4,5]")
					.isEqualTo(Unfolding.beckon(4));
		}

		private static Stream<Arguments> returnsLastElementCases()
		{
			return Stream.of(
					new DuskCase("single element", Cascade.beckon(42), Unfolding.beckon(42)),
					new DuskCase("last of many integers", Cascade.beckon(1, 2, 3), Unfolding.beckon(3)),
					new DuskCase("last of many strings",
							Cascade.beckon("alpha", "beta", "gamma"), Unfolding.beckon("gamma")),
					new DuskCase("last after first is different",
							Cascade.beckon(10, 20, 30), Unfolding.beckon(30))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record DuskCase(String as, Cascade<?> source, Unfolding<?> expected)
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
			assertThat(Cascade.abyss().dusk())
					.as("dusk on empty cascade")
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("last() returns empty Unfolding")
		void lastReturnsEmptyUnfolding()
		{
			assertThat(Cascade.<Integer>abyss().last())
					.as("last() on empty cascade")
					.isEqualTo(Unfolding.chaos());
		}
	}
}