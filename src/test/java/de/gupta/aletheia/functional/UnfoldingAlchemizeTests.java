package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

final class UnfoldingAlchemizeTests
{
	@Nested
	@DisplayName("Tests for alchemize() method")
	class AlchemizeTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("alchemyTests")
		<T, R> void metamorphose(final String description, final Unfolding<T> source,
								 final Function<T, Optional<? extends R>> potion,
								 final Unfolding<R> expectedResult)
		{
			assertThat(source.alchemize(potion))
					.as("alchemize() for %s should result in %s when the potion is %s", source, expectedResult, potion)
					.isEqualTo(expectedResult);
		}

		private static Stream<Arguments> alchemyTests()
		{
			return Stream.of(
					// Basic cases
					AlchemyTestCase.of("Empty source with empty potion", Unfolding.chaos(), _ -> Optional.empty(),
							Unfolding.chaos()),
					AlchemyTestCase.of("Empty source with non-empty potion", Unfolding.chaos(), _ -> Optional.of(1),
							Unfolding.chaos()),
					AlchemyTestCase.of("Identity transformation", Unfolding.beckon("Hello"), Optional::of,
							Unfolding.beckon("Hello")),

					// Type conversions
					AlchemyTestCase.of("String to Integer conversion", Unfolding.beckon("42"),
							s ->
							{
								try
								{
									return Optional.of(Integer.parseInt(s));
								}
								catch (NumberFormatException e)
								{
									return Optional.empty();
								}
							}, Unfolding.beckon(42)),
					AlchemyTestCase.of("String to Integer conversion with invalid input", Unfolding.beckon("abc"),
							s ->
							{
								try
								{
									return Optional.of(Integer.parseInt(s));
								}
								catch (NumberFormatException e)
								{
									return Optional.empty();
								}
							}, Unfolding.chaos()),

					// Null handling
					AlchemyTestCase.of("Null value transformation", Unfolding.beckon(null), Optional::ofNullable,
							Unfolding.chaos()),
					AlchemyTestCase.of("Non-null to null transformation", Unfolding.beckon("test"),
							_ -> Optional.ofNullable(null), Unfolding.chaos()),

					// Complex transformations
					AlchemyTestCase.of("String length transformation", Unfolding.beckon("Hello World"),
							s -> Optional.of(s.length()), Unfolding.beckon(11)),
					AlchemyTestCase.of("String uppercase transformation", Unfolding.beckon("hello"),
							s -> Optional.of(s.toUpperCase()), Unfolding.beckon("HELLO")),
					AlchemyTestCase.of("Collection size transformation",
							Unfolding.beckon(java.util.List.of(1, 2, 3, 4, 5)),
							list -> Optional.of(list.size()), Unfolding.beckon(5)),

					// Edge cases with numbers
					AlchemyTestCase.of("Zero to string transformation", Unfolding.beckon(0),
							n -> Optional.of(n.toString()), Unfolding.beckon("0")),
					AlchemyTestCase.of("Negative number transformation", Unfolding.beckon(-42),
							n -> Optional.of(Math.abs(n)), Unfolding.beckon(42)),
					AlchemyTestCase.of("Large number transformation", Unfolding.beckon(Long.MAX_VALUE),
							n -> Optional.of(n / 2), Unfolding.beckon(Long.MAX_VALUE / 2)),

					// Boolean transformations
					AlchemyTestCase.of("Boolean to string transformation", Unfolding.beckon(true),
							b -> Optional.of(b.toString()), Unfolding.beckon("true")),
					AlchemyTestCase.of("Boolean negation", Unfolding.beckon(false),
							b -> Optional.of(!b), Unfolding.beckon(true)),

					// Conditional transformations
					AlchemyTestCase.of("Conditional transformation - positive", Unfolding.beckon(10),
							n -> n > 5 ? Optional.of("large") : Optional.empty(), Unfolding.beckon("large")),
					AlchemyTestCase.of("Conditional transformation - negative", Unfolding.beckon(3),
							n -> n > 5 ? Optional.of("large") : Optional.empty(), Unfolding.chaos()),

					// String manipulations
					AlchemyTestCase.of("Empty string transformation", Unfolding.beckon(""),
							s -> s.isEmpty() ? Optional.of("EMPTY") : Optional.of(s), Unfolding.beckon("EMPTY")),
					AlchemyTestCase.of("String trimming", Unfolding.beckon("  spaced  "),
							s -> Optional.of(s.trim()), Unfolding.beckon("spaced")),
					AlchemyTestCase.of("String first character", Unfolding.beckon("Hello"),
							s -> !s.isEmpty() ? Optional.of(s.charAt(0)) : Optional.empty(), Unfolding.beckon('H')),

					// Complex object transformations
					AlchemyTestCase.of("Array to first element",
							Unfolding.beckon(new String[]{"first", "second", "third"}),
							arr -> arr.length > 0 ? Optional.of(arr[0]) : Optional.empty(), Unfolding.beckon("first")),
					AlchemyTestCase.of("Empty array transformation", Unfolding.beckon(new String[0]),
							arr -> arr.length > 0 ? Optional.of(arr[0]) : Optional.empty(), Unfolding.chaos()),

					// Chained conditional logic
					AlchemyTestCase.of("Multi-condition transformation", Unfolding.beckon(15),
							n ->
							{
								if (n < 0) return Optional.empty();
								if (n < 10) return Optional.of("small");
								if (n < 20) return Optional.of("medium");
								return Optional.of("large");
							}, Unfolding.beckon("medium")),

					// Mathematical operations
					AlchemyTestCase.of("Square root transformation", Unfolding.beckon(16.0),
							n -> n >= 0 ? Optional.of(Math.sqrt(n)) : Optional.empty(), Unfolding.beckon(4.0)),
					AlchemyTestCase.of("Square root of negative", Unfolding.beckon(-4.0),
							n -> n >= 0 ? Optional.of(Math.sqrt(n)) : Optional.empty(), Unfolding.chaos()),

					// Generic type transformations
					AlchemyTestCase.of("Map key extraction", Unfolding.beckon(java.util.Map.of("key", "value")),
							map -> map.containsKey("key") ? Optional.of(map.get("key")) : Optional.empty(),
							Unfolding.beckon("value"))
			).map(tc -> Arguments.of(tc.description, tc.source, tc.potion, tc.expectedResult));
		}

		private record AlchemyTestCase<T, R>(String description, Unfolding<T> source,
											 Function<T, Optional<? extends R>> potion,
											 Unfolding<R> expectedResult)
		{
			static <T, R> AlchemyTestCase<T, R> of(final String description, final Unfolding<T> source,
												   final Function<T, Optional<? extends R>> potion,
												   final Unfolding<R> expectedResult)
			{
				return new AlchemyTestCase<>(description, source, potion, expectedResult);
			}
		}
	}
}