package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cascade beckon(varargs) tests")
final class CascadeBeckonVarargsTest
{
	@Nested
	@DisplayName("Aspects of vararg invocation shapes")
	final class InvocationShapeTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("invocationShapes")
		@DisplayName("should summon the ordained current")
		void shouldSummonOrdainedCurrent(final String as, final BeckonVarargsCase tc)
		{
			var cascade = Cascade.beckon(tc.invocation());

			assertThat(cascade)
					.as("beckon(varargs) as %s should yield %s", as, tc.expectedImpl().getSimpleName())
					.isInstanceOf(tc.expectedImpl());

			assertThat(cascade.supple())
					.as("supple should be %s for %s", tc.expectedSupple(), as)
					.isEqualTo(tc.expectedSupple());

			assertThat(cascade.sterile())
					.as("sterile should be %s for %s", tc.expectedSterile(), as)
					.isEqualTo(tc.expectedSterile());

			if (!tc.expectedElements().isEmpty())
			{
				assertThat(cascade.summon())
						.as("summoned elements should mirror varargs for %s", as)
						.containsExactlyElementsOf(tc.expectedElements());
			}
		}

		private static Stream<Arguments> invocationShapes()
		{
			return Stream.of(
					new BeckonVarargsCase("The river begins with no stones",
							new Object[]{}, List.of(), Nadir.class, false, true),
					new BeckonVarargsCase("A single omen starts the stream",
							new Object[]{"atlas"}, List.of("atlas"), Brook.class, true, false),
					new BeckonVarargsCase("Many omens flow in sequence",
							new Object[]{1, 2, 3}, List.of(1, 2, 3), Brook.class, true, false),
					new BeckonVarargsCase("Null elements are carried unchanged",
							new Object[]{"oracle", null, "echo"}, Arrays.asList("oracle", null, "echo"), Brook.class,
							true, false)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record BeckonVarargsCase(String as, Object[] invocation, List<Object> expectedElements,
		                                 Class<?> expectedImpl, boolean expectedSupple, boolean expectedSterile)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of nullary references")
	final class NullaryReferenceTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("nullArrayShapes")
		@DisplayName("should descend to abyss when vararg array is null")
		void shouldDescendToAbyss(final String as, final NullVarargCase tc)
		{
			var cascade = Cascade.beckon(tc.invocation());

			assertThat(cascade)
					.as("beckon((T[]) null) as %s should resolve to Nadir", as)
					.isInstanceOf(Nadir.class);
			assertThat(cascade.sterile())
					.as("null varargs should yield sterile cascade")
					.isTrue();
		}

		private static Stream<Arguments> nullArrayShapes()
		{
			return Stream.of(
					new NullVarargCase("A nameless invocation", null)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record NullVarargCase(String as, String[] invocation)
		{
		}
	}
}