package de.gupta.aletheia.functional;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class PresentVerdictArmed<T, R> implements VerdictArmed<T, R>
{
	private final T hero;
	private final SequencedCollection<Augury<T, R>> auguries = new ArrayList<>();

	@Override
	public VerdictArmed<T, R> when(final Predicate<? super T> judgement,
	                               final Function<? super T, ? extends R> reward)
	{
		Objects.requireNonNull(judgement, "judgement may not be null");
		Objects.requireNonNull(reward, "reward may not be null");
		auguries.add(new Augury<>(judgement, reward));
		return this;
	}

	@Override
	public VerdictArmed<T, R> when(final Predicate<? super T> judgement, final Supplier<? extends R> reward)
	{
		Objects.requireNonNull(reward, "reward may not be null");
		return when(judgement, _ -> reward.get());
	}

	@Override
	public VerdictArmed<T, R> when(final Predicate<? super T> judgement, final R reward)
	{
		Objects.requireNonNull(reward, "reward may not be null");
		return when(judgement, _ -> reward);
	}

	@Override
	public R infuse(final R manifestation)
	{
		Objects.requireNonNull(manifestation, "manifestation may not be null");
		return evaluate().orElse(manifestation);
	}

	@Override
	public R infuse(final Supplier<? extends R> revelation)
	{
		Objects.requireNonNull(revelation, "revelation may not be null");
		return evaluate().orElseGet(revelation);
	}

	@Override
	public R infuse(final Function<? super T, ? extends R> revelation)
	{
		Objects.requireNonNull(revelation, "revelation may not be null");
		return evaluate().orElseGet(() -> revelation.apply(hero));
	}

	@Override
	public R smite(final Supplier<? extends RuntimeException> wrath)
	{
		Objects.requireNonNull(wrath, "wrath may not be null");
		return evaluate().orElseThrow(wrath);
	}

	@Override
	public R fulminate()
	{
		return evaluate().orElseThrow(
				() -> EmptyUnfoldingException.withMessage("No judgment was met; the map must be exhaustive"));
	}

	@Override
	public Unfolding<R> pronounce()
	{
		return evaluate().map(Unfolding::beckon).orElseGet(Unfolding::chaos);
	}

	private Optional<R> evaluate()
	{
		return auguries.stream()
		               .filter(augury -> augury.judgement().test(hero))
		               .findFirst()
		               .map(augury -> augury.reward().apply(hero));
	}

	PresentVerdictArmed(final T hero)
	{
		this.hero = hero;
	}

	private record Augury<T, R>(Predicate<? super T> judgement, Function<? super T, ? extends R> reward)
	{
	}
}