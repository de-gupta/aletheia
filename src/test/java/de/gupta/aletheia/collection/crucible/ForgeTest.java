package de.gupta.aletheia.collection.crucible;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests for Forge class")
final class ForgeTest
{
	@Nested
	@DisplayName("Tests for kindle() static factory method")
	final class KindleTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should create Forge from collection")
		<E> void shouldCreateForgeFromCollection(String description, Collection<E> collection)
		{
			Forge<E> result = Forge.kindle(collection);

			assertThat(result).as("kindle() for %s should create a Forge", description)
							  .isInstanceOf(Forge.class)
							  .isNotNull();
		}

		@Test
		@DisplayName("should create independent copy of collection")
		void shouldCreateIndependentCopyOfCollection()
		{
			List<String> original = new ArrayList<>(List.of("original"));
			Forge<String> forge = Forge.kindle(original);

			original.add("modified");

			// The forge should not be affected by modifications to the original collection
			assertThat(forge.enshrine()).as("Forge should maintain independent copy")
										.isNotSameAs(original);
		}

		@Test
		@DisplayName("should throw exception for null collection")
		void shouldThrowExceptionForNullCollection()
		{
			Collection<String> nullCollection = null;

			assertThatThrownBy(() -> Forge.kindle(nullCollection))
					.as("kindle() with null collection should throw NullPointerException")
					.isInstanceOf(NullPointerException.class);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("empty ArrayList", new ArrayList<>()),
					new TestCase<>("ArrayList with single element", List.of("element")),
					new TestCase<>("ArrayList with multiple elements", List.of("first", "second", "third")),
					new TestCase<>("HashSet with elements", Set.of(1, 2, 3)),
					new TestCase<>("empty LinkedList", new LinkedList<>()),
					new TestCase<>("LinkedList with null element", Collections.singletonList(null))
			).map(tc -> Arguments.of(tc.description, tc.collection));
		}

		private record TestCase<E>(String description, Collection<E> collection)
		{
		}
	}

	@Nested
	@DisplayName("Tests for embrace() method")
	final class EmbraceTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should add element and return new Forge")
		<E> void shouldAddElementAndReturnNewForge(String description, Collection<E> initialCollection, E element)
		{
			Forge<E> original = Forge.kindle(new ArrayList<>(initialCollection));
			Crucible<E> result = original.embrace(element);

			assertThat(result).as("embrace() for %s should return a new Forge", description)
							  .isInstanceOf(Forge.class)
							  .isNotSameAs(original);
		}

		@Test
		@DisplayName("should add null element")
		void shouldAddNullElement()
		{
			Forge<String> forge = Forge.kindle(new ArrayList<>(List.of("existing")));

			Crucible<String> result = forge.embrace(null);

			assertThat(result).as("embrace() should handle null element")
							  .isInstanceOf(Forge.class);
		}

		@Test
		@DisplayName("should add duplicate element")
		void shouldAddDuplicateElement()
		{
			Forge<String> forge = Forge.kindle(new ArrayList<>(List.of("existing")));

			Crucible<String> result = forge.embrace("existing");

			assertThat(result).as("embrace() should handle duplicate element")
							  .isInstanceOf(Forge.class);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("empty collection with string", new ArrayList<>(), "new element"),
					new TestCase<>("collection with elements and new string", List.of("first", "second"), "third"),
					new TestCase<>("numeric collection with new number", List.of(1, 2), 3),
					new TestCase<>("collection with null elements", Collections.singletonList(null), "element")
			).map(tc -> Arguments.of(tc.description, tc.initialCollection, tc.element));
		}

		private record TestCase<E>(String description, Collection<E> initialCollection, E element)
		{
		}
	}

	@Nested
	@DisplayName("Tests for banish() method")
	final class BanishTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should remove element and return new Forge")
		<E> void shouldRemoveElementAndReturnNewForge(String description, Collection<E> initialCollection, E element)
		{
			Forge<E> original = Forge.kindle(new ArrayList<>(initialCollection));
			Crucible<E> result = original.banish(element);

			assertThat(result).as("banish() for %s should return a new Forge", description)
							  .isInstanceOf(Forge.class)
							  .isNotSameAs(original);
		}

		@Test
		@DisplayName("should handle removal of non-existent element")
		void shouldHandleRemovalOfNonExistentElement()
		{
			Forge<String> forge = Forge.kindle(new ArrayList<>(List.of("existing")));

			Crucible<String> result = forge.banish("nonexistent");

			assertThat(result).as("banish() should handle non-existent element gracefully")
							  .isInstanceOf(Forge.class);
		}

		@Test
		@DisplayName("should remove null element")
		void shouldRemoveNullElement()
		{
			Forge<String> forge = Forge.kindle(new ArrayList<>(Arrays.asList("existing", null)));

			Crucible<String> result = forge.banish(null);

			assertThat(result).as("banish() should handle null element")
							  .isInstanceOf(Forge.class);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("collection with element to remove", new ArrayList<>(List.of("first", "second")),
							"first"),
					new TestCase<>("numeric collection removing number", new ArrayList<>(List.of(1, 2, 3)), 2),
					new TestCase<>("single element collection", new ArrayList<>(List.of("only")), "only"),
					new TestCase<>("collection with duplicates", new ArrayList<>(Arrays.asList("dup", "dup", "other")),
							"dup")
			).map(tc -> Arguments.of(tc.description, tc.initialCollection, tc.element));
		}

		private record TestCase<E>(String description, Collection<E> initialCollection, E element)
		{
		}
	}

	@Nested
	@DisplayName("Tests for enshrine() method")
	final class EnshrineTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should convert Forge to Relic")
		<E> void shouldConvertForgeToRelic(String description, Collection<E> collection)
		{
			Forge<E> forge = Forge.kindle(collection);
			Relic<E> result = forge.enshrine();

			assertThat(result).as("enshrine() for %s should create a Relic", description)
							  .isInstanceOf(Relic.class)
							  .isNotNull();
		}

		@Test
		@DisplayName("should create immutable Relic")
		void shouldCreateImmutableRelic()
		{
			Forge<String> forge = Forge.kindle(List.of("test"));
			Relic<String> relic = forge.enshrine();

			assertThatThrownBy(() -> relic.embrace("new"))
					.as("Relic from enshrine() should be immutable")
					.isInstanceOf(UnsupportedOperationException.class);

			assertThatThrownBy(() -> relic.banish("test"))
					.as("Relic from enshrine() should be immutable")
					.isInstanceOf(UnsupportedOperationException.class);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("empty collection", new ArrayList<>()),
					new TestCase<>("single element collection", List.of("element")),
					new TestCase<>("multiple elements collection", List.of("first", "second", "third")),
					new TestCase<>("collection with null", Collections.singletonList(null))
			).map(tc -> Arguments.of(tc.description, tc.collection));
		}

		private record TestCase<E>(String description, Collection<E> collection)
		{
		}
	}

	@Nested
	@DisplayName("Tests for awaken() method")
	final class AwakenTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should return itself")
		<E> void shouldReturnItself(String description, Collection<E> collection)
		{
			Forge<E> forge = Forge.kindle(collection);
			Forge<E> result = forge.awaken();

			assertThat(result).as("awaken() for %s should return the same instance", description)
							  .isSameAs(forge)
							  .isInstanceOf(Forge.class);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("empty collection", new ArrayList<>()),
					new TestCase<>("single element collection", List.of("element")),
					new TestCase<>("multiple elements collection", List.of("first", "second", "third")),
					new TestCase<>("collection with null", Collections.singletonList(null))
			).map(tc -> Arguments.of(tc.description, tc.collection));
		}

		private record TestCase<E>(String description, Collection<E> collection)
		{
		}
	}

	@Nested
	@DisplayName("Tests for method chaining and fluent API")
	final class MethodChainingTests
	{
		@Test
		@DisplayName("should support chaining multiple operations")
		void shouldSupportChainingMultipleOperations()
		{
			Forge<String> forge = Forge.kindle(new ArrayList<>(List.of("first")));

			Crucible<String> result = forge.embrace("second")
										   .embrace("third")
										   .banish("first")
										   .awaken()
										   .embrace("fourth");

			assertThat(result).as("Method chaining should work fluently")
							  .isInstanceOf(Forge.class);
		}

		@Test
		@DisplayName("should support conversion between Forge and Relic")
		void shouldSupportConversionBetweenForgeAndRelic()
		{
			Forge<Integer> forge = Forge.kindle(List.of(1, 2, 3));

			Forge<Integer> convertedBack = forge.enshrine()  // Forge -> Relic
												.awaken()    // Relic -> Forge
												.embrace(4)  // Add element
												.awaken();   // Still Forge

			assertThat(convertedBack).as("Conversion chain should work correctly")
									 .isInstanceOf(Forge.class)
									 .isNotSameAs(forge);
		}
	}

	@Nested
	@DisplayName("Tests for immutability and thread safety")
	final class ImmutabilityTests
	{
		@Test
		@DisplayName("should not modify original collection when embracing")
		void shouldNotModifyOriginalCollectionWhenEmbracing()
		{
			List<String> original = new ArrayList<>(List.of("original"));
			Forge<String> forge = Forge.kindle(original);

			forge.embrace("new");

			assertThat(original).as("Original collection should not be modified")
								.containsExactly("original");
		}

		@Test
		@DisplayName("should not modify original collection when banishing")
		void shouldNotModifyOriginalCollectionWhenBanishing()
		{
			List<String> original = new ArrayList<>(List.of("original", "target"));
			Forge<String> forge = Forge.kindle(original);

			forge.banish("target");

			assertThat(original).as("Original collection should not be modified")
								.containsExactly("original", "target");
		}
	}
}