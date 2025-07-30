package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Pair;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

final class Myth<T> implements Unfolding<T>
{
	private final T hero;

	static <T> Unfolding<T> of(final T value)
	{
		Objects.requireNonNull(value, "value may not be null");
		return new Myth<>(value);
	}

	@Override
	public T summon()
	{
		return hero;
	}

	@Override
	public T decree(final Supplier<? extends RuntimeException> exceptionSupplier)
	{
		return hero;
	}

	@Override
	public void interdict(final Supplier<? extends RuntimeException> exceptionSupplier)
	{
		throw exceptionSupplier.get();
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(hero);
	}	@Override
	public Unfolding<T> discern(final Predicate<? super T> judgement)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		return judgement.test(hero) ? this : Unfolding.empty();
	}

	@Override
	public boolean equals(final Object o)
	{
		return this == o || (o instanceof Myth<?> other && Objects.equals(hero, other.hero));
	}

	@Override
	public String toString()
	{
		return "Unfolding[" + hero + "]";
	}	@Override
	public Unfolding<T> discern(final Predicate<? super T> judgement,
								final Supplier<? extends RuntimeException> exceptionSupplier)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(exceptionSupplier, "exceptionSupplier may not be null");

		if (!judgement.test(hero))
		{
			throw exceptionSupplier.get();
		}
		return this;
	}

	private Myth(final T hero)
	{
		this.hero = hero;
	}



	@Override
	public Unfolding<T> develop(Predicate<? super T> judgement, Function<? super T, ? extends T> development)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(development, "development may not be null");

		return judgement.test(hero) ? Unfolding.of(development.apply(hero)) : this;
	}

	@Override
	public <R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis)
	{
		Objects.requireNonNull(metamorphosis, "folding may not be null");
		return Unfolding.of(metamorphosis.apply(hero));
	}

	@Override
	public <R> Unfolding<R> evolve(Predicate<? super T> judgement, Function<? super T, ? extends R> evolution)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(evolution, "evolution may not be null");

		return judgement.test(hero) ? Unfolding.of(evolution.apply(hero)) : Unfolding.empty();
	}

	@Override
	public <U, R> Unfolding<R> conjoin(final U consort, final BiFunction<? super T, ? super U, ? extends R> conjugation)
	{
		Objects.requireNonNull(consort, "consort may not be null");
		Objects.requireNonNull(conjugation, "conjugation may not be null");
		return Unfolding.of(conjugation.apply(hero, consort));
	}




	@Override
	public <R> Unfolding<R> cleave(final Predicate<? super T> judgement, final Function<? super T, ? extends R> reward,
								   final Function<? super T, ? extends R> punishment)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(reward, "reward may not be null");
		Objects.requireNonNull(punishment, "punishment may not be null");

		return judgement.test(hero) ? Unfolding.of(reward.apply(hero)) : Unfolding.of(punishment.apply(hero));
	}

	@Override
	public <R> Unfolding<R> entwine(final Function<? super T, Unfolding<R>> plot)
	{
		return plot.apply(hero);
	}

	@Override
	public boolean sterile()
	{
		return false;
	}

	@Override
	public <R> Unfolding<Pair<T, R>> interlace(final Function<? super T, ? extends R> interlacing)
	{
		Objects.requireNonNull(interlacing, "interlacing may not be null");

		return Unfolding.of(Pair.of(hero, interlacing.apply(hero)));
	}


	@Override
	public <R> R concludeWith(final Function<? super T, ? extends R> conclusion)
	{
		Objects.requireNonNull(conclusion, "conclusion may not be null");
		return conclusion.apply(hero);
	}

	@Override
	public T alternatively(final Supplier<? extends T> revelation)
	{
		Objects.requireNonNull(revelation, "revelation may not be null");
		return hero;
	}

	@Override
	public T alternatively(final T manifestation)
	{
		return hero;
	}

	@Override
	public Stream<T> stream()
	{
		return Stream.of(hero);
	}

	@Override
	public Unfolding<T> unlace(final Consumer<? super T> impregnator)
	{
		Objects.requireNonNull(impregnator, "impregnator may not be null");
		impregnator.accept(hero);
		return this;
	}

	@Override
	public Optional<T> optional()
	{
		return Optional.ofNullable(hero);
	}

	@Override
	public boolean supple()
	{
		return !sterile();
	}


}