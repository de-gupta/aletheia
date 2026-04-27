package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Dyad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Unfolding metamorphose tests")
class UnfoldingMetamorphoseTest
{
	@Nested
	@DisplayName("Basic metamorphose tests")
	class BasicMetamorphoseTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("basicMetamorphoseTestCases")
		@DisplayName("Should transform value correctly")
		<T, R> void testBasicMetamorphose(final String description, final Unfolding<T> source,
		                                  final Function<T, R> metamorphosis,
		                                  final Unfolding<R> expectedResult)
		{
			var actual = source.metamorphose(metamorphosis);
			assertThat(actual)
					.as("metamorphose() for %s should result in %s", source, expectedResult)
					.usingRecursiveComparison()
					.isEqualTo(expectedResult);
		}

		@Test
		@DisplayName("Should throw NullPointerException when metamorphosis is null")
		void testBasicMetamorphoseWithNullMetamorphosis()
		{
			var source = Unfolding.beckon("test");

			assertThatThrownBy(() -> source.metamorphose(null))
					.as("metamorphose() with null metamorphosis should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("metamorphosis may not be null");
		}

		@Test
		@DisplayName("Should return empty when applied to empty Unfolding")
		void testBasicMetamorphoseWithEmptySource()
		{
			Unfolding<String> empty = Unfolding.chaos();
			Function<String, Integer> metamorphosis = String::length;

			var result = empty.metamorphose(metamorphosis);

			assertThat(result.sterile())
					.as("metamorphose() on empty Unfolding should return empty result")
					.isTrue();
		}

		private static Stream<Arguments> basicMetamorphoseTestCases()
		{
			return Stream.of(
					Arguments.of(
							"String to Integer length transformation",
							Unfolding.beckon("hello"),
							(Function<String, Integer>) String::length,
							Unfolding.beckon(5)
					),
					Arguments.of(
							"Integer to String transformation",
							Unfolding.beckon(42),
							(Function<Integer, String>) Object::toString,
							Unfolding.beckon("42")
					),
					Arguments.of(
							"String to BigDecimal transformation",
							Unfolding.beckon("123.45"),
							(Function<String, BigDecimal>) BigDecimal::new,
							Unfolding.beckon(new BigDecimal("123.45"))
					),
					Arguments.of(
							"Integer to BigInteger transformation",
							Unfolding.beckon(1000),
							(Function<Integer, BigInteger>) BigInteger::valueOf,
							Unfolding.beckon(BigInteger.valueOf(1000))
					),
					Arguments.of(
							"List size transformation",
							Unfolding.beckon(List.of("a", "b", "c")),
							(Function<List<String>, Integer>) List::size,
							Unfolding.beckon(3)
					)
			);
		}
	}

