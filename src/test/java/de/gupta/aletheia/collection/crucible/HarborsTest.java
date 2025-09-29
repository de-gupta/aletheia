package de.gupta.aletheia.collection.crucible;

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

@DisplayName("Harbors method tests")
final class HarborsTest
{
	@Nested
	@DisplayName("Element presence verification")
	final class ElementPresenceTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("crucibleWithElementsTestCases")
		@DisplayName("Should correctly identify present elements")
		<E> void testHarborsExistingElements(final String description, final Crucible<E> crucible,
											 final E presentElement, final E absentElement)
		{
			assertThat(crucible.harbors(presentElement))
					.as("Crucible should harbor the present element")
					.isTrue();

			assertThat(crucible.harbors(absentElement))
					.as("Crucible should not harbor the absent element")
					.isFalse();
		}

		private static Stream<Arguments> crucibleWithElementsTestCases()
		{
			return Stream.of(
					Arguments.of(
							"Forge harbors fruit elements",
							Forge.kindle(List.of("apple", "banana", "cherry")),
							"apple",
							"grape"
					),
					Arguments.of(
							"Relic harbors mystical artifacts",
							Relic.consecrate(List.of("sword", "shield", "crown")),
							"sword",
							"bow"
					)
			);
		}
	}

	@Nested
	@DisplayName("Null element handling")
	final class NullElementTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("crucibleFactories")
		@DisplayName("Should handle null elements in collection")
		void testHarborsNullElementInCollection(final String description,
												final Function<List<String>, Crucible<String>> crucibleFactory)
		{
			final var elementsWithNull = Arrays.asList("element", null, "another");
			final var crucible = crucibleFactory.apply(elementsWithNull);

			assertThat(crucible.harbors(null))
					.as("Crucible should harbor null when collection contains null")
					.isTrue();

			assertThat(crucible.harbors("element"))
					.as("Crucible should harbor existing non-null element")
					.isTrue();
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("crucibleFactories")
		@DisplayName("Should return false for null when not present")
		void testHarborsNullWhenNotPresent(final String description,
										   final Function<List<String>, Crucible<String>> crucibleFactory)
		{
			final var elements = List.of("apple", "banana", "cherry");
			final var crucible = crucibleFactory.apply(elements);

			assertThat(crucible.harbors(null))
					.as("Crucible should not harbor null when not in collection")
					.isFalse();
		}

		private static Stream<Arguments> crucibleFactories()
		{
			return Stream.of(
					Arguments.of("Forge factory", (Function<List<String>, Crucible<String>>) Forge::kindle),
					Arguments.of("Relic factory", (Function<List<String>, Crucible<String>>) Relic::consecrate)
			);
		}
	}

	@Nested
	@DisplayName("Empty collection behavior")
	final class EmptyCollectionTests
	{
		@Test
		@DisplayName("Should return false for any element in empty collections")
		void testHarborsWithEmptyCollections()
		{
			final var emptyForge = Forge.kindle(List.of());
			final var emptyRelic = Relic.consecrate(List.of());

			assertThat(emptyForge.harbors("anything"))
					.as("Empty forge should not harbor any element")
					.isFalse();

			assertThat(emptyRelic.harbors("anything"))
					.as("Empty relic should not harbor any element")
					.isFalse();

			assertThat(emptyForge.harbors(null))
					.as("Empty forge should not harbor null")
					.isFalse();

			assertThat(emptyRelic.harbors(null))
					.as("Empty relic should not harbor null")
					.isFalse();
		}
	}
}