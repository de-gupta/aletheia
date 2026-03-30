package de.gupta.aletheia.collection.crucible;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

public final class Relic<E> implements Crucible<E>
{
	private final Collection<? extends E> elements;

	static <E> Relic<E> consecrate(final Collection<? extends E> elements)
	{
		Objects.requireNonNull(elements, "elements may not be null");
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
	public <F> Crucible<F> metamorphose(final Function<? super E, ? extends F> metamorphosis)
	{
		return consecrate(
				this.elements.stream()
							 .map(metamorphosis)
							 .toList()
		);
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

	private Relic(final Collection<? extends E> elements)
	{
		this.elements = new ArrayList<>(elements);
	}
}