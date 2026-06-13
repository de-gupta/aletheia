package de.gupta.aletheia.functional;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class PresentVerdict<T> implements Verdict<T>
{
	private final T hero;

	@Override
	public <R> VerdictArmed<T, R> when(final Predicate<? super T> judgement,
	                                   final Function<? super T, ? extends R> reward)
	{
		return new PresentVerdictArmed<T, R>(hero).when(judgement, reward);
	}

	@Override
	public <R> VerdictArmed<T, R> when(final Predicate<? super T> judgement, final Supplier<? extends R> reward)
	{
		return new PresentVerdictArmed<T, R>(hero).when(judgement, reward);
	}

	@Override
	public <R> VerdictArmed<T, R> when(final Predicate<? super T> judgement, final R reward)
	{
		return new PresentVerdictArmed<T, R>(hero).when(judgement, reward);
	}

	PresentVerdict(final T hero)
	{
		this.hero = hero;
	}
}