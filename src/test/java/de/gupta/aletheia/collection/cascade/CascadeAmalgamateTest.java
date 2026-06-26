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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade#amalgamate")
final class CascadeAmalgamateTest
{
	private static Cascade<Term> combine(final Cascade<Term> source)
	{
		return source.amalgamate(Term::element, (a, b) -> Term.of(a.element(), a.coefficient() + b.coefficient()));
	}

	private record Term(String element, int coefficient)
	{
		static Term of(final String element, final int coefficient)
		{
			return new Term(element, coefficient);
		}
	}

	@Nested
	@DisplayName("amalgamate(essence, confluence) — combine like terms")
	final class WithoutDissolution
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("combinesLikeTermsCases")
			@DisplayName("combines elements sharing the same key via confluence")
			void combinesLikeTerms(final String as, final AmalgamateCase tc)
			{
				final List<Term> result = combine(tc.source()).summon().stream().toList();

				assertThat(result)
						.as(as)
						.containsExactlyInAnyOrderElementsOf(tc.expected());
			}

			@Test
			@DisplayName("preserves elements with distinct keys unchanged")
			void preservesDistinctKeyElements()
			{
				final Cascade<Term> source = Cascade.beckon(Term.of("x", 2), Term.of("y", 3));

				final List<Term> result = combine(source).summon().stream().toList();

				assertThat(result)
						.as("distinct keys — no combination should occur")
						.containsExactlyInAnyOrder(Term.of("x", 2), Term.of("y", 3));
			}

			@Test
			@DisplayName("combines string values by concatenation")
			void combinesStringValuesByConcatenation()
			{
				record Phrase(String topic, String text)
				{
				}

				final Cascade<Phrase> source = Cascade.beckon(
						new Phrase("greeting", "hello"),
						new Phrase("greeting", " world"),
						new Phrase("farewell", "goodbye")
				);

				final List<Phrase> result = source.amalgamate(
						Phrase::topic,
						(a, b) -> new Phrase(a.topic(), a.text() + b.text())
				).summon().stream().toList();

				assertThat(result)
						.as("greeting phrases concatenated; farewell unchanged")
						.containsExactlyInAnyOrder(new Phrase("greeting", "hello world"),
								new Phrase("farewell", "goodbye"));
			}

			@Test
			@DisplayName("combines list values by union")
			void combinesListValuesByUnion()
			{
				record Bucket(String label, List<Integer> items)
				{
				}

				final Cascade<Bucket> source = Cascade.beckon(
						new Bucket("a", List.of(1, 2)),
						new Bucket("b", List.of(10)),
						new Bucket("a", List.of(3, 4))
				);

				final List<Bucket> result = source.amalgamate(
						Bucket::label,
						(x, y) ->
						{
							final var merged = new java.util.ArrayList<Integer>(x.items());
							merged.addAll(y.items());
							return new Bucket(x.label(), List.copyOf(merged));
						}
				).summon().stream().toList();

				assertThat(result).as("label 'a' buckets merged; 'b' unchanged").hasSize(2);
				assertThat(result.stream().filter(b -> b.label().equals("a")).findFirst().orElseThrow().items())
						.as("merged 'a' items")
						.containsExactlyInAnyOrder(1, 2, 3, 4);
				assertThat(result.stream().filter(b -> b.label().equals("b")).findFirst().orElseThrow().items())
						.as("'b' items unchanged")
						.containsExactly(10);
			}

			@Test
			@DisplayName("works with non-string key extractor — Integer key")
			void worksWithIntegerKey()
			{
				record Entry(int parity, String label)
				{
				}

				final Cascade<Entry> source = Cascade.beckon(
						new Entry(0, "even-a"),
						new Entry(1, "odd-a"),
						new Entry(0, "even-b"),
						new Entry(1, "odd-b")
				);

				final List<Entry> result = source.amalgamate(
						Entry::parity,
						(a, b) -> new Entry(a.parity(), a.label() + "+" + b.label())
				).summon().stream().toList();

				assertThat(result).as("two distinct parities → two entries after amalgamation").hasSize(2);
				assertThat(result.stream().filter(e -> e.parity() == 0).findFirst().orElseThrow().label())
						.as("even entries combined").isEqualTo("even-a+even-b");
				assertThat(result.stream().filter(e -> e.parity() == 1).findFirst().orElseThrow().label())
						.as("odd entries combined").isEqualTo("odd-a+odd-b");
			}

