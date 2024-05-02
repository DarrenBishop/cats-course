# Cats Type Class Hierarchy

```mermaid
flowchart BT
    S("`**Semigroup**
    _combine_`")
    MD("`**Monoid**
    _empty_`")
    MD --> S
    FO("`**Foldable**
    _foldLeft_
    _foldRight_`")
    T("`**Traverse**
    _traverse_
    _sequence_`")
    F("`**Functor**
    _map_`")
    T --> FO
    T --> F
    SL("`**Semigroupal**
    _product_`")
    AP("`**Apply**
    _ap_`")
    AP --> F
    AP --> SL
    FM("`**FlatMap**
    _flatMap_`")
    FM --> AP
    A("`**Applicative**
    _pure_`")
    A --> AP
    AE("`**ApplicativeError**
    _raiseError_
    _handleErrorWith_`")
    AE --> A
    M(Monad)
    M --> FM
    M --> A
    ME("`**MonadError**
    _ensure_`")
    ME ---> M
    ME --> AE
```
