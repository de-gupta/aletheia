package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade ascend tests")
final class CascadeAscendTest
{
	@Nested
	@DisplayName("Aspects of ascension levels")
	final class AscensionLevelTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("ascendCases")
		@DisplayName("should ascend each element per given levels")
		void shouldAscendEachElementPerGivenLevels(final String as, final AscendCase tc)
		{
			var result = tc.source().ascend(tc.ascension(), tc.levels());

			assertThat(result.summon())
					.as("ascend should honor levels for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> ascendCases()
		{
			return Stream.of(
					new AscendCase(
							"Two levels add two to each integer",
							Cascade.beckon(1, 2, 3),
							n -> n + 1,
							2,
							List.of(3, 4, 5)
					),
					new AscendCase(
							"Negative levels keep the old song",
							Cascade.beckon(7, 9),
							n -> n * 3,
							-1,
							List.of(7, 9)
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record AscendCase(String as, Cascade<Integer> source,
		                          UnaryOperator<Integer> ascension,
		                          int levels,
		                          List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of empty source and guards")
	final class GuardTests
	{
		@Test
		@DisplayName("should remain nadir for abyss source")
		void shouldRemainNadirForAbyssSource()
		{
			assertThat(Cascade.<Integer>abyss().ascend(n -> n + 1, 5)).isSameAs(Cascade.abyss());
		}

		@Test
		@DisplayName("should reject null ascension on brook")
		void shouldRejectNullAscensionOnBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).ascend(null, 2))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("ascension may not be null");
		}
	}
}