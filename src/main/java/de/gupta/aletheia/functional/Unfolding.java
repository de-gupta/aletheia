package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Pair;

import java.util.Optional;
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

	static <T> Unfolding<T> beckon(T apparition)
	{
		return Optional.ofNullable(apparition)
					   .map(Myth::beckon)
					   .orElseGet(Unfolding::chaos);
	}

	static <T> Unfolding<T> chaos()
	{
		return Shell.instance();
	}

	@Deprecated(since = "0.0.6", forRemoval = true)
	static <T> Unfolding<T> of(T apparition)
	{
		return beckon(apparition);
	}

	T summon();

	T decree(Supplier<? extends RuntimeException> exceptionSupplier);

	void interdict(Supplier<? extends RuntimeException> exceptionSupplier);

	boolean sterile();

	boolean supple();

	Unfolding<T> discern(Predicate<? super T> judgement);

	Unfolding<T> discern(final Predicate<? super T> judgement,
						 final Supplier<? extends RuntimeException> exceptionSupplier);

	Unfolding<T> develop(Predicate<? super T> judgement, Function<? super T, ? extends T> development);

	@Deprecated(since = "0.0.3", forRemoval = true)
	default <R> Unfolding<R> refold(Function<? super T, ? extends R> folding)
	{
		return metamorphose(folding);
	}

	<R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis);

	<R> Unfolding<R> evolve(Predicate<? super T> judgement, Function<? super T, ? extends R> evolution);

	<R> Unfolding<R> cleave(Predicate<? super T> judgement,
							Function<? super T, ? extends R> reward,
							Function<? super T, ? extends R> punishment);

	<R> R cleave(Predicate<? super T> judgement, R reward, R punishment);

	<R> Unfolding<R> entwine(final Function<? super T, Unfolding<R>> plot);

	<R> Unfolding<Pair<T, R>> interlace(Function<? super T, ? extends R> interlacing);

	<U, R> Unfolding<R> conjoin(U consort, BiFunction<? super T, ? super U, ? extends R> conjugation);

	<U, R> Unfolding<R> conjoin(Function<T, U> marriage, BiFunction<? super T, ? super U, ? extends R> conjugation);

	<R, U> Unfolding<U> braid(Unfolding<R> consort, BiFunction<? super T, ? super R, ? extends U> weaver);

	<R> R concludeWith(Function<? super T, ? extends R> conclusion);

	T rescue(Supplier<? extends T> revelation);

	T rescue(T manifestation);

	Unfolding<T> resurrect(Supplier<Unfolding<T>> grace);

	Stream<T> stream();

	Unfolding<T> unlace(Consumer<? super T> impregnator);

	Optional<T> optional();
}