package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Dyad;

import java.util.Map;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.function.*;
import java.util.stream.Stream;

public sealed interface Unfolding<T> permits Shell, Myth
{
	static <T> Unfolding<T> distill(final Stream<T> brook)
	{
		return augur(brook.findFirst());
	}

	static <T> Unfolding<T> augur(final Optional<T> omen)
	{
		return omen.map(Unfolding::beckon).orElseGet(Unfolding::chaos);
	}

	static <T> Unfolding<T> chaos()
	{
		return Shell.instance();
	}

	static <T> Unfolding<T> beckon(final T apparition)
	{
		return Optional.ofNullable(apparition)
		               .map(Myth::beckon)
		               .orElseGet(Unfolding::chaos);
	}

	static <T> Unfolding<T> adjudicate(final T apparition, final boolean judgement)
	{
		return judgement ? beckon(apparition) : chaos();
	}

	/**
	 * Presence and extraction.
	 */
	T summon();

	T decree(final Supplier<? extends RuntimeException> wrath);

	boolean sterile();

	boolean supple();

	Optional<T> optional();

	Stream<T> stream();

	<R> R coronate(final Function<? super T, ? extends R> proclamation);

	<R> R coronate(final Predicate<? super T> judgement,
	               final Function<? super T, ? extends R> reward,
	               final Function<? super T, ? extends R> punishment);

	/**
	 * Transformation and transmutation.
	 */
	Unfolding<T> develop(final Predicate<? super T> judgement, Function<? super T, ? extends T> development);

	<R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis);

	<U, R> Unfolding<Dyad<U, R>> metamorphose(final Function<? super T, ? extends U> fate,
	                                          final Function<? super T, ? extends R> destiny);

	<R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis,
	                              final Supplier<? extends RuntimeException> wrath);

	<R> Unfolding<R> alchemize(final Function<? super T, Optional<? extends R>> potion);

	<R> Unfolding<R> evolve(final Predicate<? super T> judgement, Function<? super T, ? extends R> evolution);

	Unfolding<T> ascend(final UnaryOperator<T> ascension, final int levels);

	/**
	 * Judgment and branching.
	 */
	Unfolding<T> discern(final Predicate<? super T> judgement);

	Unfolding<T> discern(final Predicate<? super T> judgement,
	                     final Supplier<? extends RuntimeException> wrath);

	<R> Unfolding<R> cleave(final Predicate<? super T> judgement,
	                        final Function<? super T, ? extends R> reward,
	                        final Function<? super T, ? extends R> punishment);

	<R> R cleave(final Predicate<? super T> judgement, R reward, R punishment);

	<R> R cleave(final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments, final R punishment);

	<R> R smite(final Map<Predicate<? super T>, Function<? super T, R>> judgments,
	            final Supplier<? extends RuntimeException> wrath);

	<U, R> Unfolding<R> sanctify(final Function<? super T, U> marriage,
	                             final BiFunction<? super T, ? super U, R> conjugation,
	                             final Predicate<? super R> judgement,
	                             final Supplier<? extends RuntimeException> wrath);

	/**
	 * Composition and pairing.
	 */
	<R> Unfolding<R> entwine(final Function<? super T, Unfolding<R>> plot);

	<R> Unfolding<Dyad<T, R>> interlace(final Function<? super T, ? extends R> interlacing);

	<U, R> Unfolding<R> conjoin(final U consort, final BiFunction<? super T, ? super U, ? extends R> conjugation);

	<U, R> Unfolding<R> emanate(final Function<? super T, U> marriage,
	                            final BiFunction<? super T, ? super U, R> conjugation);

	<R, U> Unfolding<U> braid(final Unfolding<R> consort, final BiFunction<? super T, ? super R, ? extends U> weaver);

	/**
	 * Composition and gathering
	 */
	<R, A> Unfolding<A> convoke(final SequencedCollection<Function<? super T, ? extends R>> omens,
	                            final Function<? super SequencedCollection<? extends R>, ? extends A> oracle);

	/**
	 * Recovery and renewal.
	 */
	T infuse(final Supplier<? extends T> revelation);

	T infuse(final T manifestation);

	Unfolding<T> resurrect(final Supplier<Unfolding<T>> grace);

	Unfolding<T> revive(final Supplier<T> grace);

	/**
	 * Effects and interruption.
	 */
	Unfolding<T> unlace(final Consumer<? super T> impregnator);

	Unfolding<T> unlace(final Predicate<? super T> judgement, final Consumer<? super T> impregnator);

	void interdict(final Supplier<? extends RuntimeException> wrath);

	void interdict(final Function<? super T, Supplier<? extends RuntimeException>> wrath);
}