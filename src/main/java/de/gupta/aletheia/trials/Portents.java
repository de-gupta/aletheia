package de.gupta.aletheia.trials;

import java.util.List;
import java.util.Objects;

final class Portents
{
	static <R> Fallible<R> redeem(final Exception fury, final List<Portent<R>> portents)
	{
		Objects.requireNonNull(fury, "fury may not be null");
		Objects.requireNonNull(portents, "portents may not be null");

		for (var portent : portents)
		{
			var redemption = Objects.requireNonNull(portent, "portents may not contain null")
			                        .heed(fury);

			if (redemption.isPresent())
			{
				return Fallible.beckon(redemption.get());
			}
		}

		return Fury.invoke(fury);
	}

	private Portents()
	{
	}
}