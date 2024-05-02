package typeclasses

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

package object ec {

  case class Context private[ec](private[ec] val hook: Context.ShutdownHook)(implicit val execution: ExecutionContext)

  object Context {
    type ShutdownHook = () => Unit
    private val hooksM = scala.collection.concurrent.TrieMap.empty[String, ShutdownHook]

    def shutdownAll(): Unit = hooksM.keys.foreach { k =>
      hooksM.updateWith(k)(_.flatMap { h => h(); None })
    }

    def apply(threadPoolSize: Int= 4): Context = {
      val executorService: ExecutorService = Executors.newFixedThreadPool(threadPoolSize)
      val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)
      val hook: ShutdownHook = () => executorService.shutdown()
      hooksM.addOne(executorService.toString, hook)
      sys.addShutdownHook(hook())
      Context(hook)(ec)
    }
  }

  trait Types {
    type EC = Context
    val EC: Context.type = Context

    type Future[+A] = scala.concurrent.Future[A]
    val Future: scala.concurrent.Future.type = scala.concurrent.Future

    val Await: scala.concurrent.Await.type = scala.concurrent.Await

    type ShutdownHook = Context.ShutdownHook
    case class ShutdownOps(private val context: Context) {
      def shutdown(): Unit = context.hook()
    }

    case class AsyncSettings(duration: FiniteDuration)
  }

  trait Implicits { _: Types =>
    implicit def ecToExecutionContext(implicit ec: EC): ExecutionContext = ec.execution

    implicit def toShutdownOps(context: Context): ShutdownOps = ShutdownOps(context)

    import scala.concurrent.duration.DurationInt
    implicit def toDuration(n: Int): DurationInt = DurationInt(n)

    implicit val DefaultAsyncSettings: AsyncSettings = AsyncSettings(toDuration(1).second)
    implicit def toDuration(implicit settings: AsyncSettings): FiniteDuration = settings.duration
  }

  trait API { _: Types with Implicits =>
    def shutdownAll(): Unit = Context.shutdownAll()


    def runAsyncF[R](af: EC => Future[R])(implicit duration: FiniteDuration): Future[R] = {
      val ec = EC()
      val fR = af(ec)
      Await.ready(fR, duration)
      ec.shutdown()
      fR
    }

    final def runAsync[R](af: EC => Future[R])(implicit duration: FiniteDuration): R = runAsyncF[R](af)(duration).value match {
      case Some(Success(result)) => result
      case Some(Failure(ex)) => throw ex
      case None => throw new IllegalStateException("This should never happen!")
    }

    final def printlnAsync(af: EC => Future[Any])(implicit duration: FiniteDuration): Unit = runAsyncF(af)(duration).value match {
      case Some(Success(result)) => println(result)
      case Some(Failure(ex)) => println(s"Throw: $ex")
      case None => throw new IllegalStateException("This should never happen!")
    }
  }

  trait Syntax extends Types with Implicits with API
}
