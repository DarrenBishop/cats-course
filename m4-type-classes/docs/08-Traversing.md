# Traverse

## Higher-kinded type class extending `Foldable` and `Functor`; provides _nested context inversion_ methods:
 - a `traverse` method to apply a function producing a contextual value, to a value from a different context
    ```scala mdoc
    def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
    ```
    e.g.
    ```scala mdoc
    import cats.Traverse
    import cats.instances.list._
    
    val listTraverse = Traverse[List] // fetches the implicit instance
    val aList = List(1, 2, 3, 4, 5)
    val anOptionOfList = listTraverse.traverse(aList) { x => Option(x) }
    ```
 - a `sequence` method to invert the context of a nested contextual value
     ```scala mdoc
     def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]]=
       traverse(fga)(identity)
     ```
     e.g.
     ```scala mdoc
     implicit val ec: EC()
     val aListOfFutures = aList.map(Future(_))
     val aFutureOfList = listTraverse.sequence(aListOfFutures)
     ```
 