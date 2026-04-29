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
			Crucible<String> forge = Forge.kindle(original);

			original.add("modified");

			// The forge should not be affected by modifications to the original collection
			assertThat(forge.manifest()).as("Forge should maintain independent copy").isNotSameAs(original)
										.containsExactly("original");
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
					new TestCase<>("ArrayList with multiple elements", List.of("sinister", "dexter", "dusk")),
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

		@Test
		@DisplayName("should not modify the original forge when embracing")
		void shouldNotModifyTheOriginalForgeWhenEmbracing()
		{
			Forge<String> original = Forge.kindle(new ArrayList<>(List.of("sinister", "dexter")));

			Crucible<String> embraced = original.embrace("dusk");

			assertThat(original.manifest()).as("The original forge should remain unchanged after embrace()")
			                               .containsExactly("sinister", "dexter");
			assertThat(embraced.manifest()).as("The returned forge should contain the embraced element")
			                               .containsExactly("sinister", "dexter", "dusk");
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("empty collection with string", new ArrayList<>(), "new element"),
					new TestCase<>("collection with elements and new string", List.of("sinister", "dexter"), "dusk"),
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

		@Test
		@DisplayName("should not modify the original forge when banishing")
		void shouldNotModifyTheOriginalForgeWhenBanishing()
		{
			Forge<String> original = Forge.kindle(new ArrayList<>(List.of("sinister", "dexter", "dusk")));

			Crucible<String> banished = original.banish("dexter");

			assertThat(original.manifest()).as("The original forge should remain unchanged after banish()")
			                               .containsExactly("sinister", "dexter", "dusk");
			assertThat(banished.manifest()).as("The returned forge should exclude the banished element")
			                               .containsExactly("sinister", "dusk");
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("collection with element to remove", new ArrayList<>(List.of("sinister", "dexter")),
							"sinister"),
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
					new TestCase<>("multiple elements collection", List.of("sinister", "dexter", "dusk")),
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
					new TestCase<>("multiple elements collection", List.of("sinister", "dexter", "dusk")),
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
			Forge<String> forge = Forge.kindle(new ArrayList<>(List.of("sinister")));

			Crucible<String> result = forge.embrace("dexter")
			                               .embrace("dusk")
										   .banish("sinister")
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

	@Nested
	@DisplayName("Tests for manifest() method")
	final class ManifestTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("collectionTypeTestCases")
		@DisplayName("should return unmodifiable collection from different input types")
		<E> void shouldReturnUnmodifiableCollectionFromDifferentTypes(String description, Collection<E> inputCollection,
																	  Collection<E> expectedElements)
		{
			Forge<E> forge = Forge.kindle(inputCollection);
			Collection<E> result = forge.manifest();

			assertThat(result).as("manifest() for %s should return collection with expected elements", description)
							  .containsExactlyInAnyOrderElementsOf(expectedElements);

			assertThatThrownBy(() -> result.add(
					inputCollection.iterator().hasNext() ? inputCollection.iterator().next() : null)).as(
					"manifest() should return unmodifiable collection").isInstanceOf(
					UnsupportedOperationException.class);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("edgeCaseTestCases")
		@DisplayName("should handle edge cases correctly")
		<E> void shouldHandleEdgeCasesCorrectly(String description, Collection<E> inputCollection,
												Collection<E> expectedElements)
		{
			Forge<E> forge = Forge.kindle(inputCollection);
			Collection<E> result = forge.manifest();

			assertThat(result).as("manifest() for %s should handle edge case", description)
							  .containsExactlyElementsOf(expectedElements);
		}

		@Test
		@DisplayName("should return independent copy - modifications to returned collection should not affect forge")
		void shouldReturnIndependentCopy()
		{
			List<String> original = new ArrayList<>(List.of("a", "b", "c"));
			Forge<String> forge = Forge.kindle(original);
			Collection<String> manifested = forge.manifest();

			// Verify returned collection is unmodifiable
			assertThatThrownBy(manifested::clear).as("manifest() should return unmodifiable collection")
												 .isInstanceOf(UnsupportedOperationException.class);

			assertThatThrownBy(() -> manifested.remove("a")).as("manifest() should return unmodifiable collection")
															.isInstanceOf(UnsupportedOperationException.class);

			assertThatThrownBy(() -> manifested.add("d")).as("manifest() should return unmodifiable collection")
														 .isInstanceOf(UnsupportedOperationException.class);
		}

		@Test
		@DisplayName("should reflect changes when forge is modified")
		void shouldReflectChangesWhenForgeIsModified()
		{
			List<String> original = new ArrayList<>(List.of("a", "b"));
			Forge<String> forge = Forge.kindle(original);

			Collection<String> manifestBefore = forge.manifest();
			assertThat(manifestBefore).containsExactly("a", "b");

			Forge<String> modifiedForge = (Forge<String>) forge.embrace("c");
			Collection<String> manifestAfter = modifiedForge.manifest();

			assertThat(manifestAfter).as("manifest() should reflect forge modifications")
									 .containsExactly("a", "b", "c");

			// Original manifest should be unchanged (different forge instance)
			assertThat(manifestBefore).as("Original manifest should remain unchanged").containsExactly("a", "b");
		}

		@Test
		@DisplayName("should not be affected by modifications to original input collection")
		void shouldNotBeAffectedByModificationsToOriginalInputCollection()
		{
			List<String> original = new ArrayList<>(List.of("a", "b"));
			Forge<String> forge = Forge.kindle(original);
			Collection<String> manifested = forge.manifest();

			// Modify original collection
			original.add("c");
			original.remove("a");

			// Manifest should not be affected
			assertThat(manifested).as("manifest() should not be affected by original collection changes")
								  .containsExactly("a", "b");
		}

		@Test
		@DisplayName("should handle large collections efficiently")
		void shouldHandleLargeCollectionsEfficiently()
		{
			List<Integer> largeList = new ArrayList<>();
			for (int i = 0; i < 10000; i++)
			{
				largeList.add(i);
			}

			Forge<Integer> forge = Forge.kindle(largeList);
			Collection<Integer> manifested = forge.manifest();

			assertThat(manifested).as("manifest() should handle large collections").hasSize(10000)
								  .containsExactlyElementsOf(largeList);

			assertThatThrownBy(manifested::clear).as("Large manifested collection should be unmodifiable")
												 .isInstanceOf(UnsupportedOperationException.class);
		}

		@Test
		@DisplayName("should return List type specifically")
		void shouldReturnListTypeSpecifically()
		{
			Forge<String> forge = Forge.kindle(List.of("a", "b", "c"));
			Collection<String> manifested = forge.manifest();

			assertThat(manifested).as("manifest() should return List type").isInstanceOf(List.class);
		}

		private static Stream<Arguments> collectionTypeTestCases()
		{
			return Stream.of(new CollectionTypeTestCase<>("ArrayList with strings",
										 new ArrayList<>(List.of("apple", "banana", "cherry")), List.of("apple", "banana", "cherry")),
								 new CollectionTypeTestCase<>("LinkedList with integers", new LinkedList<>(List.of(1, 2, 3, 4)),
										 List.of(1, 2, 3, 4)),
								 new CollectionTypeTestCase<>("HashSet with strings", new HashSet<>(Set.of("x", "y", "z")),
										 Set.of("x", "y", "z")),
								 new CollectionTypeTestCase<>("TreeSet with integers", new TreeSet<>(List.of(3, 1, 4, 1, 5)),
										 Set.of(1, 3, 4, 5)), // TreeSet removes duplicates and sorts
								 new CollectionTypeTestCase<>("Vector with doubles", new Vector<>(List.of(1.1, 2.2, 3.3)),
										 List.of(1.1, 2.2, 3.3)))
						 .map(tc -> Arguments.of(tc.description, tc.inputCollection, tc.expectedElements));
		}

		private static Stream<Arguments> edgeCaseTestCases()
		{
			return Stream.of(new EdgeCaseTestCase<>("empty ArrayList", new ArrayList<>(), Collections.emptyList()),
								 new EdgeCaseTestCase<>("empty HashSet", new HashSet<>(), Collections.emptyList()),
								 new EdgeCaseTestCase<>("single null element", Collections.singletonList(null),
										 Collections.singletonList(null)),
								 new EdgeCaseTestCase<>("multiple null elements", Arrays.asList(null, null, null),
										 Arrays.asList(null, null, null)),
								 new EdgeCaseTestCase<>("mixed null and non-null", Arrays.asList("a", null, "b", null),
										 Arrays.asList("a", null, "b", null)),
								 new EdgeCaseTestCase<>("collection with duplicates", Arrays.asList("dup", "dup", "other", "dup"),
										 Arrays.asList("dup", "dup", "other", "dup")),
								 new EdgeCaseTestCase<>("single element collection", Collections.singletonList("single"),
										 Collections.singletonList("single")))
						 .map(tc -> Arguments.of(tc.description, tc.inputCollection, tc.expectedElements));
		}

		private record CollectionTypeTestCase<E>(String description, Collection<E> inputCollection,
												 Collection<E> expectedElements)
		{
		}

		private record EdgeCaseTestCase<E>(String description, Collection<E> inputCollection,
										   Collection<E> expectedElements)
		{
		}
	}
}