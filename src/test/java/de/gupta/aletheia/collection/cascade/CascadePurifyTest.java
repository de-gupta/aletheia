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

@DisplayName("Cascade purify tests")
final class CascadePurifyTest
{
	@Nested
	@DisplayName("Aspects of distinct current")
	final class DistinctCurrentTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("purifyCases")
		@DisplayName("should remove duplicates while preserving first encounter order")
		void shouldRemoveDuplicatesWhilePreservingFirstEncounterOrder(final String as, final PurifyCase tc)
		{
			var result = tc.source().purify();

			assertThat(result.summon())
					.as("purify should keep only first occurrences for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> purifyCases()
		{
			return Stream.of(
					new PurifyCase(
							"Repeated runes collapse into one procession",
							Cascade.beckon("a", "b", "a", "c", "b"),
							List.of("a", "b", "c")
					),
					new PurifyCase(
							"Already pure streams remain untouched",
							Cascade.beckon("odin", "thor"),
							List.of("odin", "thor")
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record PurifyCase(String as, Cascade<String> source, List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of abyss")
	final class AbyssTests
	{
		@Test
		@DisplayName("should remain nadir when source is abyss")
		void shouldRemainNadirWhenSourceIsAbyss()
		{
			assertThat(Cascade.abyss().purify()).isSameAs(Cascade.abyss());
		}
	}
}