	@Nested
	@DisplayName("Metamorphose with exception supplier tests")
	class MetamorphoseWithExceptionTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("metamorphoseWithExceptionTestCases")
		@DisplayName("Should transform value correctly when no exception occurs")
		<T, R> void testMetamorphoseWithExceptionSuccess(final String description, final Unfolding<T> source,
		                                                 final Function<T, R> metamorphosis,
		                                                 final Supplier<RuntimeException> wrath,
		                                                 final Unfolding<R> expectedResult)
		{
			var actual = source.metamorphose(metamorphosis, wrath);
			assertThat(actual)
					.as("metamorphose() with exception supplier for %s should result in %s", source, expectedResult)
					.usingRecursiveComparison()
					.isEqualTo(expectedResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("metamorphoseExceptionThrowingTestCases")
		@DisplayName("Should throw supplied exception when metamorphosis throws")
		<T, R> void testMetamorphoseWithExceptionThrows(final String description, final Unfolding<T> source,
		                                                final Function<T, R> metamorphosis,
		                                                final Supplier<RuntimeException> wrath,
		                                                final Class<? extends RuntimeException> expectedExceptionType)
		{
			assertThatThrownBy(() -> source.metamorphose(metamorphosis, wrath))
					.as("metamorphose() with exception supplier should throw %s", expectedExceptionType.getSimpleName())
					.isInstanceOf(expectedExceptionType);
		}

		@Test
		@DisplayName("Should preserve the original failure as the cause")
		void testMetamorphoseWithExceptionPreservesCause()
		{
			var source = Unfolding.beckon("invalid");
			Function<String, BigDecimal> metamorphosis = BigDecimal::new;
			Supplier<RuntimeException> wrath = () -> new IllegalArgumentException("Custom exception");

			assertThatThrownBy(() -> source.metamorphose(metamorphosis, wrath))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Custom exception")
					.cause()
					.isInstanceOf(NumberFormatException.class)
					.hasMessageContaining("Character");
		}

		@Test
		@DisplayName("Should keep an existing cause on the supplied wrath exception")
		void testMetamorphoseWithExceptionKeepsExistingCause()
		{
			var source = Unfolding.beckon("invalid");
			Function<String, BigDecimal> metamorphosis = BigDecimal::new;
			IllegalStateException existing =
					new IllegalStateException("Wrapped already", new IllegalArgumentException("existing"));

			assertThatThrownBy(() -> source.metamorphose(metamorphosis, () -> existing))
					.isSameAs(existing)
					.hasCauseInstanceOf(IllegalArgumentException.class)
					.hasRootCauseMessage("existing");
		}

		@Test
		@DisplayName("Should return empty when applied to empty Unfolding")
		void testMetamorphoseWithExceptionOnEmptySource()
		{
			Unfolding<String> empty = Unfolding.chaos();
			Function<String, Integer> metamorphosis = String::length;
			Supplier<RuntimeException> wrath = () -> new IllegalStateException("Should not be called");

			var result = empty.metamorphose(metamorphosis, wrath);

			assertThat(result.sterile())
					.as("metamorphose() with exception supplier on empty Unfolding should return empty result")
					.isTrue();
		}

		@Test
		@DisplayName("Should throw NullPointerException when metamorphosis is null")
		void testMetamorphoseWithExceptionNullMetamorphosis()
		{
			var source = Unfolding.beckon("test");
			Supplier<RuntimeException> wrath = () -> new IllegalArgumentException("Test exception");

			assertThatThrownBy(() -> source.metamorphose(null, wrath))
					.as("metamorphose() with null metamorphosis should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("metamorphosis may not be null");
		}

		@Test
		@DisplayName("Should throw NullPointerException when wrath is null")
		void testMetamorphoseWithExceptionNullWrath()
		{
			var source = Unfolding.beckon("test");
			Function<String, Integer> metamorphosis = String::length;

			assertThatThrownBy(() -> source.metamorphose(metamorphosis, (Supplier<RuntimeException>) null))
					.as("metamorphose() with null wrath should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("wrath may not be null");
		}

		@Test
		@DisplayName("Should throw NullPointerException when both arguments are null")
		void testMetamorphoseWithExceptionBothNull()
		{
			var source = Unfolding.beckon("test");

			assertThatThrownBy(
					() -> source.metamorphose((Function<String, Integer>) null, (Supplier<RuntimeException>) null))
					.as("metamorphose() with both null arguments should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("metamorphosis may not be null");
		}

		private static Stream<Arguments> metamorphoseWithExceptionTestCases()
		{
			return Stream.of(
					Arguments.of(
							"String to Integer length transformation with exception supplier",
							Unfolding.beckon("hello"),
							(Function<String, Integer>) String::length,
							(Supplier<RuntimeException>) () -> new IllegalStateException("Should not be thrown"),
							Unfolding.beckon(5)
					),
					Arguments.of(
							"Integer to String transformation with exception supplier",
							Unfolding.beckon(42),
							(Function<Integer, String>) Object::toString,
							(Supplier<RuntimeException>) () -> new RuntimeException("Should not be thrown"),
							Unfolding.beckon("42")
					),
					Arguments.of(
							"BigDecimal creation with exception supplier",
							Unfolding.beckon("123.45"),
							(Function<String, BigDecimal>) BigDecimal::new,
							(Supplier<RuntimeException>) () -> new IllegalArgumentException("Invalid number format"),
							Unfolding.beckon(new BigDecimal("123.45"))
					),
					Arguments.of(
							"List size calculation with exception supplier",
							Unfolding.beckon(List.of("a", "b", "c", "d")),
							(Function<List<String>, Integer>) List::size,
							(Supplier<RuntimeException>) () -> new UnsupportedOperationException(
									"List operation failed"),
							Unfolding.beckon(4)
					)
			);
		}

		private static Stream<Arguments> metamorphoseExceptionThrowingTestCases()
		{
			return Stream.of(
					Arguments.of(
							"Should throw IllegalArgumentException on invalid BigDecimal",
							Unfolding.beckon("invalid_number"),
							(Function<String, BigDecimal>) BigDecimal::new,
							(Supplier<RuntimeException>) () -> new IllegalArgumentException("Custom exception"),
							IllegalArgumentException.class
					),
					Arguments.of(
							"Should throw RuntimeException on division by zero",
							Unfolding.beckon(0),
							(Function<Integer, Integer>) x -> 10 / x,
							(Supplier<RuntimeException>) () -> new RuntimeException("Division by zero handled"),
							RuntimeException.class
					),
					Arguments.of(
							"Should throw IllegalStateException on null pointer exception",
							Unfolding.beckon("test"),
							(Function<String, Integer>) _ ->
							{
								throw new NullPointerException("Simulated NPE");
							},
							(Supplier<RuntimeException>) () -> new IllegalStateException("Null value encountered"),
							IllegalStateException.class
					),
					Arguments.of(
							"Should throw UnsupportedOperationException on array access",
							Unfolding.beckon(new int[0]),
							(Function<int[], Integer>) arr -> arr[5],
							(Supplier<RuntimeException>) () -> new UnsupportedOperationException("Array access failed"),
							UnsupportedOperationException.class
					)
			);
		}
	}

	@Nested
	@DisplayName("Edge cases and integration tests")
	class EdgeCaseTests
	{
		@Test
		@DisplayName("Should handle complex transformation chain")
		void testComplexTransformationChain()
		{
			var source = Unfolding.beckon("123");
			Supplier<RuntimeException> wrath = () -> new IllegalArgumentException("Chain failed");

			var result = source
					.metamorphose(Integer::valueOf, wrath)
					.metamorphose(x -> x * 2, wrath)
					.metamorphose(Object::toString, wrath);

			assertThat(result.summon())
					.as("complex transformation chain should result in '246'")
					.isEqualTo("246");
		}

		@Test
		@DisplayName("Should handle transformation with null result")
		void testTransformationWithNullResult()
		{
			var source = Unfolding.beckon("test");
			Function<String, String> nullReturningFunction = _ -> null;
			Supplier<RuntimeException> wrath = () -> new RuntimeException("Should not be thrown");

			var result = source.metamorphose(nullReturningFunction, wrath);

			assertThat(result.sterile())
					.as("transformation with null result should return empty Unfolding")
					.isTrue();
		}

		@Test
		@DisplayName("Should handle identity transformation")
		void testIdentityTransformation()
		{
			var source = Unfolding.beckon("unchanged");
			Function<String, String> identity = Function.identity();
			Supplier<RuntimeException> wrath = () -> new RuntimeException("Should not be thrown");

			var result = source.metamorphose(identity, wrath);

			assertThat(result.summon())
					.as("identity transformation should return unchanged value")
					.isEqualTo("unchanged");
		}

		@Test
		@DisplayName("Should handle exception supplier that returns null")
		void testExceptionSupplierReturnsNull()
		{
			var source = Unfolding.beckon("invalid");
			Function<String, Integer> throwingFunction = Integer::valueOf;
			Supplier<RuntimeException> nullReturningWrath = () -> null;

			assertThatThrownBy(() -> source.metamorphose(throwingFunction, nullReturningWrath))
					.as("metamorphose() with exception supplier returning null should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("wrath may not return null");
		}
	}

	@Nested
	@DisplayName("Dual metamorphose tests")
	class DualMetamorphoseTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("dualMetamorphoseTestCases")
		@DisplayName("Should transform value with two functions correctly")
		<T, U, R> void testDualMetamorphose(final String description, final Unfolding<T> source,
		                                    final Function<T, U> fate, final Function<T, R> destiny,
		                                    final Unfolding<Dyad<U, R>> expectedResult)
		{
			var actual = source.metamorphose(fate, destiny);
			assertThat(actual)
					.as("dual metamorphose() for %s should result in %s", source, expectedResult)
					.usingRecursiveComparison()
					.isEqualTo(expectedResult);
		}

		@Test
		@DisplayName("Should throw NullPointerException when fate is null")
		void testDualMetamorphoseWithNullFate()
		{
			var source = Unfolding.beckon("test");

			assertThatThrownBy(() -> source.metamorphose(null, String::length))
					.as("dual metamorphose() with null fate should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("fate may not be null");
		}

		@Test
		@DisplayName("Should throw NullPointerException when destiny is null")
		void testDualMetamorphoseWithNullDestiny()
		{
			var source = Unfolding.beckon("test");

			assertThatThrownBy(() -> source.metamorphose(String::length, (Function<String, Integer>) null))
					.as("dual metamorphose() with null destiny should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("destiny may not be null");
		}

		private static Stream<Arguments> dualMetamorphoseTestCases()
		{
			return Stream.of(
					Arguments.of(
							"String to length and uppercase transformation",
							Unfolding.beckon("hello"),
							(Function<String, Integer>) String::length,
							(Function<String, String>) String::toUpperCase,
							Unfolding.beckon(Dyad.of(5, "HELLO"))
					),
					Arguments.of(
							"Integer to string and double transformation",
							Unfolding.beckon(42),
							(Function<Integer, String>) Object::toString,
							(Function<Integer, Integer>) i -> i * 2,
							Unfolding.beckon(Dyad.of("42", 84))
					)
			);
		}
	}
}