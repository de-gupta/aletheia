package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Brook kindle tests")
final class BrookKindleTest
{
	@Nested
	@DisplayName("Aspects of source invocation")
	final class SourceInvocationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("sourceShapes")
		@DisplayName("should forge brook from stream supplier")
		void shouldForgeBrookFromStreamSupplier(final String as, final SourceShapeCase tc)
		{
			var brook = Brook.kindle(tc.source());

			assertThat(brook)
					.as("kindle as %s should yield Brook", as)
					.isInstanceOf(Brook.class);

			assertThat(brook.summon())
					.as("summon should render supplier stream for %s", as)
					.containsExactlyElementsOf(tc.expectedElements());
		}

		private static Stream<Arguments> sourceShapes()
		{
			return Stream.of(
					new SourceShapeCase("A stream with singular omen", () -> Stream.of("echo"), List.of("echo")),
					new SourceShapeCase("A stream with many runes", () -> Stream.of(1, 2, 3), List.of(1, 2, 3)),
					new SourceShapeCase("A stream with null among offerings",
							() -> Stream.of("north", null, "south"), Arrays.asList("north", null, "south")),
					new SourceShapeCase("An empty stream remains empty", Stream::empty, List.of())
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record SourceShapeCase(String as, Supplier<Stream<Object>> source, List<Object> expectedElements)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of source constraints")
	final class SourceConstraintTests
	{
		@Test
		@DisplayName("should throw when source is null")
		void shouldThrowWhenSourceIsNull()
		{
			assertThatThrownBy(() -> Brook.kindle(null))
					.as("kindle(null) should guard source")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("source may not be null");
		}
	}

}