# Prep

## What we know so far
 - Scala implicits, TC (type class) pattern, extension methods
 - Cats type classes

<div style="width:100%">
<span style="width:auto; float:left">

## Major type classes
 - `Semigroup`
   - Methods: `combine`
   - Syntax: `|+|`
 - `Monoid` extends `Semigroup`
   - Methods: `empty`
 - `Functor`
   - Methods: `map`
 - `Monad` extends `Functor`
   - Methods: `pure`, `flatMap`
   - Syntax: `for-comprehension`, `>>`, `*>`, `<<`, `<*`

</span>

<span style="width:60%; float:right">

```mermaid
flowchart BT
    S(Semigroup)
    M(Monoid)
    M --> S
    F(Functor)
    O(Monad)
    O --> F
```
</span>
</div>
