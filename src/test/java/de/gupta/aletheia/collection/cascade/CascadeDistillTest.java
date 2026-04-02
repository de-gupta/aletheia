package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.crucible.Crucible;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cascade distill tests")
final class CascadeDistillTest
{
	@Nested
	@DisplayName("Aspects of crucible manifestations")
	final class CrucibleManifestTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("distillationShapes")
		@DisplayName("should distill expected cascade")
		void shouldDistillExpectedCascade(final String as, final DistillationCase tc)
		{
			var cascade = Cascade.distill(tc.crucible());

			assertThat(cascade)
					.as("distill as %s should produce %s", as, tc.expectedImpl().getSimpleName())
					.isInstanceOf(tc.expectedImpl());

			if (!tc.expectedElements().isEmpty())
			{
				assertThat(cascade.summon())
						.as("distilled summon should preserve crucible manifestation for %s", as)
						.containsExactlyElementsOf(tc.expectedElements());
			}
			else
			{
				assertThat(cascade.sterile())
						.as("empty manifestation should distill to sterile cascade for %s", as)
						.isTrue();
			}
		}

		private static Stream<Arguments> distillationShapes()
		{
			return Stream.of(
					new DistillationCase("An empty forge condenses to nadir",
							Crucible.kindle(List.of()), List.of(), Nadir.class),
					new DistillationCase("A forged sequence becomes brook",
							Crucible.kindle(List.of("iron", "silver")), List.of("iron", "silver"), Brook.class),
					new DistillationCase("A relic sequence also becomes brook",
							Crucible.consecrate(List.of(7, 8, 9)), List.of(7, 8, 9), Brook.class),
					new DistillationCase("Null-bearing crucibles keep their echoes",
							Crucible.kindle(Arrays.asList("sun", null, "moon")), Arrays.asList("sun", null, "moon"),
							Brook.class),
					new DistillationCase("A null crucible descends to nadir",
							null, List.of(), Nadir.class)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record DistillationCase(String as, Crucible<Object> crucible,
		                                List<Object> expectedElements, Class<?> expectedImpl)
		{
		}
	}
}