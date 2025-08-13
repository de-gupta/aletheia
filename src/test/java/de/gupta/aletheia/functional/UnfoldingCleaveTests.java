package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UnfoldingCleaveTests
{
	@Nested
	@DisplayName("Tests for cleave() method with direct return arguments instead of functions")
	class CleaveDirectReturnArgumentsTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("truePredicateTestCases")
		@DisplayName("should return true result when predicate matches")
		<T, R> void shouldApplyTrueMapper(String description, Unfolding<T> unfolding, R trueResult, R falseResult)
		{
			assertThat(unfolding.cleave(_ -> true, trueResult, falseResult)).as(
					"Cleave should return the true result when predicate matches").isEqualTo(trueResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("falsePredicateTestCases")
		@DisplayName("should return false result when predicate does not match")
		<T, R> void shouldApplyFalseMapper(String description, Unfolding<T> unfolding, R trueResult, R falseResult)
		{
			assertThat(unfolding.cleave(_ -> false, trueResult, falseResult)).as(
					"Cleave should return the false result when predicate does not match").isEqualTo(falseResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyUnfoldingTestCases")
		@DisplayName("should throw when input is empty")
		<T, R> void shouldReturnEmptyForEmptyInput(String description, Predicate<T> predicate, R trueResult,
												   R falseResult)
		{
			assertThatThrownBy(() -> Unfolding.<T>chaos().cleave(predicate, trueResult, falseResult))
					.as("An empty unfolding should throw when cleave() is called")
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("generalPredicateTestCases")
		@DisplayName("should return expected result according to predicate")
		<T, R> void shouldReturnExpectedResult(String description, Unfolding<T> unfolding, Predicate<T> predicate,
											   R trueResult, R falseResult)
		{
			R result = unfolding.cleave(predicate, trueResult, falseResult);
			R expectedResult = predicate.test(unfolding.summon()) ? trueResult : falseResult;
			assertThat(result).as(
					"Cleave should return expected result according to predicate").isEqualTo(expectedResult);
		}

		@Test
		@DisplayName("should throw when any argument is null")
		void shouldThrowWhenAnyArgumentIsNull()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			assertThatThrownBy(() -> unfolding.cleave(null, "a", "b"))
					.as("Cleave should throw when predicate is null")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");

			assertThatThrownBy(() -> unfolding.cleave(_ -> true, null, "b"))
					.as("Cleave should throw when true result is null").isInstanceOf(NullPointerException.class)
					.hasMessageContaining("reward may not be null");

			assertThatThrownBy(() -> unfolding.cleave(_ -> true, "a", null))
					.as("Cleave should throw when false result is null")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("punishment may not be null");
		}

		private static Stream<Arguments> truePredicateTestCases()
		{
			return Stream.of(
					Arguments.of("String length predicate matches, return true result",
							Unfolding.beckon("hello"), "HELLO", "hello_suffix"),
					Arguments.of("Integer value predicate matches, return true result",
							Unfolding.beckon(42), "Number: 42", "Small: 42"),
					Arguments.of("Boolean value predicate matches, return true result",
							Unfolding.beckon(true), "It's true", "It's false")
			);
		}

		private static Stream<Arguments> falsePredicateTestCases()
		{
			return Stream.of(
					Arguments.of("String length predicate doesn't match, return false result",
							Unfolding.beckon("hi"), "HI", "hi_suffix"),
					Arguments.of("Integer value predicate doesn't match, return false result",
							Unfolding.beckon(5), "Number: 5", "Small: 5"),
					Arguments.of("Boolean value predicate doesn't match, return false result",
							Unfolding.beckon(false), "It's true", "It's false")
			);
		}

		private static Stream<Arguments> emptyUnfoldingTestCases()
		{
			return Stream.of(
					Arguments.of("Empty string unfolding with true predicate should remain empty",
							(Predicate<String>) _ -> true, "HELLO", "hello_suffix"),
					Arguments.of("Empty integer unfolding with false predicate should remain empty",
							(Predicate<Integer>) n -> n > 10, "Number: 42", "Small: 42")
			);
		}

		private static Stream<Arguments> generalPredicateTestCases()
		{
			return Stream.of(
					Arguments.of("String starts with 'h' predicate, return appropriate result",
							Unfolding.beckon("hello"), (Predicate<String>) s -> s.startsWith("h"),
							"Starts with h", "Does not start with h"),
					Arguments.of("Integer is even predicate, return appropriate result",
							Unfolding.beckon(42), (Predicate<Integer>) n -> n % 2 == 0,
							"Even number", "Odd number"),
					Arguments.of("Boolean is true predicate, return appropriate result",
							Unfolding.beckon(false), (Predicate<Boolean>) b -> b,
							"Is true", "Is false")
			);
		}
	}
}