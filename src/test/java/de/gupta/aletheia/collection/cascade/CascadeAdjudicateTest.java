package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cascade adjudicate tests")
final class CascadeAdjudicateTest
{
	@Nested
	@DisplayName("Aspects of judgement outcomes")
	final class JudgementOutcomeTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("judgementShapes")
		@DisplayName("should obey judgement in shaping cascade")
		void shouldObeyJudgementInShapingCascade(final String as, final AdjudicationCase tc)
		{
			var cascade = Cascade.adjudicate(tc.elements(), tc.judgement());

			assertThat(cascade)
					.as("adjudicate as %s should yield %s", as, tc.expectedImpl().getSimpleName())
					.isInstanceOf(tc.expectedImpl());

			assertThat(cascade.supple())
					.as("supple should align for %s", as)
					.isEqualTo(tc.expectedSupple());
			assertThat(cascade.sterile())
					.as("sterile should align for %s", as)
					.isEqualTo(tc.expectedSterile());

			if (!tc.expectedElements().isEmpty())
			{
				assertThat(cascade.summon())
						.as("summoned sequence should match admitted elements for %s", as)
						.containsExactlyElementsOf(tc.expectedElements());
			}
		}

		private static Stream<Arguments> judgementShapes()
		{
			return Stream.of(
					new AdjudicationCase("When judgement blesses, procession continues",
							List.of("helios", "selene"), true,
							List.of("helios", "selene"), Brook.class, true, false),
					new AdjudicationCase("When judgement rejects, all returns to abyss",
							List.of("helios", "selene"), false,
							List.of(), Nadir.class, false, true),
					new AdjudicationCase("Even blessed emptiness is still abyss",
							List.of(), true, List.of(), Nadir.class, false, true),
					new AdjudicationCase("Null-bearing offerings can still pass judgement",
							Arrays.asList("atlas", null), true,
							Arrays.asList("atlas", null), Brook.class, true, false)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record AdjudicationCase(String as, Collection<Object> elements, boolean judgement,
		                                List<Object> expectedElements,
		                                Class<?> expectedImpl, boolean expectedSupple, boolean expectedSterile)
		{
		}
	}
}