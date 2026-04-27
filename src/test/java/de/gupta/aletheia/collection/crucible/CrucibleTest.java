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

@DisplayName("Tests for Crucible interface")
final class CrucibleTest
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
			Relic<E> result = Crucible.consecrate(collection);

			assertThat(result).as("consecrate() for %s should create a Relic", description)
							  .isInstanceOf(Relic.class)
							  .isNotNull();
		}

		@Test
		@DisplayName("should handle null collection gracefully")
		void shouldHandleNullCollectionGracefully()
		{
			assertThatThrownBy(() -> Crucible.consecrate(null))
					.as("consecrate() with null collection should throw an exception")
					.isInstanceOf(NullPointerException.class)
					.hasMessage("elements may not be null");
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("empty ArrayList", new ArrayList<>()),
					new TestCase<>("ArrayList with single element", List.of("element")),
					new TestCase<>("ArrayList with multiple elements", List.of("sinister", "dexter", "third")),
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
	@DisplayName("Tests for kindle() static factory method")
	final class KindleTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("testCases")
		@DisplayName("should create Forge from collection")
		<E> void shouldCreateForgeFromCollection(String description, Collection<E> collection)
		{
			Forge<E> result = Crucible.kindle(collection);

			assertThat(result).as("kindle() for %s should create a Forge", description)
							  .isInstanceOf(Forge.class)
							  .isNotNull();
		}

		@Test
		@DisplayName("should throw exception for null collection")
		void shouldThrowExceptionForNullCollection()
		{
			Collection<String> nullCollection = null;

			assertThatThrownBy(() -> Crucible.kindle(nullCollection))
					.as("kindle() with null collection should throw NullPointerException")
					.isInstanceOf(NullPointerException.class);
		}

		private static Stream<Arguments> testCases()
		{
			return Stream.of(
					new TestCase<>("empty ArrayList", new ArrayList<>()),
					new TestCase<>("ArrayList with single element", List.of("element")),
					new TestCase<>("ArrayList with multiple elements", List.of("sinister", "dexter", "third")),
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
	@DisplayName("Tests for interface contract and polymorphism")
	final class InterfaceContractTests
	{
		@Test
		@DisplayName("should maintain sealed interface contract")
		void shouldMaintainSealedInterfaceContract()
		{
			Crucible<String> forge = Crucible.kindle(List.of("test"));
			Crucible<String> relic = Crucible.consecrate(List.of("test"));

			assertThat(forge).as("Forge should implement Crucible")
							 .isInstanceOf(Crucible.class)
							 .isInstanceOf(Forge.class);

			assertThat(relic).as("Relic should implement Crucible")
							 .isInstanceOf(Crucible.class)
							 .isInstanceOf(Relic.class);
		}

		@Test
		@DisplayName("should support polymorphic usage")
		void shouldSupportPolymorphicUsage()
		{
			List<Crucible<Integer>> crucibles = List.of(
					Crucible.kindle(List.of(1, 2, 3)),
					Crucible.consecrate(List.of(4, 5, 6))
			);

			assertThat(crucibles).as("List should contain both Forge and Relic instances")
								 .hasSize(2)
								 .allSatisfy(crucible -> assertThat(crucible).isInstanceOf(Crucible.class));
		}
	}
}