package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade entwine tests")
final class CascadeEntwineTest
{
	@Nested
	@DisplayName("Aspects of unfolding paths")
	final class UnfoldingPathTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("entwineCases")
		@DisplayName("should flatten cascades returned by plot")
		void shouldFlattenCascadesReturnedByPlot(final String as, final EntwineCase tc)
		{
			var result = tc.source().entwine(tc.plot());

			assertThat(result.summon())
					.as("entwine should flatten river branches for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> entwineCases()
		{
			return Stream.of(
					new EntwineCase(
							"Each number yields a twin-echo cascade",
							Cascade.beckon(1, 2),
							n -> Cascade.beckon("left-" + n, "right-" + n),
							List.of("left-1", "right-1", "left-2", "right-2")
					),
					new EntwineCase(
							"Null and abyss branches are silently skipped",
							Cascade.beckon(1, 2, 3),
							n -> n == 1 ? null : (n == 2 ? Cascade.abyss() : Cascade.beckon("kept-3")),
							List.of("kept-3")
					),
					new EntwineCase(
							"Returned cascades may carry null elements",
							Cascade.beckon(9),
							_ -> Cascade.beckon(Arrays.asList("sun", null, "moon")),
							Arrays.asList("sun", null, "moon")
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record EntwineCase(String as, Cascade<Integer> source,
		                           Function<Integer, Cascade<String>> plot,
		                           List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of guards and abyss")
	final class GuardAndAbyssTests
	{
		@Test
		@DisplayName("should reject null plot for brook")
		void shouldRejectNullPlotForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon(1).entwine(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("plot may not be null");
		}

		@Test
		@DisplayName("should tolerate null plot for abyss")
		void shouldTolerateNullPlotForAbyss()
		{
			assertThat(Cascade.<Integer>abyss().entwine(null))
					.as("nadir entwine should remain nadir without guard checks")
					.isSameAs(Cascade.abyss());
		}
	}
}