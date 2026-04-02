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

@DisplayName("Cascade metamorphose tests")
final class CascadeMetamorphoseTest
{
	@Nested
	@DisplayName("Aspects of simple metamorphosis")
	final class SimpleMetamorphosisTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("transfigurationShapes")
		@DisplayName("should transform each element and filter null outcomes")
		void shouldTransformEachElementAndFilterNullOutcomes(final String as, final TransfigurationCase tc)
		{
			var result = tc.source().metamorphose(tc.metamorphosis());

			assertThat(result.summon())
					.as("metamorphose should transfigure expected current for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> transfigurationShapes()
		{
			return Stream.of(
					TransfigurationCase.shape(
							"A mortal string is measured into length",
							Cascade.beckon("apollo", "ares"),
							e -> ((String) e).length(),
							List.of(6, 4)
					),
					TransfigurationCase.shape(
							"Null returns are cast into oblivion",
							Cascade.beckon("atlas", "echo", "odin"),
							e -> ((String) e).startsWith("e") ? null : ((String) e).toUpperCase(),
							List.of("ATLAS", "ODIN")
					),
					TransfigurationCase.shape(
							"Even null elements can be interpreted",
							Cascade.beckon(Arrays.asList("north", null, "south")),
							e -> e == null ? "VOID" : ((String) e).toUpperCase(),
							List.of("NORTH", "VOID", "SOUTH")
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record TransfigurationCase(String as, Cascade<Object> source,
		                                   Function<Object, Object> metamorphosis, List<Object> expected)
		{
			private static TransfigurationCase shape(
					final String as,
					final Cascade<Object> source,
					final Function<Object, Object> metamorphosis,
					final List<Object> expected)
			{
				return new TransfigurationCase(as, source, metamorphosis, expected);
			}
		}
	}

	@Nested
	@DisplayName("Aspects of empty origin")
	final class EmptyOriginTests
	{
		@Test
		@DisplayName("should remain nadir and never invoke metamorphosis")
		void shouldRemainNadirAndNeverInvokeMetamorphosis()
		{
			var result = Cascade.<String>abyss().metamorphose(_ ->
			{
				throw new AssertionError("metamorphosis should not be invoked on nadir");
			});

			assertThat(result)
					.as("metamorphose on abyss should stay in nadir")
					.isSameAs(Cascade.abyss());
		}
	}

	@Nested
	@DisplayName("Aspects of null metamorphosis")
	final class NullMetamorphosisTests
	{
		@Test
		@DisplayName("should throw for brook source")
		void shouldThrowForBrookSource()
		{
			assertThatThrownBy(() -> Cascade.beckon("zeus").metamorphose(null))
					.as("brook metamorphose should reject null function")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("metamorphosis may not be null");
		}
	}
}