# Aletheia

A small functional library for Java centered on `Unfolding`, a richer `Optional` for transformation, judgment,
composition, and renewal.

Aletheia provides a compact functional algebra for code that lives at the threshold between manifestation and silence.
Its core type, `Unfolding<T>`, begins where `Optional<T>` begins, but travels further: it supports conditional
transformation, judgment, pairing, composition, renewal, and terminal conclusion without forcing consumers into a much
larger ecosystem. Alongside `Unfolding`, the library includes companion abstractions: `Cascade<E>` for
collection-shaped presence, `Fallible<T>` for checked-exception handling, `Verdict<T>` for fluent multi-branch
judgment, product types `Dyad` and `Triad`, and `Crucible` and `Loom` for collection state and folding.

## Description

`Unfolding<T>` is the heart of the library. It represents either a present value (`beckon`) or emptiness (`chaos`), then
offers fluent operators for transforming, discerning, branching, joining, reviving, and concluding that value. The goal
is not to mimic every functional library in Java, but to offer one coherent algebra for code shaped by presence and
absence.

The rest of the library serves that same style. `Cascade<E>` brings the full presence/absence algebra to collections,
mirroring `Unfolding` for the many-valued case. `Fallible<T>` adds a try-like envelope for code that throws checked
exceptions, with ordered typed recovery through `Portent`. `Verdict<T>` and `VerdictArmed<T, R>` offer a fluent
multi-branch builder for conditional extraction without nesting or maps. `Dyad` and `Triad` are lightweight product
types that arise naturally from pairing and three-way splitting. `Crucible` models mutable and immutable collection
state through `Forge` and `Relic`, while `Loom` offers a small folding abstraction for weaving iterables into values
or `Unfolding`s.

## Core Types

- `Unfolding<T>`: a rich maybe-like type for presence, absence, transformation, judgment, composition, and renewal
- `Cascade<E>`: a collection-oriented companion to `Unfolding`, bringing the same algebra to sequences of values
- `Fallible<T>`: a try-like envelope for checked-exception-throwing operations, with ordered typed recovery
- `Crucible<E>`: a collection abstraction with mutable (`Forge`) and immutable (`Relic`) forms
- `Loom<E>`: a small folding interface for weaving iterables into a single result
- `Dyad<A, B>`: a lightweight record for two values that travel together
- `Triad<A, B, C>`: a lightweight record for three values that travel together
- `Verdict<T>` / `VerdictArmed<T, R>`: a fluent multi-branch builder for conditional extraction from an `Unfolding`
- `Ordeal<T, R>`: a checked-exception-aware functional interface, bridging ordinary functions and fallible operations

## Conventional API

Both `Unfolding` and `Cascade` expose standard `Optional`/`Stream`-style names alongside their mythic vocabulary. The
conventional names delegate to their canonical counterparts:

| Conventional                    | Mythic                          |
|---------------------------------|---------------------------------|
| `of` / `ofNullable` / `empty`   | `beckon` / `chaos`              |
| `fromOptional` / `fromStream`   | `augur` / `distill`             |
| `map`                           | `metamorphose`                  |
| `flatMap`                       | `entwine`                       |
| `flatMapOptional`               | `alchemize`                     |
| `filter`                        | `discern`                       |
| `orElse` / `orElseGet`          | `infuse`                        |
| `orElseThrow`                   | `decree`                        |
| `orElseRecover`                 | `resurrect`                     |
| `peek`                          | `unlace`                        |
| `get` / `isPresent` / `isEmpty` | `summon` / `supple` / `sterile` |

`Dyad` exposes `first`/`second`, `left`/`right`, `mapFirst`/`mapSecond`, and `mapLeft`/`mapRight` as conventional
aliases for its `sinister`/`dexter` components and `transformSinister`/`transformDexter` methods.

