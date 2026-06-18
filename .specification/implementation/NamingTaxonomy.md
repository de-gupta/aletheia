# Unfolding Naming Taxonomy

This document records the current naming rationale for `Unfolding` and sets rules for future extension.

The main conclusion is that the API should be understood primarily through the **semantic operation** being performed,
not through the return type alone. Some methods have overloads that stay inside `Unfolding`, and others that resolve to
a final result. That is acceptable when the root verb still names one stable conceptual act and the overload shape
clearly communicates how that act materializes.

## Core position

`Unfolding` is a rich fluent API. It is not trying to encode every distinction directly into method names. Instead:

- the **root verb** names the operation being performed
- the **parameter shape** communicates whether the result depends on the current `T`
- the **return type** communicates whether the operation continues in the pipeline or resolves it

This means we do **not** need separate method names for every materialization mode. Overloads are acceptable when they
remain faithful to the same semantic act.

## Primary naming axis: semantic operation

The first question is always:

> What is the caller doing conceptually?

The current main families are:

- **Presence and extraction**
    - `summon`, `decree`, `infuse`, `reap`, `coronate`

- **Refinement and prohibition**
    - `discern`, `interdict`

- **Transformation**
    - `metamorphose`, `alchemize`, `entwine`, `develop`, `evolve`, `ascend`

- **Branching and adjudication**
    - `cleave`, `trifurcate`, `verdict`

- **Composition and pairing**
    - `interlace`, `conjoin`, `emanate`, `wield`, `braid`, `convoke`, `sanctify`

- **Recovery**
    - `resurrect`, `revive`

- **Observation**
    - `unlace`

Names should primarily preserve these semantic families.

## Secondary axis: dependence on the current value

The next distinction is not raw value vs functional value. The real distinction is:

- **dependent on the current `T`**
    - `Function<? super T, ...>`
    - `BiFunction<? super T, ...>`
    - similar shapes that derive their result from the present value

- **independent final outcome**
    - `R`
    - `Supplier<R>`
    - similar shapes that do not transform the current `T`, but merely provide the chosen outcome

This distinction explains why some overload families naturally support both fluent and terminal forms.

Examples:

- `cleave(pred, fn, fn)` means the branch outcome is derived from the current `T`
- `cleave(pred, reward, punishment)` means the branch selects between already-known outcomes
- `trifurcate` follows the same logic

Under this model, `R` and `Supplier<R>` are semantically close: both represent a final outcome that does not depend on
transforming `T`.

## Tertiary axis: materialization mode

After semantic operation and value dependence, the remaining question is how the operation materializes:

- continue as `Unfolding<...>`
- resolve to a terminal value
- terminate through effect or interruption

This matters, but it is **not** the primary driver of naming in this API.

The library favors semantic roots such as `cleave` and `trifurcate` over a proliferation of separate names for:

- fluent branch-transform
- eager terminal branch-result
- lazy terminal branch-result

As long as the overload shape is clear, this is an intentional design choice.

## Why `discern` and `interdict` are acceptable

At first glance, `discern` and `interdict` seem to blur categories. On closer inspection, both preserve a stable
semantic act.

### `discern`

`discern` means selective admission or recognition.

- `discern(pred)` filters by the condition
- `discern(pred, wrath)` requires the condition and throws when it fails

These differ in failure policy, but they still express the same core act: deciding whether the current value is fit to
proceed.

### `interdict`

`interdict` means prohibition.

- terminal `interdict(...)` forbids continuation by throwing
- predicate `interdict(pred, wrath)` forbids a matching state and otherwise leaves the value alone

Again, the policy varies, but the semantic center remains stable: a forbidden condition or presence is being
interdicted.

Therefore these methods are **taxonomically broad but semantically coherent**, which is acceptable for this API.

## Why `cleave` and `trifurcate` are acceptable

An earlier concern was that `cleave` and `trifurcate` mix fluent and terminal overloads. The refined position is that
this is acceptable because the root verb names the branching act itself.

### `cleave`

`cleave` means binary branching.

