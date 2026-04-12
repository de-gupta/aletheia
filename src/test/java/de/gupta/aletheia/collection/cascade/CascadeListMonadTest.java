package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.crucible.Crucible;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that Cascade obeys List monad semantics: an empty Brook is a valid,
 * supple, first-class value distinct from Nadir. Nadir represents absence of a
 * cascade entirely; an empty Brook represents a cascade that produced no results.
 * Only a null reference — never an empty collection or an exhausted stream — produces Nadir.
 */
@DisplayName("Cascade list monad semantics")
final class CascadeListMonadTest
{
	// -------------------------------------------------------------------------
	// Factory boundary: only null collapses to Nadir
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of factory nullity boundary")
	final class FactoryNullityBoundaryTests
	{
		@Test
		@DisplayName("beckon(empty varargs) yields supple brook, not nadir")
		void beckonEmptyVarargs()
		{
			var cascade = Cascade.beckon(new String[]{});

			assertThat(cascade).isInstanceOf(Brook.class);
			assertThat(cascade.supple()).isTrue();
			assertThat(cascade.sterile()).isFalse();
			assertThat(cascade.summon()).isEmpty();
		}

		@Test
		@DisplayName("beckon(empty collection) yields supple brook, not nadir")
		void beckonEmptyCollection()
		{
			var cascade = Cascade.beckon(List.of());

			assertThat(cascade).isInstanceOf(Brook.class);
			assertThat(cascade.supple()).isTrue();
			assertThat(cascade.sterile()).isFalse();
			assertThat(cascade.summon()).isEmpty();
		}

		@Test
		@DisplayName("beckon(null varargs) yields nadir — null reference is absence")
		void beckonNullVarargs()
		{
			var cascade = Cascade.beckon((String[]) null);

			assertThat(cascade).isInstanceOf(Nadir.class);
			assertThat(cascade.sterile()).isTrue();
		}

		@Test
		@DisplayName("beckon(null collection) yields nadir — null reference is absence")
		void beckonNullCollection()
		{
			var cascade = Cascade.beckon((java.util.Collection<String>) null);

			assertThat(cascade).isInstanceOf(Nadir.class);
			assertThat(cascade.sterile()).isTrue();
		}

		@Test
		@DisplayName("distill(empty crucible) yields supple brook, not nadir")
		void distillEmptyCrucible()
		{
			var cascade = Cascade.distill(Crucible.kindle(List.of()));

			assertThat(cascade).isInstanceOf(Brook.class);
			assertThat(cascade.supple()).isTrue();
			assertThat(cascade.summon()).isEmpty();
		}

		@Test
		@DisplayName("adjudicate(empty collection, true) yields supple brook, not nadir")
		void adjudicateEmptyCollectionBlessed()
		{
			var cascade = Cascade.adjudicate(List.of(), true);

			assertThat(cascade).isInstanceOf(Brook.class);
			assertThat(cascade.supple()).isTrue();
			assertThat(cascade.summon()).isEmpty();
		}

		@Test
		@DisplayName("adjudicate(non-empty collection, false) yields nadir — judgement is absence of intent")
		void adjudicateNonEmptyCollectionRejected()
		{
			var cascade = Cascade.adjudicate(List.of("helios"), false);

			assertThat(cascade).isInstanceOf(Nadir.class);
			assertThat(cascade.sterile()).isTrue();
		}
	}

	// -------------------------------------------------------------------------
	// Transformation: exhausting a brook yields empty brook, not nadir
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of exhaustion through transformation")
	final class ExhaustionThroughTransformationTests
	{
		@Test
		@DisplayName("discern that rejects all elements yields supple empty brook")
		void discernRejectsAll()
		{
			var result = Cascade.beckon("apollo", "ares").discern(_ -> false);

			assertThat(result).isInstanceOf(Brook.class);
			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}

		@Test
		@DisplayName("metamorphose that nullifies all elements yields supple empty brook")
		void metamorphoseNullifiesAll()
		{
			var result = Cascade.beckon("atlas", "echo").metamorphose(_ -> null);

			assertThat(result).isInstanceOf(Brook.class);
			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}

		@Test
		@DisplayName("evolve that matches no element yields supple empty brook")
		void evolveMatchesNone()
		{
			var result = Cascade.beckon(1, 2, 3).evolve(n -> n > 100, n -> n * 2);

			assertThat(result).isInstanceOf(Brook.class);
			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}

		@Test
		@DisplayName("alchemize that empties all yields supple empty brook")
		void alchemizeEmptiesAll()
		{
			var result = Cascade.beckon("zeus", "hera")
			                    .alchemize(_ -> java.util.Optional.empty());

			assertThat(result).isInstanceOf(Brook.class);
			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}

		@Test
		@DisplayName("entwine that returns empty cascade for all elements yields supple empty brook")
		void entwineReturnsEmptyForAll()
		{
			var result = Cascade.beckon("orpheus", "hermes")
			                    .entwine(_ -> Cascade.beckon(List.of()));

			assertThat(result).isInstanceOf(Brook.class);
			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}

