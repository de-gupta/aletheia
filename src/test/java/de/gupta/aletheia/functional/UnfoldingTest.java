package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

final class UnfoldingTest
{
	@Nested
	@DisplayName("Tests for coronate() method")
	class CoronateTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("successTestCases")
		@DisplayName("should transform present values")
		<T, R> void shouldTransformValue(String description, Unfolding<T> unfolding, Function<T, R> extractor,
										 R expectedResult)
		{
			R result = unfolding.coronate(extractor);
			assertThat(result).as("coronate() for %s should give %s", unfolding, expectedResult)
							  .isEqualTo(expectedResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("exceptionTestCases")
		@DisplayName("should throw exception for empty unfolding")
		<T, R> void shouldThrowExceptionForEmptyUnfolding(String description, Unfolding<T> unfolding,
														  Function<T, R> extractor)
		{
			assertThatThrownBy(() -> unfolding.coronate(extractor)).as(
																		   "coronate() for %s should throw EmptyUnfoldingException", unfolding)
																   .isInstanceOf(EmptyUnfoldingException.class)
																   .hasMessageContaining("empty");
		}

		@DisplayName("should throw exception for null conclusion")
		@Test
		void shouldThrowExceptionForNullExtractor()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Function<String, Integer> nullConclusion = null;

			assertThatThrownBy(() -> unfolding.coronate(nullConclusion)).as(
																				"coronate() with null conclusion should throw NullPointerException")
																		.isInstanceOf(NullPointerException.class)
																		.hasMessageContaining(
																					"conclusion may not be null");
		}

		private static Stream<Arguments> successTestCases()
		{
			return Stream.of(
								 new SuccessTestCase<>("Present value should be transformed by extractor", Unfolding.beckon("hello"),
										 String::length, 5),
								 new SuccessTestCase<>("Complex transformation should work on present value", Unfolding.beckon(42),
										 num -> "Number: " + num, "Number: 42"),
								 new SuccessTestCase<>("Identity function should return the same value", Unfolding.beckon("test"),
										 Function.identity(), "test"),
								 new SuccessTestCase<>("Extractor returning null should work", Unfolding.beckon("hello"), _ -> null,
										 null))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.extractor, tc.expectedResult));
		}

		private static Stream<Arguments> exceptionTestCases()
		{
			return Stream.of(
								 new ExceptionTestCase<>("Empty unfolding should throw exception", Unfolding.chaos(), _ -> "unused"),
								 new ExceptionTestCase<>("Unfolding created with null should throw exception",
										 Unfolding.beckon(null), _ -> "unused"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.extractor));
		}

		private record SuccessTestCase<T, R>(String description, Unfolding<T> unfolding, Function<T, R> extractor,
											 R expectedResult)
		{
		}

		private record ExceptionTestCase<T, R>(String description, Unfolding<T> unfolding, Function<T, R> extractor)
		{
		}
	}

	@Nested
	@DisplayName("Tests for alternatively() method")
	class AlternativelyTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should return value if present or fallback if empty")
		<T> void shouldReturnValueOrFallback(String description, Unfolding<T> unfolding, T fallbackValue,
											 Supplier<T> fallbackSupplier, T expectedResult, boolean useSupplier)
		{
			T result = useSupplier ? unfolding.rescue(fallbackSupplier) : unfolding.rescue(fallbackValue);

			String fallbackDescription =
					useSupplier ? "supplier returning " + expectedResult : String.valueOf(fallbackValue);

			assertThat(result).as("alternatively() for %s with fallback %s should give %s", unfolding,
					fallbackDescription, expectedResult).isEqualTo(expectedResult);
		}

		@DisplayName("should throw exception for null revelation")
		@Test
		void shouldThrowExceptionForNullSupplier()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Supplier<String> nullSupplier = null;

			assertThatThrownBy(() -> unfolding.rescue(nullSupplier)).as(
																			"alternatively() with null revelation should throw NullPointerException")
																	.isInstanceOf(NullPointerException.class)
																	.hasMessageContaining("revelation may not be null");
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(new TestCase<>("Present value should be returned ignoring fallback value",
										 Unfolding.beckon("primary"), "fallback", null, "primary", false),
								 new TestCase<>("Empty unfolding should return fallback value", Unfolding.chaos(), "fallback", null,
										 "fallback", false),
								 new TestCase<>("Present value should be returned ignoring fallback supplier", Unfolding.beckon(100),
										 null, () -> 200, 100, true),
								 new TestCase<>("Present value should be returned ignoring fallback and fallback supplier",
										 Unfolding.beckon(100), 300, () -> 200, 100, true),
								 new TestCase<>("Empty unfolding should use fallback supplier", Unfolding.chaos(), null, () -> 200,
										 200, true),
								 new TestCase<>("Null fallback value should be returned for empty unfolding", Unfolding.chaos(),
										 null, null, null, false),
								 new TestCase<>("Null value from fallback supplier should be returned for empty unfolding",
										 Unfolding.chaos(), null, () -> null, null, true),
								 new TestCase<>("Unfolding created with null should return fallback value", Unfolding.beckon(null),
										 "fallback", null, "fallback", false),
								 new TestCase<>("Unfolding created with null should use fallback supplier", Unfolding.beckon(null),
										 null, () -> "fallback", "fallback", true))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.fallbackValue, tc.fallbackSupplier,
								 tc.expectedResult, tc.useSupplier));
		}

		record TestCase<T>(String description, Unfolding<T> unfolding, T fallbackValue, Supplier<T> fallbackSupplier,
						   T expectedResult, boolean useSupplier)
		{
		}
	}

	@Nested
	@DisplayName("Tests for supple() method")
	class SupplicationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should correctly identify if value exists")
		<T> void shouldIdentifyValueExistence(String description, Unfolding<T> unfolding, boolean expectedResult)
		{
			boolean result = unfolding.supple();

			assertThat(result).as("supple() for %s should give %s", unfolding, expectedResult)
							  .isEqualTo(expectedResult);
		}

		@DisplayName("should be consistent with isEmpty()")
		@Test
		void shouldBeConsistentWithIsEmpty()
		{
			Unfolding<String> presentUnfolding = Unfolding.beckon("test");
			Unfolding<Integer> emptyUnfolding = Unfolding.chaos();
			Unfolding<String> nullUnfolding = Unfolding.beckon(null);

			assertThat(presentUnfolding.supple()).as("supple() should be opposite of isEmpty()")
												 .isEqualTo(!presentUnfolding.sterile());
			assertThat(emptyUnfolding.supple()).as("supple() should be opposite of isEmpty()")
											   .isEqualTo(!emptyUnfolding.sterile());
			assertThat(nullUnfolding.supple()).as("supple() should be opposite of isEmpty()")
											  .isEqualTo(!nullUnfolding.sterile());
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("Unfolding with non-null value should be present", Unfolding.beckon("value"), true),
					new TestCase<>("Empty unfolding should not be present", Unfolding.chaos(), false),
					new TestCase<>("Unfolding created with null should be empty and not present",
							Unfolding.beckon(null), false),
					new TestCase<>("Unfolding with empty string should be present", Unfolding.beckon(""), true),
					new TestCase<>("Unfolding with zero should be present", Unfolding.beckon(0), true),
					new TestCase<>("Unfolding with false should be present", Unfolding.beckon(false), true),
					new TestCase<>("Unfolding with empty list should be present", Unfolding.beckon(new ArrayList<>()),
							true)).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedResult));
		}

		private record TestCase<T>(String description, Unfolding<T> unfolding, boolean expectedResult)
		{
		}
	}

	@Nested
	@DisplayName("Tests for metamorphose() method")
	class MetamorphosesTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should transform present value to new present value")
		<T, R> void shouldTransformPresentValue(String description, Unfolding<T> unfolding, Function<T, R> mapper,
												Unfolding<R> expectedResult)
		{
			Unfolding<R> result = unfolding.metamorphose(mapper);

			assertThat(result.supple()).as("metamorphose() for %s should result in present unfolding", unfolding)
									   .isTrue();
			assertThat(result.summon()).as("metamorphose() result value should match expected")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T, R> void shouldTransformToEmptyUnfolding(String description, Unfolding<T> unfolding, Function<T, R> mapper)
		{
			Unfolding<R> result = unfolding.metamorphose(mapper);

			assertThat(result.sterile()).as("metamorphose() for %s should result in empty unfolding", unfolding)
										.isTrue();
		}

		@DisplayName("should throw exception for null mapper")
		@Test
		void shouldThrowExceptionForNullMapper()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Function<String, Integer> nullMapper = null;

			assertThatThrownBy(() -> unfolding.metamorphose(nullMapper)).as(
																				"metamorphose() with null mapper should throw NullPointerException")
																		.isInstanceOf(NullPointerException.class)
																		.hasMessageContaining(
																				"metamorphosis may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value should be transformed by mapper",
										 Unfolding.beckon("hello"), String::length, Unfolding.beckon(5)),
								 new PresentResultTestCase<>("Complex transformation should work on present value",
										 Unfolding.beckon(42), num -> "Number: " + num, Unfolding.beckon("Number: 42")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.mapper, tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(
								 new EmptyResultTestCase<>("Empty unfolding should remain empty after mapping", Unfolding.chaos(),
										 String::length),
								 new EmptyResultTestCase<>("Mapper returning null should result in empty unfolding",
										 Unfolding.beckon("test"), _ -> null))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.mapper));
		}

		private record PresentResultTestCase<T, R>(String description, Unfolding<T> unfolding, Function<T, R> mapper,
												   Unfolding<R> expectedResult)
		{
		}

		private record EmptyResultTestCase<T, R>(String description, Unfolding<T> unfolding, Function<T, R> mapper)
		{
		}
	}

	@Nested
	@DisplayName("Tests for develop() method")
	class DevelopTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should conditionally transform present value to new present value")
		<T> void shouldTransformToPresentValue(String description, Unfolding<T> unfolding, Predicate<T> predicate,
											   Function<T, T> mapper, Unfolding<T> expectedResult)
		{
			Unfolding<T> result = unfolding.develop(predicate, mapper);

			assertThat(result.supple()).as("develop() for %s with predicate should result in present unfolding",
					unfolding).isTrue();
			assertThat(result.summon()).as("develop() result value should match expected")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T> void shouldTransformToEmptyUnfolding(String description, Unfolding<T> unfolding, Predicate<T> predicate,
												 Function<T, T> mapper)
		{
			Unfolding<T> result = unfolding.develop(predicate, mapper);

			assertThat(result.sterile()).as("develop() for %s with predicate should result in empty unfolding",
					unfolding).isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Predicate<String> nullPredicate = null;
			Function<String, String> mapper = String::toUpperCase;

			assertThatThrownBy(() -> unfolding.develop(nullPredicate, mapper)).as(
																					  "develop() with null predicate should throw NullPointerException")
																			  .isInstanceOf(NullPointerException.class)
																			  .hasMessageContaining(
																					  "judgement may not be null");
		}

		@DisplayName("should throw exception for null mapper")
		@Test
		void shouldThrowExceptionForNullMapper()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> nullMapper = null;

			assertThatThrownBy(() -> unfolding.develop(predicate, nullMapper)).as(
																					  "develop() with null mapper should throw NullPointerException")
																			  .isInstanceOf(NullPointerException.class)
																			  .hasMessageContaining(
																					  "development may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value matching predicate should be transformed",
										 Unfolding.beckon("hello"), s -> s.length() > 3, String::toUpperCase, Unfolding.beckon("HELLO")),
								 new PresentResultTestCase<>("Present value not matching predicate should remain unchanged",
										 Unfolding.beckon("hi"), s -> s.length() > 3, String::toUpperCase,
										 Unfolding.beckon("hi")),
								 new PresentResultTestCase<>("Mapper should not be applied when predicate doesn't match",
										 Unfolding.beckon(5), n -> n > 10, _ -> null, Unfolding.beckon(5)))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper,
								 tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(
								 new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of predicate and mapper",
										 Unfolding.<String>chaos(), _ -> true, String::toUpperCase),
								 new EmptyResultTestCase<>(
										 "Mapper returning null should result in empty unfolding when predicate matches",
										 Unfolding.beckon(42), n -> n > 10, _ -> null))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper));
		}

		private record PresentResultTestCase<T>(String description, Unfolding<T> unfolding, Predicate<T> predicate,
												Function<T, T> mapper, Unfolding<T> expectedResult)
		{
		}

		private record EmptyResultTestCase<T>(String description, Unfolding<T> unfolding, Predicate<T> predicate,
											  Function<T, T> mapper)
		{
		}
	}

	@Nested
	@DisplayName("Tests for evolve() method")
	class EvolveTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should conditionally transform present value to new present value")
		<T, R> void shouldTransformToPresentValue(String description, Unfolding<T> unfolding, Predicate<T> predicate,
												  Function<T, R> mapper, Unfolding<R> expectedResult)
		{
			Unfolding<R> result = unfolding.evolve(predicate, mapper);

			assertThat(result.supple()).as("evolve() for %s with predicate should result in present unfolding",
					unfolding).isTrue();
			assertThat(result.summon()).as("evolve() result value should match expected")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T, R> void shouldTransformToEmptyUnfolding(String description, Unfolding<T> unfolding, Predicate<T> predicate,
													Function<T, R> mapper)
		{
			Unfolding<R> result = unfolding.evolve(predicate, mapper);

			assertThat(result.sterile()).as("evolve() for %s with predicate should result in empty unfolding",
					unfolding).isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Predicate<String> nullPredicate = null;
			Function<String, String> mapper = String::toUpperCase;

			assertThatThrownBy(() -> unfolding.evolve(nullPredicate, mapper)).as(
																					 "evolve() with null predicate should throw NullPointerException")
																			 .isInstanceOf(NullPointerException.class)
																			 .hasMessageContaining(
																					 "judgement may not be null");
		}

		@DisplayName("should throw exception for null mapper")
		@Test
		void shouldThrowExceptionForNullMapper()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> nullMapper = null;

			assertThatThrownBy(() -> unfolding.evolve(predicate, nullMapper)).as(
																					 "evolve() with null mapper should throw NullPointerException")
																			 .isInstanceOf(NullPointerException.class)
																			 .hasMessageContaining(
																					 "evolution may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value matching predicate should be transformed",
										 Unfolding.beckon("hello"), s -> s.length() > 3, String::toUpperCase, Unfolding.beckon("HELLO")),
								 new PresentResultTestCase<>("Type transformation should work when predicate matches",
										 Unfolding.beckon(42), n -> n > 10, n -> "Number: " + n,
										 Unfolding.beckon("Number: 42")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper,
								 tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Present value not matching predicate should become empty",
										 Unfolding.beckon("hi"), s -> s.length() > 3, String::toUpperCase),
								 new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of predicate and mapper",
										 Unfolding.<String>chaos(), _ -> true, String::toUpperCase),
								 new EmptyResultTestCase<>(
										 "Mapper returning null should result in empty unfolding when predicate matches",
										 Unfolding.beckon(42), n -> n > 10, _ -> null),
								 new EmptyResultTestCase<>("Mapper should not be applied when predicate doesn't match",
										 Unfolding.beckon(5), n -> n > 10, _ -> "transformed"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper));
		}

		private record PresentResultTestCase<T, R>(String description, Unfolding<T> unfolding, Predicate<T> predicate,
												   Function<T, R> mapper, Unfolding<R> expectedResult)
		{
		}

		private record EmptyResultTestCase<T, R>(String description, Unfolding<T> unfolding, Predicate<T> predicate,
												 Function<T, R> mapper)
		{
		}
	}

	@Nested
	@DisplayName("Tests for cleave() method")
	class CleaveTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("truePredicateTestCases")
		@DisplayName("should apply trueMapper when predicate matches")
		<T, R> void shouldApplyTrueMapper(String description, Unfolding<T> unfolding, Predicate<T> predicate,
										  Function<T, R> trueMapper, Function<T, R> falseMapper,
										  Unfolding<R> expectedResult)
		{
			Unfolding<R> result = unfolding.cleave(predicate, trueMapper, falseMapper);

			assertThat(result.supple()).as("cleave() for %s with matching predicate should result in present unfolding",
					unfolding).isTrue();
			assertThat(result.summon()).as("cleave() result value should match expected from trueMapper")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("falsePredicateTestCases")
		@DisplayName("should apply falseMapper when predicate doesn't match")
		<T, R> void shouldApplyFalseMapper(String description, Unfolding<T> unfolding, Predicate<T> predicate,
										   Function<T, R> trueMapper, Function<T, R> falseMapper,
										   Unfolding<R> expectedResult)
		{
			Unfolding<R> result = unfolding.cleave(predicate, trueMapper, falseMapper);

			assertThat(result.supple()).as(
											   "cleave() for %s with non-matching predicate should result in present unfolding", unfolding)
									   .isTrue();
			assertThat(result.summon()).as("cleave() result value should match expected from falseMapper")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyUnfoldingTestCases")
		@DisplayName("should return empty unfolding when input is empty")
		<T, R> void shouldReturnEmptyForEmptyInput(String description, Unfolding<T> unfolding, Predicate<T> predicate,
												   Function<T, R> trueMapper, Function<T, R> falseMapper)
		{
			Unfolding<R> result = unfolding.cleave(predicate, trueMapper, falseMapper);

			assertThat(result.sterile()).as("cleave() for empty unfolding should result in empty unfolding").isTrue();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("nullResultTestCases")
		@DisplayName("should return empty unfolding when mapper returns null")
		<T, R> void shouldReturnEmptyForNullMapperResult(String description, Unfolding<T> unfolding,
														 Predicate<T> predicate, Function<T, R> trueMapper,
														 Function<T, R> falseMapper)
		{
			Unfolding<R> result = unfolding.cleave(predicate, trueMapper, falseMapper);

			assertThat(result.sterile()).as("cleave() with mapper returning null should result in empty unfolding")
										.isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Predicate<String> nullPredicate = null;
			Function<String, String> trueMapper = String::toUpperCase;
			Function<String, String> falseMapper = s -> s + "_suffix";

			assertThatThrownBy(() -> unfolding.cleave(nullPredicate, trueMapper, falseMapper)).as(
					"cleave() with null predicate should throw NullPointerException").isInstanceOf(
					NullPointerException.class).hasMessageContaining("judgement may not be null");
		}

		@DisplayName("should throw exception for null reward")
		@Test
		void shouldThrowExceptionForNullTrueMapper()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> nullMapper = null;
			Function<String, String> falseMapper = s -> s + "_suffix";

			assertThatThrownBy(() -> unfolding.cleave(predicate, nullMapper, falseMapper)).as(
					"cleave() with null reward should throw NullPointerException").isInstanceOf(
					NullPointerException.class).hasMessageContaining("reward may not be null");
		}

		@DisplayName("should throw exception for null punishment")
		@Test
		void shouldThrowExceptionForNullFalseMapper()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> trueMapper = String::toUpperCase;
			Function<String, String> nullMapper = null;

			assertThatThrownBy(() -> unfolding.cleave(predicate, trueMapper, nullMapper)).as(
					"cleave() with null punishment should throw NullPointerException").isInstanceOf(
					NullPointerException.class).hasMessageContaining("punishment may not be null");
		}

		private static Stream<Arguments> truePredicateTestCases()
		{
			return Stream.of(new TestCase<>("String length predicate matches, apply uppercase transformation",
										 Unfolding.beckon("hello"), s -> s.length() > 3, String::toUpperCase, s -> s + "_suffix",
										 Unfolding.beckon("HELLO")),
								 new TestCase<>("Integer value predicate matches, apply string conversion", Unfolding.beckon(42),
										 n -> n > 10, n -> "Number: " + n, n -> "Small: " + n, Unfolding.beckon("Number: 42")),
								 new TestCase<>("Boolean value predicate matches, apply conditional text", Unfolding.beckon(true),
										 b -> b, _ -> "It's true", _ -> "It's false", Unfolding.beckon("It's true")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.trueMapper,
								 tc.falseMapper, tc.expectedResult));
		}

		private static Stream<Arguments> falsePredicateTestCases()
		{
			return Stream.of(new TestCase<>("String length predicate doesn't match, apply suffix transformation",
										 Unfolding.beckon("hi"), s -> s.length() > 3, String::toUpperCase, s -> s + "_suffix",
										 Unfolding.beckon("hi_suffix")),
								 new TestCase<>("Integer value predicate doesn't match, apply small number conversion",
										 Unfolding.beckon(5), n -> n > 10, n -> "Number: " + n, n -> "Small: " + n,
										 Unfolding.beckon("Small: 5")),
								 new TestCase<>("Boolean value predicate doesn't match, apply conditional text",
										 Unfolding.beckon(false), b -> b, _ -> "It's true", _ -> "It's false",
										 Unfolding.beckon("It's false")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.trueMapper,
								 tc.falseMapper, tc.expectedResult));
		}

		private static Stream<Arguments> emptyUnfoldingTestCases()
		{
			return Stream.of(new EmptyTestCase<>("Empty unfolding with true predicate should remain empty",
										 Unfolding.<String>chaos(), _ -> true, String::toUpperCase, s -> s + "_suffix"),
								 new EmptyTestCase<>("Empty unfolding with false predicate should remain empty",
										 Unfolding.<Integer>chaos(), n -> n > 10, n -> n * 2, n -> n / 2))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.trueMapper,
								 tc.falseMapper));
		}

		private static Stream<Arguments> nullResultTestCases()
		{
			return Stream.of(new EmptyTestCase<>("TrueMapper returning null should result in empty unfolding",
										 Unfolding.beckon("test"), s -> s.length() > 3, _ -> null, s -> s + "_suffix"),
								 new EmptyTestCase<>("FalseMapper returning null should result in empty unfolding",
										 Unfolding.beckon("hi"), s -> s.length() > 3, String::toUpperCase, _ -> null))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.trueMapper,
								 tc.falseMapper));
		}

		private record TestCase<T, R>(String description, Unfolding<T> unfolding, Predicate<T> predicate,
									  Function<T, R> trueMapper, Function<T, R> falseMapper,
									  Unfolding<R> expectedResult)
		{
		}

		private record EmptyTestCase<T, R>(String description, Unfolding<T> unfolding, Predicate<T> predicate,
										   Function<T, R> trueMapper, Function<T, R> falseMapper)
		{
		}
	}


	@Nested
	@DisplayName("Tests for discern() method")
	class DiscernTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should keep value when predicate matches")
		<T> void shouldKeepValueWhenPredicateMatches(String description, Unfolding<T> unfolding, Predicate<T> predicate)
		{
			Unfolding<T> result = unfolding.discern(predicate);

			assertThat(result.supple()).as("discern() for %s with matching predicate should remain present", unfolding)
									   .isTrue();
			assertThat(result.summon()).as("discern() result value should match original")
									   .isEqualTo(unfolding.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should discard value when predicate doesn't match")
		<T> void shouldDiscardValueWhenPredicateDoesnotMatch(String description, Unfolding<T> unfolding,
															 Predicate<T> predicate)
		{
			Unfolding<T> result = unfolding.discern(predicate);

			assertThat(result.sterile()).as("discern() for %s with non-matching predicate should become empty",
					unfolding).isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Predicate<String> nullPredicate = null;

			assertThatThrownBy(() -> unfolding.discern(nullPredicate)).as(
																			  "discern() with null predicate should throw NullPointerException")
																	  .isInstanceOf(NullPointerException.class)
																	  .hasMessageContaining(
																			  "judgement may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value matching predicate should remain unchanged",
										 Unfolding.beckon("hello"), s -> s.length() > 3),
								 new PresentResultTestCase<>("Predicate with complex logic should work correctly (even numbers)",
										 Unfolding.beckon(42), n -> n % 2 == 0))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Present value not matching predicate should become empty",
										 Unfolding.beckon("hi"), s -> s.length() > 3),
								 new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of predicate",
										 Unfolding.<String>chaos(), _ -> true),
								 new EmptyResultTestCase<>("Predicate with complex logic should work correctly (odd numbers)",
										 Unfolding.beckon(43), n -> n % 2 == 0))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate));
		}

		private record PresentResultTestCase<T>(String description, Unfolding<T> unfolding, Predicate<T> predicate)
		{
		}

		private record EmptyResultTestCase<T>(String description, Unfolding<T> unfolding, Predicate<T> predicate)
		{
		}
	}

	@Nested
	@DisplayName("Tests for unlace() method")
	class UnlaceTests
	{
		@DisplayName("should apply consumer to present value and return the same unfolding")
		@Test
		void shouldApplyConsumerToPresentValue()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			List<String> capturedValues = new ArrayList<>();
			Consumer<String> consumer = capturedValues::add;

			Unfolding<String> result = unfolding.unlace(consumer);

			assertThat(result).as("unlace() should return the same unfolding").isSameAs(unfolding);
			assertThat(capturedValues).as("Consumer should be applied to the value").containsExactly("test");
		}

		@DisplayName("should not apply consumer to empty unfolding and return the same unfolding")
		@Test
		void shouldNotApplyConsumerToEmptyUnfolding()
		{
			Unfolding<String> unfolding = Unfolding.chaos();
			List<String> capturedValues = new ArrayList<>();
			Consumer<String> consumer = capturedValues::add;

			Unfolding<String> result = unfolding.unlace(consumer);

			assertThat(result).as("unlace() should return the same unfolding").isSameAs(unfolding);
			assertThat(capturedValues).as("Consumer should not be applied to empty unfolding").isEmpty();
		}

		@DisplayName("should throw exception for null consumer")
		@Test
		void shouldThrowExceptionForNullConsumer()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Consumer<String> nullConsumer = null;

			assertThatThrownBy(() -> unfolding.unlace(nullConsumer)).as(
																			"unlace() with null consumer should throw NullPointerException")
																	.isInstanceOf(NullPointerException.class)
																	.hasMessageContaining(
																			"impregnator may not be null");
		}
	}

	@Nested
	@DisplayName("Tests for summon() method")
	class SummonTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("successTestCases")
		@DisplayName("should return value for present unfolding")
		<T> void shouldReturnValue(String description, Unfolding<T> unfolding, T expectedValue)
		{
			T result = unfolding.summon();
			assertThat(result).as("summon() for %s should return %s", unfolding, expectedValue)
							  .isEqualTo(expectedValue);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("exceptionTestCases")
		@DisplayName("should throw exception for empty unfolding")
		<T> void shouldThrowException(String description, Unfolding<T> unfolding)
		{
			assertThatThrownBy(unfolding::summon).as("summon() for %s should throw EmptyUnfoldingException", unfolding)
												 .isInstanceOf(EmptyUnfoldingException.class)
												 .hasMessageContaining("empty");
		}

		private static Stream<Arguments> successTestCases()
		{
			return Stream.of(
								 new SuccessTestCase<>("Present value should be returned", Unfolding.beckon("hello"), "hello"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedValue));
		}

		private static Stream<Arguments> exceptionTestCases()
		{
			return Stream.of(new ExceptionTestCase<>("Empty unfolding should throw exception", Unfolding.chaos()),
								 new ExceptionTestCase<>("Unfolding with null value should throw exception", Unfolding.beckon(null)))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding));
		}

		private record SuccessTestCase<T>(String description, Unfolding<T> unfolding, T expectedValue)
		{
		}

		private record ExceptionTestCase<T>(String description, Unfolding<T> unfolding)
		{
		}
	}

	@Nested
	@DisplayName("Tests for optional() method")
	class OptionalTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentTestCases")
		@DisplayName("should convert present unfolding to present Optional")
		<T> void shouldConvertPresentUnfoldingToPresentOptional(String description, Unfolding<T> unfolding,
																T expectedValue)
		{
			Optional<T> result = unfolding.optional();

			assertThat(result.isPresent()).as("optional() for %s should result in present Optional", unfolding)
										  .isTrue();
			assertThat(result.get()).as("optional() result value should match expected").isEqualTo(expectedValue);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyTestCases")
		@DisplayName("should convert empty unfolding to empty Optional")
		<T> void shouldConvertEmptyUnfoldingToEmptyOptional(String description, Unfolding<T> unfolding)
		{
			Optional<T> result = unfolding.optional();

			assertThat(result.isPresent()).as("optional() for %s should result in empty Optional", unfolding).isFalse();
		}

		private static Stream<Arguments> presentTestCases()
		{
			return Stream.of(new PresentTestCase<>("Present value should be converted to present Optional",
								 Unfolding.beckon("hello"), "hello"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedValue));
		}

		private static Stream<Arguments> emptyTestCases()
		{
			return Stream.of(
					new EmptyTestCase<>("Empty unfolding should be converted to empty Optional", Unfolding.chaos()),
					new EmptyTestCase<>("Unfolding created with null should be converted to empty Optional",
							Unfolding.beckon(null))).map(tc -> Arguments.of(tc.description, tc.unfolding));
		}

		private record PresentTestCase<T>(String description, Unfolding<T> unfolding, T expectedValue)
		{
		}

		private record EmptyTestCase<T>(String description, Unfolding<T> unfolding)
		{
		}
	}

	@Nested
	@DisplayName("Tests for isEmpty() method")
	class IsEmptyTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should correctly identify if unfolding is empty")
		<T> void shouldIdentifyIfEmpty(String description, Unfolding<T> unfolding, boolean expectedResult)
		{
			boolean result = unfolding.sterile();
			assertThat(result).as("isEmpty() for %s should give %s", unfolding, expectedResult)
							  .isEqualTo(expectedResult);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
								 new TestCase<>("Unfolding with non-null value should not be empty", Unfolding.beckon("value"),
										 false),
								 new TestCase<>("Empty unfolding should be empty", Unfolding.chaos(), true),
								 new TestCase<>("Unfolding created with null should be empty", Unfolding.beckon(null), true))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedResult));
		}

		private record TestCase<T>(String description, Unfolding<T> unfolding, boolean expectedResult)
		{
		}
	}

	@Nested
	@DisplayName("Tests for factory methods")
	class FactoryMethodTests
	{
		@Nested
		@DisplayName("Tests for beckon() method")
		class OfMethodTests
		{
			@DisplayName("should create non-empty unfolding for non-null value")
			@Test
			void shouldCreateNonEmptyUnfoldingForNonNullValue()
			{
				String value = "test";
				Unfolding<String> unfolding = Unfolding.beckon(value);

				assertThat(unfolding.supple()).as("beckon() with non-null value should create present unfolding")
											  .isTrue();
				assertThat(unfolding.summon()).as("beckon() should store the provided value").isEqualTo(value);
			}

			@DisplayName("should create empty unfolding for null value")
			@Test
			void shouldCreateEmptyUnfoldingForNullValue()
			{
				Unfolding<String> unfolding = Unfolding.beckon(null);

				assertThat(unfolding.sterile()).as("beckon() with null value should create empty unfolding").isTrue();
				assertThatThrownBy(unfolding::summon).as("summon() on empty unfolding should throw exception")
													 .isInstanceOf(EmptyUnfoldingException.class);
			}

			@DisplayName("should create unfolding with primitive value")
			@Test
			void shouldCreateUnfoldingWithPrimitiveValue()
			{
				int value = 42;
				Unfolding<Integer> unfolding = Unfolding.beckon(value);

				assertThat(unfolding.supple()).as("beckon() with primitive value should create present unfolding")
											  .isTrue();
				assertThat(unfolding.summon()).as("beckon() should store the provided primitive value")
											  .isEqualTo(value);
			}
		}

		@Nested
		@DisplayName("Tests for augur() method")
		class AugurMethodTests
		{
			@DisplayName("should create an empty unfolding")
			@Test
			void shouldCreateChaoticUnfolding()
			{
				var unfolding = Unfolding.augur(Optional.empty());

				assertThat(unfolding.sterile()).as("An empty optional creates a sterile unfolding").isTrue();
			}

			@DisplayName("should create a non-empty unfolding")
			@Test
			void shouldCreatePresentUnfolding()
			{
				var unfolding = Unfolding.augur(Optional.of("test"));

				assertThat(unfolding.supple()).as("A non-empty optional creates a present unfolding").isTrue();
				assertThat(unfolding.summon()).as("summon() should return the value from the optional")
											  .isEqualTo("test");
			}
		}

		@Nested
		@DisplayName("Tests for distill() method")
		class DistillMethodTests
		{
			@DisplayName("Should create a chaotic unfolding from an empty stream")
			@Test
			void shouldCreateChaoticUnfoldingFromEmptyStream()
			{
				var unfolding = Unfolding.distill(Stream.empty());

				assertThat(unfolding.sterile()).as("An empty stream creates a sterile unfolding").isTrue();
			}

			@DisplayName("Should create a present unfolding from a non-empty stream")
			@Test
			void shouldCreatePresentUnfoldingFromNonEmptyStream()
			{
				var unfolding = Unfolding.distill(Stream.of("test"));

				assertThat(unfolding.supple()).as("A non-empty stream creates a present unfolding").isTrue();
				assertThat(unfolding.summon()).as("summon() should return the value from the stream").isEqualTo("test");
			}

			@DisplayName("should create a chaotic unfolding from an empty collection")
			@ParameterizedTest(name = "{0}")
			@MethodSource("emptyCollections")
			<T> void shouldCreateAChaoticUnfoldingFromEmptyCollection(final String description,
																	  final Collection<T> collection)
			{
				assertThat(collection).as("The provided collection must be empty for this test case").isEmpty();

				assertThat(Unfolding.distill(collection.stream()).sterile()).as(
						"An empty collection creates a sterile unfolding").isTrue();
			}

			@DisplayName("should create an unfolding with one of the elements from collection")
			@ParameterizedTest(name = "{0}")
			@MethodSource("collectionTestCases")
			<T> void shouldCreateUnfoldingWithOneOfTheElementsFromCollection(String description, Collection<T> values)
			{
				assertThat(values).as("collection should not be empty").isNotEmpty();

				var unfolding = Unfolding.distill(values.stream());

				assertThat(unfolding.supple()).as("distill() should create a supple unfolding").isTrue();

				assertThat(unfolding.summon()).as("summon() should return the value from the stream").isIn(values);
			}

			private static Stream<Arguments> emptyCollections()
			{
				return Stream.of(new CollectionTestCase<>("List.of()", List.of()),
									 new CollectionTestCase<>("Set.of()", Set.of()),
									 new CollectionTestCase<>("Collections.emptySet()", Collections.emptySet()),
									 new CollectionTestCase<>("Collections.emptyList()", Collections.emptyList()),
									 new CollectionTestCase<>("Arraylist", new ArrayList<>()),
									 new CollectionTestCase<>("HashSet", new HashSet<>()))
							 .map(tc -> Arguments.of(tc.description(), tc.values()));
			}

			private static Stream<Arguments> collectionTestCases()
			{
				return Stream.of(
						new CollectionTestCase<>("a single element set should produce that unfolding", Set.of("a")),
						new CollectionTestCase<>("a single element list should produce the corresponding unfolding",
								List.of(1.3e8)),
						new CollectionTestCase<>("a multiple element list should produce the corresponding unfolding",
								List.of(1, 2))).map(tc -> Arguments.of(tc.description, tc.values));
			}

			private record CollectionTestCase<T>(String description, Collection<T> values)
			{
			}
		}

		@Nested
		@DisplayName("Tests for chaos() method")
		class EmptyMethodTests
		{
			@DisplayName("should create empty unfolding")
			@Test
			void shouldCreateEmptyUnfolding()
			{
				Unfolding<String> unfolding = Unfolding.chaos();

				assertThat(unfolding.sterile()).as("chaos() should create empty unfolding").isTrue();
				assertThat(unfolding.supple()).as("chaos() should create unfolding that is not present").isFalse();
				assertThatThrownBy(unfolding::summon).as("summon() on empty unfolding should throw exception")
													 .isInstanceOf(EmptyUnfoldingException.class);
			}

			@DisplayName("should return same instance for multiple calls")
			@Test
			void shouldReturnSameInstanceForMultipleCalls()
			{
				Unfolding<String> unfolding1 = Unfolding.chaos();
				Unfolding<Integer> unfolding2 = Unfolding.chaos();

				assertThat(unfolding1).as("chaos() should return same instance for different calls")
									  .isSameAs(unfolding2);
			}

			@DisplayName("should return same instance for different generic types")
			@Test
			void shouldReturnSameInstanceForDifferentGenericTypes()
			{
				Unfolding<String> stringUnfolding = Unfolding.chaos();
				Unfolding<Integer> intUnfolding = Unfolding.chaos();
				Unfolding<List<String>> listUnfolding = Unfolding.chaos();

				assertThat(stringUnfolding).as("chaos() should return same instance for String type")
										   .isSameAs(intUnfolding);
				assertThat(intUnfolding).as("chaos() should return same instance for Integer type")
										.isSameAs(listUnfolding);
			}
		}
	}

	@Nested
	@DisplayName("Tests for entwine() method")
	class EntwineTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should transform present value to another unfolding")
		<T, R> void shouldTransformToPresentValue(String description, Unfolding<T> unfolding,
												  Function<T, Unfolding<R>> transformer, Unfolding<R> expectedResult)
		{
			Unfolding<R> result = unfolding.entwine(transformer);

			assertThat(result.supple()).as("entwine() for %s should result in present unfolding", unfolding).isTrue();
			assertThat(result.summon()).as("entwine() result value should match expected")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T, R> void shouldTransformToEmptyUnfolding(String description, Unfolding<T> unfolding,
													Function<T, Unfolding<R>> transformer)
		{
			Unfolding<R> result = unfolding.entwine(transformer);

			assertThat(result.sterile()).as("entwine() for %s should result in empty unfolding", unfolding).isTrue();
		}

		@DisplayName("should throw exception for null transformer")
		@Test
		void shouldThrowExceptionForNullTransformer()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Function<String, Unfolding<Integer>> nullTransformer = null;

			assertThatThrownBy(() -> unfolding.entwine(nullTransformer)).as(
																				"entwine() with null transformer should throw NullPointerException")
																		.isInstanceOf(NullPointerException.class);
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(
								 new PresentResultTestCase<>("Present value should be transformed to another present unfolding",
										 Unfolding.beckon("hello"), s -> Unfolding.beckon(s.length()), Unfolding.beckon(5)),
								 new PresentResultTestCase<>(
										 "Present value should be transformed to another present unfolding with same type",
										 Unfolding.beckon("hello"), s -> Unfolding.beckon(s.toUpperCase()),
										 Unfolding.beckon("HELLO")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.transformer, tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of transformer",
								 Unfolding.<String>chaos(), s -> Unfolding.beckon(s.length())), new EmptyResultTestCase<>(
								 "Present value should be transformed to empty unfolding when transformer returns empty",
								 Unfolding.beckon(42), _ -> Unfolding.<String>chaos()))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.transformer));
		}

		private record PresentResultTestCase<T, R>(String description, Unfolding<T> unfolding,
												   Function<T, Unfolding<R>> transformer, Unfolding<R> expectedResult)
		{
		}

		private record EmptyResultTestCase<T, R>(String description, Unfolding<T> unfolding,
												 Function<T, Unfolding<R>> transformer)
		{
		}
	}

	@Nested
	@DisplayName("Tests for interlace() method")
	class InterlaceTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should transform present value to pair containing original and transformed values")
		<T, R> void shouldTransformToPresentValue(String description, Unfolding<T> unfolding,
												  Function<T, R> transformer, Unfolding<Pair<T, R>> expectedResult)
		{
			Unfolding<Pair<T, R>> result = unfolding.interlace(transformer);

			assertThat(result.supple()).as("interlace() for %s should result in present unfolding", unfolding).isTrue();
			assertThat(result.summon().first()).as("interlace() result first value should match original")
											   .isEqualTo(unfolding.summon());
			assertThat(result.summon().second()).as("interlace() result second value should match transformed")
												.isEqualTo(expectedResult.summon().second());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T, R> void shouldTransformToEmptyUnfolding(String description, Unfolding<T> unfolding,
													Function<T, R> transformer)
		{
			Unfolding<Pair<T, R>> result = unfolding.interlace(transformer);

			assertThat(result.sterile()).as("interlace() for %s should result in empty unfolding", unfolding).isTrue();
		}

		@DisplayName("should throw exception for null transformer")
		@Test
		void shouldThrowExceptionForNullTransformer()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Function<String, Integer> nullTransformer = null;

			assertThatThrownBy(() -> unfolding.interlace(nullTransformer)).as(
																				  "interlace() with null transformer should throw NullPointerException")
																		  .isInstanceOf(NullPointerException.class)
																		  .hasMessageContaining(
																				  "interlacing may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(
								 new PresentResultTestCase<>("Present string value should be transformed to pair with length",
										 Unfolding.beckon("hello"), String::length, Unfolding.beckon(Pair.of("hello", 5))),
								 new PresentResultTestCase<>("Present string value should be transformed to pair with uppercase",
										 Unfolding.beckon("hello"), String::toUpperCase,
										 Unfolding.beckon(Pair.of("hello", "HELLO"))), new PresentResultTestCase<>(
										 "Present integer value should be transformed to pair with string representation",
										 Unfolding.beckon(42), Object::toString, Unfolding.beckon(Pair.of(42, "42"))),
								 new PresentResultTestCase<>(
										 "Present value should be transformed to pair with null when transformer returns null",
										 Unfolding.beckon("test"), _ -> null, Unfolding.beckon(Pair.of("test", null))),
								 new PresentResultTestCase<>("Present empty string should be transformed correctly",
										 Unfolding.beckon(""), String::length, Unfolding.beckon(Pair.of("", 0))))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.transformer, tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of transformer",
								 Unfolding.chaos(), String::length))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.transformer));
		}

		private record PresentResultTestCase<T, R>(String description, Unfolding<T> unfolding,
												   Function<T, R> transformer, Unfolding<Pair<T, R>> expectedResult)
		{
		}

		private record EmptyResultTestCase<T, R>(String description, Unfolding<T> unfolding, Function<T, R> transformer)
		{
		}
	}

	@Nested
	@DisplayName("Tests for braid() method")
	class BraidTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("suppleBraidTestCases")
		@DisplayName("should braid two supple unfoldings")
		<T, U, R> void shouldBraidTwoSuppleUnfoldings(String description, Unfolding<T> journey, Unfolding<U> consort,
													  BiFunction<T, U, R> weaver, Unfolding<R> expectedResult)
		{
			Unfolding<R> result = journey.braid(consort, weaver);

			assertThat(result.supple()).as("braid() for %s should result in expected unfolding", journey)
									   .isEqualTo(expectedResult.supple());
			assertThat(result.summon()).as("braid() result value should match expected")
									   .isEqualTo(expectedResult.summon());
		}

		@DisplayName("should throw exception for null consort")
		@Test
		void shouldThrowExceptionForNullConsort()
		{
			Unfolding<String> journey = Unfolding.beckon("test");
			Unfolding<String> nullConsort = null;
			BiFunction<String, String, String> weaver = String::concat;

			assertThatThrownBy(() -> journey.braid(nullConsort, weaver)).as(
																				"braid() with null consort should throw NullPointerException")
																		.isInstanceOf(NullPointerException.class)
																		.hasMessageContaining(
																				"consort may not be null");
		}

		@DisplayName("should throw exception for null weaver")
		@Test
		void shouldThrowExceptionForNullWeaver()
		{
			Unfolding<String> journey = Unfolding.beckon("test");
			Unfolding<String> consort = Unfolding.beckon("partner");
			BiFunction<String, String, String> nullWeaver = null;

			assertThatThrownBy(() -> journey.braid(consort, nullWeaver)).as(
																				"braid() with null weaver should throw NullPointerException")
																		.isInstanceOf(NullPointerException.class)
																		.hasMessageContaining("weaver may not be null");
		}

		@DisplayName("should return empty unfolding when journey is empty")
		@Test
		void shouldReturnEmptyUnfoldingWhenJourneyIsEmpty()
		{
			Unfolding<String> emptyJourney = Unfolding.chaos();
			Unfolding<String> consort = Unfolding.beckon("partner");
			BiFunction<String, String, String> weaver = String::concat;

			Unfolding<String> result = emptyJourney.braid(consort, weaver);

			assertThat(result.sterile()).as("braid() with empty journey should result in empty unfolding").isTrue();
		}

		@DisplayName("should return empty unfolding when consort is empty")
		@Test
		void shouldReturnEmptyUnfoldingWhenConsortIsEmpty()
		{
			Unfolding<String> journey = Unfolding.beckon("test");
			Unfolding<String> emptyConsort = Unfolding.chaos();
			BiFunction<String, String, String> weaver = String::concat;

			Unfolding<String> result = journey.braid(emptyConsort, weaver);

			assertThat(result.sterile()).as("braid() with empty consort should result in empty unfolding").isTrue();
		}

		@DisplayName("should return empty unfolding when weaver provides null result")
		@Test
		void shouldReturnEmptyUnfoldingWhenWeaverProvidesNullResult()
		{
			Unfolding<String> journey = Unfolding.beckon("test");
			Unfolding<String> consort = Unfolding.beckon("partner");
			BiFunction<String, String, String> nullResultWeaver = (_, _) -> null;

			Unfolding<String> result = journey.braid(consort, nullResultWeaver);

			assertThat(result.sterile()).as("braid() with weaver returning null should result in empty unfolding")
										.isTrue();
		}

		private static Stream<Arguments> suppleBraidTestCases()
		{
			return Stream.of(new SuppleBraidTestCase<>("Two string unfoldings should be combined with concatenation",
										 Unfolding.beckon("hello"), Unfolding.beckon(" world"), String::concat,
										 Unfolding.beckon("hello world")), new SuppleBraidTestCase<>(
										 "Integer and string unfoldings should be combined with toString and concatenation",
										 Unfolding.beckon(42), Unfolding.beckon(" is the answer"), (i, s) -> i.toString() + s,
										 Unfolding.beckon("42 is the answer")),
								 new SuppleBraidTestCase<>("Two integer unfoldings should be combined with addition",
										 Unfolding.beckon(10), Unfolding.beckon(5), Integer::sum, Unfolding.beckon(15)),
								 new SuppleBraidTestCase<>("Two unfoldings should be combined to create a pair",
										 Unfolding.beckon("key"), Unfolding.beckon("value"), Pair::of,
										 Unfolding.beckon(Pair.of("key", "value"))))
						 .map(tc -> Arguments.of(tc.description, tc.journey, tc.consort, tc.weaver, tc.expectedResult));
		}

		private record SuppleBraidTestCase<T, U, R>(String description, Unfolding<T> journey, Unfolding<U> consort,
													BiFunction<T, U, R> weaver, Unfolding<R> expectedResult)
		{
		}
	}

	@Nested
	@DisplayName("Tests for resurrect() method")
	class ResurrectTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("suppleResurrectTestCases")
		@DisplayName("should resurrect myth unfolding")
		<T> void shouldResurrectMythUnfolding(String description, Unfolding<T> unfolding, Supplier<Unfolding<T>> grace,
											  Unfolding<T> expectedResult)
		{
			Unfolding<T> result = unfolding.resurrect(grace);

			assertThat(result.supple()).as("resurrect() for %s should result in expected unfolding", unfolding)
									   .isEqualTo(expectedResult.supple());
			assertThat(result.summon()).as("resurrect() result value should match expected")
									   .isEqualTo(expectedResult.summon());
		}

		@DisplayName("should throw exception for null grace supplier")
		@Test
		void shouldThrowExceptionForNullGraceSupplier()
		{
			Unfolding<String> unfolding = Unfolding.chaos();
			Supplier<Unfolding<String>> nullGrace = null;

			assertThatThrownBy(() -> unfolding.resurrect(nullGrace)).as(
																			"resurrect() with null grace supplier should throw NullPointerException")
																	.isInstanceOf(NullPointerException.class)
																	.hasMessageContaining("grace may not be null");
		}

		@DisplayName("should return empty unfolding when grace supplier returns null")
		@Test
		void shouldReturnEmptyUnfoldingWhenGraceSupplierReturnsNull()
		{
			Unfolding<String> unfolding = Unfolding.chaos();
			Supplier<Unfolding<String>> nullResultGrace = () -> null;

			Unfolding<String> result = unfolding.resurrect(nullResultGrace);

			assertThat(result.sterile()).as(
					"resurrect() with grace supplier returning null should result in empty unfolding").isTrue();
		}

		@DisplayName("should return empty unfolding when grace supplier returns empty unfolding")
		@Test
		void shouldReturnEmptyUnfoldingWhenGraceSupplierReturnsEmptyUnfolding()
		{
			Unfolding<String> unfolding = Unfolding.chaos();
			Supplier<Unfolding<String>> emptyUnfoldingGrace = Unfolding::chaos;

			Unfolding<String> result = unfolding.resurrect(emptyUnfoldingGrace);

			assertThat(result.sterile()).as(
												"resurrect() with grace supplier returning empty unfolding should result in empty unfolding")
										.isTrue();
		}

		private static Stream<Arguments> suppleResurrectTestCases()
		{
			return Stream.of(new SuppleResurrectTestCase<>("Myth unfolding should return itself regardless of grace",
										 Unfolding.beckon("test"), () -> Unfolding.beckon("grace"), Unfolding.beckon("test")),
								 new SuppleResurrectTestCase<>("Shell unfolding should return grace unfolding", Unfolding.chaos(),
										 () -> Unfolding.beckon("grace"), Unfolding.beckon("grace")),
								 new SuppleResurrectTestCase<>("Shell unfolding with integer grace", Unfolding.chaos(),
										 () -> Unfolding.beckon(42), Unfolding.beckon(42)))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.grace, tc.expectedResult));
		}

		private record SuppleResurrectTestCase<T>(String description, Unfolding<T> unfolding,
												  Supplier<Unfolding<T>> grace, Unfolding<T> expectedResult)
		{
		}
	}

	@Nested
	@DisplayName("Tests for conjoin() method")
	class ConjoinTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should transform present value with consort to new value")
		<T, U, R> void shouldTransformToPresentValue(String description, Unfolding<T> unfolding, U consort,
													 BiFunction<T, U, R> conjugation, Unfolding<R> expectedResult)
		{
			Unfolding<R> result = unfolding.conjoin(consort, conjugation);

			assertThat(result.supple()).as("conjoin() for %s should result in present unfolding", unfolding).isTrue();
			assertThat(result.summon()).as("conjoin() result should match expected value")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T, U, R> void shouldTransformToEmptyUnfolding(String description, Unfolding<T> unfolding, U consort,
													   BiFunction<T, U, R> conjugation)
		{
			Unfolding<R> result = unfolding.conjoin(consort, conjugation);

			assertThat(result.sterile()).as("conjoin() for %s should result in empty unfolding", unfolding).isTrue();
		}

		@DisplayName("should throw exception for null consort")
		@Test
		void shouldThrowExceptionForNullConsort()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			String nullConsort = null;
			BiFunction<String, String, String> conjugation = String::concat;

			assertThatThrownBy(() -> unfolding.conjoin(nullConsort, conjugation)).as(
					"conjoin() with null consort should throw NullPointerException").isInstanceOf(
					NullPointerException.class).hasMessageContaining("consort may not be null");
		}

		@DisplayName("should throw exception for null conjugation function")
		@Test
		void shouldThrowExceptionForNullConjugation()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			String consort = "partner";
			BiFunction<String, String, String> nullConjugation = null;

			assertThatThrownBy(() -> unfolding.conjoin(consort, nullConjugation)).as(
					"conjoin() with null conjugation should throw NullPointerException").isInstanceOf(
					NullPointerException.class).hasMessageContaining("conjugation may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present string value should be combined with another string",
										 Unfolding.beckon("hello"), " world", String::concat, Unfolding.beckon("hello world")),
								 new PresentResultTestCase<>("Present string value should be combined with an integer",
										 Unfolding.beckon("Count: "), 42, (s, i) -> s + i, Unfolding.beckon("Count: 42")),
								 new PresentResultTestCase<>("Present integer value should be combined with another integer",
										 Unfolding.beckon(10), 5, Integer::sum, Unfolding.beckon(15)),
								 new PresentResultTestCase<>("Present value should be combined with consort to create a pair",
										 Unfolding.beckon("key"), "value", Pair::of, Unfolding.beckon(Pair.of("key", "value"))),
								 new PresentResultTestCase<>("Present empty string should be combined correctly",
										 Unfolding.beckon(""), "suffix", String::concat, Unfolding.beckon("suffix")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.consort, tc.conjugation,
								 tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>(
								 "Empty unfolding should remain empty regardless of consort and conjugation", Unfolding.chaos(),
								 "partner", String::concat))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.consort, tc.conjugation));
		}

		private record PresentResultTestCase<T, U, R>(String description, Unfolding<T> unfolding, U consort,
													  BiFunction<T, U, R> conjugation, Unfolding<R> expectedResult)
		{
		}

		private record EmptyResultTestCase<T, U, R>(String description, Unfolding<T> unfolding, U consort,
													BiFunction<T, U, R> conjugation)
		{
		}
	}

	@Nested
	@DisplayName("Tests for interdict() method")
	class InterdictTests
	{
		@DisplayName("should throw exception for present value")
		@Test
		void shouldThrowExceptionForPresentValue()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			RuntimeException testException = new RuntimeException("Test exception");

			assertThatThrownBy(() -> unfolding.interdict(() -> testException)).as(
					"interdict() for present value should throw the supplied exception").isSameAs(testException);
		}

		@DisplayName("should not throw exception for empty unfolding")
		@Test
		void shouldNotThrowExceptionForEmptyUnfolding()
		{
			Unfolding<String> unfolding = Unfolding.chaos();
			RuntimeException testException = new RuntimeException("Test exception");

			assertThatCode(() -> unfolding.interdict(() -> testException)).as(
					"interdict() for empty unfolding should not throw exception").doesNotThrowAnyException();
		}

		@DisplayName("should throw exception for null supplier")
		@Test
		void shouldThrowExceptionForNullSupplier()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Supplier<RuntimeException> nullSupplier = null;

			assertThatThrownBy(() -> unfolding.interdict(nullSupplier)).as(
																			   "interdict() with null supplier should throw NullPointerException")
																	   .isInstanceOf(NullPointerException.class);
		}

		@DisplayName("should throw exception when supplier returns null")
		@Test
		void shouldThrowExceptionWhenSupplierReturnsNull()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");

			assertThatThrownBy(() -> unfolding.interdict(() -> null)).as(
																			 "interdict() with supplier returning null should throw NullPointerException")
																	 .isInstanceOf(NullPointerException.class);
		}

		@DisplayName("should throw exception for present value with function")
		@Test
		void shouldThrowExceptionForPresentValueWithFunction()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Function<? super String, Supplier<? extends RuntimeException>> wrathFunction =
					value -> () -> new RuntimeException("Test exception for: " + value);

			assertThatThrownBy(() -> unfolding.interdict(wrathFunction)).as(
																				"interdict() with function for present value should throw the exception from function result")
																		.isInstanceOf(RuntimeException.class)
																		.hasMessage("Test exception for: test");
		}

		@DisplayName("should not throw exception for empty unfolding with function")
		@Test
		void shouldNotThrowExceptionForEmptyUnfoldingWithFunction()
		{
			Unfolding<String> unfolding = Unfolding.chaos();
			Function<String, Supplier<? extends RuntimeException>> wrathFunction = _ -> () -> new RuntimeException(
					"Should not be called");

			assertThatCode(() -> unfolding.interdict(wrathFunction)).as(
																			"interdict() with function for empty unfolding should not throw exception")
																	.doesNotThrowAnyException();
		}

		@DisplayName("should throw exception for null function")
		@Test
		void shouldThrowExceptionForNullFunction()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Function<String, Supplier<? extends RuntimeException>> nullFunction = null;

			assertThatThrownBy(() -> unfolding.interdict(nullFunction)).as(
																			   "interdict() with null function should throw NullPointerException")
																	   .isInstanceOf(NullPointerException.class);
		}

		@DisplayName("should throw exception when function returns null")
		@Test
		void shouldThrowExceptionWhenFunctionReturnsNull()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Function<String, Supplier<? extends RuntimeException>> nullReturningFunction = _ -> null;

			assertThatThrownBy(() -> unfolding.interdict(nullReturningFunction)).as(
																						"interdict() with function returning null should throw NullPointerException")
																				.isInstanceOf(
																						NullPointerException.class);
		}

		@DisplayName("should throw exception when function's supplier returns null")
		@Test
		void shouldThrowExceptionWhenFunctionSupplierReturnsNull()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Function<String, Supplier<? extends RuntimeException>> nullSupplierFunction = _ -> () -> null;

			assertThatThrownBy(() -> unfolding.interdict(nullSupplierFunction)).as(
																					   "interdict() with function whose supplier returns null should throw NullPointerException")
																			   .isInstanceOf(
																					   NullPointerException.class);
		}

		@DisplayName("should pass correct value to function")
		@Test
		void shouldPassCorrectValueToFunction()
		{
			String testValue = "test-value";
			Unfolding<String> unfolding = Unfolding.beckon(testValue);

			Function<String, Supplier<? extends RuntimeException>> wrathFunction = value ->
			{
				assertThat(value).as("Function should receive the correct value").isEqualTo(testValue);
				return () -> new RuntimeException("Expected exception");
			};

			assertThatThrownBy(() -> unfolding.interdict(wrathFunction))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("Expected exception");
		}
	}

	@Nested
	@DisplayName("Tests for discern() method with exception supplier")
	class DiscernWithExceptionTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("successTestCases")
		@DisplayName("should keep value when predicate matches")
		<T> void shouldKeepValueWhenPredicateMatches(String description, Unfolding<T> unfolding, Predicate<T> predicate)
		{
			RuntimeException testException = new RuntimeException("Test exception");
			Unfolding<T> result = unfolding.discern(predicate, () -> testException);

			assertThat(result.supple()).as(
											   "discern() with exception supplier for %s with matching predicate should remain present", unfolding)
									   .isTrue();
			assertThat(result.summon()).as("discern() with exception supplier result value should match original")
									   .isEqualTo(unfolding.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("exceptionTestCases")
		@DisplayName("should throw exception when predicate doesn't match")
		<T> void shouldThrowExceptionWhenPredicateDoesntMatch(String description, Unfolding<T> unfolding,
															  Predicate<T> predicate)
		{
			RuntimeException testException = new RuntimeException("Test exception");

			assertThatThrownBy(() -> unfolding.discern(predicate, () -> testException)).as(
					"discern() with exception supplier for %s with non-matching predicate should throw exception",
					unfolding).isSameAs(testException);
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Predicate<String> nullPredicate = null;
			RuntimeException testException = new RuntimeException("Test exception");

			assertThatThrownBy(() -> unfolding.discern(nullPredicate, () -> testException)).as(
																								   "discern() with exception supplier with null predicate should throw NullPointerException")
																						   .isInstanceOf(
																								   NullPointerException.class)
																						   .hasMessageContaining(
																								   "judgement may not be null");
		}

		@DisplayName("should throw exception for null exception supplier")
		@Test
		void shouldThrowExceptionForNullExceptionSupplier()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");
			Supplier<RuntimeException> nullSupplier = null;

			assertThatThrownBy(() -> unfolding.discern(_ -> true, nullSupplier)).as(
					"discern() with null exception supplier should throw NullPointerException").isInstanceOf(
					NullPointerException.class).hasMessageContaining("exceptionSupplier may not be null");
		}

		@DisplayName("should throw exception when supplier returns null")
		@Test
		void shouldThrowExceptionWhenSupplierReturnsNull()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");

			assertThatThrownBy(() -> unfolding.discern(_ -> false, () -> null)).as(
					"discern() with supplier returning null should throw NullPointerException").isInstanceOf(
					NullPointerException.class);
		}

		@DisplayName("should always throw exception for empty unfolding")
		@Test
		void shouldAlwaysThrowExceptionForEmptyUnfolding()
		{
			Unfolding<String> unfolding = Unfolding.chaos();
			RuntimeException testException = new RuntimeException("Test exception");

			assertThatThrownBy(() -> unfolding.discern(_ -> true, () -> testException)).as(
																							   "discern() with exception supplier for empty unfolding should throw exception")
																					   .isSameAs(testException);
		}

		private static Stream<Arguments> successTestCases()
		{
			return Stream.of(new SuccessTestCase<>("Present value matching predicate should remain unchanged",
										 Unfolding.beckon("hello"), s -> s.length() > 3),
								 new SuccessTestCase<>("Predicate with complex logic should work correctly (even numbers)",
										 Unfolding.beckon(42), n -> n % 2 == 0))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate));
		}

		private static Stream<Arguments> exceptionTestCases()
		{
			return Stream.of(new ExceptionTestCase<>("Present value not matching predicate should throw exception",
										 Unfolding.beckon("hi"), s -> s.length() > 3),
								 new ExceptionTestCase<>("Predicate with complex logic should work correctly (odd numbers)",
										 Unfolding.beckon(43), n -> n % 2 == 0))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate));
		}

		private record SuccessTestCase<T>(String description, Unfolding<T> unfolding, Predicate<T> predicate)
		{
		}

		private record ExceptionTestCase<T>(String description, Unfolding<T> unfolding, Predicate<T> predicate)
		{
		}
	}

	@Nested
	@DisplayName("Tests for method chaining")
	class MethodChainingTests
	{
		@DisplayName("should support chaining multiple operations on present value")
		@Test
		void shouldSupportChainingMultipleOperationsOnPresentValue()
		{
			Unfolding<String> unfolding = Unfolding.beckon("hello world");
			List<String> capturedValues = new ArrayList<>();

			String result =
					unfolding.metamorphose(String::toUpperCase).unlace(capturedValues::add).discern(s -> s.length() > 5)
							 .develop(s -> s.contains("WORLD"), s -> s + "!").coronate(s -> s.substring(0, 5));

			assertThat(result).as("Chained operations should produce expected result").isEqualTo("HELLO");
			assertThat(capturedValues).as("unlace() should have been applied during chain")
									  .containsExactly("HELLO WORLD");
		}

		@DisplayName("should short-circuit on empty unfolding")
		@Test
		void shouldShortCircuitOnEmptyUnfolding()
		{
			Unfolding<String> unfolding = Unfolding.chaos();
			List<String> capturedValues = new ArrayList<>();

			Unfolding<String> result =
					unfolding.metamorphose(String::toUpperCase).unlace(capturedValues::add).discern(s -> s.length() > 5)
							 .develop(s -> s.contains("WORLD"), s -> s + "!");

			assertThat(result.sterile()).as("Result of chained operations on empty unfolding should be empty").isTrue();
			assertThat(capturedValues).as("unlace() should not have been applied to empty unfolding").isEmpty();
		}

		@DisplayName("should handle complex transformations in chain")
		@Test
		void shouldHandleComplexTransformationsInChain()
		{
			Unfolding<Integer> unfolding = Unfolding.beckon(42);

			String result = unfolding.metamorphose(n -> n * 2).discern(n -> n > 50).metamorphose(Object::toString)
									 .develop(s -> s.length() == 2, s -> "0" + s).rescue("Not found");

			assertThat(result).as("Complex chain should produce expected result").isEqualTo("084");
		}

		@DisplayName("should handle filter making unfolding empty in middle of chain")
		@Test
		void shouldHandleFilterMakingUnfoldingEmptyInMiddleOfChain()
		{
			Unfolding<Integer> unfolding = Unfolding.beckon(42);
			List<String> capturedValues = new ArrayList<>();

			String result = unfolding.metamorphose(n -> n * 2).discern(n -> n < 50).metamorphose(Object::toString)
									 .unlace(capturedValues::add).rescue("Not found");

			assertThat(result).as("Chain with filter making unfolding empty should use fallback")
							  .isEqualTo("Not found");
			assertThat(capturedValues).as("unlace() should not be called after filter made unfolding empty").isEmpty();
		}
	}

	@Nested
	@DisplayName("Tests for stream() method")
	class StreamTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentValueTestCases")
		@DisplayName("should convert present value to non-empty stream")
		<T> void shouldConvertPresentValueToNonEmptyStream(String description, Unfolding<T> unfolding, T expectedValue)
		{
			Stream<T> result = unfolding.stream();

			assertThat(result).isNotNull();
			assertThat(result).containsExactly(expectedValue);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyUnfoldingTestCases")
		@DisplayName("should convert empty unfolding to empty stream")
		<T> void shouldConvertEmptyUnfoldingToEmptyStream(String description, Unfolding<T> unfolding)
		{
			Stream<T> result = unfolding.stream();

			assertThat(result).isNotNull();
			assertThat(result).isEmpty();
		}

		private static Stream<Arguments> presentValueTestCases()
		{
			return Stream.of(new PresentTestCase<>("String value should be converted to stream with that value",
										 Unfolding.beckon("test"), "test"),
								 new PresentTestCase<>("Integer value should be converted to stream with that value",
										 Unfolding.beckon(42), 42),
								 new PresentTestCase<>("Boolean value should be converted to stream with that value",
										 Unfolding.beckon(true), true))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedValue));
		}

		private static Stream<Arguments> emptyUnfoldingTestCases()
		{
			return Stream.of(new EmptyTestCase<>("Empty String unfolding should convert to empty stream",
							Unfolding.<String>chaos()),
					new EmptyTestCase<>("Empty Integer unfolding should convert to empty stream",
							Unfolding.<Integer>chaos()),
					new EmptyTestCase<>("Empty Boolean unfolding should convert to empty stream",
							Unfolding.<Boolean>chaos())).map(tc -> Arguments.of(tc.description, tc.unfolding));
		}

		private record PresentTestCase<T>(String description, Unfolding<T> unfolding, T expectedValue)
		{
		}

		private record EmptyTestCase<T>(String description, Unfolding<T> unfolding)
		{
		}
	}

	@Nested
	@DisplayName("Tests for toString() method")
	class ToStringTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentValueTestCases")
		@DisplayName("should return formatted string for present values")
		<T> void shouldReturnFormattedStringForPresentValues(String description, Unfolding<T> unfolding,
															 String expectedResult)
		{
			String result = unfolding.toString();

			assertThat(result).as("toString() for %s should return expected formatted string", unfolding)
							  .isEqualTo(expectedResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyUnfoldingTestCases")
		@DisplayName("should return formatted string for empty unfolding")
		<T> void shouldReturnFormattedStringForEmptyUnfolding(String description, Unfolding<T> unfolding)
		{
			String result = unfolding.toString();

			assertThat(result).as("toString() for empty unfolding should return expected formatted string")
							  .isEqualTo(Shell.instance().toString());
		}

		private static Stream<Arguments> presentValueTestCases()
		{
			return Stream.of(
								 new PresentTestCase<>("String value should be formatted correctly", Unfolding.beckon("test"),
										 "Unfolding[test]"),
								 new PresentTestCase<>("Integer value should be formatted correctly", Unfolding.beckon(42),
										 "Unfolding[42]"),
								 new PresentTestCase<>("Boolean value should be formatted correctly", Unfolding.beckon(true),
										 "Unfolding[true]"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedResult));
		}

		private static Stream<Arguments> emptyUnfoldingTestCases()
		{
			return Stream.of(new EmptyTestCase<>("Empty String unfolding should be formatted correctly",
							Unfolding.<String>chaos()),
					new EmptyTestCase<>("Empty Integer unfolding should be formatted correctly",
							Unfolding.<Integer>chaos()),
					new EmptyTestCase<>("Empty Boolean unfolding should be formatted correctly",
							Unfolding.<Boolean>chaos())).map(tc -> Arguments.of(tc.description, tc.unfolding));
		}

		private record PresentTestCase<T>(String description, Unfolding<T> unfolding, String expectedResult)
		{
		}

		private record EmptyTestCase<T>(String description, Unfolding<T> unfolding)
		{
		}
	}

	@Nested
	@DisplayName("Tests for equals() and hashCode() methods")
	class EqualsAndHashCodeTests
	{
		@DisplayName("should satisfy reflexivity (x.equals(x) == true)")
		@Test
		void shouldSatisfyReflexivity()
		{
			Unfolding<String> stringUnfolding = Unfolding.beckon("test");
			assertThat(stringUnfolding.equals(stringUnfolding)).as("An unfolding should equal itself (reflexivity)")
															   .isTrue();

			Unfolding<Integer> emptyUnfolding = Unfolding.chaos();
			assertThat(emptyUnfolding.equals(emptyUnfolding)).as("An empty unfolding should equal itself (reflexivity)")
															 .isTrue();
		}

		@DisplayName("should satisfy symmetry (x.equals(y) == y.equals(x))")
		@Test
		void shouldSatisfySymmetry()
		{
			Unfolding<String> unfolding1 = Unfolding.beckon("test");
			Unfolding<String> unfolding2 = Unfolding.beckon("test");

			assertThat(unfolding1.equals(unfolding2)).as(
															 "First unfolding should equal second unfolding with same value")
													 .isEqualTo(unfolding2.equals(unfolding1));

			Unfolding<String> presentUnfolding = Unfolding.beckon("test");
			Unfolding<String> emptyUnfolding = Unfolding.chaos();

			assertThat(presentUnfolding.equals(emptyUnfolding)).as(
																	   "Present unfolding equality with empty unfolding should be symmetric")
															   .isEqualTo(emptyUnfolding.equals(presentUnfolding));

			Unfolding<String> emptyUnfolding1 = Unfolding.chaos();
			Unfolding<Integer> emptyUnfolding2 = Unfolding.chaos();

			assertThat(emptyUnfolding1.equals(emptyUnfolding2)).as("Empty unfolding equality should be symmetric")
															   .isEqualTo(emptyUnfolding2.equals(emptyUnfolding1));
		}

		@DisplayName("should satisfy transitivity (if x.equals(y) and y.equals(z), then x.equals(z))")
		@Test
		void shouldSatisfyTransitivity()
		{
			Unfolding<String> unfolding1 = Unfolding.beckon("test");
			Unfolding<String> unfolding2 = Unfolding.beckon("test");
			Unfolding<String> unfolding3 = Unfolding.beckon("test");

			boolean firstEqualsSecond = unfolding1.equals(unfolding2);
			boolean secondEqualsThird = unfolding2.equals(unfolding3);

			assertThat(firstEqualsSecond && secondEqualsThird).as(
					"Precondition: first equals second and second equals third").isTrue();

			assertThat(unfolding1.equals(unfolding3)).as(
					"Transitivity: if first equals second and second equals third, then first equals third").isTrue();

			Unfolding<String> emptyUnfolding1 = Unfolding.chaos();
			Unfolding<Integer> emptyUnfolding2 = Unfolding.chaos();
			Unfolding<List<String>> emptyUnfolding3 = Unfolding.chaos();

			boolean firstEmptyEqualsSecond = emptyUnfolding1.equals(emptyUnfolding2);
			boolean secondEmptyEqualsThird = emptyUnfolding2.equals(emptyUnfolding3);

			assertThat(firstEmptyEqualsSecond && secondEmptyEqualsThird).as(
					"Precondition: first empty equals second empty and second empty equals third empty").isTrue();

			assertThat(emptyUnfolding1.equals(emptyUnfolding3)).as(
																	   "Transitivity for empty: if first equals second and second equals third, then first equals third")
															   .isTrue();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("hashCodeConsistencyTestCases")
		@DisplayName("should have consistent hashCode with equals (if x.equals(y), then x.hashCode() == y.hashCode())")
		<T, U> void shouldHaveConsistentHashCodeWithEquals(String description, Unfolding<T> first, Unfolding<U> second,
														   boolean shouldBeEqual)
		{
			assertThat(first.equals(second)).as(description).isEqualTo(shouldBeEqual);

			// If they should be equal, their hashCodes must be equal too
			if (shouldBeEqual)
			{
				assertThat(first.hashCode()).as("Equal unfoldings should have the same hashCode")
											.isEqualTo(second.hashCode());
			}

			// Note: Different unfoldings may have the same hashCode by coincidence,
			// so we don't assert that they must have different hashCodes when not equal
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("equalityWithDifferentValueTypesTestCases")
		@DisplayName("should handle equality with different value types correctly")
		<T, U> void shouldHandleEqualityWithDifferentValueTypesCorrectly(String description, Unfolding<T> first,
																		 Unfolding<U> second, boolean shouldBeEqual)
		{
			assertThat(first.equals(second)).as(description).isEqualTo(shouldBeEqual);
		}

		@DisplayName("should handle equality with null values and empty unfoldings correctly")
		@Test
		void shouldHandleEqualityWithNullValuesAndEmptyUnfoldingsCorrectly()
		{
			Unfolding<String> emptyStringUnfolding = Unfolding.chaos();
			Unfolding<Integer> emptyIntegerUnfolding = Unfolding.chaos();
			Unfolding<List<String>> emptyListUnfolding = Unfolding.chaos();

			assertThat(emptyStringUnfolding.equals(emptyIntegerUnfolding)).as(
					"Empty unfoldings of different types should be equal").isTrue();

			assertThat(emptyIntegerUnfolding.equals(emptyListUnfolding)).as(
					"Empty unfoldings of different types should be equal").isTrue();

			Unfolding<String> nullStringUnfolding = Unfolding.beckon(null);
			Unfolding<Integer> nullIntegerUnfolding = Unfolding.beckon(null);

			assertThat(nullStringUnfolding.equals(emptyStringUnfolding)).as(
					"Unfolding.beckon(null) should equal Unfolding.chaos()").isTrue();

			assertThat(nullIntegerUnfolding.equals(emptyIntegerUnfolding)).as(
					"Unfolding.beckon(null) should equal Unfolding.chaos()").isTrue();

			assertThat(nullStringUnfolding.equals(nullIntegerUnfolding)).as(
					"Unfolding.beckon(null) instances should be equal regardless of type").isTrue();

			Unfolding<String> presentUnfolding = Unfolding.beckon("test");

			assertThat(emptyStringUnfolding.equals(presentUnfolding)).as(
					"Empty unfolding should not equal present unfolding").isFalse();

			assertThat(nullStringUnfolding.equals(presentUnfolding)).as(
					"Unfolding.beckon(null) should not equal present unfolding").isFalse();

			Unfolding<String> emptyStringValueUnfolding = Unfolding.beckon("");

			assertThat(emptyStringValueUnfolding.equals(emptyStringUnfolding)).as(
					"Unfolding with empty string should not equal empty unfolding").isFalse();

			assertThat(emptyStringValueUnfolding.equals(nullStringUnfolding)).as(
					"Unfolding with empty string should not equal Unfolding.beckon(null)").isFalse();

			assertThat(presentUnfolding == null).as("Unfolding should not equal null").isFalse();

			assertThat(emptyStringUnfolding == null).as("Empty unfolding should not equal null").isFalse();

			String stringObject = "test";

			assertThat(presentUnfolding.equals(stringObject)).as("Unfolding should not equal non-Unfolding object")
															 .isFalse();

			assertThat(emptyStringUnfolding.equals(stringObject)).as(
					"Empty unfolding should not equal non-Unfolding object").isFalse();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptinessComparisonTestCases")
		@DisplayName("should handle emptiness comparison correctly")
		<T, U> void shouldHandleEmptinessComparisonCorrectly(String description, Unfolding<T> first,
															 Unfolding<U> second, boolean shouldBeEqual)
		{
			assertThat(first.equals(second)).as(description).isEqualTo(shouldBeEqual);

			assertThat(second.equals(first)).as(description + " (symmetric check)").isEqualTo(shouldBeEqual);
		}

		private static Stream<Arguments> hashCodeConsistencyTestCases()
		{
			Unfolding<String> unfolding1 = Unfolding.beckon("test");
			Unfolding<String> unfolding2 = Unfolding.beckon("test");

			Unfolding<String> unfolding3 = Unfolding.beckon("different");

			Unfolding<String> emptyUnfolding1 = Unfolding.chaos();
			Unfolding<Integer> emptyUnfolding2 = Unfolding.chaos();

			return Stream.of(new HashCodeTestCase<>("Unfoldings with same value should be equal and have same hashCode",
										 unfolding1, unfolding2, true),
								 new HashCodeTestCase<>("Unfoldings with different values should not be equal", unfolding1,
										 unfolding3, false),
								 new HashCodeTestCase<>("Empty unfoldings should be equal and have same hashCode", emptyUnfolding1,
										 emptyUnfolding2, true))
						 .map(tc -> Arguments.of(tc.description, tc.first, tc.second, tc.shouldBeEqual));
		}

		private static Stream<Arguments> equalityWithDifferentValueTypesTestCases()
		{
			Unfolding<String> stringUnfolding1 = Unfolding.beckon("test");
			Unfolding<String> stringUnfolding2 = Unfolding.beckon("test");
			Unfolding<String> differentStringUnfolding = Unfolding.beckon("different");

			Unfolding<Integer> intUnfolding1 = Unfolding.beckon(42);
			Unfolding<Integer> intUnfolding2 = Unfolding.beckon(42);
			Unfolding<Integer> differentIntUnfolding = Unfolding.beckon(100);

			List<String> list1 = new ArrayList<>();
			list1.add("item");
			List<String> list2 = new ArrayList<>();
			list2.add("item");
			List<String> differentList = new ArrayList<>();
			differentList.add("different");

			Unfolding<List<String>> listUnfolding1 = Unfolding.beckon(list1);
			Unfolding<List<String>> listUnfolding2 = Unfolding.beckon(list2);
			Unfolding<List<String>> differentListUnfolding = Unfolding.beckon(differentList);

			Unfolding<Integer> integerUnfolding = Unfolding.beckon(123);
			Unfolding<String> stringNumberUnfolding = Unfolding.beckon("123");

			return Stream.of(
								 new EqualityTestCase<>("Unfoldings with same string value should be equal", stringUnfolding1,
										 stringUnfolding2, true),
								 new EqualityTestCase<>("Unfoldings with different string values should not be equal",
										 stringUnfolding1, differentStringUnfolding, false),
								 new EqualityTestCase<>("Unfoldings with same integer value should be equal", intUnfolding1,
										 intUnfolding2, true),
								 new EqualityTestCase<>("Unfoldings with different integer values should not be equal",
										 intUnfolding1, differentIntUnfolding, false),
								 new EqualityTestCase<>("Unfoldings with equal list values should be equal", listUnfolding1,
										 listUnfolding2, true),
								 new EqualityTestCase<>("Unfoldings with different list values should not be equal", listUnfolding1,
										 differentListUnfolding, false),
								 new EqualityTestCase<>("Unfoldings with different types but similar values should not be equal",
										 integerUnfolding, stringNumberUnfolding, false))
						 .map(tc -> Arguments.of(tc.description, tc.first, tc.second, tc.shouldBeEqual));
		}

		private static Stream<Arguments> emptinessComparisonTestCases()
		{
			Unfolding<String> emptyStringUnfolding = Unfolding.chaos();
			Unfolding<Integer> emptyIntegerUnfolding = Unfolding.chaos();
			Unfolding<String> presentStringUnfolding = Unfolding.beckon("test");
			Unfolding<Integer> presentIntegerUnfolding = Unfolding.beckon(42);
			Unfolding<String> emptyStringValueUnfolding = Unfolding.beckon("");

			return Stream.of(
								 new EqualityTestCase<>("Empty unfoldings of same type should be equal", emptyStringUnfolding,
										 emptyStringUnfolding, true),
								 new EqualityTestCase<>("Empty unfoldings of different types should be equal", emptyStringUnfolding,
										 emptyIntegerUnfolding, true),
								 new EqualityTestCase<>("Present unfolding should not equal empty unfolding (string)",
										 presentStringUnfolding, emptyStringUnfolding, false),
								 new EqualityTestCase<>("Empty unfolding should not equal present unfolding (string)",
										 emptyStringUnfolding, presentStringUnfolding, false),
								 new EqualityTestCase<>("Present unfolding should not equal empty unfolding (integer)",
										 presentIntegerUnfolding, emptyIntegerUnfolding, false),
								 new EqualityTestCase<>("Empty unfolding should not equal present unfolding (string)",
										 emptyStringUnfolding, presentStringUnfolding, false),
								 new EqualityTestCase<>("Empty unfolding should not equal present unfolding (integer)",
										 emptyIntegerUnfolding, presentIntegerUnfolding, false),
								 new EqualityTestCase<>("Unfolding with empty string value should not equal empty unfolding",
										 emptyStringValueUnfolding, emptyStringUnfolding, false),
								 new EqualityTestCase<>("Empty unfolding should not equal unfolding with empty string value",
										 emptyStringUnfolding, emptyStringValueUnfolding, false))
						 .map(tc -> Arguments.of(tc.description, tc.first, tc.second, tc.shouldBeEqual));
		}

		private record HashCodeTestCase<T, U>(String description, Unfolding<T> first, Unfolding<U> second,
											  boolean shouldBeEqual)
		{
		}

		private record EqualityTestCase<T, U>(String description, Unfolding<T> first, Unfolding<U> second,
											  boolean shouldBeEqual)
		{
		}
	}

	@Nested
	@DisplayName("Narrative test cases — full myths")
	class MythosNarratives
	{
		@Test
		@DisplayName("Prometheus: The flame that became light")
		void prometheus()
		{
			String result = Unfolding.beckon("flame").develop(f -> f.equals("flame"), _ -> "blaze")
									 .cleave(b -> b.startsWith("b"), _ -> "light", _ -> "smoke").rescue("ash");

			assertThat(result).isEqualTo("light");
		}

		@Test
		@DisplayName("Orpheus: The song that lost its echo")
		void orpheus()
		{
			String result = Unfolding.<String>chaos().develop(s -> s.length() > 5, String::toUpperCase)
									 .evolve(s -> s.startsWith("A"), _ -> "HARMONY").rescue("silence");

			assertThat(result).isEqualTo("silence");
		}

		@Test
		@DisplayName("Eurydice: The name that reappeared in reflection")
		void eurydice()
		{
			String result = Unfolding.beckon("Eurydice").interlace(String::length)
									 .metamorphose(pair -> pair.first() + " (" + pair.second() + ")")
									 .coronate(Function.identity());

			assertThat(result).isEqualTo("Eurydice (8)");
		}

		@Test
		@DisplayName("Icarus: The fall despite bright beginnings")
		void icarus()
		{
			String result = Unfolding.beckon("wings").develop(s -> s.equals("wings"), _ -> "sky")
									 .cleave(s -> s.equals("sun"), _ -> "ascend", _ -> "fall")
									 .coronate(Function.identity());

			assertThat(result).isEqualTo("fall");
		}

		@Test
		@DisplayName("Theseus: From thread to revelation")
		void theseus()
		{
			String result = Unfolding.beckon("thread").develop(s -> s.equals("thread"), _ -> "path")
									 .evolve(s -> s.equals("path"), _ -> "exit").coronate(s -> s);

			assertThat(result).isEqualTo("exit");
		}
	}

	@Nested
	@DisplayName("Invariant tests — MythosLaws")
	class MythosLaws
	{
		@Test
		@DisplayName("Sterile and supple are logical complements")
		void sterileAndSuppleAreComplements()
		{
			Unfolding<String> myth = Unfolding.beckon("echo");
			Unfolding<String> shell = Unfolding.chaos();

			assertThat(myth.supple()).isEqualTo(!myth.sterile());
			assertThat(shell.supple()).isEqualTo(!shell.sterile());
		}

		@Test
		@DisplayName("Refolding identity should yield the same value")
		void metamorphoseWithIdentityPreservesValue()
		{
			Unfolding<String> myth = Unfolding.beckon("echo");
			Unfolding<String> metamorphosed = myth.metamorphose(Function.identity());

			assertThat(metamorphosed.optional()).isEqualTo(myth.optional());
		}

		@Test
		@DisplayName("Empty unfolding remains empty through transformation")
		void emptyUnfoldingIsIdempotent()
		{
			Unfolding<String> shell = Unfolding.chaos();

			assertThat(shell.metamorphose(_ -> "new")).isSameAs(shell);
			assertThat(shell.evolve(_ -> true, _ -> "anything")).isEqualTo(Unfolding.chaos());
			assertThat(shell.discern(_ -> false)).isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("Summoning from supple should not throw")
		void suppleUnfoldingCanBeSummoned()
		{
			Unfolding<String> unfolding = Unfolding.beckon("vital");

			assertThatCode(unfolding::summon).doesNotThrowAnyException();
			assertThat(unfolding.summon()).isEqualTo("vital");
		}

		@Test
		@DisplayName("Summoning from sterile should throw")
		void sterileUnfoldingCannotBeSummoned()
		{
			assertThatThrownBy(() -> Unfolding.chaos().summon()).isInstanceOf(EmptyUnfoldingException.class)
																.hasMessageContaining("empty");
		}
	}

	@Nested
	class StoryOfJesus
	{
		@Test
		@DisplayName("Chronicle of Christ: An Unfolding Gospel")
		void chronicleOfChrist()
		{
			LocalDate christmas = LocalDate.of(0, Month.DECEMBER, 25);
			Unfolding<LocalDate> gospel = Unfolding.beckon(christmas);

			String poeticSummary = gospel
					// Develop into Epiphany (Baptism) // Jan 6
					.develop(_ -> true, d -> d.plusYears(30).withMonth(1).withDayOfMonth(6))

					// Cleave into Crucifixion (if Epiphany accepted)
					.cleave(_ -> true, _ -> LocalDate.of(33, 4, 3), d -> d)

					// Evolve into Resurrection
					.evolve(d -> d.equals(LocalDate.of(33, 4, 3)), crucifixion -> crucifixion.plusDays(3))

					// Develop into Ascension
					.develop(d -> d.equals(LocalDate.of(33, 4, 6)), resurrection -> resurrection.plusDays(40))

					// Interlace with corresponding Feast
					.interlace(this::mapToFeast)

					// Refold into SacredEvent
					.metamorphose(pair -> SacredEvent.of(pair.second(), christmas, pair.first()))

					// Interlace again with duration from Christmas
					.interlace(SacredEvent::daysSinceChristmas)

					// Refold into poetic summary
					.metamorphose(pair -> formatSummary(pair.first(), pair.second()))

					// Conclude
					.coronate(Function.identity());

			assertThat(poeticSummary).contains("Ascension").contains("11830 days").contains("He ascended to Heaven");
		}

		private FeastDay mapToFeast(LocalDate date)
		{
			if (date.equals(LocalDate.of(0, 12, 25))) return FeastDay.CHRISTMAS;
			if (date.equals(LocalDate.of(30, 1, 6))) return FeastDay.EPIPHANY;
			if (date.equals(LocalDate.of(33, 4, 3))) return FeastDay.GOOD_FRIDAY;
			if (date.equals(LocalDate.of(33, 4, 6))) return FeastDay.EASTER;
			if (date.equals(LocalDate.of(33, 5, 16))) return FeastDay.ASCENSION;
			throw new IllegalArgumentException("Unknown date: " + date);
		}

		private String formatSummary(SacredEvent event, long daysSinceBirth)
		{
			return switch (event.feast())
			{
				case CHRISTMAS -> "The Light entered the world";
				case EPIPHANY -> "He was revealed to the nations after " + daysSinceBirth + " days";
				case GOOD_FRIDAY -> "He suffered and died after " + daysSinceBirth + " days";
				case EASTER -> "He rose again in glory after " + daysSinceBirth + " days";
				case ASCENSION ->
						"Ascension: He ascended to Heaven after " + daysSinceBirth + " days — and the myth " + "continues.";
			};
		}

		private enum FeastDay
		{
			CHRISTMAS, EPIPHANY, GOOD_FRIDAY, EASTER, ASCENSION
		}

		private record SacredEvent(FeastDay feast, LocalDate date, long daysSinceChristmas)
		{
			private static SacredEvent of(FeastDay feast, LocalDate base, LocalDate eventDate)
			{
				return new SacredEvent(feast, eventDate,
						Duration.between(base.atStartOfDay(), eventDate.atStartOfDay()).toDays());
			}
		}
	}

	@Nested
	@DisplayName("Tests for the decree")
	class DecreeTests
	{
		@Test
		@DisplayName("Decree: The decree that is true")
		void decree()
		{
			Unfolding<String> unfolding = Unfolding.beckon("test");

			assertThat(unfolding.decree(RuntimeException::new)).as(
					"A supple unfolding should return the hero when decreed").isEqualTo("test");

			assertThatCode(() -> unfolding.decree(RuntimeException::new)).as(
					"A supple unfolding should not throw when decreed").doesNotThrowAnyException();
		}

		@Test
		@DisplayName("Decree: The decree that is false")
		void decreeFalse()
		{
			Unfolding<String> unfolding = Unfolding.chaos();

			Supplier<RuntimeException> exceptionSupplier = () -> new IllegalStateException("test");

			assertThatThrownBy(() -> unfolding.decree(exceptionSupplier)).as(
																				 "An empty unfolding should throw when decreed").isInstanceOf(exceptionSupplier.get().getClass())
																		 .hasMessageContaining("test");
		}
	}
}