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

@DisplayName("Cascade#admit and Cascade#precede")
final class CascadeAdmitPrecedeTest
{
	@Nested
	@DisplayName("admit — appends element at tail")
	final class Admit
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("appendsElementAtTailCases")
			@DisplayName("appends element at the tail of the cascade")
			void appendsElementAtTail(final String as, final AdmitCase tc)
			{
				assertThat(tc.source().admit(tc.element()).summon())
						.as(as)
						.containsExactlyElementsOf(tc.expected());
			}

			@Test
			@DisplayName("works with non-String element type")
			void worksWithNonStringElementType()
			{
				assertThat(Cascade.beckon(10, 20).admit(30).summon())
						.as("integer cascade with admitted integer")
						.containsExactly(10, 20, 30);
			}

			@Test
			@DisplayName("multiple admits accumulate in order")
			void multipleAdmitsAccumulateInOrder()
			{
				assertThat(Cascade.beckon(1, 2).admit(3).admit(4).summon())
						.as("elements admitted in order append at tail")
						.containsExactly(1, 2, 3, 4);
			}

			@Test
			@DisplayName("append() delegates to admit()")
			void appendDelegatesToAdmit()
			{
				final Cascade<String> source = Cascade.beckon("a", "b");

				assertThat(source.append("c").summon())
						.as("append() must equal admit()")
						.containsExactlyElementsOf(source.admit("c").summon());
			}

			private static Stream<Arguments> appendsElementAtTailCases()
			{
				return Stream.of(
						new AdmitCase("single element cascade — admitted element appended",
								Cascade.beckon("a"), "b", List.of("a", "b")),
						new AdmitCase("multi-element cascade — admitted element at end",
								Cascade.beckon("a", "b", "c"), "d", List.of("a", "b", "c", "d")),
						new AdmitCase("admitted element is distinct from existing elements",
								Cascade.beckon("x", "y"), "z", List.of("x", "y", "z"))
				).map(tc -> Arguments.of(tc.as(), tc));
			}

