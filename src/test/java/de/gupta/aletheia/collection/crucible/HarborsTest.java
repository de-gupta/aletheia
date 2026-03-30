package de.gupta.aletheia.collection.crucible;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

	private record CaseInsensitiveString(String value)
	{
		@Override
		public int hashCode()
		{
			return value.toLowerCase().hashCode();
		}

		@Override
		public boolean equals(final Object other)
		{
			if (this == other) return true;
			if (other == null || getClass() != other.getClass()) return false;
			final var that = (CaseInsensitiveString) other;
			return value.equalsIgnoreCase(that.value);
		}

		@Override
		public String toString()
		{
			return value;
		}

		private CaseInsensitiveString(final String value)
		{
			this.value = Objects.requireNonNull(value);
		}
	}

	@Nested
	@DisplayName("Custom equals method behavior")
	final class CustomEqualsTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("crucibleFactories")
		@DisplayName("Should respect custom equals implementation for case insensitive strings")
		void testHarborsWithCaseInsensitiveStrings(final String description,
												   final Function<List<CaseInsensitiveString>, Crucible<CaseInsensitiveString>> crucibleFactory)
		{
			final var elements = List.of(
					new CaseInsensitiveString("hello"),
					new CaseInsensitiveString("world"),
					new CaseInsensitiveString("test")
			);
			final var crucible = crucibleFactory.apply(elements);

			assertThat(crucible.harbors(new CaseInsensitiveString("HELLO")))
					.as("Crucible should harbor case insensitive match for hello")
					.isTrue();

			assertThat(crucible.harbors(new CaseInsensitiveString("Hello")))
					.as("Crucible should harbor mixed case match for hello")
					.isTrue();

			assertThat(crucible.harbors(new CaseInsensitiveString("WORLD")))
					.as("Crucible should harbor case insensitive match for world")
					.isTrue();

			assertThat(crucible.harbors(new CaseInsensitiveString("missing")))
					.as("Crucible should not harbor absent element")
					.isFalse();

			assertThat(crucible.harbors(new CaseInsensitiveString("MISSING")))
					.as("Crucible should not harbor absent element even with different case")
					.isFalse();
		}

		private static Stream<Arguments> crucibleFactories()
		{
			return Stream.of(
					Arguments.of("Forge with case insensitive strings",
							(Function<List<CaseInsensitiveString>, Crucible<CaseInsensitiveString>>) Forge::kindle),
					Arguments.of("Relic with case insensitive strings",
							(Function<List<CaseInsensitiveString>, Crucible<CaseInsensitiveString>>) Relic::consecrate)
			);
		}
	}
}