			@Test
			@DisplayName("works with composite key extracted from multiple fields")
			void worksWithCompositeKey()
			{
				record Observation(String region, String category, int value)
				{
				}

				final Cascade<Observation> source = Cascade.beckon(
						new Observation("north", "sales", 100),
						new Observation("north", "costs", 40),
						new Observation("north", "sales", 50),
						new Observation("south", "sales", 200)
				);

				final List<Observation> result = source.amalgamate(
						obs -> obs.region() + ":" + obs.category(),
						(a, b) -> new Observation(a.region(), a.category(), a.value() + b.value())
				).summon().stream().toList();

				assertThat(result).as("three distinct region:category pairs").hasSize(3);
				assertThat(result.stream()
				                 .filter(o -> o.region().equals("north") && o.category().equals("sales"))
				                 .findFirst().orElseThrow().value())
						.as("north:sales combined").isEqualTo(150);
			}

			@Test
			@DisplayName("normalize() delegates to amalgamate()")
			void normalizeDelegatesToAmalgamate()
			{
				final Cascade<Term> source = Cascade.beckon(Term.of("x", 1), Term.of("x", 2));

				assertThat(source.normalize(Term::element,
						                 (a, b) -> Term.of(a.element(), a.coefficient() + b.coefficient()))
				                 .summon())
						.as("normalize() result must equal amalgamate() result")
						.containsExactlyInAnyOrderElementsOf(combine(source).summon());
			}

			private static Stream<Arguments> combinesLikeTermsCases()
			{
				return Stream.of(
						new AmalgamateCase("two terms with same key",
								Cascade.beckon(Term.of("x", 1), Term.of("x", 2)),
								List.of(Term.of("x", 3))),
						new AmalgamateCase("three terms — two share a key",
								Cascade.beckon(Term.of("x", 1), Term.of("y", 5), Term.of("x", 2)),
								List.of(Term.of("x", 3), Term.of("y", 5))),
						new AmalgamateCase("three terms sharing one key",
								Cascade.beckon(Term.of("z", 1), Term.of("z", 2), Term.of("z", 3)),
								List.of(Term.of("z", 6))),
						new AmalgamateCase("no duplicates — all distinct keys",
								Cascade.beckon(Term.of("a", 1), Term.of("b", 2), Term.of("c", 3)),
								List.of(Term.of("a", 1), Term.of("b", 2), Term.of("c", 3))),
						new AmalgamateCase("single element — no combination possible",
								Cascade.beckon(Term.of("x", 7)),
								List.of(Term.of("x", 7)))
				).map(tc -> Arguments.of(tc.as(), tc));
			}

			private record AmalgamateCase(String as, Cascade<Term> source, List<Term> expected)
			{
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns empty Cascade")
			void returnsEmpty()
			{
				assertThat(combine(Cascade.abyss()).sterile())
						.as("amalgamate on empty cascade")
						.isTrue();
			}
		}

