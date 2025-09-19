package de.gupta.aletheia.collection.crucible;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests for metamorphose method")
final class MetamorphoseTest
{
	@Nested
	@DisplayName("Tests for Relic metamorphose behavior")
	final class RelicMetamorphoseTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("transformationTestCases")
		@DisplayName("should transform elements and return new Relic")
		<T, R> void shouldTransformElementsAndReturnNewRelic(String description, Collection<T> input,
															 Function<T, R> transformation, Collection<R> expected)
		{
			Relic<T> relic = Crucible.consecrate(input);

			Crucible<R> result = relic.metamorphose(transformation);

			assertThat(result).as("metamorphose() on Relic should return a Relic")
							  .isInstanceOf(Relic.class);

			assertThat(result.manifest()).as("transformed elements should match expected for %s", description)
										 .containsExactlyElementsOf(expected);
		}

		@Test
		@DisplayName("should handle null transformation gracefully")
		void shouldHandleNullTransformationGracefully()
		{
			Relic<String> relic = Crucible.consecrate(List.of("test"));
			Function<String, Integer> nullTransformation = null;

			assertThatThrownBy(() -> relic.metamorphose(nullTransformation))
					.as("metamorphose() with null transformation should throw NullPointerException")
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		@DisplayName("should preserve immutability after transformation")
		void shouldPreserveImmutabilityAfterTransformation()
		{
			Relic<Integer> original = Crucible.consecrate(List.of(1, 2, 3));

			Crucible<String> transformed = original.metamorphose(Object::toString);

			assertThat(original.manifest()).as("original Relic should remain unchanged")
										   .containsExactly(1, 2, 3);

			assertThat(transformed.manifest()).as("transformed Relic should contain new values")
											  .containsExactly("1", "2", "3");
		}

		private static Stream<Arguments> transformationTestCases()
		{
			return Stream.of(
					new TransformationTestCase<>("empty collection",
							Collections.emptyList(),
							(Integer x) -> x.toString(),
							Collections.emptyList()),
					new TransformationTestCase<>("single element transformation",
							List.of(42),
							(Integer x) -> x * 2,
							List.of(84)),
					new TransformationTestCase<>("multiple elements to strings",
							List.of(1, 2, 3),
							Object::toString,
							List.of("1", "2", "3")),
					new TransformationTestCase<>("strings to lengths",
							List.of("hello", "world", "test"),
							String::length,
							List.of(5, 5, 4)),
					new TransformationTestCase<>("collection with nulls",
							Arrays.asList(1, null, 3),
							(Integer x) -> x == null ? "null" : x.toString(),
							List.of("1", "null", "3"))
			).map(tc -> Arguments.of(tc.description, tc.input, tc.transformation, tc.expected));
		}

		private record TransformationTestCase<T, R>(String description, Collection<T> input,
													Function<T, R> transformation, Collection<R> expected)
		{
		}
	}

	@Nested
	@DisplayName("Tests for Forge metamorphose behavior")
	final class ForgeMetamorphoseTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("transformationTestCases")
		@DisplayName("should transform elements and return new Forge")
		<T, R> void shouldTransformElementsAndReturnNewForge(String description, Collection<T> input,
															 Function<T, R> transformation, Collection<R> expected)
		{
			Forge<T> forge = Crucible.kindle(input);

			Crucible<R> result = forge.metamorphose(transformation);

			assertThat(result).as("metamorphose() on Forge should return a Forge")
							  .isInstanceOf(Forge.class);

			assertThat(result.manifest()).as("transformed elements should match expected for %s", description)
										 .containsExactlyElementsOf(expected);
		}

		@Test
		@DisplayName("should handle null transformation gracefully")
		void shouldHandleNullTransformationGracefully()
		{
			Forge<String> forge = Crucible.kindle(List.of("test"));
			Function<String, Integer> nullTransformation = null;

			assertThatThrownBy(() -> forge.metamorphose(nullTransformation))
					.as("metamorphose() with null transformation should throw NullPointerException")
					.isInstanceOf(NullPointerException.class);
		}

