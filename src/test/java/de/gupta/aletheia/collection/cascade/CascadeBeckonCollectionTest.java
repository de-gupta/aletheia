package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cascade beckon(collection) tests")
final class CascadeBeckonCollectionTest
{
	@Nested
	@DisplayName("Aspects of collection forms")
	final class CollectionShapeTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("collectionShapes")
		@DisplayName("should create expected cascade expression")
		void shouldCreateExpectedCascadeExpression(final String as, final CollectionShapeCase tc)
		{
			var cascade = Cascade.beckon(tc.source());

			assertThat(cascade)
					.as("beckon(collection) as %s should yield %s", as, tc.expectedImpl().getSimpleName())
					.isInstanceOf(tc.expectedImpl());

			assertThat(cascade.supple())
					.as("supple shape should align for %s", as)
					.isEqualTo(tc.expectedSupple());

			assertThat(cascade.sterile())
					.as("sterile shape should align for %s", as)
					.isEqualTo(tc.expectedSterile());

			if (!tc.expectedElements().isEmpty())
			{
				assertThat(cascade.summon())
						.as("summon should preserve collection iteration order for %s", as)
						.containsExactlyElementsOf(tc.expectedElements());
			}
		}

		private static Stream<Arguments> collectionShapes()
		{
			return Stream.of(
					new CollectionShapeCase("An empty amphora yields a supple but empty brook",
							List.of(), List.of(), Brook.class, true, false),
					new CollectionShapeCase("A mutable list enters as flowing brook",
							new ArrayList<>(List.of("a", "b", "c")), List.of("a", "b", "c"), Brook.class,
							true, false),
					new CollectionShapeCase("A linked set keeps insertion rune order",
							new LinkedHashSet<>(List.of(3, 1, 2)), List.of(3, 1, 2), Brook.class, true, false),
					new CollectionShapeCase("Null pebbles can still be carried",
							Arrays.asList("north", null, "south"), Arrays.asList("north", null, "south"),
							Brook.class, true, false),
					new CollectionShapeCase("A null vessel collapses to nadir",
							null, List.of(), Nadir.class, false, true)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record CollectionShapeCase(String as, Collection<Object> source, List<Object> expectedElements,
		                                   Class<?> expectedImpl, boolean expectedSupple, boolean expectedSterile)
		{
		}
	}
}