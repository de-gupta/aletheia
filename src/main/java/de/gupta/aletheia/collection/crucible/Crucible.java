package de.gupta.aletheia.collection.crucible;

import java.util.Collection;

public sealed interface Crucible<E> permits Forge, Relic
{
	static <E> Relic<E> consecrate(final Collection<E> collection)
	{
		return Relic.consecrate(collection);
	}

	static <E> Forge<E> kindle(final Collection<E> elements)
	{
		return Forge.kindle(elements);
	}

	Crucible<E> embrace(E element);

	Crucible<E> banish(E element);

	Relic<E> enshrine();

	Forge<E> awaken();

	Collection<E> manifest();
}