`Fallible` exposes `success`, `failure`, `map`, and `fold` as conventional aliases for `beckon`, `Fury.arise`,
`metamorphose`, and `coronate`.

`Ordeal` exposes `apply` as a conventional alias for `endure`.

## Laws

These laws describe the intended behavior of the core algebra.

### Unfolding

- Presence law: `Unfolding.beckon(x).supple()` is `true` for non-null `x`, and `Unfolding.chaos().sterile()` is `true`.
- Null law: `Unfolding.beckon(null)` is equivalent to `Unfolding.chaos()`.
- Identity law: `u.metamorphose(Function.identity())` preserves the same value as `u`.
- Empty preservation law: if an `Unfolding` is empty, then operations like `metamorphose`, `evolve`, `entwine`,
  `interlace`, and `ascend` keep it empty.
- Development law: `develop` preserves the original value when its predicate fails.
- Evolution law: `evolve` becomes empty when its predicate fails.
- Composition law: `entwine` is the operation that keeps transformations inside the `Unfolding` world.
- Recovery law: `revive` and `resurrect` only matter when the value is absent; a present value remains untouched.
- Reap law: `reap` returns its harvest regardless of whether the `Unfolding` is present or empty; chain exceptions
  still propagate before `reap` is reached.
- Trifurcate law: `trifurcate` routes through `diminished`, `balanced`, or `ascendant` according to the sign of the
  reckoning result (negative, zero, or positive).

### Cascade

- Presence law: `Cascade.beckon(elements).supple()` is `true` for a non-null collection;
  `Cascade.abyss().sterile()` is `true`.
- Empty preservation law: `metamorphose`, `evolve`, `entwine`, and `alchemize` on an absent `Cascade` remain absent.
- Recovery law: `resurrect` and `revive` only matter when the `Cascade` is absent; a present cascade remains
  untouched.

### Fallible

- Success law: `Fallible.beckon(x).coronate(f, g)` returns `f.apply(x)`.
- Failure law: `Fallible.failure(e).coronate(f, g)` returns `g.apply(e)`.
- Recovery law: when `metamorphose` catches an exception, the first matching `Portent` returns a recovered success;
  unmatched exceptions remain as a `Fury`.
- Propagation law: `metamorphose` on a `Fury` is a no-op; the failure propagates unchanged.

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

preserved.summon();  // "echo"
vanished.sterile();  // true
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

### The instrument is forged before the crossroads

When an intermediate value must inform branching without losing the original, `wield` derives it first and
hands the original `Unfolding` directly into the handler — no re-wrapping, no allocation.

```java
var result = Unfolding.beckon(680)
                      .wield(
                              score -> score - 750,
                              (self, gap) -> self.cleave(
                                      _ -> gap >= 0,
                                      s -> s,
                                      s -> s + (-gap / 2)
                              )
                      )
                      .summon();

// 715 — partial recovery applied because 680 is below the threshold of 750
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

When the `Unfolding` may be empty, a refuge prevents the exception:

```java
var safe = Unfolding.<Integer>chaos()
                    .coronate(n -> "value=" + n, "absent");

// "absent"
```

### The hero is judged by ordered signs

```java
var omens = new java.util.TreeMap<java.util.function.Predicate<? super String>,
        java.util.function.Function<? super String, String>>();

omens.put(OrderedPredicate.of(0, s -> s.startsWith("Odysseus")), _ -> "the wanderer returns");
omens.put(OrderedPredicate.of(1, s -> s.startsWith("Achilles")), _ -> "the warrior enters glory");

var fate = Unfolding.beckon("Odysseus of Ithaca")
                    .cleave(omens, "the gods remain silent");

// "the wanderer returns"
```

### Three signs divide the path

`trifurcate` routes through three branches by the sign of a comparison result: negative, zero, or positive. The
function-returning overload wraps the result in a new `Unfolding`:

```java
var status = Unfolding.beckon(42)
                      .trifurcate(
                              n -> n.compareTo(100),
                              _ -> "below threshold",
                              _ -> "at threshold",
                              _ -> "above threshold"
                      )
                      .summon();

