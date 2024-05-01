package typeclasses

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.ExecutionContext

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
    type ShutdownHook = Context.ShutdownHook
  }

  trait Syntax extends Types {
    def shutdownAll(): Unit = Context.shutdownAll()

    implicit def ecToExecutionContext(implicit ec: EC): ExecutionContext = ec.execution

    import scala.concurrent.duration.DurationInt
    implicit def toDuration(n: Int): DurationInt = DurationInt(n)

    case class ShutdownOps(private val context: Context) {
      def shutdown(): Unit = context.hook()
    }
    implicit def toShutdownOps(context: Context): ShutdownOps = ShutdownOps(context)
  }
}
