package de.gupta.aletheia.functional;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class EmptyVerdictArmed<T, R> implements VerdictArmed<T, R>
{
	private static final EmptyVerdictArmed<?, ?> INSTANCE = new EmptyVerdictArmed<>();

	@SuppressWarnings("unchecked")
	static <T, R> VerdictArmed<T, R> instance()
	{
		return (VerdictArmed<T, R>) INSTANCE;
	}

	@Override
	public VerdictArmed<T, R> when(final Predicate<? super T> judgement,
	                               final Function<? super T, ? extends R> reward)
	{
		return this;
	}

	@Override
	public VerdictArmed<T, R> when(final Predicate<? super T> judgement, final Supplier<? extends R> reward)
	{
		return this;
	}

	@Override
	public VerdictArmed<T, R> when(final Predicate<? super T> judgement, final R reward)
	{
		return this;
	}

	@Override
	public R infuse(final R manifestation)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public R infuse(final Supplier<? extends R> revelation)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public R infuse(final Function<? super T, ? extends R> revelation)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public R smite(final Supplier<? extends RuntimeException> wrath)
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public R fulminate()
	{
		throw EmptyUnfoldingException.instance();
	}

	@Override
	public Unfolding<R> pronounce()
	{
		return Unfolding.chaos();
	}

	private EmptyVerdictArmed()
	{
	}
}