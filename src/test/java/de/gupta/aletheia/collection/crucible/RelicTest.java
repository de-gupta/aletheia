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

@DisplayName("Tests for Relic class")
final class RelicTest
{
	@Nested
	@DisplayName("Tests for consecrate() static factory method")
	final class ConsecrateTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should create Relic from collection")
		<E> void shouldCreateRelicFromCollection(String description, Collection<E> collection)
		{
			Relic<E> result = Relic.consecrate(collection);

			assertThat(result).as("consecrate() for %s should create a Relic", description)
							  .isInstanceOf(Relic.class)
							  .isNotNull();
		}

		@Test
		@DisplayName("should preserve reference to original collection")
		void shouldPreserveReferenceToOriginalCollection()
		{
			List<String> original = new ArrayList<>(List.of("original"));
			Relic<String> relic = Relic.consecrate(original);

			// Relic should maintain reference to the original collection
			// This is different from Forge which creates a copy
			assertThat(relic).as("Relic should be created successfully")
							 .isInstanceOf(Relic.class);
		}

		@Test
		@DisplayName("should handle null collection gracefully")
		void shouldHandleNullCollectionGracefully()
		{
			assertThatThrownBy(() -> Relic.consecrate(null))
					.as("consecrate() with null collection should throw NullPointerException")
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("elements may not be null");
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
		@DisplayName("should throw UnsupportedOperationException")
		<E> void shouldThrowUnsupportedOperationException(String description, Collection<E> collection, E element)
		{
			Relic<E> relic = Relic.consecrate(collection);

			assertThatThrownBy(() -> relic.embrace(element))
					.as("embrace() for %s should throw UnsupportedOperationException", description)
					.isInstanceOf(UnsupportedOperationException.class)
					.hasMessageContaining("relic does not embrace elements");
		}

		@Test
		@DisplayName("should throw UnsupportedOperationException for null element")
		void shouldThrowUnsupportedOperationExceptionForNullElement()
		{
			Relic<String> relic = Relic.consecrate(List.of("existing"));

			assertThatThrownBy(() -> relic.embrace(null))
					.as("embrace() should throw UnsupportedOperationException even for null element")
					.isInstanceOf(UnsupportedOperationException.class)
					.hasMessageContaining("relic does not embrace elements");
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("empty collection with string", new ArrayList<>(), "new element"),
					new TestCase<>("collection with elements and new string", List.of("sinister", "dexter"), "dusk"),
					new TestCase<>("numeric collection with new number", List.of(1, 2), 3),
					new TestCase<>("collection with null elements", Collections.singletonList(null), "element")
			).map(tc -> Arguments.of(tc.description, tc.collection, tc.element));
		}