		@Test
		@DisplayName("purify that collapses all duplicates to one then filter yields empty brook")
		void purifyThenDiscernYieldsEmpty()
		{
			var result = Cascade.beckon("echo", "echo", "echo")
			                    .purify()
			                    .discern(s -> s.length() > 10);

			assertThat(result).isInstanceOf(Brook.class);
			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}

		@Test
		@DisplayName("transfigure returning empty collection yields supple empty brook")
		void transfigureReturnsEmpty()
		{
			var result = Cascade.beckon("atlas", "ares").transfigure(_ -> List.of());

			assertThat(result).isInstanceOf(Brook.class);
			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}
	}

	// -------------------------------------------------------------------------
	// Empty brook is chainable — operations propagate without throwing
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of empty brook chainability")
	final class EmptyBrookChainabilityTests
	{
		@Test
		@DisplayName("empty brook survives a full transformation chain without throwing")
		void emptyBrookSurvivesChain()
		{
			var result = Cascade.beckon(List.<String>of())
			                    .metamorphose(String::toUpperCase)
			                    .discern(s -> s.startsWith("A"))
			                    .ordain()
			                    .purify();

			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}

		@Test
		@DisplayName("empty brook summon returns empty collection, not exception")
		void emptyBrookSummonReturnsEmpty()
		{
			var cascade = Cascade.beckon(List.<Integer>of());

			assertThat(cascade.summon()).isEmpty();
		}

		@Test
		@DisplayName("nadir summon still throws — nadir and empty brook are distinct")
		void nadirSummonThrows()
		{
			assertThatThrownBy(() -> Cascade.abyss().summon())
					.isInstanceOf(EmptyCascadeException.class);
		}

		@Test
		@DisplayName("empty brook and nadir are not the same instance")
		void emptyBrookIsNotNadir()
		{
			var emptyBrook = Cascade.beckon(List.of());
			var nadir = Cascade.abyss();

			assertThat(emptyBrook).isNotSameAs(nadir);
			assertThat(emptyBrook).isNotInstanceOf(Nadir.class);
		}

		@Test
		@DisplayName("empty brook weave returns initial value unchanged")
		void emptyBrookWeaveReturnsInitial()
		{
			var result = Cascade.beckon(List.<Integer>of())
			                    .weave(42, Integer::sum);

			assertThat(result).isEqualTo(42);
		}

		@Test
		@DisplayName("empty brook forge returns chaos — no elements to reduce")
		void emptyBrookSmeltReturnsChaos()
		{
			var result = Cascade.beckon(List.<Integer>of())
			                    .smelt(Integer::sum);

			assertThat(result.sterile()).isTrue();
		}
	}

	// -------------------------------------------------------------------------
	// Monad laws on empty brook
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of monad law compliance")
	final class MonadLawTests
	{
		@Test
		@DisplayName("right identity: empty brook entwined with unit returns empty brook")
		void rightIdentityEmptyBrook()
		{
			var empty = Cascade.beckon(List.<String>of());
			var result = empty.entwine(Cascade::beckon);

			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}

		@Test
		@DisplayName("associativity: chained entwine on empty brook yields empty brook")
		void associativityEmptyBrook()
		{
			var empty = Cascade.beckon(List.<Integer>of());

			var leftAssoc = empty
					.entwine(n -> Cascade.beckon(n, n * 2))
					.entwine(n -> Cascade.beckon(n + 1));

			var rightAssoc = empty
					.entwine(n -> Cascade.beckon(n, n * 2).entwine(m -> Cascade.beckon(m + 1)));

			assertThat(leftAssoc.supple()).isTrue();
			assertThat(leftAssoc.summon()).isEmpty();
			assertThat(rightAssoc.supple()).isTrue();
			assertThat(rightAssoc.summon()).isEmpty();
		}

		@Test
		@DisplayName("functor identity: metamorphose(identity) on empty brook returns empty brook")
		void functorIdentityEmptyBrook()
		{
			var empty = Cascade.beckon(List.<String>of());
			var result = empty.metamorphose(s -> s);

			assertThat(result.supple()).isTrue();
			assertThat(result.summon()).isEmpty();
		}
	}

	// -------------------------------------------------------------------------
	// Null-bearing collections: null elements are valid, null reference is not
	// -------------------------------------------------------------------------

	@Nested
	@DisplayName("Aspects of null element tolerance")
	final class NullElementToleranceTests
	{
		@Test
		@DisplayName("collection with null elements is a supple brook, not nadir")
		void collectionWithNullElementsIsSuppleBrook()
		{
			var cascade = Cascade.beckon(Arrays.asList("sun", null, "moon"));

			assertThat(cascade).isInstanceOf(Brook.class);
			assertThat(cascade.supple()).isTrue();
			assertThat(cascade.summon()).containsExactly("sun", null, "moon");
		}
	}
}