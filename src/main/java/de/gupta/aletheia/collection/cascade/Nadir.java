package de.gupta.aletheia.collection.cascade;

import de.gupta.aletheia.collection.Pair;
import de.gupta.aletheia.collection.crucible.Crucible;
import de.gupta.aletheia.collection.crucible.Forge;
import de.gupta.aletheia.collection.crucible.Relic;
import de.gupta.aletheia.collection.folding.Loom;
import de.gupta.aletheia.functional.Unfolding;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

final class Nadir<E> implements Cascade<E>
{
	private static final Nadir<?> INSTANCE = new Nadir<>();

	@SuppressWarnings("unchecked")
	static <E> Cascade<E> instance()
	{
		return (Cascade<E>) INSTANCE;
	}

	@Override
	public boolean sterile()
	{
		return true;
	}

	// -------------------------------------------------------------------------
	// Presence and extraction
	// -------------------------------------------------------------------------

	@Override
	public boolean supple()
	{
		return false;
	}

	@Override
	public Collection<E> summon()
	{
		throw EmptyCascadeException.instance();
	}

	@Override
	public Stream<E> stream()
	{
		return Stream.empty();
	}

	@Override
	public Relic<E> enshrine()
	{
		return Crucible.consecrate(List.of());
	}

	@Override
	public Forge<E> awaken()
	{
		return Crucible.kindle(List.of());
	}

	@Override
	public <R> R coronate(final Function<? super Stream<E>, ? extends R> conclusion)
	{
		throw EmptyCascadeException.instance();
	}

	@Override
	public <R> R coronate(final Function<? super Stream<E>, ? extends R> conclusion, final Supplier<? extends R> grace)
	{
		Objects.requireNonNull(grace, "grace may not be null");
		return grace.get();
	}

	@Override
	public <F> Cascade<F> metamorphose(final Function<? super E, ? extends F> metamorphosis)
	{
		return instance();
	}

	// -------------------------------------------------------------------------
	// Transformation and transmutation
	// -------------------------------------------------------------------------

	@Override
	public <U, F> Cascade<Pair<U, F>> metamorphose(final Function<? super E, ? extends U> fate,
	                                               final Function<? super E, ? extends F> destiny)
	{
		return instance();
	}

	@Override
	public Cascade<E> develop(final Predicate<? super E> judgement,
	                          final Function<? super E, ? extends E> development)
	{
		return instance();
	}

	@Override
	public <F> Cascade<F> evolve(final Predicate<? super E> judgement,
	                             final Function<? super E, ? extends F> evolution)
	{
		return instance();
	}

	@Override
	public Cascade<E> ascend(final UnaryOperator<E> ascension, final int levels)
	{
		return instance();
	}

	@Override
	public <F> Cascade<Pair<E, F>> interlace(final Function<? super E, ? extends F> interlacing)
	{
		return instance();
	}

	@Override
	public Cascade<E> purify()
	{
		return instance();
	}

	@Override
	public Cascade<E> ordain(final Comparator<? super E> order)
	{
		return instance();
	}

	@Override
	public Cascade<E> ordain()
	{
		return instance();
	}

	@Override
	public <F> Cascade<F> transfigure(final Function<Collection<? extends E>, ? extends Collection<F>> transmutation)
	{
		return instance();
	}

	@Override
	public Cascade<E> discern(final Predicate<? super E> judgement)
	{
		return instance();
	}

	// -------------------------------------------------------------------------
	// Judgment and branching
	// -------------------------------------------------------------------------

	@Override
	public Cascade<E> discern(final Predicate<? super E> judgement,
	                          final Supplier<? extends RuntimeException> wrath)
	{
		return instance();
	}

	@Override
	public <F> Cascade<F> cleave(final Predicate<? super E> judgement,
	                             final Function<? super E, ? extends F> reward,
	                             final Function<? super E, ? extends F> punishment)
	{
		return instance();
	}

	@Override
	public <F> Cascade<F> entwine(final Function<? super E, Cascade<F>> plot)
	{
		return instance();
	}

	// -------------------------------------------------------------------------
	// Composition
	// -------------------------------------------------------------------------

	@Override
	public <F> Cascade<F> alchemize(final Function<? super E, Optional<? extends F>> potion)
	{
		return instance();
	}

	@Override
	public <U, F> Cascade<F> conjoin(final U consort,
	                                 final BiFunction<? super E, ? super U, ? extends F> conjugation)
	{
		return instance();
	}

	@Override
	public <F, G> Cascade<G> braid(final Cascade<F> consort,
	                               final BiFunction<Unfolding<E>, Unfolding<F>, ? extends G> weaver)
	{
		Objects.requireNonNull(consort, "consort may not be null");
		Objects.requireNonNull(weaver, "weaver may not be null");

		return Cascade.beckon(consort.stream()
		                             .map(f -> weaver.apply(Unfolding.chaos(), Unfolding.beckon(f))));
	}

	@Override
	public <R> R weave(final R initial, final BiFunction<? super R, ? super E, ? extends R> operation)
	{
		Objects.requireNonNull(operation, "operation may not be null");
		return initial;
	}

	// -------------------------------------------------------------------------
	// Folding / reduction
	// -------------------------------------------------------------------------

	@Override
	public Unfolding<E> smelt(final BiFunction<? super E, ? super E, ? extends E> operation)
	{
		Objects.requireNonNull(operation, "operation may not be null");
		return Loom.<E>thread(List.of()).forge(operation);
	}

	@Override
	public Collection<E> infuse(final Supplier<? extends Collection<? extends E>> revelation)
	{
		Objects.requireNonNull(revelation, "revelation may not be null");
		@SuppressWarnings("unchecked")
		var result = (Collection<E>) revelation.get();
		return result;
	}

	// -------------------------------------------------------------------------
	// Recovery and renewal
	// -------------------------------------------------------------------------

	@Override
	public Collection<E> infuse(final Collection<? extends E> manifestation)
	{
		Objects.requireNonNull(manifestation, "manifestation may not be null");
		@SuppressWarnings("unchecked")
		var result = (Collection<E>) manifestation;
		return result;
	}

	@Override
	public Cascade<E> resurrect(final Supplier<Cascade<E>> grace)
	{
		Objects.requireNonNull(grace, "grace may not be null");
		return Optional.ofNullable(grace.get()).orElseGet(Nadir::instance);
	}

	@Override
	public Cascade<E> revive(final Supplier<? extends E> grace)
	{
		Objects.requireNonNull(grace, "grace may not be null");
		return Cascade.beckon(grace.get());
	}

	@Override
	public Cascade<E> unlace(final Consumer<? super E> impregnator)
	{
		return instance();
	}

	// -------------------------------------------------------------------------
	// Effects
	// -------------------------------------------------------------------------

	@Override
	public Cascade<E> unlace(final Predicate<? super E> judgement, final Consumer<? super E> impregnator)
	{
		return instance();
	}

	@Override
	public String toString()
	{
		return "Cascade{}";
	}

	private Nadir()
	{
	}
}