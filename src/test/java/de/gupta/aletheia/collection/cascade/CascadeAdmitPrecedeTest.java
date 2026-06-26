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
				assertThatThrownBy(() -> Cascade.beckon("a").admit(null))
						.as("null element on present cascade")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("element may not be null");
			}

			@Test
			@DisplayName("throws when element is null on empty cascade")
			void throwsWhenElementIsNullOnEmptyCascade()
			{
				assertThatThrownBy(() -> Cascade.<String>abyss().admit(null))
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
				assertThatThrownBy(() -> Cascade.beckon("a").precede(null))
						.as("null element on present cascade")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("element may not be null");
			}

			@Test
			@DisplayName("throws when element is null on empty cascade")
			void throwsWhenElementIsNullOnEmptyCascade()
			{
				assertThatThrownBy(() -> Cascade.<String>abyss().precede(null))
						.as("null element on empty cascade")
						.isInstanceOf(NullPointerException.class)
						.hasMessageContaining("element may not be null");
			}
		}
	}
}