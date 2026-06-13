package de.gupta.aletheia.functional;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Verdict<T>
{
	<R> VerdictArmed<T, R> when(Predicate<? super T> judgement, Function<? super T, ? extends R> reward);

	<R> VerdictArmed<T, R> when(Predicate<? super T> judgement, Supplier<? extends R> reward);

	<R> VerdictArmed<T, R> when(Predicate<? super T> judgement, R reward);
}