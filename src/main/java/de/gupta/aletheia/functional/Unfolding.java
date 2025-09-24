package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
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

	@Deprecated(since = "0.0.6", forRemoval = true)
	static <T> Unfolding<T> of(final T apparition)
	{
		return beckon(apparition);
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

	T summon();

	T decree(final Supplier<? extends RuntimeException> wrath);

	void interdict(final Supplier<? extends RuntimeException> wrath);

	void interdict(final Function<? super T, Supplier<? extends RuntimeException>> wrath);

	boolean sterile();

	boolean supple();

	Unfolding<T> discern(final Predicate<? super T> judgement);

	Unfolding<T> discern(final Predicate<? super T> judgement,
						 final Supplier<? extends RuntimeException> wrath);

	Unfolding<T> develop(final Predicate<? super T> judgement, Function<? super T, ? extends T> development);

	@Deprecated(since = "0.0.3", forRemoval = true)
	default <R> Unfolding<R> refold(final Function<? super T, ? extends R> folding)
	{
		return metamorphose(folding);
	}

	<R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis);

	<U, R> Unfolding<Pair<U, R>> metamorphose(final Function<? super T, ? extends U> fate,
											  final Function<? super T, ? extends R> destiny);

	<R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis,
								  final Supplier<? extends RuntimeException> wrath);

	<R> Unfolding<R> alchemize(final Function<? super T, Optional<? extends R>> potion);

	<R> Unfolding<R> evolve(final Predicate<? super T> judgement, Function<? super T, ? extends R> evolution);

	<R> Unfolding<R> cleave(final Predicate<? super T> judgement,
							final Function<? super T, ? extends R> reward,
							final Function<? super T, ? extends R> punishment);

	<R> R cleave(final Predicate<? super T> judgement, R reward, R punishment);

	<R> R cleave(final Map<Predicate<? super T>, Function<? super T, R>> judgments, final R punishment);

	<R> R smite(final Map<Predicate<? super T>, Function<? super T, R>> judgments,
				final Supplier<? extends RuntimeException> wrath);

	<R> R cleave(final SortedMap<Predicate<? super T>, Function<? super T, R>> judgments, final R punishment);

	<R> Unfolding<R> entwine(final Function<? super T, Unfolding<R>> plot);

	<R> Unfolding<Pair<T, R>> interlace(final Function<? super T, ? extends R> interlacing);

	<U, R> Unfolding<R> conjoin(final U consort, final BiFunction<? super T, ? super U, ? extends R> conjugation);

	<U, R> Unfolding<R> emanate(final Function<? super T, U> marriage,
								final BiFunction<? super T, ? super U, R> conjugation);

	<U, R> Unfolding<R> sanctify(final Function<? super T, U> marriage,
								 final BiFunction<? super T, ? super U, R> conjugation,
								 final Predicate<? super R> judgment,
								 final Supplier<? extends RuntimeException> wrath);

	<R, U> Unfolding<U> braid(final Unfolding<R> consort, final BiFunction<? super T, ? super R, ? extends U> weaver);

	<R> R coronate(final Function<? super T, ? extends R> conclusion);

	T rescue(final Supplier<? extends T> revelation);

	T rescue(final T manifestation);

	Unfolding<T> resurrect(final Supplier<Unfolding<T>> grace);

	Unfolding<T> revive(final Supplier<T> grace);

	Stream<T> stream();

	Unfolding<T> unlace(final Consumer<? super T> impregnator);

	Optional<T> optional();

	Unfolding<T> ascend(final UnaryOperator<T> ascension, final int levels);
}