package de.gupta.aletheia.functional;

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
					   .map(Myth::with)
					   .orElseGet(Shell::instance);
	}

	T summon();

	<R> Unfolding<R> refold(Function<? super T, ? extends R> folding);

	boolean sterile();

	Unfolding<T> develop(Predicate<? super T> judgement, Function<? super T, ? extends T> development);

	<R> Unfolding<R> evolve(Predicate<? super T> judgement, Function<? super T, ? extends R> evolution);

	Stream<T> stream();

	<R> Unfolding<R> cleave(Predicate<? super T> judgement,
							Function<? super T, ? extends R> reward,
							Function<? super T, ? extends R> punishment);

	Unfolding<T> discern(Predicate<? super T> judgement);

	Unfolding<T> unlace(Consumer<? super T> impregnator);

	boolean supple();

	<R> R concludeWith(Function<? super T, ? extends R> conclusion);

	T alternatively(Supplier<? extends T> revelation);

	T alternatively(T alternative);

	Optional<T> optional();
}