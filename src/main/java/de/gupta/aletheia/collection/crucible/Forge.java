package de.gupta.aletheia.collection.crucible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

public final class Forge<E> implements Crucible<E>
{
	private final Collection<E> elements;

	static <E> Forge<E> kindle(final Collection<? extends E> elements)
	{
		Objects.requireNonNull(elements, "elements may not be null");
		return new Forge<>(elements);
	}

	@Override
	public Crucible<E> embrace(final E element)
	{
		Collection<E> embraced = new ArrayList<>(this.elements);
		embraced.add(element);
		return kindle(embraced);
	}

	@Override
	public Crucible<E> banish(final E element)
	{
		Collection<E> banished = new ArrayList<>(this.elements);
		banished.remove(element);
		return kindle(banished);
	}

	@Override
	public <F> Crucible<F> metamorphose(final Function<? super E, ? extends F> metamorphosis)
	{
		return kindle(this.elements.stream().map(metamorphosis).toList());
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

	@Override
	public Collection<E> manifest()
	{
		return Collections.unmodifiableList(new ArrayList<>(this.elements));
	}

	private Forge(final Collection<? extends E> elements)
	{
		this.elements = new ArrayList<>(elements);
	}
}