// "below threshold"
```

The supplier overload produces a terminal result without a new `Unfolding`:

```java
String label = Unfolding.beckon(100)
                        .trifurcate(
                                n -> n.compareTo(100),
                                () -> "below",
                                () -> "at",
                                () -> "above"
                        );

// "at"
```

### The apparition is crowned or silenced by condition

`adjudicate` creates a present `Unfolding` only when its condition holds:

```java
var adminView = Unfolding.adjudicate(user, user.hasRole("admin"))
                         .metamorphose(User::getAdminPanel);

// present if user is admin, chaos otherwise
```

### The value climbs through repeated trials

`ascend` applies a unary operator a fixed number of times:

```java
var result = Unfolding.beckon(1)
                      .ascend(n -> n * 2, 4)
                      .summon();

// 16 — doubled four times
```

### The pairing emerges from within

`emanate` derives a companion from the value itself and combines the pair — a null companion collapses to empty:

```java
var result = Unfolding.beckon("Athens")
                      .emanate(
                              city -> city.length() > 3 ? city.toUpperCase() : null,
                              (original, upper) -> original + " → " + upper
                      )
                      .summon();

// "Athens → ATHENS"
```

### Two unfoldings meet in the weaving

`braid` combines two independent `Unfolding`s — the result is empty if either is:

```java
var left  = Unfolding.beckon("Hermes");
var right = Unfolding.beckon(7);

var result = left.braid(right, (name, n) -> name + " carried the staff " + n + " times")
                 .summon();

// "Hermes carried the staff 7 times"
```

### The oracles are summoned as one

`convoke` applies a sequence of functions to the value, collects the results, then hands them to a final oracle:

```java
var summary = Unfolding.beckon("hello world")
                       .convoke(
                               java.util.List.of(
                                       String::length,
                                       s -> (int) s.chars().filter(c -> c == ' ').count()
                               ),
                               results -> "chars=" + results.getFirst() + ",spaces=" + results.getLast()
                       )
                       .summon();

// "chars=11,spaces=1"
```

### The harvest is claimed from any state

`reap` returns a constant value at the end of a fluent chain — whether the `Unfolding` is present or empty. Chain
exceptions still propagate before `reap` is reached:

```java
var constant = Unfolding.beckon("hi")
                        .discern(s -> s.length() > 10)  // empties the Unfolding
                        .reap("always-this");

// "always-this" — reap returns the harvest even though discern emptied the chain
```

### The judgment is built in layers

`verdict()` opens a fluent multi-branch builder. Branches are tested in declaration order; the first match wins.
`fulminate()` requires exhaustiveness and throws if no branch matches. `infuse` provides a fallback. `pronounce()`
returns the result as a new `Unfolding`:

```java
var label = Unfolding.beckon(-3)
                     .verdict()
                     .when(n -> n < 0, n -> "negative: " + n)
                     .when(n -> n == 0, _ -> "zero")
                     .when(n -> n > 0, n -> "positive: " + n)
                     .fulminate();

// "negative: -3"
```

`infuse` supplies a fallback when no branch matches:

```java
var label = Unfolding.beckon(0)
                     .verdict()
                     .when(n -> n < 0, _ -> "negative")
                     .when(n -> n > 0, _ -> "positive")
                     .infuse("zero");

// "zero"
```

`pronounce` returns the result wrapped in an `Unfolding`, making the chain composable:

```java
var doubled = Unfolding.beckon(7)
                       .verdict()
                       .when(n -> n > 0, n -> n * 2)
                       .pronounce()
                       .metamorphose(n -> "doubled: " + n)
                       .summon();

// "doubled: 14"
```

### Two values travel as one

`Dyad` is a lightweight two-element product type. Its record components are `sinister` (left) and `dexter` (right):

```java
var dyad = Dyad.of("Castor", "Pollux");

