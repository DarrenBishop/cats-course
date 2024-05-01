# Functors

## Higher-kinded type class that provides:
 - a `map` method to transform values in sequence
   ```scala mdoc
   import cats.Functor
   //import cats.instances.list._
   
   val listFunctor = Functor[List] // fetches the implicit instance
   val incrNumbers = listFunctor.map(List(1, 2, 3))(_ + 1) // fundamental method: map
   ```
 - a `map` extension method
   ```scala mdoc
   import cats.syntax.functor._ // adds the map extension method
   import absmath.Functors.{Tree, Branch, Leaf}
   val tree: Tree[Int] = Branch(40, Branch(5, Leaf(10), Leaf(30)), Leaf(20))
   implicit val treeFunctor: Functor[Tree] = absmath.Functors.TreeFunctor // provide an implicit instance
   val processedTree = tree.map(_ * 2) // returns Branch(80, Branch(10, Leaf(20), Leaf(60)), Leaf(40))
   ```

## Useful for general API
```scala mdoc
def do10x[F[_]](fi: F[Int])(implicit functor: Functor[F]): F[Int] = functor.map(fi)(_ * 10)
```
or
```scala mdoc
import cats.syntax.functor._ // adds the map extension method
def do10xShort[F[_]: Functor](fi: F[Int]): F[Int] = fi.map(_ * 10)
```

## Use cases: data structures meant to be transformed in sequence
 - specialized data structures for high-performance algorithms
 - any "mappable" structures under the same high-level API (see `Tree` above)
