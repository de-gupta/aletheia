package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cascade#toList and Cascade#toSet")
final class CascadeToListSetTest
{
	@Nested
	@DisplayName("toList() — returns typed List")
	final class ToList
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@Test
			@DisplayName("returns all elements as an unmodifiable List in encounter order")
			void returnsAllElementsInOrder()
			{
				assertThat(Cascade.beckon(3, 1, 4, 1, 5).toList())
						.as("elements in encounter order including duplicates")
						.containsExactly(3, 1, 4, 1, 5);
			}

			@Test
			@DisplayName("preserves duplicates — does not deduplicate")
			void preservesDuplicates()
			{
				assertThat(Cascade.beckon(1, 1, 2, 2).toList())
						.as("duplicates preserved")
						.containsExactly(1, 1, 2, 2);
			}

			@Test
			@DisplayName("returns a List — not just Collection")
			void returnsListType()
			{
				assertThat(Cascade.beckon("a", "b").toList())
						.as("result is a List")
						.isInstanceOf(java.util.List.class);
			}

			@Test
			@DisplayName("single element returns single-element list")
			void singleElementReturnsSingleElementList()
			{
				assertThat(Cascade.beckon(42).toList())
						.as("single element list")
						.containsExactly(42);
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns empty List — does not throw")
			void returnsEmptyList()
			{
				assertThat(Cascade.abyss().toList())
						.as("absent cascade gives empty list without throwing")
						.isEmpty();
			}

			@Test
			@DisplayName("result is a List instance")
			void resultIsListInstance()
			{
				assertThat(Cascade.abyss().toList())
						.isInstanceOf(java.util.List.class);
			}
		}
	}

	@Nested
	@DisplayName("toSet() — returns typed Set")
	final class ToSet
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@Test
			@DisplayName("returns all distinct elements as a Set")
			void returnsDistinctElements()
			{
				assertThat(Cascade.beckon(1, 2, 3, 2, 1).toSet())
						.as("duplicates removed by Set semantics")
						.containsExactlyInAnyOrder(1, 2, 3);
			}

			@Test
			@DisplayName("returns a Set — not just Collection")
			void returnsSetType()
			{
				assertThat(Cascade.beckon("a", "b").toSet())
						.as("result is a Set")
						.isInstanceOf(java.util.Set.class);
			}

			@Test
			@DisplayName("all elements distinct — set equals cascade content")
			void allDistinctElementsPreserved()
			{
				assertThat(Cascade.beckon(10, 20, 30).toSet())
						.as("all distinct — same elements")
						.containsExactlyInAnyOrder(10, 20, 30);
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns empty Set — does not throw")
			void returnsEmptySet()
			{
				assertThat(Cascade.abyss().toSet())
						.as("absent cascade gives empty set without throwing")
						.isEmpty();
			}
		}
	}
}