dyad.sinister(); // "Castor"
dyad.dexter();   // "Pollux"

var upper = dyad.transformSinister(String::toUpperCase);
// Dyad[sinister=CASTOR, dexter=Pollux]
```

Conventional aliases `first`/`second` and `mapFirst`/`mapSecond` are also available:

```java
dyad.first();                                     // "Castor"
dyad.mapSecond(String::toUpperCase).second();     // "POLLUX"
```

`Unfolding.interlace` and `Cascade.interlace` produce `Dyad` values naturally:

```java
var pair = Unfolding.beckon("Ariadne")
                    .interlace(String::length)
                    .summon();

// Dyad[sinister=Ariadne, dexter=7]
```

### Three values travel as one

`Triad` is a lightweight three-element product type. Its record components are `dawn` (first), `zenith` (second), and
`dusk` (third):

```java
var triad = Triad.of("Apollo", "Artemis", "Athena");

triad.dawn();   // "Apollo"
triad.zenith(); // "Artemis"
triad.dusk();   // "Athena"
```

`Triad` can be assembled from a `Dyad` and a third element, or from two elements and a `Dyad`:

```java
var dyad  = Dyad.of("Apollo", "Artemis");
var triad = Triad.of(dyad, "Athena");

// Triad[dawn=Apollo, zenith=Artemis, dusk=Athena]
```

Conventional aliases `first`, `second`, and `third` are available:

```java
triad.first();  // "Apollo"
triad.second(); // "Artemis"
triad.third();  // "Athena"
```

### The crucible tempers matter

```java
var forge = Crucible.kindle(java.util.List.of("a", "b"));
var grown = forge.embrace("c");

forge.manifest(); // [a, b]
grown.manifest(); // [a, b, c]
```

### The loom gathers scattered threads

```java
var loom = Loom.harness(java.util.List.of(1, 2, 3, 4));
var sum = loom.weave(0, Integer::sum);

// 10
```

---

### The cascade holds many, or it is silent

`Cascade<E>` is a collection-level companion to `Unfolding`. It is either present (`Brook`) with a non-null
collection, or absent (`Nadir`). Its algebra mirrors `Unfolding` but operates across all elements:

```java
var cascade = Cascade.of("Orpheus", "Eurydice", "Hermes");

cascade.supple();  // true
cascade.summon();  // [Orpheus, Eurydice, Hermes]

var absent = Cascade.empty();
absent.sterile();  // true
```

#### The cascade is transformed element by element

```java
var lengths = Cascade.of("Apollo", "Artemis", "Ares")
                     .metamorphose(String::length)
                     .summon();

// [6, 7, 4]
```

#### Elements that fail the test are removed

```java
var filtered = Cascade.of("Apollo", "Io", "Ares")
                      .discern(s -> s.length() > 3)
                      .summon();

// [Apollo, Ares]
```

#### The cascade is flattened from within

```java
var words = Cascade.of("hello world", "foo bar")
                   .entwine(s -> Cascade.of(s.split(" ")))
                   .summon();

// [hello, world, foo, bar]
```

#### The cascade is folded into a single value

```java
var sum = Cascade.of(1, 2, 3, 4)
                 .weave(0, Integer::sum);

// 10
```

#### A reduction finds the last word

```java
var longest = Cascade.of("Io", "Hermes", "Persephone", "Ares")
                     .smelt((a, b) -> a.length() >= b.length() ? a : b)
                     .summon();

// "Persephone"
```

#### The cascade is sorted and deduplicated

`ordain()` uses natural ordering and requires elements to implement `Comparable`:

```java
var sorted = Cascade.of("Ares", "Apollo", "Athena", "Apollo")
                    .ordain()
                    .purify()
                    .summon();

