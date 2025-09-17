package de.gupta.aletheia.collection;

import java.util.ArrayList;
import java.util.Collection;

public final class Forge<E> implements Crucible<E>
{
	private final Collection<E> elements;

	static <E> Forge<E> kindle(final Collection<E> elements)
	{
		return new Forge<>(elements);
	}

	@Override
	public Crucible<E> embrace(final E element)
	{
		elements.add(element);
		return kindle(this.elements);
	}

	@Override
	public Crucible<E> banish(final E element)
	{
		elements.remove(element);
		return kindle(this.elements);
	}

	@Override
	public Relic<E> enshrine()
	{
		return Relic.consecrate(this.elements);
	}

	@Override
	public Forge<E> awaken()
	{
		return this;
	}

	private Forge(final Collection<E> elements)
	{
		this.elements = new ArrayList<>(elements);
	}
}