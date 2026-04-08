package de.gupta.aletheia.functional;

import de.gupta.aletheia.collection.Pair;

import java.util.*;
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
	public void interdict(final Function<? super T, Supplier<? extends RuntimeException>> wrath)
	{
		throw wrath.apply(hero).get();
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
	public <R> R cleave(final SequencedMap<Predicate<? super T>, Function<? super T, R>> judgments, final R punishment)
	{
		Objects.requireNonNull(judgments, "judgments may not be null");
		Objects.requireNonNull(punishment, "punishment may not be null");

		judgments.forEach((judgement, reward) ->
		{
			Objects.requireNonNull(judgement, "judgement may not be null");
			Objects.requireNonNull(reward, "reward may not be null");
		});

		return judgments.sequencedEntrySet()
						.stream()
						.filter(entry -> entry.getKey().test(hero))
						.findFirst()
						.map(entry -> entry.getValue().apply(hero))
						.orElse(punishment);
	}

	@Override
	public <R> R smite(final Map<Predicate<? super T>, Function<? super T, R>> judgments,
					   final Supplier<? extends RuntimeException> wrath)
	{
		Objects.requireNonNull(judgments, "judgments may not be null");
		Objects.requireNonNull(wrath, "wrath may not be null");

		judgments.forEach((judgement, reward) ->
		{
			Objects.requireNonNull(judgement, "judgement may not be null");
			Objects.requireNonNull(reward, "reward may not be null");
		});

		return judgments.entrySet()
						.stream()
						.filter(entry -> entry.getKey().test(hero))
						.findFirst()
						.map(entry -> entry.getValue().apply(hero))
						.orElseThrow(wrath);
	}

	@Override
	public <R> R coronate(final Function<? super T, ? extends R> proclamation)
	{
		Objects.requireNonNull(proclamation, "conclusion may not be null");
		return proclamation.apply(hero);
	}

	@Override
	public Unfolding<T> develop(final Predicate<? super T> judgement,
								final Function<? super T, ? extends T> development)
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
	public <U, R> Unfolding<Pair<U, R>> metamorphose(final Function<? super T, ? extends U> fate,
													 final Function<? super T, ? extends R> destiny)
	{
		Objects.requireNonNull(fate, "fate may not be null");
		Objects.requireNonNull(destiny, "destiny may not be null");
		return Unfolding.beckon(Pair.of(fate.apply(hero), destiny.apply(hero)));
	}

	@Override
	public <R> Unfolding<R> metamorphose(final Function<? super T, ? extends R> metamorphosis,
										 final Supplier<? extends RuntimeException> wrath)
	{
		Objects.requireNonNull(metamorphosis, "metamorphosis may not be null");
		Objects.requireNonNull(wrath, "wrath may not be null");

		try
		{
			return Unfolding.beckon(metamorphosis.apply(hero));
		}
		catch (RuntimeException e)
		{
			RuntimeException wrapped = Objects.requireNonNull(wrath.get(), "wrath may not return null");
			if (wrapped != e && wrapped.getCause() == null)
			{
				wrapped.initCause(e);
			}
			throw wrapped;
		}
	}

	@Override
	public <R> Unfolding<R> alchemize(final Function<? super T, Optional<? extends R>> potion)
	{
		Objects.requireNonNull(potion, "potion may not be null");

		var transmutation = potion.apply(hero);

		return transmutation.isPresent() ? Unfolding.beckon(transmutation.get()) : Unfolding.chaos();
	}

	@Override
	public <R> Unfolding<R> evolve(final Predicate<? super T> judgement,
								   final Function<? super T, ? extends R> evolution)
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
										final Predicate<? super R> judgement,
										final Supplier<? extends RuntimeException> wrath)
	{
		Objects.requireNonNull(marriage, "marriage may not be null");
		Objects.requireNonNull(conjugation, "conjugation may not be null");
		Objects.requireNonNull(judgement, "judgment may not be null");
		Objects.requireNonNull(wrath, "wrath may not be null");

		return emanate(marriage, conjugation).discern(judgement, wrath);
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
	public <R, A> Unfolding<A> convoke(final SequencedCollection<Function<? super T, ? extends R>> omens,
	                                   final Function<? super SequencedCollection<? extends R>, ? extends A> oracle)
	{
		Objects.requireNonNull(omens, "omens may not be null");
		Objects.requireNonNull(oracle, "oracle may not be null");
		omens.forEach(omen -> Objects.requireNonNull(omen, "omen may not be null"));

		return Unfolding.beckon(oracle.apply(omens.stream()
		                                          .map(omen -> omen.apply(hero))
		                                          .toList()));
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
	public T ordain(final Supplier<? extends T> revelation)
	{
		Objects.requireNonNull(revelation, "revelation may not be null");
		return hero;
	}

	@Override
	public <R> R coronate(final Predicate<? super T> judgement, final Function<? super T, ? extends R> reward,
						  final Function<? super T, ? extends R> punishment)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(reward, "reward may not be null");
		Objects.requireNonNull(punishment, "punishment may not be null");

		return judgement.test(hero) ? reward.apply(hero) : punishment.apply(hero);
	}

	@Override
	public T ordain(final T manifestation)
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
	public Unfolding<T> unlace(final Predicate<? super T> judgement, final Consumer<? super T> impregnator)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(impregnator, "impregnator may not be null");

		if (judgement.test(hero))
		{
			impregnator.accept(hero);
		}
		return this;
	}

	@Override
	public boolean supple()
	{
		return !sterile();
	}

	@Override
	public Unfolding<T> ascend(final UnaryOperator<T> ascension, final int levels)
	{
		return metamorphose(value ->
		{
			T current = value;
			for (int i = 0; i < levels; i++)
			{
				current = ascension.apply(current);
			}
			return current;
		});
	}
}