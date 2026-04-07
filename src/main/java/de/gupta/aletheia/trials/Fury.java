package de.gupta.aletheia.trials;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

final class Fury<T> implements Fallible<T>
{
	private final Exception doom;

	static <T> Fury<T> invoke(final Exception doom)
	{
		return new Fury<>(doom);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> Fallible<R> metamorphose(final Ordeal<? super T, ? extends R> ordeal,
	                                    final List<Portent<R>> portents)
	{
		Objects.requireNonNull(ordeal, "ordeal may not be null");
		Objects.requireNonNull(portents, "portents may not be null");

		return (Fallible<R>) this;
	}

	@Override
	public <R> R coronate(final Function<? super T, ? extends R> triumph,
	                      final Function<? super Exception, ? extends R> fury)
	{
		Objects.requireNonNull(triumph, "triumph may not be null");
		Objects.requireNonNull(fury, "fury may not be null");

		return fury.apply(doom);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(doom);
	}

	@Override
	public boolean equals(final Object o)
	{
		return this == o || (o instanceof Fury<?> other && Objects.equals(doom, other.doom));
	}

	private Fury(final Exception doom)
	{
		this.doom = Objects.requireNonNull(doom, "doom may not be null");
	}
}