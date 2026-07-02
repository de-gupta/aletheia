package de.gupta.aletheia.trials;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

final class Triumph<T> implements Fallible<T>
{
	private final T boon;

	static <T> Triumph<T> beckon(final T boon)
	{
		return new Triumph<>(boon);
	}

	@Override
	public T summon()
	{
		return boon;
	}

	@Override
	public <R> Fallible<R> metamorphose(final Ordeal<? super T, ? extends R> ordeal,
	                                    final List<Portent<R>> portents)
	{
		Objects.requireNonNull(ordeal, "ordeal may not be null");
		Objects.requireNonNull(portents, "portents may not be null");

		try
		{
			return Fallible.beckon(ordeal.endure(boon));
		}
		catch (Exception fury)
		{
			return Portents.redeem(fury, portents);
		}
	}

	@Override
	public <R> R coronate(final Function<? super T, ? extends R> triumph,
	                      final Function<? super Exception, ? extends R> fury)
	{
		Objects.requireNonNull(triumph, "triumph may not be null");
		Objects.requireNonNull(fury, "fury may not be null");

		return triumph.apply(boon);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(boon);
	}

	@Override
	public boolean equals(final Object o)
	{
		return this == o || (o instanceof Triumph<?> other && Objects.equals(boon, other.boon));
	}

	private Triumph(final T boon)
	{
		this.boon = boon;
	}
}