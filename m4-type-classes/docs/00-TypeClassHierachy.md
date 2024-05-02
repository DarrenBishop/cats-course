# Cats Type Class Hierarchy

```mermaid
flowchart BT
    MO(Monoid) --> S(Semigroup)
    A(Applicative) --> Ap(Apply)
    Ap --> F(Functor)
    Ap --> SL(Semigroupal)
    MA(Monad) --> A
```
