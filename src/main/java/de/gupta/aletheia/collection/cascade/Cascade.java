package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.Dyad;
import de.gupta.aletheia.collection.crucible.Crucible;
import de.gupta.aletheia.collection.crucible.Forge;
import de.gupta.aletheia.collection.crucible.Relic;
import de.gupta.aletheia.functional.Unfolding;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

public sealed interface Cascade<E> permits Brook, Nadir
{
	// ── Conventional names ────────────────────────────────────────────────────────────────────────
	// Standard Stream/Collection-style API. Each method delegates to its mythic equivalent below.

	@SafeVarargs
	static <E> Cascade<E> of(final E... elements)
	{
		return beckon(elements);
	}

	static <E> Cascade<E> of(final Collection<E> elements)
	{
		return beckon(elements);
	}

	static <E> Cascade<E> of(final Stream<? extends E> stream)
	{
		return beckon(stream);
	}

	static <E> Cascade<E> empty()
	{
		return abyss();
	}

	static <E> Cascade<E> ofConditional(final Collection<E> elements, final boolean condition)
	{
		return adjudicate(elements, condition);
	}

	static <E> Cascade<E> fromCrucible(final Crucible<E> crucible)
	{
		return distill(crucible);
	}

	default boolean isEmpty()
	{
		return sterile();
	}

	default boolean isNotEmpty()
	{
		return supple();
	}

	default Collection<E> toCollection()
	{
		return summon();
	}

	default <F> Cascade<F> map(final Function<? super E, ? extends F> mapper)
	{
		return metamorphose(mapper);
	}

	default Cascade<E> filter(final Predicate<? super E> predicate)
	{
		return discern(predicate);
	}

	default <F> Cascade<F> flatMap(final Function<? super E, Cascade<F>> mapper)
	{
		return entwine(mapper);
	}

	default <F> Cascade<F> flatMapOptional(final Function<? super E, Optional<? extends F>> mapper)
	{
		return alchemize(mapper);
	}

	default Cascade<E> peek(final Consumer<? super E> consumer)
	{
		return unlace(consumer);
	}

	default Cascade<E> sorted(final Comparator<? super E> comparator)
	{
		return ordain(comparator);
	}

	default Cascade<E> sorted()
	{
		return ordain();
	}

	default Cascade<E> distinct()
	{
		return purify();
	}

	default <R> R fold(final R initial, final BiFunction<? super R, ? super E, ? extends R> operation)
	{
		return weave(initial, operation);
	}

	default Unfolding<E> reduce(final BiFunction<? super E, ? super E, ? extends E> operation)
	{
		return smelt(operation);
	}

	default Collection<E> orElse(final Collection<? extends E> other)
	{
		return infuse(other);
	}

	default Collection<E> orElseGet(final Supplier<? extends Collection<? extends E>> supplier)
	{
		return infuse(supplier);
	}

	default Cascade<E> orElseRecover(final Supplier<Cascade<E>> recovery)
	{
		return resurrect(recovery);
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────
	// Primary vocabulary. Conventional aliases are above.

	/**
	 * Static factories
	 */

	@SafeVarargs
	static <E> Cascade<E> beckon(final E... elements)
	{
		return Unfolding.beckon(elements)
		                .metamorphose(Brook::kindle)
		                .infuse(Cascade::abyss);
	}

	static <E> Cascade<E> beckon(final Collection<E> elements)
	{
		return Unfolding.beckon(elements)
		                .metamorphose(Brook::kindle)
		                .infuse(Cascade::abyss);
	}

	static <E> Cascade<E> beckon(final Stream<? extends E> stream)
	{
		return Unfolding.beckon(stream)
		                .<Cascade<E>>metamorphose(Brook::kindle)
		                .infuse(Cascade::abyss);
	}

	static <E> Cascade<E> abyss()
	{
		return Nadir.instance();
	}

	static <E> Cascade<E> adjudicate(final Collection<E> elements, final boolean judgement)
	{
		return Unfolding.adjudicate(elements, judgement)
		                .metamorphose(Brook::kindle)
		                .infuse(Cascade::abyss);
	}

	static <E> Cascade<E> distill(final Crucible<E> crucible)
	{
		return Unfolding.beckon(crucible)
		                .metamorphose(Brook::kindle)
		                .infuse(Cascade::abyss);
	}

	/**
	 * Presence and extraction
	 */

	boolean sterile();

	boolean supple();

	Collection<E> summon();

	Stream<E> stream();

	Relic<E> enshrine();

	Forge<E> awaken();

	<R> R coronate(final Function<? super Stream<E>, ? extends R> conclusion);

	<R> R coronate(final Function<? super Stream<E>, ? extends R> conclusion,
	               final Supplier<? extends R> grace);

	/**
	 * Transformation and transmutation.
	 */

	<F> Cascade<F> metamorphose(final Function<? super E, ? extends F> metamorphosis);

	<U, F> Cascade<Dyad<U, F>> metamorphose(final Function<? super E, ? extends U> fate,
	                                        final Function<? super E, ? extends F> destiny);

	Cascade<E> develop(final Predicate<? super E> judgement,
	                   final Function<? super E, ? extends E> development);

	<F> Cascade<F> evolve(final Predicate<? super E> judgement,
	                      final Function<? super E, ? extends F> evolution);

	Cascade<E> ascend(final UnaryOperator<E> ascension, final int levels);

	<F> Cascade<Dyad<E, F>> interlace(final Function<? super E, ? extends F> interlacing);

	Cascade<E> purify();

	Cascade<E> ordain(final Comparator<? super E> order);

	Cascade<E> ordain();

	<F> Cascade<F> transfigure(final Function<Collection<? extends E>, ? extends Collection<F>> transmutation);

	/**
	 * Judgement and branching
	 */

	Cascade<E> discern(final Predicate<? super E> judgement);

	Cascade<E> discern(final Predicate<? super E> judgement,
	                   final Supplier<? extends RuntimeException> wrath);

	<F> Cascade<F> cleave(final Predicate<? super E> judgement,
	                      final Function<? super E, ? extends F> reward,
	                      final Function<? super E, ? extends F> punishment);

	/**
	 * Composition
	 */

	<F> Cascade<F> entwine(final Function<? super E, Cascade<F>> plot);

	<F> Cascade<F> alchemize(final Function<? super E, Optional<? extends F>> potion);

	<U, F> Cascade<F> conjoin(final U consort,
	                          final BiFunction<? super E, ? super U, ? extends F> conjugation);

	<F, G> Cascade<G> braid(final Cascade<F> consort,
	                        final BiFunction<Unfolding<E>, Unfolding<F>, ? extends G> weaver);

	/**
	 * Folding / reduction
	 */

	<R> R weave(final R initial, final BiFunction<? super R, ? super E, ? extends R> operation);

	Unfolding<E> smelt(final BiFunction<? super E, ? super E, ? extends E> operation);

	/**
	 * Recovery and renewal
	 */

	Collection<E> infuse(final Supplier<? extends Collection<? extends E>> revelation);

	Collection<E> infuse(final Collection<? extends E> manifestation);

	Cascade<E> resurrect(final Supplier<Cascade<E>> grace);

	Cascade<E> revive(final Supplier<? extends E> grace);

	/**
	 * Effects and interruption
	 */

	Cascade<E> unlace(final Consumer<? super E> impregnator);

	Cascade<E> unlace(final Predicate<? super E> judgement, final Consumer<? super E> impregnator);
}