package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UnfoldingConjoinTests
{
	@Nested
	@DisplayName("Tests for emanate() method")
	class EmanateTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should emanate correct values when everything is present")
		@MethodSource("emanateTestCases")
		<T, U, R> void testEmanate(final String description, final Unfolding<T> source, final Function<T, U> marriage,
								   final BiFunction<T, U, R> conjugation, final Unfolding<R> expectedResult)
		{
			assertThat(source.emanate(marriage, conjugation))
					.as("emanate() for %s should result in %s", source, expectedResult)
					.isEqualTo(expectedResult);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("emptyMarriageTestCases")
		@DisplayName("should return empty unfolding when marriage produces empty consort")
		<T, U, R> void testEmanateWithEmptyConsort(final String description, final Unfolding<T> source,
												   final BiFunction<T, U, R> conjugation)
		{
			final Function<T, U> marriage = _ -> null;
			assertThat(source.emanate(marriage, conjugation))
					.as("emanate() for %s should result in %s", source, Unfolding.chaos())
					.isEqualTo(Unfolding.chaos());
		}

		@Test
		@DisplayName("should handle null marriage function gracefully")
		void testEmanateWithNullMarriage()
		{
			final Unfolding<String> source = Unfolding.beckon("test");
			final BiFunction<String, String, String> conjugation = String::concat;

			assertThatThrownBy(() -> source.emanate(null, conjugation))
					.as("emanate() with null marriage should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("marriage may not be null");
		}

		@Test
		@DisplayName("should handle null conjugation function gracefully")
		void testEmanateWithNullConjugation()
		{
			final Unfolding<String> source = Unfolding.beckon("test");
			final Function<String, String> marriage = String::toUpperCase;

			assertThatThrownBy(() -> source.emanate(marriage, null))
					.as("emanate() with null conjugation should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("conjugation may not be null");
		}

		@Test
		@DisplayName("should handle both null arguments gracefully")
		void testEmanateWithBothNullArguments()
		{
			final Unfolding<String> source = Unfolding.beckon("test");

			assertThatThrownBy(() -> source.emanate(null, null))
					.as("emanate() with both null arguments should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("marriage may not be null");
		}

		@Test
		@DisplayName("should handle null source with valid arguments")
		void testEmanateWithNullSource()
		{
			final Unfolding<String> source = Unfolding.chaos();
			final Function<String, String> marriage = String::toUpperCase;
			final BiFunction<String, String, String> conjugation = String::concat;

			assertThat(source.emanate(marriage, conjugation))
					.as("emanate() with null source should return chaos")
					.isEqualTo(Unfolding.chaos());
		}

		private static Stream<Arguments> emptyMarriageTestCases()
		{
			return Stream.of(
					new EmptyMarriageTestCase<>("String source", Unfolding.beckon("Hello"), (_, _) -> " World"),
					new EmptyMarriageTestCase<>("Integer source", Unfolding.beckon(1), Integer::sum),
					new EmptyMarriageTestCase<>("Double source", Unfolding.beckon(1.0), (x, y) -> x * y)
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.conjugation()));
		}

		private static Stream<Arguments> emanateTestCases()
		{
			return Stream.of(
					// Basic arithmetic operations
					new EmanateTestCase<>("Integer addition marriage", Unfolding.beckon(1), x -> x + 1, Integer::sum,
							Unfolding.beckon(3)),
					new EmanateTestCase<>("Integer multiplication marriage", Unfolding.beckon(1), x -> x + 1,
							(x, y) -> x * y,
							Unfolding.beckon(2)),
					new EmanateTestCase<>("Integer subtraction marriage", Unfolding.beckon(1), x -> x + 1,
							(x, y) -> x - y,
							Unfolding.beckon(-1)),
					new EmanateTestCase<>("Double division marriage", Unfolding.beckon(1.0), x -> x + 1,
							(x, y) -> x / y,
							Unfolding.beckon(0.5)),
					new EmanateTestCase<>("Integer division marriage", Unfolding.beckon(1), x -> x + 1,
							(x, y) -> x / y,
							Unfolding.beckon(0)),

					// String operations
					new EmanateTestCase<>("String concatenation marriage", Unfolding.beckon("Hello"), _ -> " World",
							String::concat,
							Unfolding.beckon("Hello World")),
					new EmanateTestCase<>("String trim marriage", Unfolding.beckon(" Hello"), _ -> " World ",
							(u, v) -> u.concat(v).trim(),
							Unfolding.beckon("Hello World")),
					new EmanateTestCase<>("String uppercase marriage", Unfolding.beckon("hello"), String::toUpperCase,
							(original, transformed) -> original + " -> " + transformed,
							Unfolding.beckon("hello -> HELLO")),
					new EmanateTestCase<>("String length marriage", Unfolding.beckon("test"), String::length,
							(str, len) -> str + " has " + len + " characters",
							Unfolding.beckon("test has 4 characters")),

					// Edge cases with boundaries
					new EmanateTestCase<>("Zero value marriage", Unfolding.beckon(0), x -> x * 2, Integer::sum,
							Unfolding.beckon(0)),
					new EmanateTestCase<>("Negative number marriage", Unfolding.beckon(-5), Math::abs,
							Integer::sum,
							Unfolding.beckon(0)),
					new EmanateTestCase<>("Large number marriage", Unfolding.beckon(Integer.MAX_VALUE), _ -> 0,
							Integer::sum,
							Unfolding.beckon(Integer.MAX_VALUE)),
					new EmanateTestCase<>("Small number marriage", Unfolding.beckon(Integer.MIN_VALUE), _ -> 0,
							Integer::sum,
							Unfolding.beckon(Integer.MIN_VALUE)),

					// BigDecimal and BigInteger operations
					new EmanateTestCase<>("BigInteger marriage", Unfolding.beckon(BigInteger.valueOf(100)),
							x -> x.multiply(BigInteger.TEN), BigInteger::add,
							Unfolding.beckon(BigInteger.valueOf(1100))),
					new EmanateTestCase<>("BigDecimal marriage", Unfolding.beckon(new BigDecimal("10.5")),
							x -> x.multiply(new BigDecimal("2")), BigDecimal::add,
							Unfolding.beckon(new BigDecimal("31.5"))),

					// Boolean operations
					new EmanateTestCase<>("Boolean negation marriage", Unfolding.beckon(true), x -> !x,
							(original, transformed) -> original && transformed,
							Unfolding.beckon(false)),
					new EmanateTestCase<>("Boolean OR marriage", Unfolding.beckon(false), _ -> true,
							(original, transformed) -> original || transformed,
							Unfolding.beckon(true)),

					// List operations
					new EmanateTestCase<>("List size marriage", Unfolding.beckon(List.of(1, 2, 3)), List::size,
							(list, size) -> "List of size " + size + " contains " + list,
							Unfolding.beckon("List of size 3 contains [1, 2, 3]")),
					new EmanateTestCase<>("Empty list marriage", Unfolding.beckon(List.<String>of()), List::isEmpty,
							(_, isEmpty) -> isEmpty ? "Empty list" : "Non-empty list",
							Unfolding.beckon("Empty list")),

					// Character operations
					new EmanateTestCase<>("Character to ASCII marriage", Unfolding.beckon('A'), x -> (int) x,
							(ch, ascii) -> ch + " = " + ascii,
							Unfolding.beckon("A = 65")),

					// Complex transformations
					new EmanateTestCase<>("Complex string transformation", Unfolding.beckon("  Hello World  "),
							x -> x.trim().split(" "),
							(original, words) -> original.length() + " chars -> " + words.length + " words",
							Unfolding.beckon("15 chars -> 2 words")),

					new EmanateTestCase<>("Identity marriage", Unfolding.beckon("test"), Function.identity(),
							(original, same) -> original.equals(same) ? "Same" : "Different",
							Unfolding.beckon("Same")),

					// Type conversion marriage
					new EmanateTestCase<>("Integer to string marriage", Unfolding.beckon(42), Object::toString,
							(num, str) -> "Number " + num + " as string '" + str + "'",
							Unfolding.beckon("Number 42 as string '42'"))
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.marriage(), tc.conjugation(),
					tc.expectedResult()));
		}

		private record EmptyMarriageTestCase<T, R>(String description, Unfolding<T> source,
												   BiFunction<T, T, R> conjugation)
		{
		}

		private record EmanateTestCase<T, U, R>(String description, Unfolding<T> source, Function<T, U> marriage,
												BiFunction<T, U, R> conjugation, Unfolding<R> expectedResult)
		{
		}
	}

	@Nested
	@DisplayName("Tests for sanctify() method")
	class SanctifyTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should sanctify correct values when judgment passes")
		@MethodSource("sanctifyTestCases")
		<T, U, R> void testSanctify(final String description, final Unfolding<T> source, final Function<T, U> marriage,
									final BiFunction<T, U, R> conjugation, final Predicate<R> judgment,
									final Unfolding<R> expectedResult)
		{
			final Supplier<RuntimeException> wrath = () -> new IllegalStateException("Judgment failed");

			assertThat(source.sanctify(marriage, conjugation, judgment, wrath))
					.as("sanctify() for %s should result in %s", source, expectedResult)
					.isEqualTo(expectedResult);
		}

		@ParameterizedTest(name = "{0}")
		@DisplayName("should throw exception when judgment fails")
		@MethodSource("sanctifyJudgmentFailureTestCases")
		<T, U, R> void testSanctifyWithFailedJudgment(final String description, final Unfolding<T> source,
													  final Function<T, U> marriage,
													  final BiFunction<T, U, R> conjugation,
													  final Predicate<R> judgment, final String expectedMessage)
		{
			final Supplier<RuntimeException> wrath = () -> new IllegalArgumentException(expectedMessage);

			assertThatThrownBy(() -> source.sanctify(marriage, conjugation, judgment, wrath))
					.as("sanctify() with failed judgment for %s should throw exception", source)
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage(expectedMessage);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("sanctifyEmptyMarriageTestCases")
		@DisplayName("should throw exception when marriage produces empty consort")
		<T, U, R> void testSanctifyWithEmptyConsort(final String description, final Unfolding<T> source,
													final BiFunction<T, U, R> conjugation, final Predicate<R> judgment,
													final String expectedMessage)
		{
			final Function<T, U> marriage = _ -> null;
			final Supplier<RuntimeException> wrath = () -> new IllegalStateException(expectedMessage);

			assertThatThrownBy(() -> source.sanctify(marriage, conjugation, judgment, wrath))
					.as("sanctify() with empty marriage for %s should throw exception", source)
					.isInstanceOf(IllegalStateException.class)
					.hasMessage(expectedMessage);
		}

		@Test
		@DisplayName("should handle null marriage function gracefully")
		void testSanctifyWithNullMarriage()
		{
			final Unfolding<String> source = Unfolding.beckon("test");
			final BiFunction<String, String, String> conjugation = String::concat;
			final Predicate<String> judgment = s -> !s.isEmpty();
			final Supplier<RuntimeException> wrath = () -> new IllegalStateException("Failed");

			assertThatThrownBy(() -> source.sanctify(null, conjugation, judgment, wrath))
					.as("sanctify() with null marriage should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("marriage may not be null");
		}

		@Test
		@DisplayName("should handle null conjugation function gracefully")
		void testSanctifyWithNullConjugation()
		{
			final Unfolding<String> source = Unfolding.beckon("test");
			final Function<String, String> marriage = String::toUpperCase;
			final Predicate<String> judgment = s -> !s.isEmpty();
			final Supplier<RuntimeException> wrath = () -> new IllegalStateException("Failed");

			assertThatThrownBy(() -> source.sanctify(marriage, null, judgment, wrath))
					.as("sanctify() with null conjugation should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("conjugation may not be null");
		}

		@Test
		@DisplayName("should handle null judgment gracefully")
		void testSanctifyWithNullJudgment()
		{
			final Unfolding<String> source = Unfolding.beckon("test");
			final Function<String, String> marriage = String::toUpperCase;
			final BiFunction<String, String, String> conjugation = String::concat;
			final Supplier<RuntimeException> wrath = () -> new IllegalStateException("Failed");

			assertThatThrownBy(() -> source.sanctify(marriage, conjugation, null, wrath))
					.as("sanctify() with null judgment should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgment may not be null");
		}

		@Test
		@DisplayName("should handle null wrath supplier gracefully")
		void testSanctifyWithNullWrath()
		{
			final Unfolding<String> source = Unfolding.beckon("test");
			final Function<String, String> marriage = String::toUpperCase;
			final BiFunction<String, String, String> conjugation = String::concat;
			final Predicate<String> judgment = s -> !s.isEmpty();

			assertThatThrownBy(() -> source.sanctify(marriage, conjugation, judgment, null))
					.as("sanctify() with null wrath should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("wrath may not be null");
		}

		@Test
		@DisplayName("should handle multiple null arguments gracefully")
		void testSanctifyWithMultipleNullArguments()
		{
			final Unfolding<String> source = Unfolding.beckon("test");

			assertThatThrownBy(() -> source.sanctify(null, null, null, null))
					.as("sanctify() with multiple null arguments should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("marriage may not be null");
		}

		@Test
		@DisplayName("should handle null source with valid arguments")
		void testSanctifyWithNullSource()
		{
			final Unfolding<String> source = Unfolding.chaos();
			final Function<String, String> marriage = String::toUpperCase;
			final BiFunction<String, String, String> conjugation = String::concat;
			final Predicate<String> judgment = s -> !s.isEmpty();
			final Supplier<RuntimeException> wrath = () -> new IllegalStateException("Empty source");

			assertThatThrownBy(() -> source.sanctify(marriage, conjugation, judgment, wrath))
					.as("sanctify() with null source should throw the wrath exception")
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("Empty source");
		}

		private static Stream<Arguments> sanctifyEmptyMarriageTestCases()
		{
			return Stream.of(
					new SanctifyEmptyMarriageTestCase<>("String source", Unfolding.beckon("Hello"),
							(_, _) -> " World", s -> !s.isEmpty(), "Marriage failed"),
					new SanctifyEmptyMarriageTestCase<>("Integer source", Unfolding.beckon(1),
							Integer::sum, x -> x > 0, "Invalid result"),
					new SanctifyEmptyMarriageTestCase<>("Double source", Unfolding.beckon(1.0),
							(x, y) -> x * y, x -> x > 0.0, "Calculation failed")
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.conjugation(), tc.judgment(),
					tc.expectedMessage()));
		}

		private static Stream<Arguments> sanctifyJudgmentFailureTestCases()
		{
			return Stream.of(
					// String operations with failed judgments
					new SanctifyJudgmentFailureTestCase<>("String too short", Unfolding.beckon("Hi"),
							_ -> " World", String::concat, s -> s.length() > 10, "String too short"),
					new SanctifyJudgmentFailureTestCase<>("Empty result", Unfolding.beckon(""),
							_ -> "", String::concat, s -> !s.isEmpty(), "Result is empty"),
					new SanctifyJudgmentFailureTestCase<>("Non-uppercase result", Unfolding.beckon("hello"),
							String::toUpperCase, (orig, _) -> orig, s -> s.equals(s.toUpperCase()),
							"Not uppercase"),

					// Numeric operations with failed judgments
					new SanctifyJudgmentFailureTestCase<>("Negative result", Unfolding.beckon(1), _ -> -2,
							Integer::sum, x -> x >= 0, "Result is negative"),
					new SanctifyJudgmentFailureTestCase<>("Too large result", Unfolding.beckon(1000),
							x -> x * 1000, Integer::sum, x -> x <= 1000, "Result too large"),

					// Boolean operations with failed judgments
					new SanctifyJudgmentFailureTestCase<>("Expected true", Unfolding.beckon(false),
							_ -> true, (orig, transformed) -> orig && transformed, x -> x, "Expected true result"),

					// List operations with failed judgments
					new SanctifyJudgmentFailureTestCase<>("List too small", Unfolding.beckon(List.of(1)),
							List::size, (_, size) -> size, x -> x > 5, "List size insufficient")
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.marriage(), tc.conjugation(),
					tc.judgment(), tc.expectedMessage()));
		}

		private static Stream<Arguments> sanctifyTestCases()
		{
			return Stream.of(
					// Basic arithmetic operations with passing judgments
					new SanctifyTestCase<>("Integer addition with positive check", Unfolding.beckon(1), x -> x + 1,
							Integer::sum, x -> x > 0, Unfolding.beckon(3)),
					new SanctifyTestCase<>("Integer multiplication with non-zero check", Unfolding.beckon(2),
							x -> x + 1,
							(x, y) -> x * y, x -> x != 0, Unfolding.beckon(6)),
					new SanctifyTestCase<>("Double division with finite check", Unfolding.beckon(10.0), x -> x / 2,
							(x, y) -> x / y, Double::isFinite, Unfolding.beckon(2.0)),

					// String operations with passing judgments
					new SanctifyTestCase<>("String concatenation with length check", Unfolding.beckon("Hello"),
							_ -> " World", String::concat, s -> s.length() > 5, Unfolding.beckon("Hello World")),
					new SanctifyTestCase<>("String uppercase with case check", Unfolding.beckon("hello"),
							String::toUpperCase, (_, upper) -> upper, s -> s.equals(s.toUpperCase()),
							Unfolding.beckon("HELLO")),
					new SanctifyTestCase<>("String trim with no whitespace check", Unfolding.beckon(" test "),
							String::trim, (_, trimmed) -> trimmed, s -> !s.contains(" "),
							Unfolding.beckon("test")),

					// Edge cases with boundaries
					new SanctifyTestCase<>("Zero value with non-negative check", Unfolding.beckon(0), x -> x,
							Integer::sum, x -> x >= 0, Unfolding.beckon(0)),
					new SanctifyTestCase<>("Negative to positive with positive check", Unfolding.beckon(-5),
							Math::abs, (u, v) -> u + 2 * v, x -> x > 0, Unfolding.beckon(5)),

					// BigDecimal and BigInteger operations
					new SanctifyTestCase<>("BigInteger with range check", Unfolding.beckon(BigInteger.valueOf(100)),
							x -> x.multiply(BigInteger.TEN), BigInteger::add,
							x -> x.compareTo(BigInteger.valueOf(1000)) > 0, Unfolding.beckon(BigInteger.valueOf(1100))),
					new SanctifyTestCase<>("BigDecimal precision check", Unfolding.beckon(new BigDecimal("10.5")),
							x -> x.multiply(new BigDecimal("2")), BigDecimal::add,
							x -> x.scale() <= 1, Unfolding.beckon(new BigDecimal("31.5"))),

					// Boolean operations with logical checks
					new SanctifyTestCase<>("Boolean OR with true result", Unfolding.beckon(false), _ -> true,
							(orig, transformed) -> orig || transformed, x -> x, Unfolding.beckon(true)),
					new SanctifyTestCase<>("Boolean identity with consistency check", Unfolding.beckon(true),
							Function.identity(), (orig, same) -> orig.equals(same), x -> x,
							Unfolding.beckon(true)),

					// List operations with size checks
					new SanctifyTestCase<>("List operation with size validation", Unfolding.beckon(List.of(1, 2, 3)),
							List::size, (_, size) -> size * 2, x -> x > 0, Unfolding.beckon(6)),
					new SanctifyTestCase<>("Empty list with empty check", Unfolding.beckon(List.<String>of()),
							List::isEmpty, (_, isEmpty) -> isEmpty ? 0 : 1, x -> x == 0, Unfolding.beckon(0)),

					// Character operations
					new SanctifyTestCase<>("Character ASCII with range check", Unfolding.beckon('A'), x -> (int) x,
							(_, ascii) -> ascii, x -> x >= 65 && x <= 90, Unfolding.beckon(65)),

					// Complex transformations
					new SanctifyTestCase<>("String word count with minimum words", Unfolding.beckon("Hello World"),
							x -> x.split(" "), (_, words) -> words.length,
							x -> x >= 2, Unfolding.beckon(2)),

					// Type conversions with format checks
					new SanctifyTestCase<>("Integer to string with digit check", Unfolding.beckon(42),
							Object::toString, (_, str) -> str, s -> s.matches("\\d+"), Unfolding.beckon("42")),

					// Always true judgments (edge case)
					new SanctifyTestCase<>("Always passing judgment", Unfolding.beckon("test"), Function.identity(),
							(orig, _) -> orig, _ -> true, Unfolding.beckon("test"))
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.marriage(), tc.conjugation(),
					tc.judgment(), tc.expectedResult()));
		}

		private record SanctifyEmptyMarriageTestCase<T, R>(String description, Unfolding<T> source,
														   BiFunction<T, T, R> conjugation, Predicate<R> judgment,
														   String expectedMessage)
		{
		}

		private record SanctifyJudgmentFailureTestCase<T, U, R>(String description, Unfolding<T> source,
																Function<T, U> marriage,
																BiFunction<T, U, R> conjugation,
																Predicate<R> judgment, String expectedMessage)
		{
		}

		private record SanctifyTestCase<T, U, R>(String description, Unfolding<T> source, Function<T, U> marriage,
												 BiFunction<T, U, R> conjugation, Predicate<R> judgment,
												 Unfolding<R> expectedResult)
		{
		}
	}
}