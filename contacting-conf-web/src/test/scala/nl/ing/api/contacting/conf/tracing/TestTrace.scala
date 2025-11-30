package nl.ing.api.contacting.conf.tracing

import cats.effect.IO
import cats.effect.Resource
import cats.effect.Sync
import nl.ing.api.contacting.tracing.Span
import nl.ing.api.contacting.tracing.Trace

import scala.collection.mutable


object TestTrace {

  case class ContactingTestSpan[F[_]: Sync](span: mutable.ListBuffer[String]) extends Span[F] {

    override def span(name: String, activateSpan: Boolean): Resource[F, Span[F]] =
      Resource
        .make(Sync[F].delay(name)) {
          _ =>
            Sync[F].unit
        }
        .map(s => ContactingTestSpan(span += s))

    override def put(fields: (String, String)*): F[Unit] = Sync[F].pure(())
  }

  class TestTrace[A](thunk: String => A) extends Trace[IO] {

    def span[A](name: String, activateSpan: Boolean)(k: IO[A]): IO[A] =
      IO {
        thunk.apply(name)
      }.flatMap(_ => k)

    override def put(fields: (String, String)*): IO[Unit] = IO.pure(())
  }
}
