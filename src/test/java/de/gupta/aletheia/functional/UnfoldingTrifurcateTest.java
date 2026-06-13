package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UnfoldingTrifurcateTest
{
	@Nested
	@DisplayName("Tests for trifurcate(ToIntFunction, Function, Function, Function) — returns Unfolding<R>")
	class TrifurcateUnfoldingTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should route to the correct branch based on comparison result")
		@MethodSource("trifurcateUnfoldingTestCases")
		<T, R> void testBranchRouting(final String description, final Unfolding<T> source,
		                              final ToIntFunction<T> reckoning, final Unfolding<R> expected)
		{
			assertThat(source.trifurcate(reckoning, _ -> "diminished", _ -> "balanced", _ -> "ascendant"))
					.as("trifurcate() for %s should produce %s", source, expected)
					.isEqualTo(expected);
		}

		@ParameterizedTest(name = "{0}")
		@DisplayName("should pass original value into the selected branch function")
		@MethodSource("trifurcateUnfoldingValuePassthroughCases")
		<T, R> void testValuePassedToBranch(final String description, final Unfolding<T> source,
		                                    final ToIntFunction<T> reckoning,
		                                    final Function<T, R> expectedBranch,
		                                    final Unfolding<R> expected)
		{
			assertThat(source.trifurcate(reckoning, expectedBranch, expectedBranch, expectedBranch))
					.as("trifurcate() for %s should pass value into branch", source)
					.isEqualTo(expected);
		}

		@Test
		@DisplayName("should return empty when source is empty")
		void testEmptySourceReturnsEmpty()
		{
			assertThat(Unfolding.<Integer>chaos().trifurcate(n -> n.compareTo(0),
					_ -> "diminished", _ -> "balanced", _ -> "ascendant"))
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("should throw NullPointerException when reckoning is null")
		void testNullReckoning()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(
					(ToIntFunction<Integer>) null, _ -> "d", _ -> "b", _ -> "a"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("reckoning may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when diminished is null")
		void testNullDiminished()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(n -> n, null, _ -> "b", _ -> "a"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("diminished may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when balanced is null")
		void testNullBalanced()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(n -> n, _ -> "d", null, _ -> "a"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("balanced may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when ascendant is null")
		void testNullAscendant()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(n -> n, _ -> "d", _ -> "b", null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("ascendant may not be null");
		}

		private static Stream<Arguments> trifurcateUnfoldingTestCases()
		{
			return Stream.of(
					Arguments.of("negative comparison routes to diminished",
							Unfolding.beckon(3), (ToIntFunction<Integer>) n -> n.compareTo(10),
							Unfolding.beckon("diminished")),
					Arguments.of("zero comparison routes to balanced",
							Unfolding.beckon(5), (ToIntFunction<Integer>) n -> n.compareTo(5),
							Unfolding.beckon("balanced")),
					Arguments.of("positive comparison routes to ascendant",
							Unfolding.beckon(9), (ToIntFunction<Integer>) n -> n.compareTo(4),
							Unfolding.beckon("ascendant")),
					Arguments.of("large negative value treated as diminished",
							Unfolding.beckon("alpha"), (ToIntFunction<String>) s -> s.compareTo("zeta"),
							Unfolding.beckon("diminished")),
					Arguments.of("large positive value treated as ascendant",
							Unfolding.beckon("zeta"), (ToIntFunction<String>) s -> s.compareTo("alpha"),
							Unfolding.beckon("ascendant")),
					Arguments.of("empty source stays empty",
							Unfolding.chaos(), (ToIntFunction<Integer>) n -> n.compareTo(0),
							Unfolding.chaos())
			);
		}

		private static Stream<Arguments> trifurcateUnfoldingValuePassthroughCases()
		{
			return Stream.of(
					Arguments.of("value available in diminished branch",
							Unfolding.beckon(3), (ToIntFunction<Integer>) n -> n.compareTo(10),
							(Function<Integer, String>) n -> "score=" + n,
							Unfolding.beckon("score=3")),
					Arguments.of("value available in balanced branch",
							Unfolding.beckon(5), (ToIntFunction<Integer>) n -> n.compareTo(5),
							(Function<Integer, String>) n -> "score=" + n,
							Unfolding.beckon("score=5")),
					Arguments.of("value available in ascendant branch",
							Unfolding.beckon(9), (ToIntFunction<Integer>) n -> n.compareTo(4),
							(Function<Integer, String>) n -> "score=" + n,
							Unfolding.beckon("score=9"))
			);
		}
	}

	@Nested
	@DisplayName("Tests for trifurcate(ToIntFunction, Supplier, Supplier, Supplier) — returns R directly")
	class TrifurcateSupplierTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should route to the correct branch and return R directly")
		@MethodSource("trifurcateSupplierTestCases")
		<T, R> void testBranchRouting(final String description, final Unfolding<T> source,
		                              final ToIntFunction<T> reckoning, final R expected)
		{
			assertThat(source.<R>trifurcate(reckoning, () -> (R) "diminished", () -> (R) "balanced",
					() -> (R) "ascendant"))
					.as("trifurcate() for %s should return %s", source, expected)
					.isEqualTo(expected);
		}

		@Test
		@DisplayName("should evaluate only the matching supplier — not the others")
		void testOnlyMatchingSupplierEvaluated()
		{
			final AtomicInteger diminishedCalls = new AtomicInteger();
			final AtomicInteger balancedCalls = new AtomicInteger();
			final AtomicInteger ascendantCalls = new AtomicInteger();

			Unfolding.beckon(3).trifurcate(
					n -> n.compareTo(10),
					() ->
					{
						diminishedCalls.incrementAndGet();
						return "diminished";
					},
					() ->
					{
						balancedCalls.incrementAndGet();
						return "balanced";
					},
					() ->
					{
						ascendantCalls.incrementAndGet();
						return "ascendant";
					}
			);

			assertThat(diminishedCalls.get()).isEqualTo(1);
			assertThat(balancedCalls.get()).isEqualTo(0);
			assertThat(ascendantCalls.get()).isEqualTo(0);
		}

		@Test
		@DisplayName("should evaluate only balanced supplier when comparison is zero")
		void testOnlyBalancedSupplierEvaluatedOnZero()
		{
			final AtomicInteger diminishedCalls = new AtomicInteger();
			final AtomicInteger balancedCalls = new AtomicInteger();
			final AtomicInteger ascendantCalls = new AtomicInteger();

			Unfolding.beckon(5).trifurcate(
					n -> n.compareTo(5),
					() ->
					{
						diminishedCalls.incrementAndGet();
						return "diminished";
					},
					() ->
					{
						balancedCalls.incrementAndGet();
						return "balanced";
					},
					() ->
					{
						ascendantCalls.incrementAndGet();
						return "ascendant";
					}
			);

			assertThat(diminishedCalls.get()).isEqualTo(0);
			assertThat(balancedCalls.get()).isEqualTo(1);
			assertThat(ascendantCalls.get()).isEqualTo(0);
		}

		@Test
		@DisplayName("should throw EmptyUnfoldingException when source is empty")
		void testEmptySourceThrows()
		{
			assertThatThrownBy(() -> Unfolding.<Integer>chaos().trifurcate(
					n -> n.compareTo(0), () -> "d", () -> "b", () -> "a"))
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		@Test
		@DisplayName("should throw NullPointerException when reckoning is null")
		void testNullReckoning()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(
					(ToIntFunction<Integer>) null, () -> "d", () -> "b", () -> "a"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("reckoning may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when diminished is null")
		void testNullDiminished()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(
					n -> n, (Supplier<String>) null, () -> "b", () -> "a"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("diminished may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when balanced is null")
		void testNullBalanced()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(
					n -> n, () -> "d", (Supplier<String>) null, () -> "a"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("balanced may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when ascendant is null")
		void testNullAscendant()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(
					n -> n, () -> "d", () -> "b", (Supplier<String>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("ascendant may not be null");
		}

		private static Stream<Arguments> trifurcateSupplierTestCases()
		{
			return Stream.of(
					Arguments.of("negative comparison routes to diminished",
							Unfolding.beckon(3), (ToIntFunction<Integer>) n -> n.compareTo(10), "diminished"),
					Arguments.of("zero comparison routes to balanced",
							Unfolding.beckon(5), (ToIntFunction<Integer>) n -> n.compareTo(5), "balanced"),
					Arguments.of("positive comparison routes to ascendant",
							Unfolding.beckon(9), (ToIntFunction<Integer>) n -> n.compareTo(4), "ascendant"),
					Arguments.of("large negative compareTo treated as diminished",
							Unfolding.beckon("alpha"), (ToIntFunction<String>) s -> s.compareTo("zeta"), "diminished"),
					Arguments.of("large positive compareTo treated as ascendant",
							Unfolding.beckon("zeta"), (ToIntFunction<String>) s -> s.compareTo("alpha"), "ascendant")
			);
		}
	}

	@Nested
	@DisplayName("Tests for trifurcate(ToIntFunction, R, R, R) — returns R directly, raw values")
	class TrifurcateRawValueTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should route to the correct raw value based on comparison result")
		@MethodSource("trifurcateRawTestCases")
		<T, R> void testBranchRouting(final String description, final Unfolding<T> source,
		                              final ToIntFunction<T> reckoning, final R expected)
		{
			assertThat(source.<R>trifurcate(reckoning, (R) "diminished", (R) "balanced", (R) "ascendant"))
					.as("trifurcate() for %s should return %s", source, expected)
					.isEqualTo(expected);
		}

		@Test
		@DisplayName("should throw EmptyUnfoldingException when source is empty")
		void testEmptySourceThrows()
		{
			assertThatThrownBy(() -> Unfolding.<Integer>chaos().trifurcate(n -> n.compareTo(0), "d", "b", "a"))
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		@Test
		@DisplayName("should throw NullPointerException when reckoning is null")
		void testNullReckoning()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate((ToIntFunction<Integer>) null, "d", "b", "a"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("reckoning may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when diminished is null")
		void testNullDiminished()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(n -> n, null, "b", "a"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("diminished may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when balanced is null")
		void testNullBalanced()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(n -> n, "d", null, "a"))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("balanced may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when ascendant is null")
		void testNullAscendant()
		{
			assertThatThrownBy(() -> Unfolding.beckon(1).trifurcate(n -> n, "d", "b", null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("ascendant may not be null");
		}

		private static Stream<Arguments> trifurcateRawTestCases()
		{
			return Stream.of(
					Arguments.of("negative comparison routes to diminished",
							Unfolding.beckon(3), (ToIntFunction<Integer>) n -> n.compareTo(10), "diminished"),
					Arguments.of("zero comparison routes to balanced",
							Unfolding.beckon(5), (ToIntFunction<Integer>) n -> n.compareTo(5), "balanced"),
					Arguments.of("positive comparison routes to ascendant",
							Unfolding.beckon(9), (ToIntFunction<Integer>) n -> n.compareTo(4), "ascendant"),
					Arguments.of("large negative compareTo treated as diminished",
							Unfolding.beckon("alpha"), (ToIntFunction<String>) s -> s.compareTo("zeta"), "diminished"),
					Arguments.of("large positive compareTo treated as ascendant",
							Unfolding.beckon("zeta"), (ToIntFunction<String>) s -> s.compareTo("alpha"), "ascendant")
			);
		}
	}
}