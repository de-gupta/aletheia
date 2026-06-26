package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cascade#invert")
final class CascadeInvertTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("reversesOrderCases")
		@DisplayName("reverses the encounter order of elements")
		void reversesOrder(final String as, final InvertCase tc)
		{
			assertThat(tc.source().invert().summon())
					.as(as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("works with non-Integer element type")
		void worksWithNonIntegerElementType()
		{
			assertThat(Cascade.beckon("a", "b", "c").invert().summon())
					.as("strings reversed")
					.containsExactly("c", "b", "a");
		}

		@Test
		@DisplayName("invert applied twice returns original order")
		void invertTwiceReturnsOriginalOrder()
		{
			final List<Integer> original = List.of(1, 2, 3, 4, 5);

			assertThat(Cascade.beckon(original).invert().invert().summon())
					.as("double inversion is identity")
					.containsExactlyElementsOf(original);
		}

		@Test
		@DisplayName("invert after discern reverses filtered elements in correct order")
		void invertAfterDiscernReversesFilteredElements()
		{
			assertThat(Cascade.beckon(1, 2, 3, 4, 5, 6)
			                  .discern(n -> n % 2 == 0)
			                  .invert()
			                  .summon())
					.as("even numbers reversed")
					.containsExactly(6, 4, 2);
		}

		@Test
		@DisplayName("invert preserves all elements — no duplicates lost, no elements dropped")
		void invertPreservesAllElements()
		{
			final Cascade<Integer> source = Cascade.beckon(3, 1, 4, 1, 5, 9);

			assertThat(source.invert().summon())
					.as("all elements including duplicates preserved in reversed order")
					.containsExactly(9, 5, 1, 4, 1, 3);
		}

		@Test
		@DisplayName("reverse() delegates to invert()")
		void reverseDelegatesToInvert()
		{
			final Cascade<String> source = Cascade.beckon("a", "b", "c");

			assertThat(source.reverse().summon())
					.as("reverse() must equal invert()")
					.containsExactlyElementsOf(source.invert().summon());
		}

		private static Stream<Arguments> reversesOrderCases()
		{
			return Stream.of(
					new InvertCase("three elements reversed",
							Cascade.beckon(1, 2, 3), List.of(3, 2, 1)),
					new InvertCase("single element unchanged",
							Cascade.beckon(42), List.of(42)),
					new InvertCase("five elements fully reversed",
							Cascade.beckon(1, 2, 3, 4, 5), List.of(5, 4, 3, 2, 1))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record InvertCase(String as, Cascade<Integer> source, List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns absent Cascade")
		void returnsAbsent()
		{
			assertThat(Cascade.abyss().invert().sterile())
					.as("invert on absent cascade stays absent")
					.isTrue();
		}

		@Test
		@DisplayName("invert on Nadir stays Nadir")
		void invertOnNadirStaysNadir()
		{
			assertThat(Cascade.abyss().invert())
					.as("invert on Nadir returns Nadir")
					.isSameAs(Cascade.abyss());
		}
	}
}