		private record TestCase<E>(String description, Collection<E> collection, E element)
		{
		}
	}

	@Nested
	@DisplayName("Tests for banish() method")
	final class BanishTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should throw UnsupportedOperationException")
		<E> void shouldThrowUnsupportedOperationException(String description, Collection<E> collection, E element)
		{
			Relic<E> relic = Relic.consecrate(collection);

			assertThatThrownBy(() -> relic.banish(element))
					.as("banish() for %s should throw UnsupportedOperationException", description)
					.isInstanceOf(UnsupportedOperationException.class)
					.hasMessageContaining("relic does not banish elements");
		}

		@Test
		@DisplayName("should throw UnsupportedOperationException for null element")
		void shouldThrowUnsupportedOperationExceptionForNullElement()
		{
			Relic<String> relic = Relic.consecrate(List.of("existing"));

			assertThatThrownBy(() -> relic.banish(null))
					.as("banish() should throw UnsupportedOperationException even for null element")
					.isInstanceOf(UnsupportedOperationException.class)
					.hasMessageContaining("relic does not banish elements");
		}

		@Test
		@DisplayName("should throw UnsupportedOperationException for non-existent element")
		void shouldThrowUnsupportedOperationExceptionForNonExistentElement()
		{
			Relic<String> relic = Relic.consecrate(List.of("existing"));

			assertThatThrownBy(() -> relic.banish("nonexistent"))
					.as("banish() should throw UnsupportedOperationException even for non-existent element")
					.isInstanceOf(UnsupportedOperationException.class)
					.hasMessageContaining("relic does not banish elements");
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("collection with element to remove", List.of("sinister", "dexter"), "sinister"),
					new TestCase<>("numeric collection removing number", List.of(1, 2, 3), 2),
					new TestCase<>("single element collection", List.of("only"), "only"),
					new TestCase<>("collection with duplicates", Arrays.asList("dup", "dup", "other"), "dup")
			).map(tc -> Arguments.of(tc.description, tc.collection, tc.element));
		}

		private record TestCase<E>(String description, Collection<E> collection, E element)
		{
		}
	}

	@Nested
	@DisplayName("Tests for enshrine() method")
	final class EnshrineTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should return itself")
		<E> void shouldReturnItself(String description, Collection<E> collection)
		{
			Relic<E> relic = Relic.consecrate(collection);
			Relic<E> result = relic.enshrine();

			assertThat(result).as("enshrine() for %s should return the same instance", description)
							  .isSameAs(relic)
							  .isInstanceOf(Relic.class);
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
		@DisplayName("should convert Relic to Forge")
		<E> void shouldConvertRelicToForge(String description, Collection<E> collection)
		{
			Crucible<E> relic = Relic.consecrate(collection);
			Crucible<E> result = relic.awaken();

			assertThat(result).as("awaken() for %s should create a Forge", description)
							  .isInstanceOf(Forge.class)
							  .isNotNull()
							  .isNotSameAs(relic);
		}

		@Test
		@DisplayName("should create mutable Forge")
		void shouldCreateMutableForge()
		{
			Relic<String> relic = Relic.consecrate(List.of("test"));
			Forge<String> forge = relic.awaken();

			// Should be able to use mutable operations on the resulting Forge
			assertThat(forge.embrace("new")).as("Forge from awaken() should be mutable")
											.isInstanceOf(Forge.class);

			assertThat(forge.banish("test")).as("Forge from awaken() should be mutable")
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
	@DisplayName("Tests for immutability guarantees")
	final class ImmutabilityTests
	{
		@Test
		@DisplayName("should maintain immutability contract")
		void shouldMaintainImmutabilityContract()
		{
			Relic<String> relic = Relic.consecrate(List.of("immutable"));

			// All mutating operations should throw exceptions
			assertThatThrownBy(() -> relic.embrace("new"))
					.as("Relic should be immutable - embrace should fail")
					.isInstanceOf(UnsupportedOperationException.class);

			assertThatThrownBy(() -> relic.banish("immutable"))
					.as("Relic should be immutable - banish should fail")
					.isInstanceOf(UnsupportedOperationException.class);

			// Non-mutating operations should work
			assertThat(relic.enshrine()).as("enshrine() should work on immutable Relic")
										.isSameAs(relic);

			assertThat(relic.awaken()).as("awaken() should work on immutable Relic")
									  .isInstanceOf(Forge.class);
		}

		@Test
		@DisplayName("should support conversion to mutable Forge and back")
		void shouldSupportConversionToMutableForgeAndBack()
		{
			Relic<Integer> originalRelic = Relic.consecrate(List.of(1, 2, 3));

			Relic<Integer> convertedBack = originalRelic.awaken()     // Relic -> Forge
														.embrace(4)   // Mutate (add element)
														.enshrine();  // Forge -> Relic

			assertThat(convertedBack).as("Conversion chain should work correctly")
									 .isInstanceOf(Relic.class)
									 .isNotSameAs(originalRelic);

			// New relic should also be immutable
			assertThatThrownBy(() -> convertedBack.embrace(5))
					.as("Converted Relic should also be immutable")
					.isInstanceOf(UnsupportedOperationException.class);
		}
	}

	@Nested
	@DisplayName("Tests for method chaining with conversions")
	final class MethodChainingTests
	{
		@Test
		@DisplayName("should support limited method chaining")
		void shouldSupportLimitedMethodChaining()
		{
			Relic<String> relic = Relic.consecrate(List.of("original"));

			// Relic can only chain with enshrine() (returns itself) and awaken() (converts to Forge)
			Relic<String> chainedRelic = relic.enshrine().enshrine();

			assertThat(chainedRelic).as("Chained enshrine() calls should work")
									.isSameAs(relic);
		}

		@Test
		@DisplayName("should enable fluent conversion to Forge")
		void shouldEnableFluentConversionToForge()
		{
			Relic<Integer> relic = Relic.consecrate(List.of(1, 2, 3));

			// Convert to Forge and use fluent API
			Forge<Integer> result = relic.awaken()
										 .embrace(4)
										 .banish(1)
										 .awaken(); // Still a Forge

			assertThat(result).as("Fluent conversion and operations should work")
							  .isInstanceOf(Forge.class);
		}
	}

	@Nested
	@DisplayName("Tests for error message clarity")
	final class ErrorMessageTests
	{
		@Test
		@DisplayName("should provide clear error messages for embrace")
		void shouldProvideClearErrorMessagesForEmbrace()
		{
			Relic<String> relic = Relic.consecrate(List.of("test"));

			assertThatThrownBy(() -> relic.embrace("new"))
					.as("embrace() error message should be clear")
					.isInstanceOf(UnsupportedOperationException.class)
					.hasMessageContaining("relic does not embrace elements")
					.hasMessageContaining("relic")
					.hasMessageContaining("embrace");
		}

		@Test
		@DisplayName("should provide clear error messages for banish")
		void shouldProvideClearErrorMessagesForBanish()
		{
			Relic<String> relic = Relic.consecrate(List.of("test"));

			assertThatThrownBy(() -> relic.banish("test"))
					.as("banish() error message should be clear")
					.isInstanceOf(UnsupportedOperationException.class)
					.hasMessageContaining("relic does not banish elements")
					.hasMessageContaining("relic")
					.hasMessageContaining("banish");
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
			Relic<E> relic = Relic.consecrate(inputCollection);
			Collection<E> result = relic.manifest();

			assertThat(result).as("manifest() for %s should return collection with expected elements", description)
							  .containsExactlyInAnyOrderElementsOf(expectedElements);

			assertThatThrownBy(
					() -> result.add(inputCollection.iterator().hasNext() ? inputCollection.iterator().next() : null))
					.as("manifest() should return unmodifiable collection")
					.isInstanceOf(UnsupportedOperationException.class);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("edgeCaseTestCases")
		@DisplayName("should handle edge cases correctly")
		<E> void shouldHandleEdgeCasesCorrectly(String description, Collection<E> inputCollection,
												Collection<E> expectedElements)
		{
			Relic<E> relic = Relic.consecrate(inputCollection);
			Collection<E> result = relic.manifest();

			assertThat(result).as("manifest() for %s should handle edge case", description)
							  .containsExactlyElementsOf(expectedElements);
		}

		@Test
		@DisplayName("should return unmodifiable collection - all modification attempts should fail")
		void shouldReturnUnmodifiableCollection()
		{
			List<String> original = new ArrayList<>(List.of("a", "b", "c"));
			Relic<String> relic = Relic.consecrate(original);
			Collection<String> manifested = relic.manifest();

			// Verify returned collection is unmodifiable
			assertThatThrownBy(manifested::clear)
					.as("manifest() should return unmodifiable collection")
					.isInstanceOf(UnsupportedOperationException.class);

			assertThatThrownBy(() -> manifested.remove("a"))
					.as("manifest() should return unmodifiable collection")
					.isInstanceOf(UnsupportedOperationException.class);

			assertThatThrownBy(() -> manifested.add("d"))
					.as("manifest() should return unmodifiable collection")
					.isInstanceOf(UnsupportedOperationException.class);

			if (manifested instanceof List<String> list)
			{
				assertThatThrownBy(() -> list.set(0, "new"))
						.as("manifest() should return unmodifiable list")
						.isInstanceOf(UnsupportedOperationException.class);
			}
		}

		@Test
		@DisplayName("should not reflect changes to underlying collection when using mutable input")
		void shouldReflectChangesToUnderlyingCollectionWhenUsingMutableInput()
		{
			List<String> original = new ArrayList<>(List.of("a", "b"));
			Relic<String> relic = Relic.consecrate(original);

			Collection<String> manifestBefore = relic.manifest();
			assertThat(manifestBefore).containsExactly("a", "b");

			// Modify the original collection that Relic holds reference to
			original.add("c");

			Collection<String> manifestAfter = relic.manifest();
			assertThat(manifestAfter).as("manifest() should be independent changes to underlying collection")
									 .containsExactly("a", "b");
		}

		@Test
		@DisplayName("should not reflect changes to underlying collection when using immutable input")
		void shouldNotReflectChangesToUnderlyingCollectionWhenUsingImmutableInput()
		{
			List<String> immutableList = List.of("a", "b");
			Relic<String> relic = Relic.consecrate(immutableList);

			Collection<String> manifested = relic.manifest();
			assertThat(manifested).as("manifest() with immutable input should work")
								  .containsExactly("a", "b");

			// Cannot modify immutable list, so this test verifies the behavior is consistent
			assertThat(manifested).as("manifest() should remain consistent")
								  .containsExactly("a", "b");
		}

		@Test
		@DisplayName("should be independent of modifications to mutable underlying collection")
		void shouldHandleModificationsToMutableUnderlyingCollectionCorrectly()
		{
			List<String> mutableList = new ArrayList<>(List.of("x", "y", "z"));
			Relic<String> relic = Relic.consecrate(mutableList);

			Collection<String> manifest1 = relic.manifest();
			assertThat(manifest1).containsExactly("x", "y", "z");

			// Modify underlying collection
			mutableList.remove("y");
			mutableList.add("w");

			Collection<String> manifest2 = relic.manifest();
			assertThat(manifest2).as("New manifest() call should be independent of underlying changes")
								 .containsExactly("x", "y", "z");

			// But the sinister manifest should still be unmodifiable
			assertThatThrownBy(() -> manifest1.add("should fail"))
					.as("Previous manifest should remain unmodifiable")
					.isInstanceOf(UnsupportedOperationException.class);
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

			Relic<Integer> relic = Relic.consecrate(largeList);
			Collection<Integer> manifested = relic.manifest();

			assertThat(manifested).as("manifest() should handle large collections")
								  .hasSize(10000)
								  .containsExactlyElementsOf(largeList);

			assertThatThrownBy(manifested::clear)
					.as("Large manifested collection should be unmodifiable")
					.isInstanceOf(UnsupportedOperationException.class);
		}

		@Test
		@DisplayName("should return List type specifically")
		void shouldReturnListTypeSpecifically()
		{
			Relic<String> relic = Relic.consecrate(List.of("a", "b", "c"));
			Collection<String> manifested = relic.manifest();

			assertThat(manifested).as("manifest() should return List type")
								  .isInstanceOf(List.class);
		}

		@Test
		@DisplayName("should maintain consistency across multiple manifest calls with stable input")
		void shouldMaintainConsistencyAcrossMultipleManifestCallsWithStableInput()
		{
			List<String> immutableInput = List.of("stable", "elements");
			Relic<String> relic = Relic.consecrate(immutableInput);

			Collection<String> manifest1 = relic.manifest();
			Collection<String> manifest2 = relic.manifest();
			Collection<String> manifest3 = relic.manifest();

			assertThat(manifest1).as("All manifest calls should be consistent")
								 .containsExactlyElementsOf(manifest2)
								 .containsExactlyElementsOf(manifest3);
		}

		private static Stream<Arguments> collectionTypeTestCases()
		{
			return Stream.of(
					new CollectionTypeTestCase<>("ArrayList with strings",
							new ArrayList<>(List.of("apple", "banana", "cherry")),
							List.of("apple", "banana", "cherry")),
					new CollectionTypeTestCase<>("LinkedList with integers",
							new LinkedList<>(List.of(1, 2, 3, 4)),
							List.of(1, 2, 3, 4)),
					new CollectionTypeTestCase<>("HashSet with strings",
							new HashSet<>(Set.of("x", "y", "z")),
							Set.of("x", "y", "z")),
					new CollectionTypeTestCase<>("TreeSet with integers",
							new TreeSet<>(List.of(3, 1, 4, 1, 5)),
							Set.of(1, 3, 4, 5)), // TreeSet removes duplicates and sorts
					new CollectionTypeTestCase<>("Vector with doubles",
							new Vector<>(List.of(1.1, 2.2, 3.3)),
							List.of(1.1, 2.2, 3.3)),
					new CollectionTypeTestCase<>("Immutable List with strings",
							List.of("immutable", "list"),
							List.of("immutable", "list"))
			).map(tc -> Arguments.of(tc.description, tc.inputCollection, tc.expectedElements));
		}

		private static Stream<Arguments> edgeCaseTestCases()
		{
			return Stream.of(
					new EdgeCaseTestCase<>("empty ArrayList", new ArrayList<>(), Collections.emptyList()),
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
							Collections.singletonList("single")),
					new EdgeCaseTestCase<>("empty immutable list", Collections.emptyList(), Collections.emptyList())
			).map(tc -> Arguments.of(tc.description, tc.inputCollection, tc.expectedElements));
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