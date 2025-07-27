package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnfoldingTest
{
	@Nested
	@DisplayName("Tests for concludeWith() method")
	class ConcludeWithTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("successTestCases")
		@DisplayName("should transform present values")
		<T, R> void shouldTransformValue(String description, IUnfolding<T> unfolding, Function<T, R> extractor,
										 R expectedResult)
		{
			R result = unfolding.concludeWith(extractor);
			assertThat(result).as("concludeWith() for %s should give %s", unfolding, expectedResult)
							  .isEqualTo(expectedResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("exceptionTestCases")
		@DisplayName("should throw exception for empty unfolding")
		<T, R> void shouldThrowExceptionForEmptyUnfolding(String description, IUnfolding<T> unfolding,
														  Function<T, R> extractor)
		{
			assertThatThrownBy(() -> unfolding.concludeWith(extractor)).as(
					"concludeWith() for %s should throw EmptyUnfoldingException",
					unfolding).isInstanceOf(EmptyUnfoldingException.class).hasMessageContaining("empty");
		}

		@DisplayName("should throw exception for null conclusion")
		@Test
		void shouldThrowExceptionForNullExtractor()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Function<String, Integer> nullConclusion = null;

			assertThatThrownBy(() -> unfolding.concludeWith(nullConclusion))
					.as("concludeWith() with null conclusion should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("conclusion may not be null");
		}

		private static Stream<Arguments> successTestCases()
		{
			return Stream.of(
								 new SuccessTestCase<>("Present value should be transformed by extractor", IUnfolding.of("hello"),
										 String::length, 5),
								 new SuccessTestCase<>("Complex transformation should work on present value", IUnfolding.of(42),
										 num -> "Number: " + num, "Number: 42"),
								 new SuccessTestCase<>("Identity function should return the same value", IUnfolding.of("test"),
										 Function.identity(), "test"),
								 new SuccessTestCase<>("Extractor returning null should work", IUnfolding.of("hello"), _ -> null,
										 null))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.extractor, tc.expectedResult));
		}

		private static Stream<Arguments> exceptionTestCases()
		{
			return Stream.of(new ExceptionTestCase<>("Empty unfolding should throw exception", IUnfolding.empty(),
							_ -> "unused"),
					new ExceptionTestCase<>("IUnfolding created with null should throw exception", IUnfolding.of(null),
							_ -> "unused")).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.extractor));
		}

		private record SuccessTestCase<T, R>(String description, IUnfolding<T> unfolding, Function<T, R> extractor,
											 R expectedResult)
		{
		}

		private record ExceptionTestCase<T, R>(String description, IUnfolding<T> unfolding, Function<T, R> extractor)
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
		<T> void shouldReturnValueOrFallback(String description, IUnfolding<T> unfolding, T fallbackValue,
											 Supplier<T> fallbackSupplier, T expectedResult, boolean useSupplier)
		{
			T result = useSupplier ? unfolding.alternatively(fallbackSupplier) : unfolding.alternatively(fallbackValue);

			String fallbackDescription =
					useSupplier ? "supplier returning " + expectedResult : String.valueOf(fallbackValue);

			assertThat(result).as("alternatively() for %s with fallback %s should give %s", unfolding,
					fallbackDescription, expectedResult).isEqualTo(expectedResult);
		}

		@DisplayName("should throw exception for null revelation")
		@Test
		void shouldThrowExceptionForNullSupplier()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Supplier<String> nullSupplier = null;

			assertThatThrownBy(() -> unfolding.alternatively(nullSupplier))
					.as("alternatively() with null revelation should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("revelation may not be null");
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
								 new TestCase<>("Present value should be returned ignoring fallback value", IUnfolding.of("primary"),
										 "fallback", null, "primary", false),
								 new TestCase<>("Empty unfolding should return fallback value", IUnfolding.empty(), "fallback", null,
										 "fallback", false),
								 new TestCase<>("Present value should be returned ignoring fallback supplier", IUnfolding.of(100),
										 null, () -> 200, 100, true),
								 new TestCase<>("Present value should be returned ignoring fallback and fallback supplier",
										 IUnfolding.of(100), 300, () -> 200, 100, true),
								 new TestCase<>("Empty unfolding should use fallback supplier", IUnfolding.empty(), null, () -> 200,
										 200, true),
								 new TestCase<>("Null fallback value should be returned for empty unfolding", IUnfolding.empty(),
										 null, null, null, false),
								 new TestCase<>("Null value from fallback supplier should be returned for empty unfolding",
										 IUnfolding.empty(), null, () -> null, null, true),
								 new TestCase<>("Unfolding created with null should return fallback value", IUnfolding.of(null),
										 "fallback", null, "fallback", false),
								 new TestCase<>("Unfolding created with null should use fallback supplier", IUnfolding.of(null),
										 null,
										 () -> "fallback", "fallback", true))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.fallbackValue, tc.fallbackSupplier,
								 tc.expectedResult, tc.useSupplier));
		}

		record TestCase<T>(String description, IUnfolding<T> unfolding, T fallbackValue, Supplier<T> fallbackSupplier,
						   T expectedResult, boolean useSupplier)
		{
		}
	}

	@Nested
	@DisplayName("Tests for isPresent() method")
	class IsPresentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should correctly identify if value exists")
		<T> void shouldIdentifyValueExistence(String description, IUnfolding<T> unfolding, boolean expectedResult)
		{
			boolean result = unfolding.isPresent();

			assertThat(result).as("isPresent() for %s should give %s", unfolding, expectedResult)
							  .isEqualTo(expectedResult);
		}

		@DisplayName("should be consistent with isEmpty()")
		@Test
		void shouldBeConsistentWithIsEmpty()
		{
			IUnfolding<String> presentUnfolding = Unfolding.of("test");
			IUnfolding<Integer> emptyUnfolding = Unfolding.empty();
			IUnfolding<String> nullUnfolding = Unfolding.of(null);

			assertThat(presentUnfolding.isPresent()).as("isPresent() should be opposite of isEmpty()")
													.isEqualTo(!presentUnfolding.isEmpty());
			assertThat(emptyUnfolding.isPresent()).as("isPresent() should be opposite of isEmpty()")
												  .isEqualTo(!emptyUnfolding.isEmpty());
			assertThat(nullUnfolding.isPresent()).as("isPresent() should be opposite of isEmpty()")
												 .isEqualTo(!nullUnfolding.isEmpty());
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("Unfolding with non-null value should be present", IUnfolding.of("value"), true),
					new TestCase<>("Empty unfolding should not be present", IUnfolding.empty(), false),
					new TestCase<>("Unfolding created with null should be empty and not present", IUnfolding.of(null),
							false),
					new TestCase<>("Unfolding with empty string should be present", IUnfolding.of(""), true),
					new TestCase<>("Unfolding with zero should be present", IUnfolding.of(0), true),
					new TestCase<>("Unfolding with false should be present", IUnfolding.of(false), true),
					new TestCase<>("Unfolding with empty list should be present", IUnfolding.of(new ArrayList<>()),
							true)).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedResult));
		}

		private record TestCase<T>(String description, IUnfolding<T> unfolding, boolean expectedResult)
		{
		}
	}

	@Nested
	@DisplayName("Tests for refold() method")
	class RefoldTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should transform present value to new present value")
		<T, R> void shouldTransformPresentValue(String description, IUnfolding<T> unfolding, Function<T, R> mapper,
												IUnfolding<R> expectedResult)
		{
			IUnfolding<R> result = unfolding.refold(mapper);

			assertThat(result.isPresent()).as("refold() for %s should result in present unfolding", unfolding).isTrue();
			assertThat(result.summon()).as("refold() result value should match expected")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T, R> void shouldTransformToEmptyUnfolding(String description, IUnfolding<T> unfolding, Function<T, R> mapper)
		{
			IUnfolding<R> result = unfolding.refold(mapper);

			assertThat(result.isEmpty()).as("refold() for %s should result in empty unfolding", unfolding).isTrue();
		}

		@DisplayName("should throw exception for null mapper")
		@Test
		void shouldThrowExceptionForNullMapper()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Function<String, Integer> nullMapper = null;

			assertThatThrownBy(() -> unfolding.refold(nullMapper))
					.as("refold() with null mapper should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("folding may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(
								 new PresentResultTestCase<>("Present value should be transformed by mapper", IUnfolding.of("hello"),
										 String::length, IUnfolding.of(5)),
								 new PresentResultTestCase<>("Complex transformation should work on present value",
										 IUnfolding.of(42),
										 num -> "Number: " + num, IUnfolding.of("Number: 42")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.mapper, tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Empty unfolding should remain empty after mapping",
										 IUnfolding.empty(), String::length),
								 new EmptyResultTestCase<>("Mapper returning null should result in empty unfolding",
										 IUnfolding.of("test"), _ -> null))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.mapper));
		}

		private record PresentResultTestCase<T, R>(String description, IUnfolding<T> unfolding, Function<T, R> mapper,
												   IUnfolding<R> expectedResult)
		{
		}

		private record EmptyResultTestCase<T, R>(String description, IUnfolding<T> unfolding, Function<T, R> mapper)
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
		<T> void shouldTransformToPresentValue(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
											   Function<T, T> mapper, IUnfolding<T> expectedResult)
		{
			IUnfolding<T> result = unfolding.develop(predicate, mapper);

			assertThat(result.isPresent()).as("develop() for %s with predicate should result in present unfolding",
					unfolding).isTrue();
			assertThat(result.summon()).as("develop() result value should match expected")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T> void shouldTransformToEmptyUnfolding(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
												 Function<T, T> mapper)
		{
			IUnfolding<T> result = unfolding.develop(predicate, mapper);

			assertThat(result.isEmpty()).as("develop() for %s with predicate should result in empty unfolding",
												unfolding)
										.isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Predicate<String> nullPredicate = null;
			Function<String, String> mapper = String::toUpperCase;

			assertThatThrownBy(() -> unfolding.develop(nullPredicate, mapper))
					.as("develop() with null predicate should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");
		}

		@DisplayName("should throw exception for null mapper")
		@Test
		void shouldThrowExceptionForNullMapper()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> nullMapper = null;

			assertThatThrownBy(() -> unfolding.develop(predicate, nullMapper))
					.as("develop() with null mapper should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("development may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value matching predicate should be transformed",
										 IUnfolding.of("hello"), s -> s.length() > 3, String::toUpperCase, IUnfolding.of("HELLO")),
								 new PresentResultTestCase<>("Present value not matching predicate should remain unchanged",
										 IUnfolding.of("hi"), s -> s.length() > 3, String::toUpperCase,
										 IUnfolding.of("hi")),
								 new PresentResultTestCase<>("Mapper should not be applied when predicate doesn't match",
										 IUnfolding.of(5), n -> n > 10, _ -> null, IUnfolding.of(5)))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper,
								 tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(
								 new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of predicate and mapper",
										 IUnfolding.<String>empty(), _ -> true, String::toUpperCase),
								 new EmptyResultTestCase<>(
										 "Mapper returning null should result in empty unfolding when predicate matches",
										 IUnfolding.of(42), n -> n > 10, _ -> null))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper));
		}

		private record PresentResultTestCase<T>(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
												Function<T, T> mapper, IUnfolding<T> expectedResult)
		{
		}

		private record EmptyResultTestCase<T>(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
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
		<T, R> void shouldTransformToPresentValue(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
												  Function<T, R> mapper, IUnfolding<R> expectedResult)
		{
			IUnfolding<R> result = unfolding.evolve(predicate, mapper);

			assertThat(result.isPresent()).as("evolve() for %s with predicate should result in present unfolding",
					unfolding).isTrue();
			assertThat(result.summon()).as("evolve() result value should match expected")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T, R> void shouldTransformToEmptyUnfolding(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
													Function<T, R> mapper)
		{
			IUnfolding<R> result = unfolding.evolve(predicate, mapper);

			assertThat(result.isEmpty()).as("evolve() for %s with predicate should result in empty unfolding",
					unfolding).isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Predicate<String> nullPredicate = null;
			Function<String, String> mapper = String::toUpperCase;

			assertThatThrownBy(() -> unfolding.evolve(nullPredicate, mapper))
					.as("evolve() with null predicate should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");
		}

		@DisplayName("should throw exception for null mapper")
		@Test
		void shouldThrowExceptionForNullMapper()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> nullMapper = null;

			assertThatThrownBy(() -> unfolding.evolve(predicate, nullMapper))
					.as("evolve() with null mapper should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("evolution may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value matching predicate should be transformed",
										 IUnfolding.of("hello"), s -> s.length() > 3, String::toUpperCase, IUnfolding.of("HELLO")),
								 new PresentResultTestCase<>("Type transformation should work when predicate matches",
										 IUnfolding.of(42), n -> n > 10, n -> "Number: " + n,
										 IUnfolding.of("Number: 42")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper,
								 tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Present value not matching predicate should become empty",
										 IUnfolding.of("hi"), s -> s.length() > 3, String::toUpperCase),
								 new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of predicate and mapper",
										 IUnfolding.<String>empty(), _ -> true, String::toUpperCase),
								 new EmptyResultTestCase<>(
										 "Mapper returning null should result in empty unfolding when predicate matches",
										 IUnfolding.of(42), n -> n > 10, _ -> null),
								 new EmptyResultTestCase<>("Mapper should not be applied when predicate doesn't match",
										 IUnfolding.of(5), n -> n > 10, _ -> "transformed"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper));
		}

		private record PresentResultTestCase<T, R>(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
												   Function<T, R> mapper, IUnfolding<R> expectedResult)
		{
		}

		private record EmptyResultTestCase<T, R>(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
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
		<T, R> void shouldApplyTrueMapper(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
										  Function<T, R> trueMapper, Function<T, R> falseMapper,
										  IUnfolding<R> expectedResult)
		{
			IUnfolding<R> result = unfolding.cleave(predicate, trueMapper, falseMapper);

			assertThat(result.isPresent()).as(
					"cleave() for %s with matching predicate should result in present unfolding",
					unfolding).isTrue();
			assertThat(result.summon()).as("cleave() result value should match expected from trueMapper")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("falsePredicateTestCases")
		@DisplayName("should apply falseMapper when predicate doesn't match")
		<T, R> void shouldApplyFalseMapper(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
										   Function<T, R> trueMapper, Function<T, R> falseMapper,
										   IUnfolding<R> expectedResult)
		{
			IUnfolding<R> result = unfolding.cleave(predicate, trueMapper, falseMapper);

			assertThat(result.isPresent()).as(
					"cleave() for %s with non-matching predicate should result in present unfolding",
					unfolding).isTrue();
			assertThat(result.summon()).as("cleave() result value should match expected from falseMapper")
									   .isEqualTo(expectedResult.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyUnfoldingTestCases")
		@DisplayName("should return empty unfolding when input is empty")
		<T, R> void shouldReturnEmptyForEmptyInput(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
												   Function<T, R> trueMapper, Function<T, R> falseMapper)
		{
			IUnfolding<R> result = unfolding.cleave(predicate, trueMapper, falseMapper);

			assertThat(result.isEmpty()).as("cleave() for empty unfolding should result in empty unfolding").isTrue();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("nullResultTestCases")
		@DisplayName("should return empty unfolding when mapper returns null")
		<T, R> void shouldReturnEmptyForNullMapperResult(String description, IUnfolding<T> unfolding,
														 Predicate<T> predicate,
														 Function<T, R> trueMapper, Function<T, R> falseMapper)
		{
			IUnfolding<R> result = unfolding.cleave(predicate, trueMapper, falseMapper);

			assertThat(result.isEmpty()).as("cleave() with mapper returning null should result in empty unfolding")
										.isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Predicate<String> nullPredicate = null;
			Function<String, String> trueMapper = String::toUpperCase;
			Function<String, String> falseMapper = s -> s + "_suffix";

			assertThatThrownBy(() -> unfolding.cleave(nullPredicate, trueMapper, falseMapper))
					.as("cleave() with null predicate should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");
		}

		@DisplayName("should throw exception for null reward")
		@Test
		void shouldThrowExceptionForNullTrueMapper()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> nullMapper = null;
			Function<String, String> falseMapper = s -> s + "_suffix";

			assertThatThrownBy(() -> unfolding.cleave(predicate, nullMapper, falseMapper))
					.as("cleave() with null reward should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("reward may not be null");
		}

		@DisplayName("should throw exception for null punishment")
		@Test
		void shouldThrowExceptionForNullFalseMapper()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> trueMapper = String::toUpperCase;
			Function<String, String> nullMapper = null;

			assertThatThrownBy(() -> unfolding.cleave(predicate, trueMapper, nullMapper))
					.as("cleave() with null punishment should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("punishment may not be null");
		}

		private static Stream<Arguments> truePredicateTestCases()
		{
			return Stream.of(
					new TestCase<>("String length predicate matches, apply uppercase transformation",
							IUnfolding.of("hello"), s -> s.length() > 3, String::toUpperCase, s -> s + "_suffix",
							IUnfolding.of("HELLO")),
					new TestCase<>("Integer value predicate matches, apply string conversion",
							IUnfolding.of(42), n -> n > 10, n -> "Number: " + n, n -> "Small: " + n,
							IUnfolding.of("Number: 42")),
					new TestCase<>("Boolean value predicate matches, apply conditional text",
							IUnfolding.of(true), b -> b, b -> "It's true", b -> "It's false",
							IUnfolding.of("It's true"))
			).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.trueMapper, tc.falseMapper,
					tc.expectedResult));
		}

		private static Stream<Arguments> falsePredicateTestCases()
		{
			return Stream.of(
					new TestCase<>("String length predicate doesn't match, apply suffix transformation",
							IUnfolding.of("hi"), s -> s.length() > 3, String::toUpperCase, s -> s + "_suffix",
							IUnfolding.of("hi_suffix")),
					new TestCase<>("Integer value predicate doesn't match, apply small number conversion",
							IUnfolding.of(5), n -> n > 10, n -> "Number: " + n, n -> "Small: " + n,
							IUnfolding.of("Small: 5")),
					new TestCase<>("Boolean value predicate doesn't match, apply conditional text",
							IUnfolding.of(false), b -> b, b -> "It's true", b -> "It's false",
							IUnfolding.of("It's false"))
			).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.trueMapper, tc.falseMapper,
					tc.expectedResult));
		}

		private static Stream<Arguments> emptyUnfoldingTestCases()
		{
			return Stream.of(
					new EmptyTestCase<>("Empty unfolding with true predicate should remain empty",
							IUnfolding.<String>empty(), s -> true, String::toUpperCase, s -> s + "_suffix"),
					new EmptyTestCase<>("Empty unfolding with false predicate should remain empty",
							IUnfolding.<Integer>empty(), n -> n > 10, n -> n * 2, n -> n / 2)
			).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.trueMapper, tc.falseMapper));
		}

		private static Stream<Arguments> nullResultTestCases()
		{
			return Stream.of(
					new EmptyTestCase<>("TrueMapper returning null should result in empty unfolding",
							IUnfolding.of("test"), s -> s.length() > 3, s -> null, s -> s + "_suffix"),
					new EmptyTestCase<>("FalseMapper returning null should result in empty unfolding",
							IUnfolding.of("hi"), s -> s.length() > 3, String::toUpperCase, s -> null)
			).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.trueMapper, tc.falseMapper));
		}

		private record TestCase<T, R>(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
									  Function<T, R> trueMapper, Function<T, R> falseMapper,
									  IUnfolding<R> expectedResult)
		{
		}

		private record EmptyTestCase<T, R>(String description, IUnfolding<T> unfolding, Predicate<T> predicate,
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
		<T> void shouldKeepValueWhenPredicateMatches(String description, IUnfolding<T> unfolding,
													 Predicate<T> predicate)
		{
			IUnfolding<T> result = unfolding.discern(predicate);

			assertThat(result.isPresent()).as("discern() for %s with matching predicate should remain present",
					unfolding).isTrue();
			assertThat(result.summon()).as("discern() result value should match original")
									   .isEqualTo(unfolding.summon());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should discard value when predicate doesn't match")
		<T> void shouldDiscardValueWhenPredicateDoesntMatch(String description, IUnfolding<T> unfolding,
															Predicate<T> predicate)
		{
			IUnfolding<T> result = unfolding.discern(predicate);

			assertThat(result.isEmpty()).as("discern() for %s with non-matching predicate should become empty",
					unfolding).isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Predicate<String> nullPredicate = null;

			assertThatThrownBy(() -> unfolding.discern(nullPredicate))
					.as("discern() with null predicate should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgement may not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value matching predicate should remain unchanged",
										 IUnfolding.of("hello"), s -> s.length() > 3),
								 new PresentResultTestCase<>("Predicate with complex logic should work correctly (even numbers)",
										 IUnfolding.of(42), n -> n % 2 == 0))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Present value not matching predicate should become empty",
										 IUnfolding.of("hi"), s -> s.length() > 3),
								 new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of predicate",
										 IUnfolding.<String>empty(), _ -> true),
								 new EmptyResultTestCase<>("Predicate with complex logic should work correctly (odd numbers)",
										 IUnfolding.of(43), n -> n % 2 == 0))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate));
		}

		private record PresentResultTestCase<T>(String description, IUnfolding<T> unfolding, Predicate<T> predicate)
		{
		}

		private record EmptyResultTestCase<T>(String description, IUnfolding<T> unfolding, Predicate<T> predicate)
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
			IUnfolding<String> unfolding = IUnfolding.of("test");
			List<String> capturedValues = new ArrayList<>();
			Consumer<String> consumer = capturedValues::add;

			IUnfolding<String> result = unfolding.unlace(consumer);

			assertThat(result).as("unlace() should return the same unfolding").isSameAs(unfolding);
			assertThat(capturedValues).as("Consumer should be applied to the value").containsExactly("test");
		}

		@DisplayName("should not apply consumer to empty unfolding and return the same unfolding")
		@Test
		void shouldNotApplyConsumerToEmptyUnfolding()
		{
			IUnfolding<String> unfolding = IUnfolding.empty();
			List<String> capturedValues = new ArrayList<>();
			Consumer<String> consumer = capturedValues::add;

			IUnfolding<String> result = unfolding.unlace(consumer);

			assertThat(result).as("unlace() should return the same unfolding").isSameAs(unfolding);
			assertThat(capturedValues).as("Consumer should not be applied to empty unfolding").isEmpty();
		}

		@DisplayName("should throw exception for null consumer")
		@Test
		void shouldThrowExceptionForNullConsumer()
		{
			IUnfolding<String> unfolding = IUnfolding.of("test");
			Consumer<String> nullConsumer = null;

			assertThatThrownBy(() -> unfolding.unlace(nullConsumer))
					.as("unlace() with null consumer should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("impregnator may not be null");
		}
	}

	@Nested
	@DisplayName("Tests for summon() method")
	class SummonTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("successTestCases")
		@DisplayName("should return value for present unfolding")
		<T> void shouldReturnValue(String description, IUnfolding<T> unfolding, T expectedValue)
		{
			T result = unfolding.summon();
			assertThat(result).as("summon() for %s should return %s", unfolding, expectedValue)
							  .isEqualTo(expectedValue);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("exceptionTestCases")
		@DisplayName("should throw exception for empty unfolding")
		<T> void shouldThrowException(String description, IUnfolding<T> unfolding)
		{
			assertThatThrownBy(unfolding::summon).as("summon() for %s should throw EmptyIUnfoldingException", unfolding)
												 .isInstanceOf(EmptyUnfoldingException.class)
												 .hasMessageContaining("empty");
		}

		private static Stream<Arguments> successTestCases()
		{
			return Stream.of(new SuccessTestCase<>("Present value should be returned", IUnfolding.of("hello"), "hello"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedValue));
		}

		private static Stream<Arguments> exceptionTestCases()
		{
			return Stream.of(new ExceptionTestCase<>("Empty unfolding should throw exception", IUnfolding.empty()),
								 new ExceptionTestCase<>("Unfolding with null value should throw exception", IUnfolding.of(null)))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding));
		}

		private record SuccessTestCase<T>(String description, IUnfolding<T> unfolding, T expectedValue)
		{
		}

		private record ExceptionTestCase<T>(String description, IUnfolding<T> unfolding)
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
		<T> void shouldConvertPresentUnfoldingToPresentOptional(String description, IUnfolding<T> unfolding,
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
		<T> void shouldConvertEmptyUnfoldingToEmptyOptional(String description, IUnfolding<T> unfolding)
		{
			Optional<T> result = unfolding.optional();

			assertThat(result.isPresent()).as("optional() for %s should result in empty Optional", unfolding).isFalse();
		}

		private static Stream<Arguments> presentTestCases()
		{
			return Stream.of(new PresentTestCase<>("Present value should be converted to present Optional",
								 IUnfolding.of("hello"), "hello"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedValue));
		}

		private static Stream<Arguments> emptyTestCases()
		{
			return Stream.of(
					new EmptyTestCase<>("Empty unfolding should be converted to empty Optional", IUnfolding.empty()),
					new EmptyTestCase<>("Unfolding created with null should be converted to empty Optional",
							Unfolding.of(null))).map(tc -> Arguments.of(tc.description, tc.unfolding));
		}

		private record PresentTestCase<T>(String description, IUnfolding<T> unfolding, T expectedValue)
		{
		}

		private record EmptyTestCase<T>(String description, IUnfolding<T> unfolding)
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
		<T> void shouldIdentifyIfEmpty(String description, IUnfolding<T> unfolding, boolean expectedResult)
		{
			boolean result = unfolding.isEmpty();
			assertThat(result).as("isEmpty() for %s should give %s", unfolding, expectedResult)
							  .isEqualTo(expectedResult);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
								 new TestCase<>("Unfolding with non-null value should not be empty", IUnfolding.of("value"), false),
								 new TestCase<>("Empty unfolding should be empty", IUnfolding.empty(), true),
								 new TestCase<>("Unfolding created with null should be empty", Unfolding.of(null), true))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedResult));
		}

		private record TestCase<T>(String description, IUnfolding<T> unfolding, boolean expectedResult)
		{
		}
	}

	@Nested
	@DisplayName("Tests for factory methods")
	class FactoryMethodTests
	{
		@Nested
		@DisplayName("Tests for of() method")
		class OfMethodTests
		{
			@DisplayName("should create non-empty unfolding for non-null value")
			@Test
			void shouldCreateNonEmptyUnfoldingForNonNullValue()
			{
				String value = "test";
				IUnfolding<String> unfolding = IUnfolding.of(value);

				assertThat(unfolding.isPresent()).as("of() with non-null value should create present unfolding")
												 .isTrue();
				assertThat(unfolding.summon()).as("of() should store the provided value").isEqualTo(value);
			}

			@DisplayName("should create empty unfolding for null value")
			@Test
			void shouldCreateEmptyUnfoldingForNullValue()
			{
				IUnfolding<String> unfolding = IUnfolding.of(null);

				assertThat(unfolding.isEmpty()).as("of() with null value should create empty unfolding").isTrue();
				assertThatThrownBy(unfolding::summon).as("summon() on empty unfolding should throw exception")
													 .isInstanceOf(EmptyUnfoldingException.class);
			}

			@DisplayName("should create unfolding with primitive value")
			@Test
			void shouldCreateUnfoldingWithPrimitiveValue()
			{
				int value = 42;
				IUnfolding<Integer> unfolding = IUnfolding.of(value);

				assertThat(unfolding.isPresent()).as("of() with primitive value should create present unfolding")
												 .isTrue();
				assertThat(unfolding.summon()).as("of() should store the provided primitive value").isEqualTo(value);
			}
		}

		@Nested
		@DisplayName("Tests for empty() method")
		class EmptyMethodTests
		{
			@DisplayName("should create empty unfolding")
			@Test
			void shouldCreateEmptyUnfolding()
			{
				IUnfolding<String> unfolding = IUnfolding.empty();

				assertThat(unfolding.isEmpty()).as("empty() should create empty unfolding").isTrue();
				assertThat(unfolding.isPresent()).as("empty() should create unfolding that is not present").isFalse();
				assertThatThrownBy(unfolding::summon).as("summon() on empty unfolding should throw exception")
													 .isInstanceOf(EmptyUnfoldingException.class);
			}

			@DisplayName("should return same instance for multiple calls")
			@Test
			void shouldReturnSameInstanceForMultipleCalls()
			{
				IUnfolding<String> unfolding1 = IUnfolding.empty();
				IUnfolding<Integer> unfolding2 = IUnfolding.empty();

				assertThat(unfolding1).as("empty() should return same instance for different calls")
									  .isSameAs(unfolding2);
			}

			@DisplayName("should return same instance for different generic types")
			@Test
			void shouldReturnSameInstanceForDifferentGenericTypes()
			{
				IUnfolding<String> stringUnfolding = IUnfolding.empty();
				IUnfolding<Integer> intUnfolding = IUnfolding.empty();
				IUnfolding<List<String>> listUnfolding = IUnfolding.empty();

				assertThat(stringUnfolding).as("empty() should return same instance for String type")
										   .isSameAs(intUnfolding);
				assertThat(intUnfolding).as("empty() should return same instance for Integer type")
										.isSameAs(listUnfolding);
			}
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
			IUnfolding<String> unfolding = IUnfolding.of("hello world");
			List<String> capturedValues = new ArrayList<>();

			String result =
					unfolding.refold(String::toUpperCase).unlace(capturedValues::add).discern(s -> s.length() > 5)
							 .develop(s -> s.contains("WORLD"), s -> s + "!")
							 .concludeWith(s -> s.substring(0, 5));

			assertThat(result).as("Chained operations should produce expected result").isEqualTo("HELLO");
			assertThat(capturedValues).as("unlace() should have been applied during chain")
									  .containsExactly("HELLO WORLD");
		}

		@DisplayName("should short-circuit on empty unfolding")
		@Test
		void shouldShortCircuitOnEmptyUnfolding()
		{
			IUnfolding<String> unfolding = Unfolding.empty();
			List<String> capturedValues = new ArrayList<>();

			IUnfolding<String> result =
					unfolding.refold(String::toUpperCase).unlace(capturedValues::add).discern(s -> s.length() > 5)
							 .develop(s -> s.contains("WORLD"), s -> s + "!");

			assertThat(result.isEmpty()).as("Result of chained operations on empty unfolding should be empty").isTrue();
			assertThat(capturedValues).as("unlace() should not have been applied to empty unfolding").isEmpty();
		}

		@DisplayName("should handle complex transformations in chain")
		@Test
		void shouldHandleComplexTransformationsInChain()
		{
			IUnfolding<Integer> unfolding = IUnfolding.of(42);

			String result = unfolding.refold(n -> n * 2).discern(n -> n > 50).refold(Object::toString)
									 .develop(s -> s.length() == 2, s -> "0" + s).alternatively("Not found");

			assertThat(result).as("Complex chain should produce expected result").isEqualTo("084");
		}

		@DisplayName("should handle filter making unfolding empty in middle of chain")
		@Test
		void shouldHandleFilterMakingUnfoldingEmptyInMiddleOfChain()
		{
			IUnfolding<Integer> unfolding = IUnfolding.of(42);
			List<String> capturedValues = new ArrayList<>();

			String result = unfolding.refold(n -> n * 2)
									 .discern(n -> n < 50)
									 .refold(Object::toString)
									 .unlace(capturedValues::add)
									 .alternatively("Not found");

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
		<T> void shouldConvertPresentValueToNonEmptyStream(String description, IUnfolding<T> unfolding, T expectedValue)
		{
			Stream<T> result = unfolding.stream();

			assertThat(result).isNotNull();
			assertThat(result).containsExactly(expectedValue);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyUnfoldingTestCases")
		@DisplayName("should convert empty unfolding to empty stream")
		<T> void shouldConvertEmptyUnfoldingToEmptyStream(String description, IUnfolding<T> unfolding)
		{
			Stream<T> result = unfolding.stream();

			assertThat(result).isNotNull();
			assertThat(result).isEmpty();
		}

		private static Stream<Arguments> presentValueTestCases()
		{
			return Stream.of(
					new PresentTestCase<>("String value should be converted to stream with that value",
							IUnfolding.of("test"), "test"),
					new PresentTestCase<>("Integer value should be converted to stream with that value",
							IUnfolding.of(42), 42),
					new PresentTestCase<>("Boolean value should be converted to stream with that value",
							IUnfolding.of(true), true)
			).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedValue));
		}

		private static Stream<Arguments> emptyUnfoldingTestCases()
		{
			return Stream.of(
					new EmptyTestCase<>("Empty String unfolding should convert to empty stream",
							IUnfolding.<String>empty()),
					new EmptyTestCase<>("Empty Integer unfolding should convert to empty stream",
							IUnfolding.<Integer>empty()),
					new EmptyTestCase<>("Empty Boolean unfolding should convert to empty stream",
							IUnfolding.<Boolean>empty())
			).map(tc -> Arguments.of(tc.description, tc.unfolding));
		}

		private record PresentTestCase<T>(String description, IUnfolding<T> unfolding, T expectedValue)
		{
		}

		private record EmptyTestCase<T>(String description, IUnfolding<T> unfolding)
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
		<T> void shouldReturnFormattedStringForPresentValues(String description, IUnfolding<T> unfolding,
															 String expectedResult)
		{
			String result = unfolding.toString();

			assertThat(result).as("toString() for %s should return expected formatted string", unfolding)
							  .isEqualTo(expectedResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyUnfoldingTestCases")
		@DisplayName("should return formatted string for empty unfolding")
		<T> void shouldReturnFormattedStringForEmptyUnfolding(String description, IUnfolding<T> unfolding)
		{
			String result = unfolding.toString();

			assertThat(result).as("toString() for empty unfolding should return expected formatted string")
							  .isEqualTo(EmptyUnfolding.instance().toString());
		}

		private static Stream<Arguments> presentValueTestCases()
		{
			return Stream.of(
					new PresentTestCase<>("String value should be formatted correctly",
							IUnfolding.of("test"), "Unfolding[test]"),
					new PresentTestCase<>("Integer value should be formatted correctly",
							IUnfolding.of(42), "Unfolding[42]"),
					new PresentTestCase<>("Boolean value should be formatted correctly",
							IUnfolding.of(true), "Unfolding[true]")
			).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedResult));
		}

		private static Stream<Arguments> emptyUnfoldingTestCases()
		{
			return Stream.of(
					new EmptyTestCase<>("Empty String unfolding should be formatted correctly",
							IUnfolding.<String>empty()),
					new EmptyTestCase<>("Empty Integer unfolding should be formatted correctly",
							IUnfolding.<Integer>empty()),
					new EmptyTestCase<>("Empty Boolean unfolding should be formatted correctly",
							IUnfolding.<Boolean>empty())
			).map(tc -> Arguments.of(tc.description, tc.unfolding));
		}

		private record PresentTestCase<T>(String description, IUnfolding<T> unfolding, String expectedResult)
		{
		}

		private record EmptyTestCase<T>(String description, IUnfolding<T> unfolding)
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
			IUnfolding<String> stringUnfolding = IUnfolding.of("test");
			assertThat(stringUnfolding.equals(stringUnfolding))
					.as("An unfolding should equal itself (reflexivity)")
					.isTrue();

			IUnfolding<Integer> emptyUnfolding = IUnfolding.empty();
			assertThat(emptyUnfolding.equals(emptyUnfolding))
					.as("An empty unfolding should equal itself (reflexivity)")
					.isTrue();
		}

		@DisplayName("should satisfy symmetry (x.equals(y) == y.equals(x))")
		@Test
		void shouldSatisfySymmetry()
		{
			IUnfolding<String> unfolding1 = IUnfolding.of("test");
			IUnfolding<String> unfolding2 = IUnfolding.of("test");

			assertThat(unfolding1.equals(unfolding2))
					.as("First unfolding should equal second unfolding with same value")
					.isEqualTo(unfolding2.equals(unfolding1));

			IUnfolding<String> presentUnfolding = IUnfolding.of("test");
			IUnfolding<String> emptyUnfolding = IUnfolding.empty();

			assertThat(presentUnfolding.equals(emptyUnfolding))
					.as("Present unfolding equality with empty unfolding should be symmetric")
					.isEqualTo(emptyUnfolding.equals(presentUnfolding));

			IUnfolding<String> emptyUnfolding1 = IUnfolding.empty();
			IUnfolding<Integer> emptyUnfolding2 = IUnfolding.empty();

			assertThat(emptyUnfolding1.equals(emptyUnfolding2))
					.as("Empty unfolding equality should be symmetric")
					.isEqualTo(emptyUnfolding2.equals(emptyUnfolding1));
		}

		@DisplayName("should satisfy transitivity (if x.equals(y) and y.equals(z), then x.equals(z))")
		@Test
		void shouldSatisfyTransitivity()
		{
			IUnfolding<String> unfolding1 = IUnfolding.of("test");
			IUnfolding<String> unfolding2 = IUnfolding.of("test");
			IUnfolding<String> unfolding3 = IUnfolding.of("test");

			boolean firstEqualsSecond = unfolding1.equals(unfolding2);
			boolean secondEqualsThird = unfolding2.equals(unfolding3);

			assertThat(firstEqualsSecond && secondEqualsThird)
					.as("Precondition: first equals second and second equals third")
					.isTrue();

			assertThat(unfolding1.equals(unfolding3))
					.as("Transitivity: if first equals second and second equals third, then first equals third")
					.isTrue();

			IUnfolding<String> emptyUnfolding1 = IUnfolding.empty();
			IUnfolding<Integer> emptyUnfolding2 = IUnfolding.empty();
			IUnfolding<List<String>> emptyUnfolding3 = IUnfolding.empty();

			boolean firstEmptyEqualsSecond = emptyUnfolding1.equals(emptyUnfolding2);
			boolean secondEmptyEqualsThird = emptyUnfolding2.equals(emptyUnfolding3);

			assertThat(firstEmptyEqualsSecond && secondEmptyEqualsThird)
					.as("Precondition: first empty equals second empty and second empty equals third empty")
					.isTrue();

			assertThat(emptyUnfolding1.equals(emptyUnfolding3))
					.as("Transitivity for empty: if first equals second and second equals third, then first equals third")
					.isTrue();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("hashCodeConsistencyTestCases")
		@DisplayName("should have consistent hashCode with equals (if x.equals(y), then x.hashCode() == y.hashCode())")
		<T, U> void shouldHaveConsistentHashCodeWithEquals(
				String description,
				IUnfolding<T> first,
				IUnfolding<U> second,
				boolean shouldBeEqual
		)
		{
			assertThat(first.equals(second))
					.as(description)
					.isEqualTo(shouldBeEqual);

			// If they should be equal, their hashCodes must be equal too
			if (shouldBeEqual)
			{
				assertThat(first.hashCode())
						.as("Equal unfoldings should have the same hashCode")
						.isEqualTo(second.hashCode());
			}

			// Note: Different unfoldings may have the same hashCode by coincidence,
			// so we don't assert that they must have different hashCodes when not equal
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("equalityWithDifferentValueTypesTestCases")
		@DisplayName("should handle equality with different value types correctly")
		<T, U> void shouldHandleEqualityWithDifferentValueTypesCorrectly(String description, IUnfolding<T> first,
																		 IUnfolding<U> second, boolean shouldBeEqual)
		{
			assertThat(first.equals(second))
					.as(description)
					.isEqualTo(shouldBeEqual);
		}

		@DisplayName("should handle equality with null values and empty unfoldings correctly")
		@Test
		void shouldHandleEqualityWithNullValuesAndEmptyUnfoldingsCorrectly()
		{
			IUnfolding<String> emptyStringUnfolding = IUnfolding.empty();
			IUnfolding<Integer> emptyIntegerUnfolding = IUnfolding.empty();
			IUnfolding<List<String>> emptyListUnfolding = IUnfolding.empty();

			assertThat(emptyStringUnfolding.equals(emptyIntegerUnfolding))
					.as("Empty unfoldings of different types should be equal")
					.isTrue();

			assertThat(emptyIntegerUnfolding.equals(emptyListUnfolding))
					.as("Empty unfoldings of different types should be equal")
					.isTrue();

			IUnfolding<String> nullStringUnfolding = IUnfolding.of(null);
			IUnfolding<Integer> nullIntegerUnfolding = IUnfolding.of(null);

			assertThat(nullStringUnfolding.equals(emptyStringUnfolding))
					.as("IUnfolding.of(null) should equal IUnfolding.empty()")
					.isTrue();

			assertThat(nullIntegerUnfolding.equals(emptyIntegerUnfolding))
					.as("IUnfolding.of(null) should equal IUnfolding.empty()")
					.isTrue();

			assertThat(nullStringUnfolding.equals(nullIntegerUnfolding))
					.as("IUnfolding.of(null) instances should be equal regardless of type")
					.isTrue();

			IUnfolding<String> presentUnfolding = IUnfolding.of("test");

			assertThat(emptyStringUnfolding.equals(presentUnfolding))
					.as("Empty unfolding should not equal present unfolding")
					.isFalse();

			assertThat(nullStringUnfolding.equals(presentUnfolding))
					.as("IUnfolding.of(null) should not equal present unfolding")
					.isFalse();

			IUnfolding<String> emptyStringValueUnfolding = IUnfolding.of("");

			assertThat(emptyStringValueUnfolding.equals(emptyStringUnfolding))
					.as("Unfolding with empty string should not equal empty unfolding")
					.isFalse();

			assertThat(emptyStringValueUnfolding.equals(nullStringUnfolding))
					.as("Unfolding with empty string should not equal IUnfolding.of(null)")
					.isFalse();

			assertThat(presentUnfolding.equals(null))
					.as("Unfolding should not equal null")
					.isFalse();

			assertThat(emptyStringUnfolding.equals(null))
					.as("Empty unfolding should not equal null")
					.isFalse();

			String stringObject = "test";

			assertThat(presentUnfolding.equals(stringObject))
					.as("Unfolding should not equal non-Unfolding object")
					.isFalse();

			assertThat(emptyStringUnfolding.equals(stringObject))
					.as("Empty unfolding should not equal non-Unfolding object")
					.isFalse();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptinessComparisonTestCases")
		@DisplayName("should handle emptiness comparison correctly")
		<T, U> void shouldHandleEmptinessComparisonCorrectly(String description, IUnfolding<T> first,
															 IUnfolding<U> second, boolean shouldBeEqual)
		{
			assertThat(first.equals(second))
					.as(description)
					.isEqualTo(shouldBeEqual);

			assertThat(second.equals(first))
					.as(description + " (symmetric check)")
					.isEqualTo(shouldBeEqual);
		}

		private static Stream<Arguments> hashCodeConsistencyTestCases()
		{
			IUnfolding<String> unfolding1 = IUnfolding.of("test");
			IUnfolding<String> unfolding2 = IUnfolding.of("test");

			IUnfolding<String> unfolding3 = IUnfolding.of("different");

			IUnfolding<String> emptyUnfolding1 = IUnfolding.empty();
			IUnfolding<Integer> emptyUnfolding2 = IUnfolding.empty();

			return Stream.of(
					new HashCodeTestCase<>("Unfoldings with same value should be equal and have same hashCode",
							unfolding1, unfolding2, true),
					new HashCodeTestCase<>("Unfoldings with different values should not be equal",
							unfolding1, unfolding3, false),
					new HashCodeTestCase<>("Empty unfoldings should be equal and have same hashCode",
							emptyUnfolding1, emptyUnfolding2, true)
			).map(tc -> Arguments.of(tc.description, tc.first, tc.second, tc.shouldBeEqual));
		}

		private static Stream<Arguments> equalityWithDifferentValueTypesTestCases()
		{
			IUnfolding<String> stringUnfolding1 = IUnfolding.of("test");
			IUnfolding<String> stringUnfolding2 = IUnfolding.of("test");
			IUnfolding<String> differentStringUnfolding = IUnfolding.of("different");

			IUnfolding<Integer> intUnfolding1 = IUnfolding.of(42);
			IUnfolding<Integer> intUnfolding2 = IUnfolding.of(42);
			IUnfolding<Integer> differentIntUnfolding = IUnfolding.of(100);

			List<String> list1 = new ArrayList<>();
			list1.add("item");
			List<String> list2 = new ArrayList<>();
			list2.add("item");
			List<String> differentList = new ArrayList<>();
			differentList.add("different");

			IUnfolding<List<String>> listUnfolding1 = IUnfolding.of(list1);
			IUnfolding<List<String>> listUnfolding2 = IUnfolding.of(list2);
			IUnfolding<List<String>> differentListUnfolding = IUnfolding.of(differentList);

			IUnfolding<Integer> integerUnfolding = IUnfolding.of(123);
			IUnfolding<String> stringNumberUnfolding = IUnfolding.of("123");

			return Stream.of(
					new EqualityTestCase<>("Unfoldings with same string value should be equal",
							stringUnfolding1, stringUnfolding2, true),
					new EqualityTestCase<>("Unfoldings with different string values should not be equal",
							stringUnfolding1, differentStringUnfolding, false),
					new EqualityTestCase<>("Unfoldings with same integer value should be equal",
							intUnfolding1, intUnfolding2, true),
					new EqualityTestCase<>("Unfoldings with different integer values should not be equal",
							intUnfolding1, differentIntUnfolding, false),
					new EqualityTestCase<>("Unfoldings with equal list values should be equal",
							listUnfolding1, listUnfolding2, true),
					new EqualityTestCase<>("Unfoldings with different list values should not be equal",
							listUnfolding1, differentListUnfolding, false),
					new EqualityTestCase<>("Unfoldings with different types but similar values should not be equal",
							integerUnfolding, stringNumberUnfolding, false)
			).map(tc -> Arguments.of(tc.description, tc.first, tc.second, tc.shouldBeEqual));
		}

		private static Stream<Arguments> emptinessComparisonTestCases()
		{
			IUnfolding<String> emptyStringUnfolding = IUnfolding.empty();
			IUnfolding<Integer> emptyIntegerUnfolding = IUnfolding.empty();
			IUnfolding<String> presentStringUnfolding = IUnfolding.of("test");
			IUnfolding<Integer> presentIntegerUnfolding = IUnfolding.of(42);
			IUnfolding<String> emptyStringValueUnfolding = IUnfolding.of("");

			return Stream.of(
					new EqualityTestCase<>("Empty unfoldings of same type should be equal",
							emptyStringUnfolding, emptyStringUnfolding, true),
					new EqualityTestCase<>("Empty unfoldings of different types should be equal",
							emptyStringUnfolding, emptyIntegerUnfolding, true),
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
							emptyStringUnfolding, emptyStringValueUnfolding, false)
			).map(tc -> Arguments.of(tc.description, tc.first, tc.second, tc.shouldBeEqual));
		}

		private record HashCodeTestCase<T, U>(String description, IUnfolding<T> first, IUnfolding<U> second,
											  boolean shouldBeEqual)
		{
		}

		private record EqualityTestCase<T, U>(String description, IUnfolding<T> first, IUnfolding<U> second,
											  boolean shouldBeEqual)
		{
		}
	}
}