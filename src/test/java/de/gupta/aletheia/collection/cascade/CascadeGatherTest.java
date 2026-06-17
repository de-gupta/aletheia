package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade#precipitate")
final class CascadePrecipitateTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("collectsViaCollectorCases")
		@DisplayName("applies the collector and returns its result")
		<R> void appliesCollectorAndReturnsResult(final String as, final PrecipitateCase<R> tc)
		{
			assertThat(tc.invoke())
					.as(as)
					.isEqualTo(tc.expected());
		}

		@Test
		@DisplayName("gather() delegates to precipitate()")
		void gatherDelegatesToPrecipitate()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3);

			assertThat(cascade.gather(Collectors.toList()))
					.as("gather() must equal precipitate()")
					.isEqualTo(cascade.precipitate(Collectors.toList()));
		}

		@Test
		@DisplayName("throws when collector is null")
		void throwsWhenCollectorIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).precipitate(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("collector may not be null");
		}

		private static Stream<Arguments> collectsViaCollectorCases()
		{
			return Stream.of(
					new PrecipitateCase<>("count via Collectors.counting()",
							() -> Cascade.beckon(1, 2, 3).precipitate(Collectors.counting()), 3L),
					new PrecipitateCase<>("join strings",
							() -> Cascade.beckon("a", "b", "c").precipitate(Collectors.joining(", ")), "a, b, c"),
					new PrecipitateCase<>("sum integers",
							() -> Cascade.beckon(1, 2, 3, 4).precipitate(Collectors.summingInt(Integer::intValue)), 10),
					new PrecipitateCase<>("collect to list",
							() -> Cascade.beckon(10, 20).precipitate(Collectors.toList()), List.of(10, 20))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		@FunctionalInterface
		private interface Invocation<R>
		{
			R invoke();
		}

		private record PrecipitateCase<R>(String as, Invocation<R> invocation, R expected)
		{
			R invoke()
			{
				return invocation.invoke();
			}
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns the collector identity for empty stream")
		void returnsCollectorIdentityForEmptyStream()
		{
			assertThat(Cascade.<Integer>abyss().precipitate(Collectors.counting()))
					.as("count on empty cascade should be zero")
					.isEqualTo(0L);
		}

		@Test
		@DisplayName("returns empty list when collecting to list")
		void returnsEmptyListWhenCollectingToList()
		{
			assertThat(Cascade.<String>abyss().precipitate(Collectors.toList()))
					.as("toList on empty cascade should be empty")
					.isEmpty();
		}
	}
}