package de.gupta.aletheia.functional;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

final class EmptyUnfolding<T> implements Unfolding<T>
{
	private static final EmptyUnfolding<?> INSTANCE = new EmptyUnfolding<>();

	@Override
	public <R> Unfolding<R> refold(final Function<? super T, ? extends R> folding)
	{
		return instance();
	}

	@Override
	public T summon()
	{
		throw EmptyUnfoldingException.instance();
	}

	public static <T> Unfolding<T> instance()
	{
		return (Unfolding<T>) INSTANCE;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public Unfolding<T> develop(final Predicate<? super T> judgement,
								final Function<? super T, ? extends T> development)
	{
		return instance();
	}

	@Override
	public <R> Unfolding<R> evolve(final Predicate<? super T> judgement,
								   final Function<? super T, ? extends R> evolution)
	{
		return instance();
	}

	@Override
	public Stream<T> stream()
	{
		return Stream.empty();
	}

	@Override
	public <R> Unfolding<R> cleave(final Predicate<? super T> judgement, final Function<? super T, ? extends R> reward,
								   final Function<? super T, ? extends R> punishment)
	{
		return instance();
	}

	@Override
	public Unfolding<T> discern(final Predicate<? super T> judgement)
	{
		return instance();
	}

	@Override
	public Unfolding<T> unlace(final Consumer<? super T> impregnator)
	{
		return instance();
	}

	@Override
	public boolean isPresent()
	{
		return false;
	}

	@Override
	public <R> R concludeWith(final Function<? super T, ? extends R> conclusion)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public T alternatively(final Supplier<? extends T> revelation)
	{
		return revelation.get();
	}

	@Override
	public T alternatively(final T alternative)
	{
		return alternative;
	}

	@Override
	public Optional<T> optional()
	{
		return Optional.empty();
	}

	@Override
	public String toString()
	{
		return "EmptyUnfolding{}";
	}

	private EmptyUnfolding()
	{
	}
}