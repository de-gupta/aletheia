package de.gupta.aletheia.functional;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IUnfolding<T>
{
	static <T> IUnfolding<T> of(T value)
	{
		return Optional.ofNullable(value)
				.map(Unfolding::from)
				.orElseGet(EmptyUnfolding::instance);
	}

	T summon();

	<R> IUnfolding<R> refold(Function<? super T, ? extends R> folding);

	boolean isEmpty();

	IUnfolding<T> develop(Predicate<? super T> judgement, Function<? super T, ? extends T> development);

	<R> IUnfolding<R> evolve(Predicate<? super T> judgement, Function<? super T, ? extends R> evolution);

	Stream<T> stream();

	<R> IUnfolding<R> cleave(Predicate<? super T> judgement,
							Function<? super T, ? extends R> reward,
							Function<? super T, ? extends R> punishment);

	IUnfolding<T> discern(Predicate<? super T> judgement);

	IUnfolding<T> unlace(Consumer<? super T> impregnator);

	boolean isPresent();

	<R> R concludeWith(Function<? super T, ? extends R> conclusion);

	T alternatively(Supplier<? extends T> revelation);

	T alternatively(T alternative);

	Optional<T> optional();
}