package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Pair;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

final class Myth<T> implements Unfolding<T>
{
	private final T value;

	static <T> Unfolding<T> of(final T value)
	{
		return new Myth<>(value);
	}

	@Override
	public T summon()
	{
		return value;
	}

	@Override
	public <R> Unfolding<R> refold(Function<? super T, ? extends R> folding)
	{
		Objects.requireNonNull(folding, "folding may not be null");
		return Unfolding.of(folding.apply(value));
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(value);
	}

	@Override
	public boolean equals(final Object o)
	{
		return this == o || (o instanceof Myth<?> other && Objects.equals(value, other.value));
	}

	@Override
	public boolean sterile()
	{
		return false;
	}

	@Override
	public Unfolding<T> develop(Predicate<? super T> judgement, Function<? super T, ? extends T> development)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(development, "development may not be null");

		return judgement.test(value) ? Unfolding.of(development.apply(value)) : this;
	}

	@Override
	public <R> Unfolding<R> evolve(Predicate<? super T> judgement, Function<? super T, ? extends R> evolution)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(evolution, "evolution may not be null");

		return judgement.test(value) ? Unfolding.of(evolution.apply(value)) : Unfolding.empty();
	}

	@Override
	public <R> Unfolding<Pair<T, R>> interlace(final Function<? super T, ? extends R> interlacing)
	{
		Objects.requireNonNull(interlacing, "interlacing may not be null");

		return Unfolding.of(Pair.of(value, interlacing.apply(value)));
	}

	@Override
	public Stream<T> stream()
	{
		return Stream.of(value);
	}

	@Override
	public <R> Unfolding<R> cleave(final Predicate<? super T> judgement,
								   final Function<? super T, ? extends R> reward,
								   final Function<? super T, ? extends R> punishment)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(reward, "reward may not be null");
		Objects.requireNonNull(punishment, "punishment may not be null");

		return judgement.test(value) ? Unfolding.of(reward.apply(value)) : Unfolding.of(punishment.apply(value));
	}

	@Override
	public Unfolding<T> discern(final Predicate<? super T> judgement)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		return judgement.test(value) ? this : Unfolding.empty();
	}

	@Override
	public <R> Unfolding<R> entwine(final Function<? super T, Unfolding<R>> plot)
	{
		return plot.apply(value);
	}

	@Override
	public <R> R concludeWith(final Function<? super T, ? extends R> conclusion)
	{
		Objects.requireNonNull(conclusion, "conclusion may not be null");
		return conclusion.apply(value);
	}

	@Override
	public boolean supple()
	{
		return !sterile();
	}

	@Override
	public T alternatively(final Supplier<? extends T> revelation)
	{
		Objects.requireNonNull(revelation, "revelation may not be null");
		return value;
	}

	@Override
	public T alternatively(final T manifestation)
	{
		return value;
	}

	@Override
	public Unfolding<T> unlace(final Consumer<? super T> impregnator)
	{
		Objects.requireNonNull(impregnator, "impregnator may not be null");
		impregnator.accept(value);
		return this;
	}

	@Override
	public Optional<T> optional()
	{
		return Optional.ofNullable(value);
	}

	@Override
	public String toString()
	{
		return "Unfolding[" + value + "]";
	}

	private Myth(final T value)
	{
		this.value = value;
	}
}