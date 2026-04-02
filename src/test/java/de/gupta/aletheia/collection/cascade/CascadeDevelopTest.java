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

@DisplayName("Cascade develop tests")
final class CascadeDevelopTest
{
	@Nested
	@DisplayName("Aspects of conditional self-transformation")
	final class ConditionalTransformationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("developCases")
		@DisplayName("should only develop elements that pass judgement")
		void shouldOnlyDevelopElementsThatPassJudgement(final String as, final DevelopCase tc)
		{
			var result = tc.source().develop(tc.judgement(), tc.development());

			assertThat(result.summon())
					.as("develop should honor judgement for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> developCases()
		{
			return Stream.of(
					new DevelopCase(
							"Even numbers ascend while odd remain as they are",
							Cascade.beckon(1, 2, 3, 4),
							n -> n % 2 == 0,
							n -> n * 10,
							List.of(1, 20, 3, 40)
					),
					new DevelopCase(
							"When no element is judged, the stream remains unchanged",
							Cascade.beckon(5, 6),
							_ -> false,
							n -> n + 100,
							List.of(5, 6)
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record DevelopCase(String as, Cascade<Integer> source,
		                           Predicate<Integer> judgement,
		                           Function<Integer, Integer> development,
		                           List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of empty source and null guards")
	final class GuardTests
	{
		@Test
		@DisplayName("should stay nadir for empty source")
		void shouldStayNadirForEmptySource()
		{
			assertThat(Cascade.<Integer>abyss().develop(_ -> true, n -> n + 1)).isSameAs(Cascade.abyss());
		}

		@Test
		@DisplayName("should reject null judgement on brook")
		void shouldRejectNullJudgementOnBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).develop(null, n -> n))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("judgement may not be null");
		}

		@Test
		@DisplayName("should reject null development on brook")
		void shouldRejectNullDevelopmentOnBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).develop(_ -> true, null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("development may not be null");
		}
	}
}