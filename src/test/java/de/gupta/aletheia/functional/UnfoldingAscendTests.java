package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Unfolding ascend tests")
final class UnfoldingAscendTests
{
	private record AscendTestCase<T>(Unfolding<T> source, UnaryOperator<T> ascension,
									 int levels, Unfolding<T> expectedResult)
	{
	}

	private record FactorialState(int n, int result)
	{
	}

	private record StringState(String value, int length)
	{
	}

	@Nested
	@DisplayName("Basic ascend tests")
	final class BasicAscendTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("basicAscendTestCases")
		@DisplayName("Should apply ascension correctly for given levels")
		<T> void testBasicAscend(final String description, final Unfolding<T> source,
								 final UnaryOperator<T> ascension, final int levels,
								 final Unfolding<T> expectedResult)
		{
			var actual = source.ascend(ascension, levels);
			assertThat(actual)
					.as("ascend() for %s with %d levels should result in %s", source, levels, expectedResult)
					.usingRecursiveComparison()
					.isEqualTo(expectedResult);
		}

		@Test
		@DisplayName("Should throw NullPointerException when ascension is null")
		void testBasicAscendWithNullAscension()
		{
			var source = Unfolding.beckon(10);

			assertThatThrownBy(() -> source.ascend(null, 3))
					.as("ascend() with null ascension should throw NullPointerException")
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		@DisplayName("Should return empty when applied to empty Unfolding")
		void testBasicAscendWithEmptySource()
		{
			Unfolding<Integer> empty = Unfolding.chaos();
			UnaryOperator<Integer> ascension = x -> x + 1;

			var result = empty.ascend(ascension, 5);

			assertThat(result.sterile())
					.as("ascend() on empty Unfolding should return empty result")
					.isTrue();
		}

		@Test
		@DisplayName("Should return original value when levels is zero")
		void testAscendWithZeroLevels()
		{
			var source = Unfolding.beckon(42);
			UnaryOperator<Integer> ascension = x -> x * 2;

			var result = source.ascend(ascension, 0);

			assertThat(result)
					.as("ascend() with 0 levels should return unchanged value")
					.usingRecursiveComparison()
					.isEqualTo(Unfolding.beckon(42));
		}

		private static Stream<Arguments> basicAscendTestCases()
		{
			return Stream.of(
					Arguments.of(
							"Integer increment by 1 for 3 levels",
							Unfolding.beckon(10),
							(UnaryOperator<Integer>) x -> x + 1,
							3,
							Unfolding.beckon(13)
					),
					Arguments.of(
							"Integer multiplication by 2 for 4 levels",
							Unfolding.beckon(2),
							(UnaryOperator<Integer>) x -> x * 2,
							4,
							Unfolding.beckon(32)
					),
					Arguments.of(
							"String concatenation for 2 levels",
							Unfolding.beckon("A"),
							(UnaryOperator<String>) s -> s + "B",
							2,
							Unfolding.beckon("ABB")
					),
					Arguments.of(
							"BigDecimal increment by 0.5 for 3 levels",
							Unfolding.beckon(new BigDecimal("1.0")),
							(UnaryOperator<BigDecimal>) bd -> bd.add(new BigDecimal("0.5")),
							3,
							Unfolding.beckon(new BigDecimal("2.5"))
					),
					Arguments.of(
							"Single level transformation",
							Unfolding.beckon(100),
							(UnaryOperator<Integer>) x -> x / 2,
							1,
							Unfolding.beckon(50)
					)
			);
		}
	}

	@Nested
	@DisplayName("Edge case tests")
	final class EdgeCaseTests
	{
		@Test
		@DisplayName("Should handle negative levels gracefully")
		void testAscendWithNegativeLevels()
		{
			var source = Unfolding.beckon(10);
			UnaryOperator<Integer> ascension = x -> x + 1;

			var result = source.ascend(ascension, -2);

			assertThat(result)
					.as("ascend() with negative levels should return original value")
					.usingRecursiveComparison()
					.isEqualTo(Unfolding.beckon(10));
		}

		@Test
		@DisplayName("Should handle large number of levels")
		void testAscendWithLargeLevels()
		{
			var source = Unfolding.beckon(1);
			UnaryOperator<Integer> ascension = x -> x + 1;

			var result = source.ascend(ascension, 1000);

			assertThat(result)
					.as("ascend() with 1000 levels should compute correctly")
					.usingRecursiveComparison()
					.isEqualTo(Unfolding.beckon(1001));
		}

		@Test
		@DisplayName("Should handle identity transformation")
		void testAscendWithIdentityTransformation()
		{
			var source = Unfolding.beckon("unchanged");
			UnaryOperator<String> identityAscension = UnaryOperator.identity();

			var result = source.ascend(identityAscension, 10);

			assertThat(result)
					.as("ascend() with identity transformation should return unchanged value")
					.usingRecursiveComparison()
					.isEqualTo(Unfolding.beckon("unchanged"));
		}

		@Test
		@DisplayName("Should handle transformation that returns null")
		void testAscendWithNullReturningTransformation()
		{
			var source = Unfolding.beckon("test");
			UnaryOperator<String> nullReturningAscension = _ -> null;

			var result = source.ascend(nullReturningAscension, 1);

			assertThat(result.sterile())
					.as("ascend() with null-returning transformation should result in empty Unfolding")
					.isTrue();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("complexAscendTestCases")
		@DisplayName("Should handle complex transformations correctly")
		<T> void testComplexAscendTransformations(final String description,
												  final AscendTestCase<T> testCase)
		{
			var result = testCase.source().ascend(testCase.ascension(), testCase.levels());

			assertThat(result)
					.as("Complex ascend transformation: %s", description)
					.usingRecursiveComparison()
					.isEqualTo(testCase.expectedResult());
		}

		private static Stream<Arguments> complexAscendTestCases()
		{
			return Stream.of(
					Arguments.of(
							"Factorial-like computation",
							new AscendTestCase<>(
									Unfolding.beckon(new FactorialState(1, 1)),
									state -> new FactorialState(state.n() + 1, state.result() * (state.n() + 1)),
									4,
									Unfolding.beckon(new FactorialState(5, 120))
							)
					),
					Arguments.of(
							"String manipulation with length tracking",
							new AscendTestCase<>(
									Unfolding.beckon(new StringState("a", 1)),
									state -> new StringState(state.value() + "b", state.length() + 1),
									3,
									Unfolding.beckon(new StringState("abbb", 4))
							)
					)
			);
		}
	}
}