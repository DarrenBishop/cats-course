# Cats Type Class Hierarchy

```mermaid
flowchart BT
    S(Semigroup)
    MO(Monoid)
    MO --> S
    F(Functor)
    SL(Semigroupal)
    MA(Monad)
    MA --> F
    MA --> SL
```
