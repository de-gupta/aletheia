package de.gupta.aletheia.trials;

import java.util.List;
import java.util.function.Function;

public sealed interface Fallible<T> permits Triumph, Fury
{
	static <T> Fallible<T> beckon(final T boon)
	{
		return Triumph.beckon(boon);
	}

	<R> Fallible<R> metamorphose(final Ordeal<? super T, ? extends R> ordeal,
	                             final List<Portent<R>> portents);

	<R> R coronate(final Function<? super T, ? extends R> triumph,
	               final Function<? super Exception, ? extends R> fury);
}