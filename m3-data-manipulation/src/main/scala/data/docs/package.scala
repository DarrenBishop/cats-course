package data

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.ExecutionContext

package object docs {
  object EC {
    type ShutdownHook = () => Unit
    private val hooksM = scala.collection.concurrent.TrieMap.empty[String, ShutdownHook]

    def shutdownAll(): Unit = hooksM.keys.foreach { k =>
      hooksM.updateWith(k)(_.flatMap { h => h(); None })
    }

    def apply(threadPoolSize: Int= 4): (ExecutionContext, ShutdownHook) = {
      val executorService: ExecutorService = Executors.newFixedThreadPool(threadPoolSize)
      val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)
      val hook: ShutdownHook = () => executorService.shutdown()
      hooksM.addOne(executorService.toString, hook)
      sys.addShutdownHook(hook())
      (ec, hook)
    }
  }
}