			private record AdmitCase(String as, Cascade<String> source, String element, List<String> expected)
			{
			}
		}

		@Nested
		@DisplayName("admit(Collection) — appends collection at tail")
		final class AdmitCollection
		{
			@Test
			@DisplayName("appends all collection elements in order at tail")
			void appendsCollectionAtTail()
			{
				assertThat(Cascade.beckon("a", "b").admit(List.of("c", "d")).summon())
						.as("collection appended at tail")
						.containsExactly("a", "b", "c", "d");
			}

			@Test
			@DisplayName("admits empty collection leaves cascade unchanged")
			void admitsEmptyCollectionLeavesUnchanged()
			{
				assertThat(Cascade.beckon("a", "b").admit(List.of()).summon())
						.as("empty collection — no change")
						.containsExactly("a", "b");
			}

			@Test
			@DisplayName("promotes Nadir to the admitted collection")
			void promotesNadirToAdmittedCollection()
			{
				assertThat(Cascade.<String>abyss().admit(List.of("x", "y")).summon())
						.as("Nadir promoted by admitting collection")
						.containsExactly("x", "y");
			}

			@Test
			@DisplayName("throws when collection is null")
			void throwsWhenCollectionIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon("a").admit((java.util.Collection<String>) null))
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("elements may not be null");
			}
		}

		@Nested
		@DisplayName("admit(Cascade) — appends Cascade at tail")
		final class AdmitCascade
		{
			@Test
			@DisplayName("appends all elements of other Cascade at tail")
			void appendsCascadeAtTail()
			{
				assertThat(Cascade.beckon("a", "b").admit(Cascade.beckon("c", "d")).summon())
						.as("other cascade appended at tail")
						.containsExactly("a", "b", "c", "d");
			}

			@Test
			@DisplayName("admits empty Cascade leaves cascade unchanged")
			void admitsEmptyCascadeLeavesUnchanged()
			{
				assertThat(Cascade.beckon("a").admit(Cascade.abyss()).summon())
						.as("empty cascade admitted — no change")
						.containsExactly("a");
			}

			@Test
			@DisplayName("Nadir returns other Cascade directly")
			void nadirReturnsOtherCascade()
			{
				final Cascade<String> other = Cascade.beckon("x", "y");

				assertThat(Cascade.<String>abyss().admit(other).summon())
						.as("Nadir + cascade = cascade")
						.containsExactlyElementsOf(other.summon());
			}

			@Test
			@DisplayName("throws when other Cascade is null")
			void throwsWhenOtherCascadeIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon("a").admit((Cascade<String>) null))
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("other may not be null");
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("promotes Nadir to single-element Cascade")
			void promotesNadirToSingleElementCascade()
			{
				assertThat(Cascade.<String>abyss().admit("x").summon())
						.as("admitting into absence creates single-element cascade")
						.containsExactly("x");
			}

			@Test
			@DisplayName("promoted Cascade is chainable")
			void promotedCascadeIsChainable()
			{
				assertThat(Cascade.<Integer>abyss().admit(42).admit(99).summon())
						.as("subsequent admits on promoted cascade")
						.containsExactly(42, 99);
			}
		}

		@Nested
		@DisplayName("with null arguments")
		final class WithNullArguments
		{
			@Test
			@DisplayName("throws when element is null on present cascade")
			void throwsWhenElementIsNullOnPresentCascade()
			{
				assertThatThrownBy(() -> Cascade.beckon("a").admit((String) null))
						.as("null element on present cascade")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("element may not be null");
			}

			@Test
			@DisplayName("throws when element is null on empty cascade")
			void throwsWhenElementIsNullOnEmptyCascade()
			{
				assertThatThrownBy(() -> Cascade.<String>abyss().admit((String) null))
						.as("null element on empty cascade")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("element may not be null");
			}
		}
	}

	@Nested
	@DisplayName("precede — prepends element at head")
	final class Precede
	{
		@Nested
		@DisplayName("when Cascade is present")
		final class WhenCascadeIsPresent
		{
			@ParameterizedTest(name = "{0}")
			@MethodSource("prependsElementAtHeadCases")
			@DisplayName("prepends element at the head of the cascade")
			void prependsElementAtHead(final String as, final PrecedeCase tc)
			{
				assertThat(tc.source().precede(tc.element()).summon())
						.as(as)
						.containsExactlyElementsOf(tc.expected());
			}

			@Test
			@DisplayName("multiple precedes accumulate in reverse order")
			void multiplePrecedesAccumulateInReverseOrder()
			{
				assertThat(Cascade.beckon(3, 4).precede(2).precede(1).summon())
						.as("each precede inserts at head")
						.containsExactly(1, 2, 3, 4);
			}

			@Test
			@DisplayName("prepend() delegates to precede()")
			void prependDelegatesToPrecede()
			{
				final Cascade<String> source = Cascade.beckon("b", "c");

				assertThat(source.prepend("a").summon())
						.as("prepend() must equal precede()")
						.containsExactlyElementsOf(source.precede("a").summon());
			}

			@Test
			@DisplayName("admit and precede produce distinct orderings")
			void admitAndPrecedeProduceDistinctOrderings()
			{
				final Cascade<String> source = Cascade.beckon("b");

				assertThat(source.admit("a").summon())
						.as("admit appends — b then a")
						.containsExactly("b", "a");

				assertThat(source.precede("a").summon())
						.as("precede prepends — a then b")
						.containsExactly("a", "b");
			}

			@Test
			@DisplayName("works with non-String element type")
			void worksWithNonStringElementType()
			{
				assertThat(Cascade.beckon(20, 30).precede(10).summon())
						.as("integer cascade with preceded integer")
						.containsExactly(10, 20, 30);
			}

			private static Stream<Arguments> prependsElementAtHeadCases()
			{
				return Stream.of(
						new PrecedeCase("single element cascade — preceding element at head",
								Cascade.beckon("b"), "a", List.of("a", "b")),
						new PrecedeCase("multi-element cascade — preceding element at front",
								Cascade.beckon("b", "c", "d"), "a", List.of("a", "b", "c", "d")),
						new PrecedeCase("preceding element is distinct from existing elements",
								Cascade.beckon("y", "z"), "x", List.of("x", "y", "z"))
				).map(tc -> Arguments.of(tc.as(), tc));
			}

			private record PrecedeCase(String as, Cascade<String> source, String element, List<String> expected)
			{
			}
		}

		@Nested
		@DisplayName("precede(Collection) — prepends collection at head")
		final class PrecedeCollection
		{
			@Test
			@DisplayName("prepends all collection elements in order at head")
			void prependsCollectionAtHead()
			{
				assertThat(Cascade.beckon("c", "d").precede(List.of("a", "b")).summon())
						.as("collection prepended at head")
						.containsExactly("a", "b", "c", "d");
			}

			@Test
			@DisplayName("precedes empty collection leaves cascade unchanged")
			void precedesEmptyCollectionLeavesUnchanged()
			{
				assertThat(Cascade.beckon("a", "b").precede(List.of()).summon())
						.as("empty collection — no change")
						.containsExactly("a", "b");
			}

			@Test
			@DisplayName("promotes Nadir to the preceded collection")
			void promotesNadirToPrecededCollection()
			{
				assertThat(Cascade.<String>abyss().precede(List.of("x", "y")).summon())
						.as("Nadir promoted by preceding collection")
						.containsExactly("x", "y");
			}

			@Test
			@DisplayName("throws when collection is null")
			void throwsWhenCollectionIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon("a").precede((java.util.Collection<String>) null))
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("elements may not be null");
			}
		}

		@Nested
		@DisplayName("precede(Cascade) — prepends Cascade at head")
		final class PrecedeCascade
		{
			@Test
			@DisplayName("prepends all elements of other Cascade at head")
			void prependsCascadeAtHead()
			{
				assertThat(Cascade.beckon("c", "d").precede(Cascade.beckon("a", "b")).summon())
						.as("other cascade prepended at head")
						.containsExactly("a", "b", "c", "d");
			}

			@Test
			@DisplayName("precedes empty Cascade leaves cascade unchanged")
			void precedesEmptyCascadeLeavesUnchanged()
			{
				assertThat(Cascade.beckon("a").precede(Cascade.abyss()).summon())
						.as("empty cascade preceded — no change")
						.containsExactly("a");
			}

			@Test
			@DisplayName("Nadir returns other Cascade directly")
			void nadirReturnsOtherCascade()
			{
				final Cascade<String> other = Cascade.beckon("x", "y");

				assertThat(Cascade.<String>abyss().precede(other).summon())
						.as("Nadir + cascade = cascade")
						.containsExactlyElementsOf(other.summon());
			}

			@Test
			@DisplayName("throws when other Cascade is null")
			void throwsWhenOtherCascadeIsNull()
			{
				assertThatThrownBy(() -> Cascade.beckon("a").precede((Cascade<String>) null))
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("other may not be null");
			}
		}

		@Nested
		@DisplayName("when Cascade is empty")
		final class WhenCascadeIsEmpty
		{
			@Test
			@DisplayName("promotes Nadir to single-element Cascade")
			void promotesNadirToSingleElementCascade()
			{
				assertThat(Cascade.<String>abyss().precede("x").summon())
						.as("preceding into absence creates single-element cascade")
						.containsExactly("x");
			}

			@Test
			@DisplayName("admit and precede on Nadir produce identical single-element result")
			void admitAndPrecedeOnNadirProduceIdenticalResult()
			{
				assertThat(Cascade.<String>abyss().admit("x").summon())
						.as("admit on Nadir")
						.containsExactlyElementsOf(Cascade.<String>abyss().precede("x").summon());
			}
		}

		@Nested
		@DisplayName("with null arguments")
		final class WithNullArguments
		{
			@Test
			@DisplayName("throws when element is null on present cascade")
			void throwsWhenElementIsNullOnPresentCascade()
			{
				assertThatThrownBy(() -> Cascade.beckon("a").precede((String) null))
						.as("null element on present cascade")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("element may not be null");
			}

			@Test
			@DisplayName("throws when element is null on empty cascade")
			void throwsWhenElementIsNullOnEmptyCascade()
			{
				assertThatThrownBy(() -> Cascade.<String>abyss().precede((String) null))
						.as("null element on empty cascade")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("element may not be null");
			}
		}
	}
}