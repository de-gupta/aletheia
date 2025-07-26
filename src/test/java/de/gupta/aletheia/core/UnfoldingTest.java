package de.gupta.aletheia.core;

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
	@DisplayName("Tests for with() method")
	class WithTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("successTestCases")
		@DisplayName("should transform present values")
		<T, R> void shouldTransformValue(String description, Unfolding<T> unfolding, Function<T, R> extractor,
										 R expectedResult)
		{
			R result = unfolding.with(extractor);
			assertThat(result).as("with() for %s should give %s", unfolding, expectedResult).isEqualTo(expectedResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("exceptionTestCases")
		@DisplayName("should throw exception for empty unfolding")
		<T, R> void shouldThrowExceptionForEmptyUnfolding(String description, Unfolding<T> unfolding,
														  Function<T, R> extractor)
		{
			assertThatThrownBy(() -> unfolding.with(extractor)).as("with() for %s should throw IllegalStateException",
					unfolding).isInstanceOf(IllegalStateException.class).hasMessageContaining("Unfolding is empty");
		}

		@DisplayName("should throw exception for null extractor")
		@Test
		void shouldThrowExceptionForNullExtractor()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			Function<String, Integer> nullExtractor = null;

			assertThatThrownBy(() -> unfolding.with(nullExtractor))
					.as("with() with null extractor should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("extractor must not be null");
		}

		private static Stream<Arguments> successTestCases()
		{
			return Stream.of(
								 new SuccessTestCase<>("Present value should be transformed by extractor", Unfolding.of("hello"),
										 String::length, 5),
								 new SuccessTestCase<>("Complex transformation should work on present value", Unfolding.of(42),
										 num -> "Number: " + num, "Number: 42"),
								 new SuccessTestCase<>("Identity function should return the same value", Unfolding.of("test"),
										 Function.identity(), "test"),
								 new SuccessTestCase<>("Extractor returning null should work", Unfolding.of("hello"), _ -> null,
										 null))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.extractor, tc.expectedResult));
		}

		private static Stream<Arguments> exceptionTestCases()
		{
			return Stream.of(new ExceptionTestCase<>("Empty unfolding should throw exception", Unfolding.empty(),
							_ -> "unused"),
					new ExceptionTestCase<>("Unfolding created with null should throw exception", Unfolding.of(null),
							_ -> "unused")).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.extractor));
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
			T result = useSupplier ? unfolding.alternatively(fallbackSupplier) : unfolding.alternatively(fallbackValue);

			String fallbackDescription =
					useSupplier ? "supplier returning " + expectedResult : String.valueOf(fallbackValue);

			assertThat(result).as("alternatively() for %s with fallback %s should give %s", unfolding,
					fallbackDescription, expectedResult).isEqualTo(expectedResult);
		}

		@DisplayName("should throw exception for null supplier")
		@Test
		void shouldThrowExceptionForNullSupplier()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			Supplier<String> nullSupplier = null;

			assertThatThrownBy(() -> unfolding.alternatively(nullSupplier))
					.as("alternatively() with null supplier should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("fallback supplier must not be null");
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
								 new TestCase<>("Present value should be returned ignoring fallback value", Unfolding.of("primary"),
										 "fallback", null, "primary", false),
								 new TestCase<>("Empty unfolding should return fallback value", Unfolding.empty(), "fallback", null,
										 "fallback", false),
								 new TestCase<>("Present value should be returned ignoring fallback supplier", Unfolding.of(100),
										 null, () -> 200, 100, true),
								 new TestCase<>("Present value should be returned ignoring fallback and fallback supplier",
										 Unfolding.of(100), 300, () -> 200, 100, true),
								 new TestCase<>("Empty unfolding should use fallback supplier", Unfolding.empty(), null, () -> 200,
										 200, true),
								 new TestCase<>("Null fallback value should be returned for empty unfolding", Unfolding.empty(),
										 null, null, null, false),
								 new TestCase<>("Null value from fallback supplier should be returned for empty unfolding",
										 Unfolding.empty(), null, () -> null, null, true),
								 new TestCase<>("Unfolding created with null should return fallback value", Unfolding.of(null),
										 "fallback", null, "fallback", false),
								 new TestCase<>("Unfolding created with null should use fallback supplier", Unfolding.of(null), null,
										 () -> "fallback", "fallback", true))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.fallbackValue, tc.fallbackSupplier,
								 tc.expectedResult, tc.useSupplier));
		}

		record TestCase<T>(String description, Unfolding<T> unfolding, T fallbackValue, Supplier<T> fallbackSupplier,
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
		<T> void shouldIdentifyValueExistence(String description, Unfolding<T> unfolding, boolean expectedResult)
		{
			boolean result = unfolding.isPresent();

			assertThat(result).as("isPresent() for %s should give %s", unfolding, expectedResult)
							  .isEqualTo(expectedResult);
		}

		@DisplayName("should be consistent with isEmpty()")
		@Test
		void shouldBeConsistentWithIsEmpty()
		{
			Unfolding<String> presentUnfolding = Unfolding.of("test");
			Unfolding<Integer> emptyUnfolding = Unfolding.empty();
			Unfolding<String> nullUnfolding = Unfolding.of(null);

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
					new TestCase<>("Unfolding with non-null value should be present", Unfolding.of("value"), true),
					new TestCase<>("Empty unfolding should not be present", Unfolding.empty(), false),
					new TestCase<>("Unfolding created with null should be empty and not present", Unfolding.of(null),
							false),
					new TestCase<>("Unfolding with empty string should be present", Unfolding.of(""), true),
					new TestCase<>("Unfolding with zero should be present", Unfolding.of(0), true),
					new TestCase<>("Unfolding with false should be present", Unfolding.of(false), true),
					new TestCase<>("Unfolding with empty list should be present", Unfolding.of(new ArrayList<>()),
							true)).map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedResult));
		}

		private record TestCase<T>(String description, Unfolding<T> unfolding, boolean expectedResult)
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
		<T, R> void shouldTransformPresentValue(String description, Unfolding<T> unfolding, Function<T, R> mapper,
												Unfolding<R> expectedResult)
		{
			Unfolding<R> result = unfolding.refold(mapper);

			assertThat(result.isPresent()).as("refold() for %s should result in present unfolding", unfolding).isTrue();
			assertThat(result.get()).as("refold() result value should match expected").isEqualTo(expectedResult.get());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T, R> void shouldTransformToEmptyUnfolding(String description, Unfolding<T> unfolding, Function<T, R> mapper)
		{
			Unfolding<R> result = unfolding.refold(mapper);

			assertThat(result.isEmpty()).as("refold() for %s should result in empty unfolding", unfolding).isTrue();
		}

		@DisplayName("should throw exception for null mapper")
		@Test
		void shouldThrowExceptionForNullMapper()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			Function<String, Integer> nullMapper = null;

			assertThatThrownBy(() -> unfolding.refold(nullMapper))
					.as("refold() with null mapper should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("mapper must not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(
								 new PresentResultTestCase<>("Present value should be transformed by mapper", Unfolding.of("hello"),
										 String::length, Unfolding.of(5)),
								 new PresentResultTestCase<>("Complex transformation should work on present value", Unfolding.of(42),
										 num -> "Number: " + num, Unfolding.of("Number: 42")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.mapper, tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Empty unfolding should remain empty after mapping",
										 Unfolding.empty(), String::length),
								 new EmptyResultTestCase<>("Mapper returning null should result in empty unfolding",
										 Unfolding.of("test"), _ -> null))
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

			assertThat(result.isPresent()).as("develop() for %s with predicate should result in present unfolding",
					unfolding).isTrue();
			assertThat(result.get()).as("develop() result value should match expected").isEqualTo(expectedResult.get());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T> void shouldTransformToEmptyUnfolding(String description, Unfolding<T> unfolding, Predicate<T> predicate,
												 Function<T, T> mapper)
		{
			Unfolding<T> result = unfolding.develop(predicate, mapper);

			assertThat(result.isEmpty()).as("develop() for %s with predicate should result in empty unfolding",
												unfolding)
										.isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			Predicate<String> nullPredicate = null;
			Function<String, String> mapper = String::toUpperCase;

			assertThatThrownBy(() -> unfolding.develop(nullPredicate, mapper))
					.as("develop() with null predicate should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("predicate must not be null");
		}

		@DisplayName("should throw exception for null mapper")
		@Test
		void shouldThrowExceptionForNullMapper()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> nullMapper = null;

			assertThatThrownBy(() -> unfolding.develop(predicate, nullMapper))
					.as("develop() with null mapper should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("mapper must not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value matching predicate should be transformed",
										 Unfolding.of("hello"), s -> s.length() > 3, String::toUpperCase, Unfolding.of("HELLO")),
								 new PresentResultTestCase<>("Present value not matching predicate should remain unchanged",
										 Unfolding.of("hi"), s -> s.length() > 3, String::toUpperCase, Unfolding.of("hi")),
								 new PresentResultTestCase<>("Mapper should not be applied when predicate doesn't match",
										 Unfolding.of(5), n -> n > 10, _ -> null, Unfolding.of(5)))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper,
								 tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(
								 new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of predicate and mapper",
										 Unfolding.<String>empty(), _ -> true, String::toUpperCase), new EmptyResultTestCase<>(
										 "Mapper returning null should result in empty unfolding when predicate matches",
										 Unfolding.of(42), n -> n > 10, _ -> null))
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
	@DisplayName("Tests for refold() method")
	class RefoldIfTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should conditionally transform present value to new present value")
		<T, R> void shouldTransformToPresentValue(String description, Unfolding<T> unfolding, Predicate<T> predicate,
												  Function<T, R> mapper, Unfolding<R> expectedResult)
		{
			Unfolding<R> result = unfolding.refold(predicate, mapper);

			assertThat(result.isPresent()).as("refold() for %s with predicate should result in present unfolding",
					unfolding).isTrue();
			assertThat(result.get()).as("refold() result value should match expected").isEqualTo(expectedResult.get());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should transform to empty unfolding when appropriate")
		<T, R> void shouldTransformToEmptyUnfolding(String description, Unfolding<T> unfolding, Predicate<T> predicate,
													Function<T, R> mapper)
		{
			Unfolding<R> result = unfolding.refold(predicate, mapper);

			assertThat(result.isEmpty()).as("refold() for %s with predicate should result in empty unfolding",
					unfolding).isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			Predicate<String> nullPredicate = null;
			Function<String, String> mapper = String::toUpperCase;

			assertThatThrownBy(() -> unfolding.refold(nullPredicate, mapper))
					.as("refold() with null predicate should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining(
							"predicate must not be null");
		}

		@DisplayName("should throw exception for null mapper")
		@Test
		void shouldThrowExceptionForNullMapper()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			Predicate<String> predicate = s -> s.length() > 3;
			Function<String, String> nullMapper = null;

			assertThatThrownBy(() -> unfolding.refold(predicate, nullMapper))
					.as("refold() with null mapper should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("mapper must not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value matching predicate should be transformed",
										 Unfolding.of("hello"), s -> s.length() > 3, String::toUpperCase, Unfolding.of("HELLO")),
								 new PresentResultTestCase<>("Type transformation should work when predicate matches",
										 Unfolding.of(42), n -> n > 10, n -> "Number: " + n, Unfolding.of("Number: 42")))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate, tc.mapper,
								 tc.expectedResult));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Present value not matching predicate should become empty",
										 Unfolding.of("hi"), s -> s.length() > 3, String::toUpperCase),
								 new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of predicate and mapper",
										 Unfolding.<String>empty(), _ -> true, String::toUpperCase), new EmptyResultTestCase<>(
										 "Mapper returning null should result in empty unfolding when predicate matches",
										 Unfolding.of(42), n -> n > 10, _ -> null),
								 new EmptyResultTestCase<>("Mapper should not be applied when predicate doesn't match",
										 Unfolding.of(5), n -> n > 10, _ -> "transformed"))
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
	@DisplayName("Tests for discern() method")
	class DiscernTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("presentResultTestCases")
		@DisplayName("should keep value when predicate matches")
		<T> void shouldKeepValueWhenPredicateMatches(String description, Unfolding<T> unfolding, Predicate<T> predicate)
		{
			Unfolding<T> result = unfolding.discern(predicate);

			assertThat(result.isPresent()).as("discern() for %s with matching predicate should remain present",
					unfolding).isTrue();
			assertThat(result.get()).as("discern() result value should match original").isEqualTo(unfolding.get());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyResultTestCases")
		@DisplayName("should discard value when predicate doesn't match")
		<T> void shouldDiscardValueWhenPredicateDoesntMatch(String description, Unfolding<T> unfolding,
															Predicate<T> predicate)
		{
			Unfolding<T> result = unfolding.discern(predicate);

			assertThat(result.isEmpty()).as("discern() for %s with non-matching predicate should become empty",
					unfolding).isTrue();
		}

		@DisplayName("should throw exception for null predicate")
		@Test
		void shouldThrowExceptionForNullPredicate()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			Predicate<String> nullPredicate = null;

			assertThatThrownBy(() -> unfolding.discern(nullPredicate))
					.as("discern() with null predicate should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("predicate must not be null");
		}

		private static Stream<Arguments> presentResultTestCases()
		{
			return Stream.of(new PresentResultTestCase<>("Present value matching predicate should remain unchanged",
										 Unfolding.of("hello"), s -> s.length() > 3),
								 new PresentResultTestCase<>("Predicate with complex logic should work correctly (even numbers)",
										 Unfolding.of(42), n -> n % 2 == 0))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.predicate));
		}

		private static Stream<Arguments> emptyResultTestCases()
		{
			return Stream.of(new EmptyResultTestCase<>("Present value not matching predicate should become empty",
										 Unfolding.of("hi"), s -> s.length() > 3),
								 new EmptyResultTestCase<>("Empty unfolding should remain empty regardless of predicate",
										 Unfolding.<String>empty(), _ -> true),
								 new EmptyResultTestCase<>("Predicate with complex logic should work correctly (odd numbers)",
										 Unfolding.of(43), n -> n % 2 == 0))
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
	@DisplayName("Tests for tap() method")
	class TapTests
	{
		@DisplayName("should apply consumer to present value and return the same unfolding")
		@Test
		void shouldApplyConsumerToPresentValue()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			List<String> capturedValues = new ArrayList<>();
			Consumer<String> consumer = capturedValues::add;

			Unfolding<String> result = unfolding.tap(consumer);

			assertThat(result).as("tap() should return the same unfolding").isSameAs(unfolding);
			assertThat(capturedValues).as("Consumer should be applied to the value").containsExactly("test");
		}

		@DisplayName("should not apply consumer to empty unfolding and return the same unfolding")
		@Test
		void shouldNotApplyConsumerToEmptyUnfolding()
		{
			Unfolding<String> unfolding = Unfolding.empty();
			List<String> capturedValues = new ArrayList<>();
			Consumer<String> consumer = capturedValues::add;

			Unfolding<String> result = unfolding.tap(consumer);

			assertThat(result).as("tap() should return the same unfolding").isSameAs(unfolding);
			assertThat(capturedValues).as("Consumer should not be applied to empty unfolding").isEmpty();
		}

		@DisplayName("should throw exception for null consumer")
		@Test
		void shouldThrowExceptionForNullConsumer()
		{
			Unfolding<String> unfolding = Unfolding.of("test");
			Consumer<String> nullConsumer = null;

			assertThatThrownBy(() -> unfolding.tap(nullConsumer)).as(
																		 "tap() with null consumer should throw NullPointerException")
																 .isInstanceOf(NullPointerException.class)
																 .hasMessageContaining("consumer must not be null");
		}
	}

	@Nested
	@DisplayName("Tests for get() method")
	class GetTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("successTestCases")
		@DisplayName("should return value for present unfolding")
		<T> void shouldReturnValue(String description, Unfolding<T> unfolding, T expectedValue)
		{
			T result = unfolding.get();
			assertThat(result).as("get() for %s should return %s", unfolding, expectedValue).isEqualTo(expectedValue);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("exceptionTestCases")
		@DisplayName("should throw exception for empty unfolding")
		<T> void shouldThrowException(String description, Unfolding<T> unfolding)
		{
			assertThatThrownBy(unfolding::get).as("get() for %s should throw IllegalStateException", unfolding)
											  .isInstanceOf(IllegalStateException.class)
											  .hasMessageContaining("Unfolding is empty");
		}

		private static Stream<Arguments> successTestCases()
		{
			return Stream.of(new SuccessTestCase<>("Present value should be returned", Unfolding.of("hello"), "hello"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedValue));
		}

		private static Stream<Arguments> exceptionTestCases()
		{
			return Stream.of(new ExceptionTestCase<>("Empty unfolding should throw exception", Unfolding.empty()),
								 new ExceptionTestCase<>("Unfolding with null value should throw exception", Unfolding.of(null)))
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
								 Unfolding.of("hello"), "hello"))
						 .map(tc -> Arguments.of(tc.description, tc.unfolding, tc.expectedValue));
		}

		private static Stream<Arguments> emptyTestCases()
		{
			return Stream.of(
					new EmptyTestCase<>("Empty unfolding should be converted to empty Optional", Unfolding.empty()),
					new EmptyTestCase<>("Unfolding created with null should be converted to empty Optional",
							Unfolding.of(null))).map(tc -> Arguments.of(tc.description, tc.unfolding));
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
			boolean result = unfolding.isEmpty();
			assertThat(result).as("isEmpty() for %s should give %s", unfolding, expectedResult)
							  .isEqualTo(expectedResult);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
								 new TestCase<>("Unfolding with non-null value should not be empty", Unfolding.of("value"), false),
								 new TestCase<>("Empty unfolding should be empty", Unfolding.empty(), true),
								 new TestCase<>("Unfolding created with null should be empty", Unfolding.of(null), true))
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
		@DisplayName("Tests for of() method")
		class OfMethodTests
		{
			@DisplayName("should create non-empty unfolding for non-null value")
			@Test
			void shouldCreateNonEmptyUnfoldingForNonNullValue()
			{
				String value = "test";
				Unfolding<String> unfolding = Unfolding.of(value);

				assertThat(unfolding.isPresent()).as("of() with non-null value should create present unfolding")
												 .isTrue();
				assertThat(unfolding.get()).as("of() should store the provided value").isEqualTo(value);
			}

			@DisplayName("should create empty unfolding for null value")
			@Test
			void shouldCreateEmptyUnfoldingForNullValue()
			{
				Unfolding<String> unfolding = Unfolding.of(null);

				assertThat(unfolding.isEmpty()).as("of() with null value should create empty unfolding").isTrue();
				assertThatThrownBy(unfolding::get).as("get() on empty unfolding should throw exception")
												  .isInstanceOf(IllegalStateException.class);
			}

			@DisplayName("should create unfolding with primitive value")
			@Test
			void shouldCreateUnfoldingWithPrimitiveValue()
			{
				int value = 42;
				Unfolding<Integer> unfolding = Unfolding.of(value);

				assertThat(unfolding.isPresent()).as("of() with primitive value should create present unfolding")
												 .isTrue();
				assertThat(unfolding.get()).as("of() should store the provided primitive value").isEqualTo(value);
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
				Unfolding<String> unfolding = Unfolding.empty();

				assertThat(unfolding.isEmpty()).as("empty() should create empty unfolding").isTrue();
				assertThat(unfolding.isPresent()).as("empty() should create unfolding that is not present").isFalse();
				assertThatThrownBy(unfolding::get).as("get() on empty unfolding should throw exception")
												  .isInstanceOf(IllegalStateException.class);
			}

			@DisplayName("should return same instance for multiple calls")
			@Test
			void shouldReturnSameInstanceForMultipleCalls()
			{
				Unfolding<String> unfolding1 = Unfolding.empty();
				Unfolding<Integer> unfolding2 = Unfolding.empty();

				assertThat(unfolding1).as("empty() should return same instance for different calls")
									  .isSameAs(unfolding2);
			}

			@DisplayName("should return same instance for different generic types")
			@Test
			void shouldReturnSameInstanceForDifferentGenericTypes()
			{
				Unfolding<String> stringUnfolding = Unfolding.empty();
				Unfolding<Integer> intUnfolding = Unfolding.empty();
				Unfolding<List<String>> listUnfolding = Unfolding.empty();

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
			Unfolding<String> unfolding = Unfolding.of("hello world");
			List<String> capturedValues = new ArrayList<>();

			String result = unfolding.refold(String::toUpperCase).tap(capturedValues::add).discern(s -> s.length() > 5)
									 .develop(s -> s.contains("WORLD"), s -> s + "!").with(s -> s.substring(0, 5));

			assertThat(result).as("Chained operations should produce expected result").isEqualTo("HELLO");
			assertThat(capturedValues).as("tap() should have been applied during chain").containsExactly("HELLO WORLD");
		}

		@DisplayName("should short-circuit on empty unfolding")
		@Test
		void shouldShortCircuitOnEmptyUnfolding()
		{
			Unfolding<String> unfolding = Unfolding.empty();
			List<String> capturedValues = new ArrayList<>();

			Unfolding<String> result =
					unfolding.refold(String::toUpperCase).tap(capturedValues::add).discern(s -> s.length() > 5)
							 .develop(s -> s.contains("WORLD"), s -> s + "!");

			assertThat(result.isEmpty()).as("Result of chained operations on empty unfolding should be empty").isTrue();
			assertThat(capturedValues).as("tap() should not have been applied to empty unfolding").isEmpty();
		}

		@DisplayName("should handle complex transformations in chain")
		@Test
		void shouldHandleComplexTransformationsInChain()
		{
			Unfolding<Integer> unfolding = Unfolding.of(42);

			String result = unfolding.refold(n -> n * 2).discern(n -> n > 50).refold(Object::toString)
									 .develop(s -> s.length() == 2, s -> "0" + s).alternatively("Not found");

			assertThat(result).as("Complex chain should produce expected result").isEqualTo("084");
		}

		@DisplayName("should handle filter making unfolding empty in middle of chain")
		@Test
		void shouldHandleFilterMakingUnfoldingEmptyInMiddleOfChain()
		{
			Unfolding<Integer> unfolding = Unfolding.of(42);
			List<String> capturedValues = new ArrayList<>();

			String result = unfolding.refold(n -> n * 2)
									 .discern(n -> n < 50)
									 .refold(Object::toString)
									 .tap(capturedValues::add)
									 .alternatively("Not found");

			assertThat(result).as("Chain with filter making unfolding empty should use fallback")
							  .isEqualTo("Not found");
			assertThat(capturedValues).as("tap() should not be called after filter made unfolding empty").isEmpty();
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
			Unfolding<String> stringUnfolding = Unfolding.of("test");
			assertThat(stringUnfolding.equals(stringUnfolding))
					.as("An unfolding should equal itself (reflexivity)")
					.isTrue();

			Unfolding<Integer> emptyUnfolding = Unfolding.empty();
			assertThat(emptyUnfolding.equals(emptyUnfolding))
					.as("An empty unfolding should equal itself (reflexivity)")
					.isTrue();
		}

		@DisplayName("should satisfy symmetry (x.equals(y) == y.equals(x))")
		@Test
		void shouldSatisfySymmetry()
		{
			Unfolding<String> unfolding1 = Unfolding.of("test");
			Unfolding<String> unfolding2 = Unfolding.of("test");

			assertThat(unfolding1.equals(unfolding2))
					.as("First unfolding should equal second unfolding with same value")
					.isEqualTo(unfolding2.equals(unfolding1));

			Unfolding<String> presentUnfolding = Unfolding.of("test");
			Unfolding<String> emptyUnfolding = Unfolding.empty();

			assertThat(presentUnfolding.equals(emptyUnfolding))
					.as("Present unfolding equality with empty unfolding should be symmetric")
					.isEqualTo(emptyUnfolding.equals(presentUnfolding));

			Unfolding<String> emptyUnfolding1 = Unfolding.empty();
			Unfolding<Integer> emptyUnfolding2 = Unfolding.empty();

			assertThat(emptyUnfolding1.equals(emptyUnfolding2))
					.as("Empty unfolding equality should be symmetric")
					.isEqualTo(emptyUnfolding2.equals(emptyUnfolding1));
		}

		@DisplayName("should satisfy transitivity (if x.equals(y) and y.equals(z), then x.equals(z))")
		@Test
		void shouldSatisfyTransitivity()
		{
			Unfolding<String> unfolding1 = Unfolding.of("test");
			Unfolding<String> unfolding2 = Unfolding.of("test");
			Unfolding<String> unfolding3 = Unfolding.of("test");

			boolean firstEqualsSecond = unfolding1.equals(unfolding2);
			boolean secondEqualsThird = unfolding2.equals(unfolding3);

			assertThat(firstEqualsSecond && secondEqualsThird)
					.as("Precondition: first equals second and second equals third")
					.isTrue();

			assertThat(unfolding1.equals(unfolding3))
					.as("Transitivity: if first equals second and second equals third, then first equals third")
					.isTrue();

			Unfolding<String> emptyUnfolding1 = Unfolding.empty();
			Unfolding<Integer> emptyUnfolding2 = Unfolding.empty();
			Unfolding<List<String>> emptyUnfolding3 = Unfolding.empty();

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
				Unfolding<T> first,
				Unfolding<U> second,
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
		<T, U> void shouldHandleEqualityWithDifferentValueTypesCorrectly(String description, Unfolding<T> first,
																		 Unfolding<U> second, boolean shouldBeEqual)
		{
			assertThat(first.equals(second))
					.as(description)
					.isEqualTo(shouldBeEqual);
		}

		@DisplayName("should handle equality with null values and empty unfoldings correctly")
		@Test
		void shouldHandleEqualityWithNullValuesAndEmptyUnfoldingsCorrectly()
		{
			Unfolding<String> emptyStringUnfolding = Unfolding.empty();
			Unfolding<Integer> emptyIntegerUnfolding = Unfolding.empty();
			Unfolding<List<String>> emptyListUnfolding = Unfolding.empty();

			assertThat(emptyStringUnfolding.equals(emptyIntegerUnfolding))
					.as("Empty unfoldings of different types should be equal")
					.isTrue();

			assertThat(emptyIntegerUnfolding.equals(emptyListUnfolding))
					.as("Empty unfoldings of different types should be equal")
					.isTrue();

			Unfolding<String> nullStringUnfolding = Unfolding.of(null);
			Unfolding<Integer> nullIntegerUnfolding = Unfolding.of(null);

			assertThat(nullStringUnfolding.equals(emptyStringUnfolding))
					.as("Unfolding.of(null) should equal Unfolding.empty()")
					.isTrue();

			assertThat(nullIntegerUnfolding.equals(emptyIntegerUnfolding))
					.as("Unfolding.of(null) should equal Unfolding.empty()")
					.isTrue();

			assertThat(nullStringUnfolding.equals(nullIntegerUnfolding))
					.as("Unfolding.of(null) instances should be equal regardless of type")
					.isTrue();

			Unfolding<String> presentUnfolding = Unfolding.of("test");

			assertThat(emptyStringUnfolding.equals(presentUnfolding))
					.as("Empty unfolding should not equal present unfolding")
					.isFalse();

			assertThat(nullStringUnfolding.equals(presentUnfolding))
					.as("Unfolding.of(null) should not equal present unfolding")
					.isFalse();

			Unfolding<String> emptyStringValueUnfolding = Unfolding.of("");

			assertThat(emptyStringValueUnfolding.equals(emptyStringUnfolding))
					.as("Unfolding with empty string should not equal empty unfolding")
					.isFalse();

			assertThat(emptyStringValueUnfolding.equals(nullStringUnfolding))
					.as("Unfolding with empty string should not equal Unfolding.of(null)")
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

		private static Stream<Arguments> hashCodeConsistencyTestCases()
		{
			Unfolding<String> unfolding1 = Unfolding.of("test");
			Unfolding<String> unfolding2 = Unfolding.of("test");

			Unfolding<String> unfolding3 = Unfolding.of("different");

			Unfolding<String> emptyUnfolding1 = Unfolding.empty();
			Unfolding<Integer> emptyUnfolding2 = Unfolding.empty();

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
			Unfolding<String> stringUnfolding1 = Unfolding.of("test");
			Unfolding<String> stringUnfolding2 = Unfolding.of("test");
			Unfolding<String> differentStringUnfolding = Unfolding.of("different");

			Unfolding<Integer> intUnfolding1 = Unfolding.of(42);
			Unfolding<Integer> intUnfolding2 = Unfolding.of(42);
			Unfolding<Integer> differentIntUnfolding = Unfolding.of(100);

			List<String> list1 = new ArrayList<>();
			list1.add("item");
			List<String> list2 = new ArrayList<>();
			list2.add("item");
			List<String> differentList = new ArrayList<>();
			differentList.add("different");

			Unfolding<List<String>> listUnfolding1 = Unfolding.of(list1);
			Unfolding<List<String>> listUnfolding2 = Unfolding.of(list2);
			Unfolding<List<String>> differentListUnfolding = Unfolding.of(differentList);

			Unfolding<Integer> integerUnfolding = Unfolding.of(123);
			Unfolding<String> stringNumberUnfolding = Unfolding.of("123");

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

		private record HashCodeTestCase<T, U>(String description, Unfolding<T> first, Unfolding<U> second,
											  boolean shouldBeEqual)
		{
		}

		private record EqualityTestCase<T, U>(String description, Unfolding<T> first, Unfolding<U> second,
											  boolean shouldBeEqual)
		{
		}
	}
}