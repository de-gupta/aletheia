package de.gupta.aletheia.collection.crucible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class Relic<E> implements Crucible<E>
{
	private final Collection<E> elements;

	static <E> Relic<E> consecrate(final Collection<E> elements)
	{
		return new Relic<>(elements);
	}

	@Override
	public Crucible<E> embrace(final E element)
	{
		throw new UnsupportedOperationException("A relic does not embrace elements");
	}

	@Override
	public Crucible<E> banish(final E element)
	{
		throw new UnsupportedOperationException("A relic does not banish elements");
	}

	@Override
	public Relic<E> enshrine()
	{
		return this;
	}

	@Override
	public Forge<E> awaken()
	{
		return Forge.kindle(this.elements);
	}

	@Override
	public Collection<E> manifest()
	{
		return Collections.unmodifiableList(new ArrayList<>(this.elements));
	}

	private Relic(final Collection<E> elements)
	{
		this.elements = elements;
	}
}