		@Test
		@DisplayName("should create independent Forge after transformation")
		void shouldCreateIndependentForgeAfterTransformation()
		{
			Forge<Integer> original = Crucible.kindle(new ArrayList<>(List.of(1, 2, 3)));

			Crucible<String> transformed = original.metamorphose(Object::toString);

			assertThat(original.manifest()).as("original Forge should remain unchanged")
										   .containsExactly(1, 2, 3);

			assertThat(transformed.manifest()).as("transformed Forge should contain new values")
											  .containsExactly("1", "2", "3");
		}

		private static Stream<Arguments> transformationTestCases()
		{
			return Stream.of(
					new TransformationTestCase<>("empty collection",
							Collections.emptyList(),
							(Integer x) -> x.toString(),
							Collections.emptyList()),
					new TransformationTestCase<>("single element transformation",
							List.of(42),
							(Integer x) -> x * 2,
							List.of(84)),
					new TransformationTestCase<>("multiple elements to strings",
							List.of(1, 2, 3),
							Object::toString,
							List.of("1", "2", "3")),
					new TransformationTestCase<>("strings to lengths",
							List.of("hello", "world", "test"),
							String::length,
							List.of(5, 5, 4)),
					new TransformationTestCase<>("collection with nulls",
							Arrays.asList(1, null, 3),
							(Integer x) -> x == null ? "null" : x.toString(),
							List.of("1", "null", "3"))
			).map(tc -> Arguments.of(tc.description, tc.input, tc.transformation, tc.expected));
		}

		private record TransformationTestCase<T, R>(String description, Collection<T> input,
													Function<T, R> transformation, Collection<R> expected)
		{
		}
	}

	@Nested
	@DisplayName("Tests for manifest method functionality")
	final class ManifestFunctionalityTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("manifestTestCases")
		@DisplayName("should return unmodifiable collection copy")
		void shouldReturnUnmodifiableCollectionCopy(String description, Collection<String> input, String crucibleType)
		{
			Crucible<String> crucible = "Relic".equals(crucibleType)
					? Crucible.consecrate(input)
					: Crucible.kindle(input);

			Collection<String> result = crucible.manifest();

			assertThat(result).as("manifest() should return collection with same elements for %s", description)
							  .containsExactlyElementsOf(input);

			assertThatThrownBy(() -> result.add(null))
					.as("manifest() should return unmodifiable collection")
					.isInstanceOf(UnsupportedOperationException.class);
		}

		@Test
		@DisplayName("should return defensive copy that is independent from original")
		void shouldReturnDefensiveCopyThatIsIndependentFromOriginal()
		{
			List<String> originalList = new ArrayList<>(List.of("original"));
			Forge<String> forge = Crucible.kindle(originalList);

			Collection<String> manifestBefore = forge.manifest();
			originalList.add("modified");
			Collection<String> manifestAfter = forge.manifest();

			assertThat(manifestBefore).as("first manifest call should contain original elements")
									  .containsExactly("original");

			assertThat(manifestAfter).as("manifest should be independent of original list modifications")
									 .containsExactly("original");
		}

		@Test
		@DisplayName("should handle empty collections correctly")
		void shouldHandleEmptyCollectionsCorrectly()
		{
			Crucible<String> emptyRelic = Crucible.consecrate(Collections.emptyList());
			Crucible<String> emptyForge = Crucible.kindle(Collections.emptyList());

			assertThat(emptyRelic.manifest()).as("empty Relic manifest should be empty")
											 .isEmpty();

			assertThat(emptyForge.manifest()).as("empty Forge manifest should be empty")
											 .isEmpty();
		}

		private static Stream<Arguments> manifestTestCases()
		{
			return Stream.of(
					new TestCase("Relic with single element", List.of("test"), "Relic"),
					new TestCase("Relic with multiple elements", List.of("a", "b", "c"), "Relic"),
					new TestCase("Relic with null element", Arrays.asList("test", null), "Relic"),
					new TestCase("Forge with single element", List.of("test"), "Forge"),
					new TestCase("Forge with multiple elements", List.of("a", "b", "c"), "Forge"),
					new TestCase("Forge with null element", Arrays.asList("test", null), "Forge")
			).map(tc -> Arguments.of(tc.description, tc.input, tc.crucibleType));
		}

		private record TestCase(String description, Collection<String> input, String crucibleType)
		{
		}
	}
}