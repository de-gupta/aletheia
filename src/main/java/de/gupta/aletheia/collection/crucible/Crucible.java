package de.gupta.aletheia.collection.crucible;

import de.gupta.aletheia.collection.cascade.Cascade;

import java.util.Collection;
import java.util.function.Function;

public sealed interface Crucible<E> permits Forge, Relic
{
	// ── Conventional names ────────────────────────────────────────────────────────────────────────
	// Standard Collection-style API. Each method delegates to its mythic equivalent below.

	/**
	 * Returns a mutable crucible.
	 */
	static <E> Forge<E> mutableOf(final Collection<E> elements)
	{
		return kindle(elements);
	}

	/**
	 * Returns an immutable crucible.
	 */
	static <E> Relic<E> immutableOf(final Collection<E> elements)
	{
		return consecrate(elements);
	}

	default Crucible<E> add(final E element)
	{
		return embrace(element);
	}

	default Crucible<E> remove(final E element)
	{
		return banish(element);
	}

	default <F> Crucible<F> map(final Function<? super E, ? extends F> mapper)
	{
		return metamorphose(mapper);
	}

	default Relic<E> freeze()
	{
		return enshrine();
	}

	default Forge<E> toMutable()
	{
		return awaken();
	}

	default Collection<E> toCollection()
	{
		return manifest();
	}

	default boolean contains(final E element)
	{
		return harbors(element);
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────
	// Primary vocabulary. Conventional aliases are above.

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

	<F> Crucible<F> metamorphose(final Function<? super E, ? extends F> metamorphosis);

	Relic<E> enshrine();

	Forge<E> awaken();

	Collection<E> manifest();

	default boolean harbors(final E essence)
	{
		return manifest().contains(essence);
	}

	default Cascade<E> cascade()
	{
		return Cascade.distill(this);
	}
}