- with `Function<T, R>` arms, the branch derives a transformed result from `T`
- with `R` or `Supplier<R>` arms, the branch selects between already-known outcomes

The semantic act is still branching. The overload shape tells the caller whether the branch is producing a continued
transformed path or selecting a terminal result.

### `trifurcate`

`trifurcate` follows the same principle for three-way branching.

In both cases, operation semantics are more important than forcing separate names for each result shape.

## `coronate` and `reap`

`coronate` and `reap` are close, but they are not redundant.

### `coronate`

`coronate` is terminal, value-sensitive resolution.

It says: take the present `T`, if present, and give it its final crowned result.

Examples:

- `coronate(fn)`
- `coronate(fn, refuge)`
- `coronate(pred, reward, punishment)`

Even when it branches or offers fallback, the center of gravity is still the contained value being resolved into its
final form.

### `reap`

`reap` is terminal harvest.

It says: conclude this unfolding and yield the supplied harvest. The result is not conceptually a transformation of `T`.

Therefore:

- `coronate` cares about the contained value
- `reap` cares about the terminal yield

Both earn their keep.

## `sanctify`

`sanctify` remains part of the surface for now.

The method signature is semantically legible:

- derive `U`
- combine `T` and `U` into `R`
- judge the result
- preserve it in `Unfolding` or throw

Future changes should still be cautious about introducing new composed primitives, but `sanctify` is currently treated
as an intentional named operation rather than a naming defect to be removed.

## Naming laws for future extensions

Future additions to `Unfolding` should follow these rules.

### 1. Name the semantic act first

A root verb should answer:

> What operation is the caller performing?

Not:

> What return type happens to come back?

### 2. Permit overloads that vary policy

A root may reasonably support nearby variations such as:

- empty vs throw
- eager constant vs lazy constant
- unconditional vs predicate-gated

These do not require a new root name when the underlying act is stable.

### 3. Do not multiply names solely for materialization shape

If the same semantic act can produce:

- a fluent transformed result
- a terminal selected result
- a lazily supplied terminal result

then overloads are preferred over inventing separate verbs, as long as the signatures remain readable.

### 4. Distinguish dependence on `T` from independent outcomes

When extending branching or resolution methods, prefer this mental model:

- `Function<T, ...>` means outcome depends on the current value
- `R` or `Supplier<R>` means outcome is already known and only selection remains

This is a more useful distinction than "functional vs non-functional".

### 5. Preserve a stable semantic center

A new overload is acceptable only if a caller who knows the root verb would still say:

> Yes, this is still that same act, just with a different policy or outcome form.

If the overload feels like a different operation entirely, it deserves a new root.

### 6. Avoid names that are mere implementation summaries

A method should not be named only because it can be described as:

- "map, then filter"
- "flatMap, then throw"
- "join, then branch"

Composed operations need a stronger semantic identity than their implementation recipe.

### 7. Favor readability of call sites over taxonomic perfection

`Unfolding` is intended to read fluently. A technically purer taxonomy that makes call sites heavier, more repetitive,
or more fragmented is not automatically better.

The API should remain teachable, but also expressive.

## Practical checklist for new methods

Before adding a new method or overload, ask:

1. What is the semantic operation?
2. Does an existing root already express that act?
3. Is this a true new act, or only a policy variation?
4. Does the outcome depend on `T`, or is it an already-known result?
5. Would the call site remain readable without adding a new root?
6. Is this a stable primitive, or just a convenient composition?

If the answer points to an existing semantic family, prefer an overload there.

## Present conclusion

The current `Unfolding` naming is best understood as **operation-semantics first**.

That means:

- `discern` and `interdict` are acceptable broad families
- `cleave` and `trifurcate` are acceptable branching families with multiple materialization modes
- `coronate` and `reap` serve different terminal roles
- overload shape is sufficient to communicate whether a branch transforms the current value or selects an independent
  outcome

Future work should focus less on forcing perfect taxonomic separation and more on preserving strong semantic roots,
readable signatures, and fluent call-site clarity.