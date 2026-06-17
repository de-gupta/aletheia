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

@DisplayName("Cascade#gather")
final class CascadeGatherTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("collectsViaCollectorCases")
		@DisplayName("applies the collector and returns its result")
		<R> void appliesCollectorAndReturnsResult(final String as, final GatherCase<R> tc)
		{
			assertThat(tc.invoke())
					.as(as)
					.isEqualTo(tc.expected());
		}

		@Test
		@DisplayName("collect() delegates to gather()")
		void collectDelegatesToGather()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3);

			assertThat(cascade.collect(Collectors.toList()))
					.as("collect() must equal gather()")
					.isEqualTo(cascade.gather(Collectors.toList()));
		}

		@Test
		@DisplayName("throws when collector is null")
		void throwsWhenCollectorIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).gather(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("collector may not be null");
		}

		private static Stream<Arguments> collectsViaCollectorCases()
		{
			return Stream.of(
					new GatherCase<>("count via Collectors.counting()",
							() -> Cascade.beckon(1, 2, 3).gather(Collectors.counting()), 3L),
					new GatherCase<>("join strings",
							() -> Cascade.beckon("a", "b", "c").gather(Collectors.joining(", ")), "a, b, c"),
					new GatherCase<>("sum integers",
							() -> Cascade.beckon(1, 2, 3, 4).gather(Collectors.summingInt(Integer::intValue)), 10),
					new GatherCase<>("collect to list",
							() -> Cascade.beckon(10, 20).gather(Collectors.toList()), List.of(10, 20))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		@FunctionalInterface
		private interface Invocation<R>
		{
			R invoke();
		}

		private record GatherCase<R>(String as, Invocation<R> invocation, R expected)
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
			assertThat(Cascade.<Integer>abyss().gather(Collectors.counting()))
					.as("count on empty cascade should be zero")
					.isEqualTo(0L);
		}

		@Test
		@DisplayName("returns empty list when collecting to list")
		void returnsEmptyListWhenCollectingToList()
		{
			assertThat(Cascade.<String>abyss().gather(Collectors.toList()))
					.as("toList on empty cascade should be empty")
					.isEmpty();
		}
	}
}