// [Apollo, Ares, Athena]
```

#### Each element is paired with a companion

```java
var pairs = Cascade.of("Apollo", "Hermes")
                   .interlace(String::length)
                   .summon();

// [Dyad[sinister=Apollo, dexter=6], Dyad[sinister=Hermes, dexter=6]]
```

#### The absent cascade is revived

`resurrect` replaces an absent `Cascade` with a new one; a present cascade passes through unchanged:

```java
var revived = Cascade.<String>empty()
                     .resurrect(() -> Cascade.of("default"))
                     .summon();

// [default]
```

#### The cascade is enshrined or awakened

`enshrine()` produces an immutable `Relic`; `awaken()` produces a mutable `Forge`:

```java
Relic<String> relic = Cascade.of("a", "b").enshrine();
Forge<String> forge = Cascade.of("a", "b").awaken();
```

---

### The ordeal is endured, or it becomes fury

`Fallible<T>` represents either a successful result (`Triumph`) or a captured exception (`Fury`). `coronate`
(conventional: `fold`) maps both branches to a common type:

```java
var result = Fallible.beckon("7")
                     .metamorphose(Integer::parseInt, java.util.List.of())
                     .coronate(
                             n  -> "parsed: " + n,
                             ex -> "failed: " + ex.getMessage()
                     );

// "parsed: 7"
```

#### The portent redeems a fury

`Portent` maps a specific exception type to a recovery value. The first matching `Portent` in the list redeems the
exception; unmatched exceptions remain as a `Fury`. Declaration order determines priority:

```java
var result = Fallible.beckon("not-a-number")
                     .metamorphose(
                             Integer::parseInt,
                             java.util.List.of(
                                     Portent.foretell(NumberFormatException.class, _ -> -1)
                             )
                     )
                     .coronate(
                             n  -> "parsed: " + n,
                             ex -> "failed: " + ex.getMessage()
                     );

// "parsed: -1" — NumberFormatException was redeemed by the portent
```

#### The ordeal wraps a checked function

`Ordeal<T, R>` is a `Function`-like interface whose `endure` method may throw any checked exception. Implement it
directly for checked calls, or adapt an ordinary function with `Ordeal.of`:

```java
Ordeal<String, byte[]> risky = s -> java.nio.file.Files.readAllBytes(java.nio.file.Path.of(s));

var result = Fallible.beckon("/etc/hostname")
                     .metamorphose(
                             risky,
                             java.util.List.of(
                                     Portent.foretell(java.io.IOException.class, _ -> new byte[0])
                             )
                     )
                     .coronate(
                             bytes -> "read " + bytes.length + " bytes",
                             ex    -> "error: " + ex.getMessage()
                     );
```

#### The triumph is folded to a single outcome

A fury that has no matching portent propagates unchanged. `coronate` handles both branches at the end of the chain:

```java
var message = Fallible.beckon("oops")
                      .metamorphose(
                              s -> { throw new IllegalStateException("forbidden"); },
                              java.util.List.of()
                      )
                      .coronate(
                              value -> "ok: " + value,
                              ex    -> "error: " + ex.getMessage()
                      );

// "error: forbidden"
```

---

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
`Try`, because it models presence and absence rather than carrying a distinct error branch. `Fallible` extends the
library into the checked-exception world, but it is a deliberate addition to the core algebra rather than the center
of it. Its value lies in how much expressive force it grants to the single form of presence and absence.

## Is It Opinionated?

Only in a narrow sense: Aletheia assumes that presence and absence can stand at the center of a fairly rich algebra.
That is more specific than `Optional`, but it is still a standard and recognizable functional idea. The library does not
impose an alien metaphysics; it extends a familiar one.

## Java Version

Aletheia targets modern Java and is intended for consumers who are comfortable staying on the bleeding edge.

## Status

The library is small, focused, and still evolving. The center of gravity is `Unfolding`; the companion abstractions
exist to support the same functional style rather than to form a giant general-purpose toolkit.