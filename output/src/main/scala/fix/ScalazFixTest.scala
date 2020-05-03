package fix

import scalaz._
import scalaz.syntax.std.either._
import scalaz.std.option._

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

  f1.toValidation

  Success(true).toDisjunction

  Coproduct.left[Option](List(8)).toValidation

  implicitly[Liskov[Int, Int]].substCt[({ type l[-a] = a => Int })#l](x => x)

  IList(1).tailMaybe.toOption

  new BindRec[Maybe] {
    override def tailrecM[A, B](a: A)(f: A => Maybe[A \/ B]): Maybe[B] = ???
    override def bind[A, B](fa: Maybe[A])(f: A => Maybe[B]) = ???
    override def map[A, B](fa: Maybe[A])(f: A => B) = ???
  }
}
