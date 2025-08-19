package de.gupta.aletheia.functional;

import de.gupta.aletheia.forge.OrderedPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
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

	@Nested
	final class CleaveMapTest
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("judgmentScenarios")
		@DisplayName("cleave(Map, punishment) — applies correct transformation or returns punishment")
		<T, R> void cleavesCorrectly(final String description,
									 final T hero,
									 final SortedMap<Predicate<? super T>, Function<? super T, R>> judgments,
									 final R punishment,
									 final R expected)
		{
			assertThat(Unfolding.beckon(hero).cleave(judgments, punishment))
					.as("cleave(judgments, punishment) should return expected result")
					.isEqualTo(expected);
		}

		@Test
		@DisplayName("cleave(Map, punishment) — throws when judgments map is null")
		void throwsIfJudgmentsIsNull()
		{
			assertThatThrownBy(() -> Unfolding.beckon("hero").cleave(null, "Fate"))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgments may not be null");
		}

		@Test
		@DisplayName("cleave(Map, punishment) — throws when punishment is null")
		void throwsIfPunishmentIsNull()
		{
			SortedMap<Predicate<? super String>, Function<? super String, String>> judgments = new TreeMap<>();

			assertThatThrownBy(() -> Unfolding.beckon("hero").cleave(judgments, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("punishment may not be null");
		}

		@Test
		@DisplayName("cleave(Map, punishment) — throws when a predicate inside map is null")
		void throwsIfAnyPredicateIsNull()
		{
			// Build via HashMap to avoid Map.of()'s null prohibition, then wrap with TreeMap
			OrderedPredicate<String> nullPred = OrderedPredicate.of(0, null);
			Map<Predicate<? super String>, Function<? super String, String>> helper = new HashMap<>();
			helper.put(nullPred, _ -> "X");
			SortedMap<Predicate<? super String>, Function<? super String, String>> judgments = new TreeMap<>(helper);

			assertThatThrownBy(() -> Unfolding.beckon("hero").cleave(judgments, "punish"))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		@DisplayName("cleave(Map, punishment) — throws when a mapper function for a matching predicate is null")
		void throwsIfAnyMapperIsNullAndPredicateMatches()
		{
			Map<Predicate<? super String>, Function<? super String, String>> helper = new HashMap<>();
			helper.put(OrderedPredicate.of(0, _ -> true), null);

			SortedMap<Predicate<? super String>, Function<? super String, String>> judgments = new TreeMap<>(helper);

			assertThatThrownBy(() -> Unfolding.beckon("hero").cleave(judgments, "punish"))
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		@DisplayName("cleave(Map, punishment) — empty unfolding throws")
		void emptyUnfoldingThrowsOnCleaveWithMap()
		{
			SortedMap<Predicate<? super String>, Function<? super String, String>> judgments = new TreeMap<>(
					Map.of(OrderedPredicate.of(0, _ -> true), _ -> "ok"));
			assertThatThrownBy(() -> Unfolding.<String>chaos().cleave(judgments, "punish"))
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		private static Stream<Arguments> judgmentScenarios()
		{
			final OrderedPredicate<String> isOdysseus = OrderedPredicate.of(0, s -> s.startsWith("Odysseus"));
			final OrderedPredicate<String> isAchilles = OrderedPredicate.of(1, s -> s.startsWith("Achilles"));
			final OrderedPredicate<String> anyNonEmpty = OrderedPredicate.of(0, s -> s != null && !s.isEmpty());
			final OrderedPredicate<String> startsWithLowerA = OrderedPredicate.of(1, s -> s.startsWith("a"));
			final OrderedPredicate<String> alwaysTrueLow = OrderedPredicate.of(99, _ -> true);

			final OrderedPredicate<Integer> negative = OrderedPredicate.of(0, n -> n < 0);
			final OrderedPredicate<Integer> divisibleBy5 = OrderedPredicate.of(1, n -> n % 5 == 0);

			final OrderedPredicate<Boolean> oracleSpeaksTruth = OrderedPredicate.of(0, b -> b);
			final OrderedPredicate<Boolean> oracleSpeaksFalsehood = OrderedPredicate.of(1, b -> !b);

			final OrderedPredicate<String> never0 = OrderedPredicate.of(0, s -> false);
			final OrderedPredicate<String> never1 = OrderedPredicate.of(1, s -> false);
			final OrderedPredicate<String> never2 = OrderedPredicate.of(2, s -> false);
			final OrderedPredicate<String> never3 = OrderedPredicate.of(3, s -> false);
			final OrderedPredicate<String> never4 = OrderedPredicate.of(4, s -> false);
			final OrderedPredicate<String> equalsZzz = OrderedPredicate.of(5, "zzz"::equals);

			final OrderedPredicate<String> order5StartsWithA = OrderedPredicate.of(5, s -> s.startsWith("A"));
			final OrderedPredicate<String> order5StartsWithB = OrderedPredicate.of(5, s -> s.startsWith("B"));

			return Stream.of(
					// Basic behavior remains covered (mythic)
					TestCase.from("Odysseus finds Penelope when his name is spoken",
							"Odysseus survives",
							Map.of(isOdysseus, _ -> "Penelope", isAchilles, _ -> "Briseis"),
							"Apollo", "Penelope"),
					TestCase.from("Achilles is bound to Briseis when his deeds are sung",
							"Achilles dies",
							Map.of(isOdysseus, _ -> "Penelope", isAchilles, _ -> "Briseis"),
							"Artemis", "Briseis"),
					TestCase.from("When neither hero answers, the Fates return the punishment",
							"Mortal",
							Map.of(isOdysseus, _ -> "Penelope", isAchilles, _ -> "Briseis"),
							"Zeus", "Zeus"),
					TestCase.from("Even Athena abides the empty oracle: punishment remains",
							"Athena",
							Map.of(),
							"Zeus", "Zeus"),

					// Edge cases (mythic flavor)
					TestCase.from("When many omens agree, the earliest order speaks first",
							"alpha",
							Map.of(anyNonEmpty, _ -> "Len>0", startsWithLowerA, _ -> "StartsWithA"),
							"Punish", "Len>0"),
//					TestCase.from("If omens share the same rank, the later inscription overwrites the former",
//							"Alpha",
//							Map.of(order5StartsWithA, _ -> "A", order5StartsWithB, _ -> "B"),
//							"Zeus", "Zeus"),
					TestCase.from("The ever-true prophecy at the tail end cannot overthrow the first sign",
							"123",
							Map.of(OrderedPredicate.of(0, s -> s.startsWith("1")), _ -> "StartsWith1", alwaysTrueLow,
									_ -> "Always"),
							"Apollo", "StartsWith1"),
					TestCase.from("Generic type: Integer champion — divisible by five wins the laurel",
							15,
							Map.of(negative, _ -> "neg", divisibleBy5, _ -> "div5"),
							"punish", "div5"),
					TestCase.from("Generic type: Boolean oracle — truth is favored in the shrine",
							true,
							Map.of(oracleSpeaksTruth, _ -> "Apollo smiles", oracleSpeaksFalsehood, _ -> "Hades scowls"),
							"Nemesis", "Apollo smiles"),
					TestCase.from("Across many false signs, only the final rune awakens the answer",
							"zzz",
							Map.of(never0, _ -> "x0", never1, _ -> "x1", never2, _ -> "x2", never3, _ -> "x3", never4,
									_ -> "x4", equalsZzz, _ -> "Zed"),
							"punish", "Zed")
			).map(tc -> Arguments.of(tc.description(), tc.hero(), tc.judgments(), tc.punishment(), tc.expected()));
		}

		private record TestCase<K, V>(String description, K hero,
									  SortedMap<Predicate<K>, Function<K, V>> judgments, V punishment,
									  V expected)
		{
			static <K, V> TestCase<K, V> from(final String description,
											  final K hero,
											  final Map<OrderedPredicate<K>, Function<K, V>> judgments,
											  final V punishment,
											  final V expected)
			{
				SortedMap<Predicate<K>, Function<K, V>> sortedJudgments = new TreeMap<>(judgments);
				return new TestCase<>(description, hero, sortedJudgments, punishment, expected);
			}
		}
	}
}