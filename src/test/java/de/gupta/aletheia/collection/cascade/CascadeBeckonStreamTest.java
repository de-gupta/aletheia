package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cascade beckon(stream) tests")
final class CascadeBeckonStreamTest
{
	@Nested
	@DisplayName("Aspects of tributary origin")
	final class StreamShapeTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("streamShapes")
		@DisplayName("should shape the expected current")
		void shouldShapeExpectedCurrent(final String as, final StreamShapeCase tc)
		{
			var cascade = Cascade.beckon(tc.stream().get());

			assertThat(cascade)
					.as("beckon(stream) as %s should yield %s", as, tc.expectedImpl().getSimpleName())
					.isInstanceOf(tc.expectedImpl());

			if (tc.expectedImpl().equals(Brook.class))
			{
				assertThat(cascade.summon())
						.as("summon should mirror stream traversal for %s", as)
						.containsExactlyElementsOf(tc.expectedElements());
			}
			else
			{
				assertThat(cascade.sterile())
						.as("null stream should resolve to sterile cascade for %s", as)
						.isTrue();
			}
		}

		private static Stream<Arguments> streamShapes()
		{
			return Stream.of(
					new StreamShapeCase("An empty stream still becomes a brook",
							Stream::empty, List.of(), Brook.class),
					new StreamShapeCase("A finite stream keeps sequence",
							() -> Stream.of("sinister", "dexter", "third"), List.of("sinister", "dexter", "third"),
							Brook.class),
					new StreamShapeCase("A mapped stream carries transformed echoes",
							() -> Stream.of(2, 3, 5).map(n -> n * n), List.of(4, 9, 25), Brook.class),
					new StreamShapeCase("A null stream reference descends to nadir",
							() -> null, List.of(), Nadir.class)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record StreamShapeCase(String as, Supplier<Stream<Object>> stream,
		                               List<Object> expectedElements, Class<?> expectedImpl)
		{
		}
	}
}