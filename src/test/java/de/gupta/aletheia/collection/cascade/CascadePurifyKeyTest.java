package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade#purify(Function) — distinct by key")
final class CascadePurifyKeyTest
{
	@Nested
	@DisplayName("when Cascade is present")
	final class WhenCascadeIsPresent
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("deduplicatesByKeyCases")
		@DisplayName("retains first occurrence of each distinct key, drops subsequent duplicates")
		void deduplicatesByKey(final String as, final PurifyKeyCase tc)
		{
			assertThat(tc.source().purify(tc.essence()).summon())
					.as(as)
					.containsExactlyElementsOf(tc.expected());
		}

		@Test
		@DisplayName("no elements share a key — cascade unchanged")
		void noDuplicatesUnchanged()
		{
			assertThat(Cascade.beckon("cat", "elephant", "on")
			                  .purify(String::length)
			                  .summon())
					.as("lengths 3, 8, 2 are all distinct — no deduplication")
					.containsExactly("cat", "elephant", "on");
		}

		@Test
		@DisplayName("uses composite key extractor")
		void usesCompositeKeyExtractor()
		{
			record Item(String category, int value)
			{
			}

			assertThat(Cascade.beckon(
					new Item("a", 1),
					new Item("b", 2),
					new Item("a", 3),
					new Item("b", 4)
			).purify(Item::category).summon())
					.as("first 'a' and first 'b' kept")
					.containsExactly(new Item("a", 1), new Item("b", 2));
		}

		@Test
		@DisplayName("first occurrence is kept — not any other")
		void firstOccurrenceIsKept()
		{
			assertThat(Cascade.beckon(10, 20, 30, 40)
			                  .purify(n -> n % 2 == 0)
			                  .summon())
					.as("first even and first odd kept (all are even here, so only 10)")
					.containsExactly(10);
		}

		@Test
		@DisplayName("encounter order is preserved for retained elements")
		void encounterOrderPreservedForRetained()
		{
			// lengths: elephant=8, ant=3, ox=2, bee=3(dup), cow=3(dup)
			assertThat(Cascade.beckon("elephant", "ant", "ox", "bee", "cow")
			                  .purify(String::length)
			                  .summon())
					.as("first of each length in encounter order: 8=elephant, 3=ant, 2=ox")
					.containsExactly("elephant", "ant", "ox");
		}

		@Test
		@DisplayName("distinctBy() delegates to purify(Function)")
		void distinctByDelegatesToPurify()
		{
			final Cascade<String> source = Cascade.beckon("cat", "dog", "ant", "fox");

			assertThat(source.distinctBy(String::length).summon())
					.as("distinctBy() must equal purify(Function)")
					.containsExactlyElementsOf(source.purify(String::length).summon());
		}

		@Test
		@DisplayName("throws when essence is null")
		void throwsWhenEssenceIsNull()
		{
			assertThatThrownBy(() -> Cascade.beckon("a").purify((Function<String, ?>) null))
					.isInstanceOf(NullPointerException.class)
					.hasMessageContaining("essence may not be null");
		}

		@Test
		@DisplayName("works with non-String element type — deduplicates integers by parity")
		void deduplicatesIntegersByParity()
		{
			assertThat(Cascade.beckon(1, 2, 3, 4, 5, 6).purify(n -> n % 2).summon())
					.as("first odd and first even kept")
					.containsExactly(1, 2);
		}

		private static Stream<Arguments> deduplicatesByKeyCases()
		{
			return Stream.of(
					new PurifyKeyCase("deduplicate strings by length",
							Cascade.beckon("cat", "dog", "ant", "elephant"),
							String::length,
							List.of("cat", "elephant")),
					new PurifyKeyCase("single element — no deduplication",
							Cascade.beckon("only"),
							String::length,
							List.of("only")),
					new PurifyKeyCase("adjacent duplicates by key",
							Cascade.beckon("a", "b", "c", "d"),
							s -> s.compareTo("b") < 0 ? "before" : "after",
							List.of("a", "b")),
					new PurifyKeyCase("all same key — only first kept",
							Cascade.beckon("a", "b", "c"),
							String::length,
							List.of("a"))
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record PurifyKeyCase(String as, Cascade<String> source,
		                             Function<String, ?> essence, List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("when Cascade is empty")
	final class WhenCascadeIsEmpty
	{
		@Test
		@DisplayName("returns absent Cascade")
		void returnsAbsent()
		{
			assertThat(Cascade.<String>abyss().purify(String::length).sterile())
					.as("purify(Function) on absent cascade stays absent")
					.isTrue();
		}
	}
}