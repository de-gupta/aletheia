package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade#classify")
final class CascadeClassifyTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("groupsByKeyCases")
		@DisplayName("groups elements by key — each key maps to a list of matching elements")
		void groupsByKey(final String as, final ClassifyCase tc)
		{
			assertThat(tc.source().classify(tc.essence()))
					.as(as)
					.isEqualTo(tc.expected());
		}

		@Test
		@DisplayName("single-element groups when all keys are distinct")
		void singleElementGroupsWhenAllKeysDistinct()
		{
			final Map<Integer, List<String>> result = Cascade.beckon("a", "bb", "ccc")
			                                                 .classify(String::length);

			assertThat(result)
					.as("each string has unique length — each group has one element")
					.containsEntry(1, List.of("a"))
					.containsEntry(2, List.of("bb"))
					.containsEntry(3, List.of("ccc"));
		}

		@Test
		@DisplayName("single group when all elements share the same key")
		void singleGroupWhenAllShareSameKey()
		{
			final Map<Integer, List<String>> result = Cascade.beckon("a", "b", "c")
			                                                 .classify(String::length);

			assertThat(result)
					.as("all length-1 strings in one group")
					.containsOnlyKeys(1)
					.containsEntry(1, List.of("a", "b", "c"));
		}

		@Test
		@DisplayName("encounter order preserved within each group")
		void encounterOrderPreservedWithinGroup()
		{
			final Map<Integer, List<Integer>> result = Cascade.beckon(3, 1, 4, 1, 5, 9, 2, 6)
			                                                  .classify(n -> n % 2);

			assertThat(result.get(1))
					.as("odd numbers in encounter order")
					.containsExactly(3, 1, 1, 5, 9);

			assertThat(result.get(0))
					.as("even numbers in encounter order")
					.containsExactly(4, 2, 6);
		}

		@Test
		@DisplayName("result is a Map — keys are unique")
		void resultIsMap()
		{
			assertThat(Cascade.beckon(1, 2, 3).classify(n -> n % 2))
					.as("result is a Map with at most two keys")
					.isInstanceOf(Map.class)
					.containsOnlyKeys(0, 1);
		}

		@Test
		@DisplayName("groupBy() delegates to classify()")
		void groupByDelegatesToClassify()
		{
			final Cascade<Integer> source = Cascade.beckon(1, 2, 3, 4, 5, 6);
			final Function<Integer, Integer> byParity = n -> n % 2;

			assertThat(source.groupBy(byParity))
					.as("groupBy() must equal classify()")
					.isEqualTo(source.classify(byParity));
		}

		@Test
		@DisplayName("throws when essence is null")
		void throwsWhenEssenceIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).classify(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("essence may not be null");
		}

		@Test
		@DisplayName("groups strings by first character")
		void groupsStringsByFirstCharacter()
		{
			assertThat(Cascade.beckon("apple", "ant", "banana", "avocado", "berry")
			                  .classify(s -> s.charAt(0)))
					.as("grouped by first character")
					.containsEntry('a', List.of("apple", "ant", "avocado"))
					.containsEntry('b', List.of("banana", "berry"));
		}

		private static Stream<Arguments> groupsByKeyCases()
		{
			return Stream.of(
					new ClassifyCase("group integers by parity",
							Cascade.beckon(1, 2, 3, 4, 5),
							n -> n % 2,
							Map.of(0, List.of(2, 4), 1, List.of(1, 3, 5))),
					new ClassifyCase("group integers by sign",
							Cascade.beckon(-2, 1, -3, 4, 0),
							Integer::signum,
							Map.of(-1, List.of(-2, -3), 0, List.of(0), 1, List.of(1, 4)))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record ClassifyCase(String as, Cascade<Integer> source,
		                            Function<Integer, Integer> essence, Map<Integer, List<Integer>> expected)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns empty Map — does not throw")
		void returnsEmptyMap()
		{
			assertThat(Cascade.<Integer>abyss().classify(n -> n % 2))
					.as("absent cascade gives empty map without throwing")
					.isEmpty();
		}
	}
}