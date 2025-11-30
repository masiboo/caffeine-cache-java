package nl.ing.api.contacting.conf.modules

import cats.effect.unsafe.IORuntime
import com.google.common.util.concurrent.{ListeningExecutorService, MoreExecutors}
import nl.ing.api.contacting.cc2.{CC2GlobalExecutionContext, ExecutionContextDecorator}

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import cats.effect.unsafe.implicits.global

/**
 * @author Ayush Mittal
 */
object ExecutionContextConfig {
  val unboundedExecutor: ExecutorService = new ExecutionContextDecorator(Executors.newCachedThreadPool())

  /**
   * Execution context backed by unbounded threads. To be used for
   * blocking , I/O operations. Be very careful in passing this around
   * and do not pass them implicitly
   */
  val ioExecutionContext: ExecutionContext = new CC2GlobalExecutionContext(ExecutionContext.fromExecutor(unboundedExecutor))
  /*val ioContextShift: ContextShift[IO] = IO.contextShift(ioExecutionContext)
  val ioTimer: Timer[IO] = IO.timer(ExecutionContextConfig.ioExecutionContext)*/

  /**
   * A thread pool for the callflow survey job
   */
  val callflowSurveyExecutionContext: ExecutionContext = new CC2GlobalExecutionContext(ExecutionContext.fromExecutor(unboundedExecutor))
 // val callflowSurveyContextShift: ContextShift[IO] = IO.contextShift(ioExecutionContext)


  /**
   * Execution context by limited threads. To be used for
   * non blocking, CPU bound operations
   */
  implicit val boundedExecutor: ExecutionContextExecutor = CC2GlobalExecutionContext.global
  implicit val executionContext: ExecutionContext = new CC2GlobalExecutionContext(CC2GlobalExecutionContext.Implicits.global)
/*
  implicit val contextShift: ContextShift[IO] = IO.contextShift(executionContext)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContextConfig.executionContext)

*/

  val listeningExecutorService: ListeningExecutorService = MoreExecutors.listeningDecorator(unboundedExecutor)

  /**
   * https://github.com/typelevel/cats-effect/discussions/1562
   * Daniel view point is that the DEFAULT runtime by cats should be more optimal
   * then any other custom runtime that we intend to create.
   * "The only exception to this would be if you're pervasively blocking on your compute pool,
   * which is a situation that the default ExecutionContext.global would handle more gracefully than IORuntime.
   * But… please don't do that."
   * So, for now we use the global because it internally has a compute and a blocking threadpool.
   * We analyze results and see how this performs.
   */
  implicit val ioRunTime = global
}
