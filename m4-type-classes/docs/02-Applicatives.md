# Applicatives

## Higher-kinded type class that extends `Functor` and provides a `pure` method
 - a `pure` method to wrap (lift) a normal value into a contextual value
   ```scala mdoc
   import cats.Applicative
   //import cats.instances.list._ // implicit Applicative[List]
   val listApplicative = Applicative[List]
   val aList = listApplicative.pure(2)
   ```

 - a `pure` extension method
   ```scala mdoc
   import cats.syntax.applicative._ // adds the pure extension method
   val anotherList = 2.pure[List] // same as listApplicative.pure(2)
   ```
   
## Not quite Monads
e.g. `Validated`
