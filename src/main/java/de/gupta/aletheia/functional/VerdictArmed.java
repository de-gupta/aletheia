package de.gupta.aletheia.functional;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface VerdictArmed<T, R>
{
	VerdictArmed<T, R> when(Predicate<? super T> judgement, Function<? super T, ? extends R> reward);

	VerdictArmed<T, R> when(Predicate<? super T> judgement, Supplier<? extends R> reward);

	VerdictArmed<T, R> when(Predicate<? super T> judgement, R reward);

	R infuse(R manifestation);

	R infuse(Supplier<? extends R> revelation);

	R infuse(Function<? super T, ? extends R> revelation);

	R smite(Supplier<? extends RuntimeException> wrath);

	R fulminate();

	Unfolding<R> pronounce();
}
