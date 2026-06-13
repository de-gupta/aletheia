package de.gupta.aletheia.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UnfoldingSmiteTest
{
	private static SequencedMap<Predicate<? super Integer>, Function<? super Integer, String>> signMap()
	{
		final LinkedHashMap<Predicate<? super Integer>, Function<? super Integer, String>> map = new LinkedHashMap<>();
		map.put(n -> n > 0, _ -> "positive");
		map.put(n -> n == 0, _ -> "zero");
		map.put(n -> n < 0, _ -> "negative");
		return map;
	}

	private static SequencedMap<Predicate<? super String>, Function<? super String, String>> vowelConsonantMap()
	{
		final LinkedHashMap<Predicate<? super String>, Function<? super String, String>> map = new LinkedHashMap<>();
		map.put(s -> "AEIOUaeiou".indexOf(s.charAt(0)) >= 0, _ -> "vowel");
		map.put(s -> !s.isEmpty(), _ -> "consonant");
		return map;
	}

	// ── Shared map builders ───────────────────────────────────────────────────

	private record SmiteTestCase<T, R>(String description, Unfolding<T> source,
	                                   SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments,
	                                   R expected)
	{
	}

	@Nested
	@DisplayName("Tests for smite(SequencedMap, Supplier) method")
	class SmiteSequencedMapWithWrathTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should return result of first matching judgment")
		@MethodSource("smiteWithWrathMatchTestCases")
		<T, R> void testMatchReturnsResult(final String description, final Unfolding<T> source,
		                                   final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments,
		                                   final R expected)
		{
			assertThat(source.smite(judgments, () -> new IllegalStateException("no match")))
					.as("smite() for %s should return %s", source, expected)
					.isEqualTo(expected);
		}

		@ParameterizedTest(name = "{0}")
		@DisplayName("should respect insertion order — first predicate in the map wins")
		@MethodSource("smiteWithWrathOrderingTestCases")
		<T, R> void testInsertionOrderRespected(final String description, final Unfolding<T> source,
		                                        final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments,
		                                        final R expected)
		{
			assertThat(source.smite(judgments, () -> new IllegalStateException("no match")))
					.as("smite() ordering for %s should return %s", source, expected)
					.isEqualTo(expected);
		}

		@Test
		@DisplayName("should throw wrath when no judgment matches")
		void testThrowsWrathOnNoMatch()
		{
			final SequencedMap<Predicate<? super Integer>, Function<? super Integer, String>> judgments =
					new LinkedHashMap<>();
			judgments.put(n -> n > 100, _ -> "large");

			assertThatThrownBy(
					() -> Unfolding.beckon(5).smite(judgments, () -> new IllegalArgumentException("no match")))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("no match");
		}

		@Test
		@DisplayName("should throw EmptyUnfoldingException when source is empty")
		void testThrowsOnEmptySource()
		{
			final SequencedMap<Predicate<? super Integer>, Function<? super Integer, String>> judgments =
					new LinkedHashMap<>();
			judgments.put(n -> n > 0, _ -> "positive");

			assertThatThrownBy(() -> Unfolding.<Integer>chaos().smite(judgments, IllegalStateException::new))
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		@Test
		@DisplayName("should throw NullPointerException when judgments is null")
		void testNullJudgments()
		{
			assertThatThrownBy(() -> Unfolding.beckon("x").smite(
					(SequencedMap<Predicate<? super String>, Function<? super String, String>>) null,
					IllegalStateException::new))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgments may not be null");
		}

		@Test
		@DisplayName("should throw NullPointerException when wrath is null")
		void testNullWrath()
		{
			final SequencedMap<Predicate<? super String>, Function<? super String, String>> judgments =
					new LinkedHashMap<>();
			judgments.put(s -> !s.isEmpty(), Function.identity());

			assertThatThrownBy(() -> Unfolding.beckon("x").smite(judgments, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("wrath may not be null");
		}

		private static Stream<Arguments> smiteWithWrathMatchTestCases()
		{
			return Stream.of(
					new SmiteTestCase<>("positive integer", Unfolding.beckon(7), signMap(), "positive"),
					new SmiteTestCase<>("zero", Unfolding.beckon(0), signMap(), "zero"),
					new SmiteTestCase<>("negative integer", Unfolding.beckon(-3), signMap(), "negative"),
					new SmiteTestCase<>("string starts with vowel", Unfolding.beckon("Athena"), vowelConsonantMap(),
							"vowel"),
					new SmiteTestCase<>("string starts with consonant", Unfolding.beckon("Poseidon"),
							vowelConsonantMap(), "consonant")
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.judgments(), tc.expected()));
		}

		private static Stream<Arguments> smiteWithWrathOrderingTestCases()
		{
			final SequencedMap<Predicate<? super Integer>, Function<? super Integer, String>> broaderFirst =
					new LinkedHashMap<>();
			broaderFirst.put(n -> n > 0, _ -> "positive");
			broaderFirst.put(n -> n > 100, _ -> "large");

			final SequencedMap<Predicate<? super Integer>, Function<? super Integer, String>> narrowerFirst =
					new LinkedHashMap<>();
			narrowerFirst.put(n -> n > 100, _ -> "large");
			narrowerFirst.put(n -> n > 0, _ -> "positive");

			final SequencedMap<Predicate<? super String>, Function<? super String, String>> prefixFirst =
					new LinkedHashMap<>();
			prefixFirst.put(s -> s.startsWith("A"), _ -> "starts with A");
			prefixFirst.put(s -> s.length() > 5, _ -> "long");

			final SequencedMap<Predicate<? super String>, Function<? super String, String>> lengthFirst =
					new LinkedHashMap<>();
			lengthFirst.put(s -> s.length() > 5, _ -> "long");
			lengthFirst.put(s -> s.startsWith("A"), _ -> "starts with A");

			return Stream.of(
					new SmiteTestCase<>("broader predicate first — first wins", Unfolding.beckon(200), broaderFirst,
							"positive"),
					new SmiteTestCase<>("narrower predicate first — first wins", Unfolding.beckon(200), narrowerFirst,
							"large"),
					new SmiteTestCase<>("string prefix check before length check", Unfolding.beckon("Arachne"),
							prefixFirst, "starts with A"),
					new SmiteTestCase<>("string length check before prefix check", Unfolding.beckon("Arachne"),
							lengthFirst, "long")
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.judgments(), tc.expected()));
		}
	}

	@Nested
	@DisplayName("Tests for smite(SequencedMap) method — exhaustive map, built-in exception")
	class SmiteSequencedMapExhaustiveTests
	{
		@ParameterizedTest(name = "{0}")
		@DisplayName("should return result when a judgment matches")
		@MethodSource("smiteExhaustiveMatchTestCases")
		<T, R> void testMatchReturnsResult(final String description, final Unfolding<T> source,
		                                   final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments,
		                                   final R expected)
		{
			assertThat(source.smite(judgments))
					.as("smite() for %s should return %s", source, expected)
					.isEqualTo(expected);
		}

		@ParameterizedTest(name = "{0}")
		@DisplayName("should respect insertion order — first predicate in the map wins")
		@MethodSource("smiteExhaustiveOrderingTestCases")
		<T, R> void testInsertionOrderRespected(final String description, final Unfolding<T> source,
		                                        final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments,
		                                        final R expected)
		{
			assertThat(source.smite(judgments))
					.as("smite() ordering for %s should return %s", source, expected)
					.isEqualTo(expected);
		}

		@Test
		@DisplayName("should throw EmptyUnfoldingException with exhaustiveness message when no judgment matches")
		void testThrowsOnNonExhaustiveMap()
		{
			final SequencedMap<Predicate<? super Integer>, Function<? super Integer, String>> judgments =
					new LinkedHashMap<>();
			judgments.put(n -> n > 100, _ -> "large");

			assertThatThrownBy(() -> Unfolding.beckon(5).smite(judgments))
					.isInstanceOf(EmptyUnfoldingException.class)
					.hasMessageContaining("exhaustive");
		}

		@Test
		@DisplayName("should throw EmptyUnfoldingException when source is empty")
		void testThrowsOnEmptySource()
		{
			final SequencedMap<Predicate<? super Integer>, Function<? super Integer, String>> judgments =
					new LinkedHashMap<>();
			judgments.put(n -> n > 0, _ -> "positive");

			assertThatThrownBy(() -> Unfolding.<Integer>chaos().smite(judgments))
					.isInstanceOf(EmptyUnfoldingException.class);
		}

		@Test
		@DisplayName("should throw NullPointerException when judgments is null")
		void testNullJudgments()
		{
			assertThatThrownBy(() -> Unfolding.beckon("x").smite(
					(SequencedMap<Predicate<? super String>, Function<? super String, String>>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("judgments may not be null");
		}

		private static Stream<Arguments> smiteExhaustiveMatchTestCases()
		{
			return Stream.of(
					new SmiteTestCase<>("positive integer", Unfolding.beckon(7), signMap(), "positive"),
					new SmiteTestCase<>("zero", Unfolding.beckon(0), signMap(), "zero"),
					new SmiteTestCase<>("negative integer", Unfolding.beckon(-3), signMap(), "negative"),
					new SmiteTestCase<>("string starts with vowel", Unfolding.beckon("Athena"), vowelConsonantMap(),
							"vowel"),
					new SmiteTestCase<>("string starts with consonant", Unfolding.beckon("Poseidon"),
							vowelConsonantMap(), "consonant")
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.judgments(), tc.expected()));
		}

		private static Stream<Arguments> smiteExhaustiveOrderingTestCases()
		{
			final SequencedMap<Predicate<? super Integer>, Function<? super Integer, String>> broaderFirst =
					new LinkedHashMap<>();
			broaderFirst.put(n -> n > 0, _ -> "positive");
			broaderFirst.put(n -> n > 100, _ -> "large");

			final SequencedMap<Predicate<? super Integer>, Function<? super Integer, String>> narrowerFirst =
					new LinkedHashMap<>();
			narrowerFirst.put(n -> n > 100, _ -> "large");
			narrowerFirst.put(n -> n > 0, _ -> "positive");

			return Stream.of(
					new SmiteTestCase<>("broader predicate first — first wins", Unfolding.beckon(200), broaderFirst,
							"positive"),
					new SmiteTestCase<>("narrower predicate first — first wins", Unfolding.beckon(200), narrowerFirst,
							"large")
			).map(tc -> Arguments.of(tc.description(), tc.source(), tc.judgments(), tc.expected()));
		}
	}
}