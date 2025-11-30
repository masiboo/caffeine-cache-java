package nl.ing.api.contacting.conf.util


object EitherUtils {

  implicit class EitherSeqSplit[A,B](seq: Seq[Either[A,B]]) {
    def splitIntoLeftRight: (Seq[A],Seq[B]) = {
      seq.foldRight[(Seq[A], Seq[B])](Nil,Nil) {
        case (Left(error), (e, i)) => (e.+:(error), i)
        case (Right(result), (e, i)) => (e, i.+:(result))
      }
    }
  }

  implicit class OptionToEither[A](opt: Option[A]) {
    def toEither[B,C](f: A => Either[B,C]): Either[B,Option[C]] = {
      opt match {
        case Some(value) =>
          f(value).map(Option(_))
        case None =>
          Right(None)
      }
    }
  }
}
