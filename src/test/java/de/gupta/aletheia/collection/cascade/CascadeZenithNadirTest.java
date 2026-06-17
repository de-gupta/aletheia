package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.functional.Unfolding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade#zenith and Cascade#nadir")
final class CascadeZenithNadirTest
{
	@Nested
	@DisplayName("zenith — maximum element")
	final class Zenith
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("returnsMaxElementCases")
			@DisplayName("returns the maximum element according to comparator")
			void returnsMaxElement(final String as, final ZenithCase tc)
			{
				assertThat(tc.source().zenith(tc.comparator()))
						.as(as)
						.isEqualTo(tc.expected());
			}

			@Test
			@DisplayName("applies custom comparator across element types")
			void appliesCustomComparatorAcrossTypes()
			{
				assertThat(Cascade.beckon("cat", "elephant", "ox").zenith(Comparator.comparingInt(String::length)))
						.as("longest string by length comparator")
						.isEqualTo(Unfolding.beckon("elephant"));
			}


			@Test
			@DisplayName("maximum() delegates to zenith()")
			void maximumDelegatesToZenith()
			{
				final Cascade<Integer> cascade = Cascade.beckon(3, 1, 2);

				assertThat(cascade.maximum(Comparator.naturalOrder()))
						.as("maximum() must equal zenith()")
						.isEqualTo(cascade.zenith(Comparator.naturalOrder()));
			}

			@Test
			@DisplayName("throws when comparator is null")
			void throwsWhenComparatorIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon(1).zenith(null))
						.isInstanceOf(NullPointerException.class)
						.hasMessage("comparator may not be null");
			}

			private static Stream<Arguments> returnsMaxElementCases()
			{
				return Stream.of(
						new ZenithCase("natural order integers",
								Cascade.beckon(3, 1, 4, 1, 5, 9), Comparator.naturalOrder(),
								Unfolding.beckon(9)),
						new ZenithCase("reverse order picks smallest",
								Cascade.beckon(3, 1, 4), Comparator.reverseOrder(),
								Unfolding.beckon(1)),
						new ZenithCase("single element returns that element",
								Cascade.beckon(42), Comparator.naturalOrder(),
								Unfolding.beckon(42))
				).map(tc -> Arguments.of(tc.as(), tc));
			}

			private record ZenithCase(String as, Cascade<Integer> source, Comparator<Integer> comparator,
			                          Unfolding<Integer> expected)
			{
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns empty Unfolding")
			void returnsEmptyUnfolding()
			{
				assertThat(Cascade.<Integer>abyss().zenith(Comparator.naturalOrder()))
						.as("zenith on empty cascade")
						.isEqualTo(Unfolding.chaos());
			}
		}
	}

	@Nested
	@DisplayName("nadir — minimum element")
	final class Nadir
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("returnsMinElementCases")
			@DisplayName("returns the minimum element according to comparator")
			void returnsMinElement(final String as, final NadirCase tc)
			{
				assertThat(tc.source().nadir(tc.comparator()))
						.as(as)
						.isEqualTo(tc.expected());
			}


			@Test
			@DisplayName("minimum() delegates to nadir()")
			void minimumDelegatesToNadir()
			{
				final Cascade<Integer> cascade = Cascade.beckon(3, 1, 2);

				assertThat(cascade.minimum(Comparator.naturalOrder()))
						.as("minimum() must equal nadir()")
						.isEqualTo(cascade.nadir(Comparator.naturalOrder()));
			}

			@Test
			@DisplayName("throws when comparator is null")
			void throwsWhenComparatorIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon(1).nadir(null))
						.isInstanceOf(NullPointerException.class)
						.hasMessage("comparator may not be null");
			}

			@Test
			@DisplayName("applies custom comparator across element types")
			void appliesCustomComparatorAcrossTypes()
			{
				assertThat(Cascade.beckon("cat", "elephant", "ox").nadir(Comparator.comparingInt(String::length)))
						.as("shortest string by length comparator")
						.isEqualTo(Unfolding.beckon("ox"));
			}

			private static Stream<Arguments> returnsMinElementCases()
			{
				return Stream.of(
						new NadirCase("natural order integers",
								Cascade.beckon(3, 1, 4, 1, 5, 9), Comparator.naturalOrder(),
								Unfolding.beckon(1)),
						new NadirCase("reverse order picks largest",
								Cascade.beckon(3, 1, 4), Comparator.reverseOrder(),
								Unfolding.beckon(4)),
						new NadirCase("single element returns that element",
								Cascade.beckon(42), Comparator.naturalOrder(),
								Unfolding.beckon(42))
				).map(tc -> Arguments.of(tc.as(), tc));
			}

			private record NadirCase(String as, Cascade<Integer> source, Comparator<Integer> comparator,
			                         Unfolding<Integer> expected)
			{
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns empty Unfolding")
			void returnsEmptyUnfolding()
			{
				assertThat(Cascade.<Integer>abyss().nadir(Comparator.naturalOrder()))
						.as("nadir on empty cascade")
						.isEqualTo(Unfolding.chaos());
			}
		}
	}
}