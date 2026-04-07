package de.gupta.aletheia.trials;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class Portent<R>
{
	private final Class<? extends Exception> omen;
	private final Function<? super Exception, ? extends R> prophecy;

	public static <E extends Exception, R> Portent<R> foretell(final Class<E> omen,
	                                                           final Function<? super E, ? extends R> prophecy)
	{
		Objects.requireNonNull(omen, "omen may not be null");
		Objects.requireNonNull(prophecy, "prophecy may not be null");

		return new Portent<>(omen, fury -> prophecy.apply(omen.cast(fury)));
	}

	Optional<R> heed(final Exception fury)
	{
		Objects.requireNonNull(fury, "fury may not be null");

		return omen.isInstance(fury) ? Optional.of(prophecy.apply(fury)) : Optional.empty();
	}

	private Portent(final Class<? extends Exception> omen, final Function<? super Exception, ? extends R> prophecy)
	{
		this.omen = omen;
		this.prophecy = prophecy;
	}
}