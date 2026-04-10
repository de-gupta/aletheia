package de.gupta.aletheia.collection.cascade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cascade transfigure tests")
final class CascadeTransfigureTest
{
	@Nested
	@DisplayName("Aspects of whole-collection transmutation")
	final class WholeCollectionTransmutationTests
	{
		@ParameterizedTest(name = "{0}")
		@MethodSource("transfigureCases")
		@DisplayName("should apply transmutation to entire collection and wrap result")
		void shouldApplyTransmutationToEntireCollectionAndWrapResult(final String as, final TransfigureCase tc)
		{
			var result = tc.source().transfigure(tc.transmutation());

			assertThat(result.summon())
					.as("transfigure should yield expected collection for %s", as)
					.containsExactlyElementsOf(tc.expected());
		}

		private static Stream<Arguments> transfigureCases()
		{
			return Stream.of(
					new TransfigureCase(
							"Reversed river flows backward",
							Cascade.beckon("atlas", "ares", "hera"),
							c ->
							{
								var list = new java.util.ArrayList<String>(c);
								java.util.Collections.reverse(list);
								return list;
							},
							List.of("hera", "ares", "atlas")
					),
					new TransfigureCase(
							"Collection is exchanged wholesale for another",
							Cascade.beckon("orpheus", "hermes", "ares"),
							_ -> List.of("zeus", "hera", "hades"),
							List.of("zeus", "hera", "hades")
					),
					new TransfigureCase(
							"Single survivor from collective judgement",
							Cascade.beckon("apollo", "ares", "athena"),
							c -> c.stream().filter(s -> s.length() == 4).collect(Collectors.toList()),
							List.of("ares")
					)
			).map(tc -> Arguments.of(tc.as(), tc));
		}

		private record TransfigureCase(String as,
		                               Cascade<String> source,
		                               Function<Collection<? extends String>, ? extends Collection<String>> transmutation,
		                               List<String> expected)
		{
		}
	}

	@Nested
	@DisplayName("Aspects of type change")
	final class TypeChangeTests
	{
		@Test
		@DisplayName("should transfigure strings into their lengths")
		void shouldTransfigureStringsIntoTheirLengths()
		{
			var result = Cascade.beckon("odin", "ra", "zeus")
			                    .transfigure(c -> c.stream().map(String::length).toList());

			assertThat(result.summon())
					.as("transfigure should produce lengths of each name")
					.containsExactly(4, 2, 4);
		}

		@Test
		@DisplayName("should transfigure into a set losing duplicates")
		void shouldTransfigureIntoASetLosingDuplicates()
		{
			var result = Cascade.beckon("ares", "ares", "hera")
			                    .<String>transfigure(HashSet::new);

			assertThat(result.summon())
					.as("transfigure into set should contain only distinct elements")
					.containsExactlyInAnyOrder("ares", "hera");
		}
	}

	@Nested
	@DisplayName("Aspects of null transmutation result")
	final class NullTransmutationResultTests
	{
		@Test
		@DisplayName("should yield abyss when transmutation returns null")
		void shouldYieldAbyssWhenTransmutationReturnsNull()
		{
			var result = Cascade.beckon("atlas").transfigure(_ -> null);

			assertThat(result)
					.as("null transmutation result should collapse to abyss")
					.isSameAs(Cascade.abyss());
		}
	}

	@Nested
	@DisplayName("Aspects of abyss source")
	final class AbyssSourceTests
	{
		@Test
		@DisplayName("should remain nadir and never invoke transmutation")
		void shouldRemainNadirAndNeverInvokeTransmutation()
		{
			var called = new AtomicBoolean(false);

			var result = Cascade.<String>abyss().transfigure(_ ->
			{
				called.set(true);
				return List.of("unreachable");
			});

			assertThat(called.get())
					.as("transmutation should not be invoked on nadir")
					.isFalse();
			assertThat(result)
					.as("transfigure on abyss should remain nadir")
					.isSameAs(Cascade.abyss());
		}
	}

	@Nested
	@DisplayName("Aspects of guards")
	final class GuardTests
	{
		@Test
		@DisplayName("should reject null transmutation for brook")
		void shouldRejectNullTransmutationForBrook()
		{
			assertThatThrownBy(() -> Cascade.beckon("zeus").transfigure(null))
					.isInstanceOf(NullPointerException.class)
					.hasMessage("transmutation may not be null");
		}

		@Test
		@DisplayName("should tolerate null transmutation for abyss")
		void shouldTolerateNullTransmutationForAbyss()
		{
			assertThat(Cascade.<String>abyss().transfigure(null))
					.as("nadir transfigure should remain nadir without guarding transmutation")
					.isSameAs(Cascade.abyss());
		}
	}

	@Nested
	@DisplayName("Aspects of empty result")
	final class EmptyResultTests
	{
		@Test
		@DisplayName("should yield supple empty brook when transmutation returns empty collection")
		void shouldYieldSuppleBrookWhenTransmutationReturnsEmptyCollection()
		{
			var result = Cascade.beckon("atlas", "ares").transfigure(_ -> List.of());

			assertThat(result.supple())
					.as("empty transmutation result should remain a supple brook, not abyss")
					.isTrue();
			assertThat(result.summon())
					.as("summon on empty transmutation result should return empty collection")
					.isEmpty();
		}
	}
}