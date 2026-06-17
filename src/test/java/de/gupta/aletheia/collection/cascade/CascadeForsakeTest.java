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

@DisplayName("Cascade#forsake")
final class CascadeForsakeTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("skipsFirstNElementsCases")
		@DisplayName("returns the cascade without the first n elements")
		void skipsFirstNElements(final String as, final ForsakeCase tc)
		{
			assertThat(tc.source().forsake(tc.n()).summon())
					.as(as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("skip() delegates to forsake()")
		void skipDelegatesToForsake()
		{
			final Cascade<Integer> cascade = Cascade.beckon(1, 2, 3, 4);

			assertThat(cascade.skip(2).summon())
					.as("skip() must equal forsake()")
					.containsExactlyElementsOf(cascade.forsake(2).summon());
		}

		@Test
		@DisplayName("forsake(0) returns the full cascade unchanged")
		void forsakeZeroReturnsFullCascade()
		{
			assertThat(Cascade.beckon(1, 2, 3).forsake(0).summon())
					.as("forsake(0) should return all elements")
					.containsExactly(1, 2, 3);
		}

		@Test
		@DisplayName("forsake(n) beyond size returns empty cascade")
		void forsakeMoreThanSizeReturnsEmpty()
		{
			assertThat(Cascade.beckon(1, 2, 3).forsake(10).summon())
					.as("forsake beyond size should return empty")
					.isEmpty();
		}

		private static Stream<Arguments> skipsFirstNElementsCases()
		{
			return Stream.of(
					new ForsakeCase("skip 1 of 4", Cascade.beckon(1, 2, 3, 4), 1, List.of(2, 3, 4)),
					new ForsakeCase("skip 2 of 5", Cascade.beckon(1, 2, 3, 4, 5), 2, List.of(3, 4, 5)),
					new ForsakeCase("skip all but last", Cascade.beckon(10, 20, 30), 2, List.of(30)),
					new ForsakeCase("skip none", Cascade.beckon(7, 8, 9), 0, List.of(7, 8, 9))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record ForsakeCase(String as, Cascade<Integer> source, int n, List<Integer> expected)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns empty regardless of n")
		void returnsEmptyRegardlessOfN()
		{
			assertThat(Cascade.abyss().forsake(5))
					.as("forsake on empty cascade")
					.isSameAs(Cascade.abyss());
		}
	}
}