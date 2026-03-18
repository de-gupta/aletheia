package de.gupta.aletheia.collection.folding;

import de.gupta.aletheia.functional.Unfolding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Loom: The art of weaving and forging")
final class LoomTest
{
	@Nested
	@DisplayName("Weave: Combining elements from a starting point")
	final class WeaveTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("weaveTestCases")
		@DisplayName("Should weave elements correctly")
		void shouldWeaveElements(final String description, final List<Integer> elements, final Integer initial,
								 final BiFunction<Integer, Integer, Integer> weaver, final Integer expected)
		{
			Loom<Integer> loom = Loom.harness(elements);
			Integer result = loom.weave(initial, weaver);
			assertThat(result).isEqualTo(expected);
		}

		private static Stream<Arguments> weaveTestCases()
		{
			return Stream.of(
					Arguments.of("Summing integers", Arrays.asList(1, 2, 3, 4, 5), 0,
							(BiFunction<Integer, Integer, Integer>) Integer::sum, 15),
					Arguments.of("Multiplying integers", Arrays.asList(1, 2, 3, 4), 1,
							(BiFunction<Integer, Integer, Integer>) (a, b) -> a * b, 24),
					Arguments.of("Empty list returns initial", Collections.emptyList(), 42,
							(BiFunction<Integer, Integer, Integer>) Integer::sum, 42),
					Arguments.of("String concatenation", Arrays.asList(1, 2, 3), 0,
							(BiFunction<Integer, Integer, Integer>) (acc, e) -> acc * 10 + e, 123)
			);
		}

		@Test
		@DisplayName("Should work with concrete subtypes (covariance)")
		void shouldWorkWithSubtypes()
		{
			List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);
			Loom<Number> loom = Loom.harness(doubles);

			Number sum = loom.weave(0.0, (acc, n) -> acc.doubleValue() + n.doubleValue());
			assertThat(sum.doubleValue()).isEqualTo(6.6);
		}
	}

	@Nested
	@DisplayName("Forge: Reducing elements into a single essence")
	final class ForgeTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("forgeTestCases")
		@DisplayName("Should forge elements correctly")
		void shouldForgeElements(final String description, final List<Integer> elements,
								 final BinaryOperator<Integer> operator, final Integer expected)
		{
			Loom<Integer> loom = Loom.harness(elements);
			Unfolding<Integer> result = loom.forge(operator);
			assertThat(result.summon()).isEqualTo(expected);
		}

		@Test
		@DisplayName("Should return chaos when forging empty loom")
		void shouldReturnChaosForEmptyLoom()
		{
			Loom<Integer> loom = Loom.harness(Collections.emptyList());
			Unfolding<Integer> result = loom.forge(Integer::sum);
			assertThat(result.sterile()).isTrue();
		}

		@Test
		@DisplayName("Should work with BinaryOperator of supertype")
		void shouldWorkWithSupertypeOperator()
		{
			List<Integer> elements = Arrays.asList(1, 2, 3);
			Loom<Integer> loom = Loom.harness(elements);

			BinaryOperator<Number> sumOperator = (n1, n2) -> n1.intValue() + n2.intValue();
			Unfolding<Integer> result = loom.forge(sumOperator);

			assertThat(result.summon()).isEqualTo(6);
		}

		private static Stream<Arguments> forgeTestCases()
		{
			return Stream.of(
					Arguments.of("Summing integers", Arrays.asList(1, 2, 3, 4, 5),
							(BinaryOperator<Integer>) Integer::sum, 15),
					Arguments.of("Multiplying integers", Arrays.asList(1, 2, 3, 4),
							(BinaryOperator<Integer>) (a, b) -> a * b, 24),
					Arguments.of("Single element returns itself", Collections.singletonList(42),
							(BinaryOperator<Integer>) Integer::sum, 42)
			);
		}
	}

	@Nested
	@DisplayName("Narrative test cases — Loom tales")
	final class LoomNarratives
	{
		@Test
		@DisplayName("The Web of Fate: Weaving a story from threads")
		void webOfFate()
		{
			List<String> threads = Arrays.asList("Clotho", "Lachesis", "Atropos");
			Loom<String> loom = Loom.harness(threads);

			String tapestry = loom.weave("The Fates: ", (acc, thread) -> acc + thread + ", ");
			assertThat(tapestry).isEqualTo("The Fates: Clotho, Lachesis, Atropos, ");
		}

		@Test
		@DisplayName("The Mjölnir's Forge: Creating power from base metals")
		void mjolnirForge()
		{
			List<Integer> metals = Arrays.asList(10, 20, 30, 40);
			Loom<Integer> loom = Loom.harness(metals);

			Unfolding<Integer> hammerPower = loom.forge(Integer::sum);
			assertThat(hammerPower.summon()).isEqualTo(100);
		}
	}
}