		@Nested
		@DisplayName("with null arguments")
		final class WithNullArguments
		{
			@Test
			@DisplayName("throws when essence is null")
			void throwsWhenEssenceIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon(Term.of("x", 1)).amalgamate(null,
						(a, b) -> Term.of(a.element(), a.coefficient() + b.coefficient())))
						.as("null essence")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("essence may not be null");
			}

			@Test
			@DisplayName("throws when confluence is null")
			void throwsWhenConfluenceIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon(Term.of("x", 1)).amalgamate(Term::element, null))
						.as("null confluence")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("confluence may not be null");
			}
		}
	}

	@Nested
	@DisplayName("amalgamate(essence, confluence, dissolution) — combine and remove dissolved terms")
	final class WithDissolution
	{
		private Cascade<Term> combineAndPurge(final Cascade<Term> source)
		{
			return source.amalgamate(Term::element,
					(a, b) -> Term.of(a.element(), a.coefficient() + b.coefficient()),
					term -> term.coefficient() == 0);
		}

		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@Test
			@DisplayName("removes terms whose combined coefficient satisfies dissolution predicate")
			void removesDissolvedTerms()
			{
				final Cascade<Term> source = Cascade.beckon(Term.of("x", 1), Term.of("x", -1), Term.of("y", 3));

				final List<Term> result = combineAndPurge(source).summon().stream().toList();

				assertThat(result)
						.as("x terms cancel to zero and should be removed; y survives")
						.containsExactly(Term.of("y", 3));
			}

			@Test
			@DisplayName("preserves terms that do not satisfy dissolution predicate after combining")
			void preservesNonDissolvedTerms()
			{
				final Cascade<Term> source = Cascade.beckon(Term.of("x", 1), Term.of("x", 2), Term.of("y", 0));

				final List<Term> result = combineAndPurge(source).summon().stream().toList();

				assertThat(result)
						.as("x combined to 3 survives; y is zero and dissolves")
						.containsExactly(Term.of("x", 3));
			}

			@Test
			@DisplayName("returns empty content when all terms dissolve")
			void returnsEmptyWhenAllTermsDissolve()
			{
				final Cascade<Term> source = Cascade.beckon(Term.of("x", 1), Term.of("x", -1));

				assertThat(combineAndPurge(source).summon())
						.as("all terms dissolved — cascade content should be empty")
						.isEmpty();
			}

			@Test
			@DisplayName("dissolves entries whose combined string value is blank")
			void dissolvesBlankStringValues()
			{
				record Tag(String key, String value)
				{
				}

				final Cascade<Tag> source = Cascade.beckon(
						new Tag("a", "hello"),
						new Tag("b", ""),
						new Tag("b", ""),
						new Tag("c", "world")
				);

				final List<Tag> result = source.amalgamate(
						Tag::key,
						(x, y) -> new Tag(x.key(), x.value() + y.value()),
						tag -> tag.value().isBlank()
				).summon().stream().toList();

				assertThat(result)
						.as("'b' dissolves — empty strings concatenate to blank")
						.containsExactlyInAnyOrder(new Tag("a", "hello"), new Tag("c", "world"));
			}

			@Test
			@DisplayName("dissolves entries whose combined list is empty after filtering")
			void dissolvesEmptyListTerms()
			{
				record Bag(String key, List<Integer> values)
				{
				}

				final Cascade<Bag> source = Cascade.beckon(
						new Bag("keep", List.of(1, 2)),
						new Bag("drop", List.of()),
						new Bag("keep", List.of(3))
				);

				final List<Bag> result = source.amalgamate(
						Bag::key,
						(a, b) ->
						{
							final var merged = new java.util.ArrayList<Integer>(a.values());
							merged.addAll(b.values());
							return new Bag(a.key(), List.copyOf(merged));
						},
						bag -> bag.values().isEmpty()
				).summon().stream().toList();

				assertThat(result)
						.as("'drop' bag has no values and dissolves; 'keep' survives")
						.hasSize(1);
				assertThat(result.getFirst().values())
						.as("'keep' bag merged values")
						.containsExactlyInAnyOrder(1, 2, 3);
			}

			@Test
			@DisplayName("normalize() with predicate delegates to amalgamate()")
			void normalizeDelegatesToAmalgamate()
			{
				final Cascade<Term> source = Cascade.beckon(Term.of("x", 1), Term.of("x", -1), Term.of("y", 2));

				assertThat(source.normalize(Term::element,
						(a, b) -> Term.of(a.element(), a.coefficient() + b.coefficient()),
						term -> term.coefficient() == 0).summon())
						.as("normalize() with predicate must equal amalgamate() result")
						.containsExactlyInAnyOrderElementsOf(combineAndPurge(source).summon());
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("returns empty Cascade")
			void returnsEmpty()
			{
				assertThat(combineAndPurge(Cascade.abyss()).sterile())
						.as("amalgamate with dissolution on empty cascade")
						.isTrue();
			}
		}

		@Nested
		@DisplayName("with null arguments")
		final class WithNullArguments
		{
			@Test
			@DisplayName("throws when dissolution is null")
			void throwsWhenDissolutionIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon(Term.of("x", 1)).amalgamate(Term::element,
						(a, b) -> Term.of(a.element(), a.coefficient() + b.coefficient()), null))
						.as("null dissolution")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("dissolution may not be null");
			}
		}
	}
}