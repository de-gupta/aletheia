package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade evolve tests")
final class CascadeEvolveTest
{
	@Nested
	@DisplayName("Aspects of selective evolution")
	final class SelectiveEvolutionTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("evolutionCases")
		@DisplayName("should evolve only judged elements")
		void shouldEvolveOnlyJudgedElements(final String as, final EvolutionCase tc)
		{
			var result = tc.source().evolve(tc.judgement(), tc.evolution());

			assertThat(result.summon())
					.as("evolve should admit only judged outcomes for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> evolutionCases()
		{
			return Stream.of(
					new EvolutionCase(
							"Only even runes cross the gate",
							Cascade.beckon(1, 2, 3, 4),
							n -> n % 2 == 0,
							n -> "even-" + n,
							List.of("even-2", "even-4")
					),
					new EvolutionCase(
							"Null evolutions are filtered from brook",
							Cascade.beckon(3, 4, 5),
							_ -> true,
							n -> n == 4 ? null : "v" + n,
							List.of("v3", "v5")
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record EvolutionCase(String as, Cascade<Integer> source,
		                             Predicate<Integer> judgement,
		                             Function<Integer, String> evolution,
		                             List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of void source and guards")
	final class GuardTests
	{
		@Test
		@DisplayName("should stay nadir when source is abyss")
		void shouldStayNadirWhenSourceIsAbyss()
		{
			assertThat(Cascade.<Integer>abyss().evolve(_ -> true, Object::toString)).isSameAs(Cascade.abyss());
		}

		@Test
		@DisplayName("should reject null judgement on brook")
		void shouldRejectNullJudgementOnBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).evolve(null, Object::toString))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		@Test
		@DisplayName("should reject null evolution on brook")
		void shouldRejectNullEvolutionOnBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).evolve(_ -> true, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("evolution may not be null");
		}
	}
}