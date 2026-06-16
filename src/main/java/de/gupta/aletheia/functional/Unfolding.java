package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Dyad;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public sealed interface Unfolding<T> permits Shell, Myth
{
	// ── Conventional names ────────────────────────────────────────────────────────────────────────
	// Standard Optional/Stream-style API. Each method delegates to its mythic equivalent below.

	static <T> Unfolding<T> of(final T value)
	{
		return beckon(Objects.requireNonNull(value));
	}

	static <T> Unfolding<T> ofNullable(final T value)
	{
		return beckon(value);
	}

	static <T> Unfolding<T> empty()
	{
		return chaos();
	}

	static <T> Unfolding<T> fromOptional(final Optional<T> optional)
	{
		return augur(optional);
	}

	static <T> Unfolding<T> fromStream(final Stream<T> stream)
	{
		return distill(stream);
	}

	default T get()
	{
		return summon();
	}

	default boolean isPresent()
	{
		return supple();
	}

	default boolean isEmpty()
	{
		return sterile();
	}

	default <R> Unfolding<R> map(final Function<? super T, ? extends R> mapper)
	{
		return metamorphose(mapper);
	}

	default <R> Unfolding<R> flatMap(final Function<? super T, Unfolding<R>> mapper)
	{
		return entwine(mapper);
	}

	default <R> Unfolding<R> flatMapOptional(final Function<? super T, Optional<? extends R>> mapper)
	{
		return alchemize(mapper);
	}

	default Unfolding<T> filter(final Predicate<? super T> predicate)
	{
		return discern(predicate);
	}

	default T orElse(final T other)
	{
		return infuse(other);
	}

	default T orElseGet(final Supplier<? extends T> supplier)
	{
		return infuse(supplier);
	}

	default T orElseThrow()
	{
		return summon();
	}

	default T orElseThrow(final Supplier<? extends RuntimeException> exceptionSupplier)
	{
		return decree(exceptionSupplier);
	}

	default Unfolding<T> orElseRecover(final Supplier<Unfolding<T>> recovery)
	{
		return resurrect(recovery);
	}

	default Unfolding<T> peek(final Consumer<? super T> consumer)
	{
		return unlace(consumer);
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────
	// Primary vocabulary. Conventional aliases are above.

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

	<R> R coronate(final Function<? super T, ? extends R> proclamation, R refuge);

	<R> R coronate(final Function<? super T, ? extends R> proclamation, Supplier<? extends R> refuge);

	<R> R coronate(final Predicate<? super T> judgement,
	               final Function<? super T, ? extends R> reward,
	               final Function<? super T, ? extends R> punishment);

	<R> R reap(R harvest);

	<R> R reap(final Supplier<? extends R> harvest);

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

	default Verdict<T> cleave()
	{
		return verdict();
	}

	<R> Unfolding<R> cleave(final Predicate<? super T> judgement,
	                        final Function<? super T, ? extends R> reward,
	                        final Function<? super T, ? extends R> punishment);

	<R> R cleave(final Predicate<? super T> judgement, R reward, R punishment);

	<R> R cleave(final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments, final R punishment);

	<R> R smite(final Map<Predicate<? super T>, Function<? super T, R>> judgments,
	            final Supplier<? extends RuntimeException> wrath);

	<R> R smite(final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments,
	            final Supplier<? extends RuntimeException> wrath);

	<R> R fulminate(final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments);

	Verdict<T> verdict();

	<R> Unfolding<R> trifurcate(final ToIntFunction<? super T> reckoning,
	                            final Function<? super T, ? extends R> diminished,
	                            final Function<? super T, ? extends R> balanced,
	                            final Function<? super T, ? extends R> ascendant);

	<R> R trifurcate(final ToIntFunction<? super T> reckoning,
	                 final Supplier<? extends R> diminished,
	                 final Supplier<? extends R> balanced,
	                 final Supplier<? extends R> ascendant);

	<R> R trifurcate(final ToIntFunction<? super T> reckoning, R diminished, R balanced, R ascendant);

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

	<U> Unfolding<T> wield(final Function<? super T, ? extends U> instrument,
	                       final BiFunction<Unfolding<T>, ? super U, Unfolding<T>> wielding);

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

	Unfolding<T> interdict(final Predicate<? super T> judgement, final Supplier<? extends RuntimeException> wrath);
}