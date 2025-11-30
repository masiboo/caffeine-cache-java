package nl.ing.api.contacting.conf

import com.twitter.util.{Future => TwFuture}

import scala.concurrent.Future
import scala.concurrent.Promise

trait FutureUtils {

  implicit class RichTwitterFuture[T](twFuture: TwFuture[T]) {

    def asScala: Future[T] = {
      val p = Promise[T]()
      twFuture.onSuccess {
        res: T =>
          p.success(res)
      }
      twFuture.onFailure {
        t: Throwable =>
          p.failure(t)
      }
      p.future
    }
  }
}

object FutureUtils extends FutureUtils
