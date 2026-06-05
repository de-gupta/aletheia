package de.gupta.aletheia.trials;

import java.util.List;
import java.util.function.Function;

public sealed interface Fallible<T> permits Triumph, Fury
{
	// ── Conventional names ────────────────────────────────────────────────────────────────────────
	// Standard Try/Either-style API. Each method delegates to its mythic equivalent below.

	static <T> Fallible<T> success(final T value)
	{
		return beckon(value);
	}

	static <T> Fallible<T> failure(final Exception exception)
	{
		return Fury.arise(exception);
	}

	/**
	 * Delegates to {@link #metamorphose(Ordeal, List)} with no recovery portents.
	 */
	default <R> Fallible<R> map(final Ordeal<? super T, ? extends R> mapper)
	{
		return metamorphose(mapper, List.of());
	}

	default <R> R fold(final Function<? super T, ? extends R> onSuccess,
	                   final Function<? super Exception, ? extends R> onFailure)
	{
		return coronate(onSuccess, onFailure);
	}

	// ── Mythic (canonical) API ────────────────────────────────────────────────────────────────────
	// Primary vocabulary. Conventional aliases are above.

	static <T> Fallible<T> beckon(final T boon)
	{
		return Triumph.beckon(boon);
	}

	<R> Fallible<R> metamorphose(final Ordeal<? super T, ? extends R> ordeal,
	                             final List<Portent<R>> portents);

	<R> R coronate(final Function<? super T, ? extends R> triumph,
	               final Function<? super Exception, ? extends R> fury);
}