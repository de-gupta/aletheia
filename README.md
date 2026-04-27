# Aletheia

A small functional library for Java centered on `Unfolding`, a richer `Optional` for transformation, judgment,
composition, and renewal.

Aletheia provides a compact functional algebra for code that lives at the threshold between manifestation and silence.
Its core type, `Unfolding<T>`, begins where `Optional<T>` begins, but travels further: it supports conditional
transformation, judgment, pairing, composition, renewal, and terminal conclusion without forcing consumers into a much
larger ecosystem. Alongside `Unfolding`, the library includes smaller companion abstractions such as `Crucible` and
`Loom` for collection state and folding.

## Description

`Unfolding<T>` is the heart of the library. It represents either a present value (`beckon`) or emptiness (`chaos`), then
offers fluent operators for transforming, discerning, branching, joining, reviving, and concluding that value. The goal
is not to mimic every functional library in Java, but to offer one coherent algebra for code shaped by presence and
absence.

The rest of the library serves that same style. `Crucible` models mutable and immutable collection state through `Forge`
and `Relic`, while `Loom` offers a small folding abstraction for weaving iterables into values or `Unfolding`s.

## Core Types

- `Unfolding<T>`: a rich maybe-like type for presence, absence, transformation, judgment, composition, and renewal
- `Crucible<E>`: a collection abstraction with mutable (`Forge`) and immutable (`Relic`) forms
- `Loom<E>`: a small folding interface for weaving iterables into a single result
- `Pair<A, B>`: a lightweight record for values that travel together

## Laws

These laws describe the intended behavior of the core algebra.

- Presence law: `Unfolding.beckon(x).supple()` is `true` for non-null `x`, and `Unfolding.chaos().sterile()` is `true`.
- Null law: `Unfolding.beckon(null)` is equivalent to `Unfolding.chaos()`.
- Identity law: `u.metamorphose(Function.identity())` preserves the same value as `u`.
- Empty preservation law: if an `Unfolding` is empty, then operations like `metamorphose`, `evolve`, `entwine`, and
  `interlace` keep it empty.
- Development law: `develop` preserves the original value when its predicate fails.
- Evolution law: `evolve` becomes empty when its predicate fails.
- Composition law: `entwine` is the operation that keeps transformations inside the `Unfolding` world.
- Recovery law: `rescue` and `revive` only matter when the value is absent; a present value remains untouched.

## Examples

### The flame becomes light

```java
var light = Unfolding.beckon("flame")
					 .develop("flame"::equals, _ -> "blaze")
					 .metamorphose(String::toUpperCase);

var result = light.summon(); // "BLAZE"
```

### The path is preserved, or it vanishes

```java
var preserved = Unfolding.beckon("echo")
						 .develop(s -> s.length() > 10, String::toUpperCase);

var vanished = Unfolding.beckon("echo")
						.evolve(s -> s.length() > 10, String::toUpperCase);

preserved.

summon(); // "echo"
vanished.

sterile(); // true
```

### Judgment divides the way

```java
var verdict = Unfolding.beckon(42)
					   .cleave(n -> n % 2 == 0,
							   n -> "even",
							   n -> "odd")
					   .summon();

// "even"
```

### Two threads are interlaced

```java
var woven = Unfolding.beckon("Ariadne")
					 .interlace(String::length)
					 .summon();

// Dyad[sinister=Ariadne, dexter=7]
```

### The empty vessel is revived

```java
var value = Unfolding.<String>chaos()
					 .revive(() -> "restored")
					 .summon();

// "restored"
```

### The journey concludes outside the unfolding

```java
var description = Unfolding.beckon(7)
						   .coronate(n -> "value=" + n);

// "value=7"
```

### The hero is judged by ordered signs

```java
var omens = new java.util.TreeMap<java.util.function.Predicate<? super String>,
		java.util.function.Function<? super String, String>>();

omens.

put(OrderedPredicate.of(0, s ->s.

startsWith("Odysseus")),_ ->"the wanderer returns");
		omens.

put(OrderedPredicate.of(1, s ->s.

startsWith("Achilles")),_ ->"the warrior enters glory");

var fate = Unfolding.beckon("Odysseus of Ithaca")
					.cleave(omens, "the gods remain silent");

// "the wanderer returns"
```

### The crucible tempers matter

```java
var forge = Crucible.kindle(java.util.List.of("a", "b"));
var grown = forge.embrace("c");

forge.

manifest(); // [a, b]
grown.

manifest(); // [a, b, c]
```

### The loom gathers scattered threads

```java
var loom = Loom.harness(java.util.List.of(1, 2, 3, 4));
var sum = loom.weave(0, Integer::sum);

// 10
```

## Why Choose Aletheia?

### Over `Optional`

Use Aletheia when `Optional` is too small for the shape of the journey you want to express.

- `Optional` gives you presence and absence with a minimal API.
- `Unfolding` adds conditional transformation, richer branching, composition, revival, and terminal conclusion in one
  fluent type.
- `Optional` stays closer to Java standard-library norms.
- `Unfolding` gives you a larger but more expressive algebra when presence and absence are central to your logic.

### Over Vavr

Use Aletheia when you want a focused mythos rather than a full functional arsenal.

- Vavr offers a broad ecosystem of functional types such as `Option`, `Either`, `Try`, collections, and pattern
  matching.
- Aletheia stays much narrower and centers on one rich maybe-like abstraction.
- Vavr is the better fit if you want a full alternative functional standard library.
- Aletheia is the better fit if you want one coherent presence/absence algebra without adopting a larger ecosystem.

### What Makes It Different?

Aletheia is not trying to replace every established abstraction. It is closer to a rich `Maybe` than to `Either` or
`Try`, because it models presence and absence rather than carrying a distinct error branch. Its value lies in how much
expressive force it grants to that single form.

## Is It Opinionated?

Only in a narrow sense: Aletheia assumes that presence and absence can stand at the center of a fairly rich algebra.
That is more specific than `Optional`, but it is still a standard and recognizable functional idea. The library does not
impose an alien metaphysics; it extends a familiar one.

## Java Version

Aletheia targets modern Java and is intended for consumers who are comfortable staying on the bleeding edge.

## Status

The library is small, focused, and still evolving. The center of gravity is `Unfolding`; the companion abstractions
exist to support the same functional style rather than to form a giant general-purpose toolkit.