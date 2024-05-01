# Cats Type Class Hierarchy

```mermaid
flowchart BT
    MO(Monoid) --> S(Semigroup)
    A(Applicative)
    A --> F(Functor)
    A --> SL(Semigroupal)
    MA(Monad) --> A
```
