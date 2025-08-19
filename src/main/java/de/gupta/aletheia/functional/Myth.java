package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

final class Myth<T> implements Unfolding<T>
{
	private final T hero;

	static <T> Unfolding<T> beckon(final T value)
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
	public T decree(final Supplier<? extends RuntimeException> wrath)
	{
		return hero;
	}

	@Override
	public void interdict(final Supplier<? extends RuntimeException> wrath)
	{
		throw wrath.get();
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(hero);
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
	}

	@Override
	public Unfolding<T> discern(final Predicate<? super T> judgement)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		return judgement.test(hero) ? this : Unfolding.chaos();
	}

	@Override
	public Unfolding<T> resurrect(final Supplier<Unfolding<T>> grace)
	{
		return this;
	}

	@Override
	public Unfolding<T> discern(final Predicate<? super T> judgement,
								final Supplier<? extends RuntimeException> wrath)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(wrath, "exceptionSupplier may not be null");

		if (!judgement.test(hero))
		{
			throw wrath.get();
		}
		return this;
	}


	@Override
	public <R> R cleave(final Predicate<? super T> judgement, final R reward, final R punishment)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(reward, "reward may not be null");
		Objects.requireNonNull(punishment, "punishment may not be null");

		return judgement.test(hero) ? reward : punishment;
	}

	@Override
	public <R> R cleave(final Map<Predicate<? super T>, Function<? super T, R>> judgments, final R punishment)
	{
		Objects.requireNonNull(judgments, "judgments may not be null");
		Objects.requireNonNull(punishment, "punishment may not be null");

		return judgments.entrySet()
						.stream()
						.filter(entry -> entry.getKey().test(hero))
						.findFirst()
						.map(entry -> entry.getValue().apply(hero))
						.orElse(punishment);
	}

	@Override
	public T rescue(final Supplier<? extends T> revelation)
	{
		Objects.requireNonNull(revelation, "revelation may not be null");
		return hero;
	}

	@Override
	public Unfolding<T> develop(Predicate<? super T> judgement, Function<? super T, ? extends T> development)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(development, "development may not be null");

		return judgement.test(hero) ? Unfolding.beckon(development.apply(hero)) : this;
	}

	@Override
	public <R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis)
	{
		Objects.requireNonNull(metamorphosis, "metamorphosis may not be null");
		return Unfolding.beckon(metamorphosis.apply(hero));
	}

	@Override
	public <R> Unfolding<R> evolve(Predicate<? super T> judgement, Function<? super T, ? extends R> evolution)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(evolution, "evolution may not be null");

		return judgement.test(hero) ? Unfolding.beckon(evolution.apply(hero)) : Unfolding.chaos();
	}


	@Override
	public <R> Unfolding<R> cleave(final Predicate<? super T> judgement, final Function<? super T, ? extends R> reward,
								   final Function<? super T, ? extends R> punishment)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(reward, "reward may not be null");
		Objects.requireNonNull(punishment, "punishment may not be null");

		return judgement.test(hero) ? Unfolding.beckon(reward.apply(hero)) : Unfolding.beckon(punishment.apply(hero));
	}

	@Override
	public <R> Unfolding<R> entwine(final Function<? super T, Unfolding<R>> plot)
	{
		Objects.requireNonNull(plot, "plot may not be null");

		return Optional.ofNullable(plot.apply(hero))
					   .orElseGet(Unfolding::chaos);
	}

	@Override
	public <R> Unfolding<Pair<T, R>> interlace(final Function<? super T, ? extends R> interlacing)
	{
		Objects.requireNonNull(interlacing, "interlacing may not be null");

		return Unfolding.beckon(Pair.of(hero, interlacing.apply(hero)));
	}

	@Override
	public <U, R> Unfolding<R> conjoin(final U consort, final BiFunction<? super T, ? super U, ? extends R> conjugation)
	{
		Objects.requireNonNull(consort, "consort may not be null");
		Objects.requireNonNull(conjugation, "conjugation may not be null");

		return Unfolding.beckon(conjugation.apply(hero, consort));
	}

	@Override
	public <U, R> Unfolding<R> emanate(final Function<? super T, U> marriage,
									   final BiFunction<? super T, ? super U, R> conjugation)
	{
		Objects.requireNonNull(marriage, "marriage may not be null");
		Objects.requireNonNull(conjugation, "conjugation may not be null");

		return Optional.ofNullable(marriage.apply(hero))
					   .map(c -> conjoin(c, conjugation))
					   .orElseGet(Unfolding::chaos);
	}

	@Override
	public <U, R> Unfolding<R> sanctify(final Function<? super T, U> marriage,
										final BiFunction<? super T, ? super U, R> conjugation,
										final Predicate<? super R> judgment,
										final Supplier<? extends RuntimeException> wrath)
	{
		Objects.requireNonNull(marriage, "marriage may not be null");
		Objects.requireNonNull(conjugation, "conjugation may not be null");
		Objects.requireNonNull(judgment, "judgment may not be null");
		Objects.requireNonNull(wrath, "wrath may not be null");

		return emanate(marriage, conjugation).discern(judgment, wrath);
	}

	@Override
	public <R, U> Unfolding<U> braid(final Unfolding<R> consort,
									 final BiFunction<? super T, ? super R, ? extends U> weaver)
	{
		Objects.requireNonNull(consort, "consort may not be null");
		Objects.requireNonNull(weaver, "weaver may not be null");

		return consort.entwine(r -> Unfolding.beckon(weaver.apply(hero, r)));
	}

	@Override
	public Optional<T> optional()
	{
		return Optional.of(hero);
	}

	@Override
	public boolean sterile()
	{
		return false;
	}

	@Override
	public <R> R coronate(final Function<? super T, ? extends R> conclusion)
	{
		Objects.requireNonNull(conclusion, "conclusion may not be null");
		return conclusion.apply(hero);
	}

	@Override
	public T rescue(final T manifestation)
	{
		return hero;
	}

	@Override
	public Unfolding<T> revive(final Supplier<T> grace)
	{
		return this;
	}

	private Myth(final T hero)
	{
		this.hero = hero;
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
	public boolean supple()
	{
		return !sterile();
	}
}