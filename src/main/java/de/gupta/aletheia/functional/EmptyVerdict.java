package de.gupta.aletheia.functional;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class EmptyVerdict<T> implements Verdict<T>
{
	private static final EmptyVerdict<?> INSTANCE = new EmptyVerdict<>();

	@SuppressWarnings("unchecked")
	static <T> Verdict<T> instance()
	{
		return (Verdict<T>) INSTANCE;
	}

	@Override
	public <R> VerdictArmed<T, R> when(final Predicate<? super T> judgement,
	                                   final Function<? super T, ? extends R> reward)
	{
		return EmptyVerdictArmed.instance();
	}

	@Override
	public <R> VerdictArmed<T, R> when(final Predicate<? super T> judgement, final Supplier<? extends R> reward)
	{
		return EmptyVerdictArmed.instance();
	}

	@Override
	public <R> VerdictArmed<T, R> when(final Predicate<? super T> judgement, final R reward)
	{
		return EmptyVerdictArmed.instance();
	}

	private EmptyVerdict()
	{
	}
}