package fix

import scalaz._
import scalaz.syntax.std.either._

trait ScalazFixTest {
  EitherT[Int, Option, String](None)
  def f1: EitherT[Int, Option, String] = ???

  ReaderT[Int, Option, String](_ => None)
  def f2: ReaderT[Int, Option, String] = ???

  WriterT[Int, Option, String](None)
  def f3: WriterT[Int, Option, String] = ???

  def f4: ContT[Int, Option, String] = ???
  def f5: StateT[Int, Option, String] = ???

  BindRec[Maybe].tailrecM[Int, String](42)(x => Maybe.empty)

  NonEmptyList.fromSeq(1, Seq(2, 3))
  NonEmptyList.nel(4, scalaz.IList(5, 6))

  Right(3).toValidation
  Left(4).toDisjunction

  \/-("a").toValidation
  -\/("b").toValidationNel
}
