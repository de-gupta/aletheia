package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Pair;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public sealed interface Unfolding<T> permits Shell, Myth
{
	static <T> Unfolding<T> empty()
	{
		return Shell.instance();
	}

	static <T> Unfolding<T> of(T value)
	{
		return Optional.ofNullable(value)
					   .map(Myth::of)
					   .orElseGet(Shell::instance);
	}

	T summon();

	boolean sterile();

	boolean supple();

	Unfolding<T> discern(Predicate<? super T> judgement);

	Unfolding<T> develop(Predicate<? super T> judgement, Function<? super T, ? extends T> development);

	<R> Unfolding<R> refold(Function<? super T, ? extends R> folding);

	<R> Unfolding<R> evolve(Predicate<? super T> judgement, Function<? super T, ? extends R> evolution);

	<R> Unfolding<R> cleave(Predicate<? super T> judgement,
							Function<? super T, ? extends R> reward,
							Function<? super T, ? extends R> punishment);

	<R> Unfolding<R> entwine(final Function<? super T, Unfolding<R>> plot);

	<R> Unfolding<Pair<T, R>> interlace(Function<? super T, ? extends R> interlacing);

	<R> R concludeWith(Function<? super T, ? extends R> conclusion);

	T alternatively(Supplier<? extends T> revelation);

	T alternatively(T manifestation);

	Stream<T> stream();

	Unfolding<T> unlace(Consumer<? super T> impregnator);

	Optional<T> optional();
}