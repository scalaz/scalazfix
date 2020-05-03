/*
rule = ScalazFix
 */
package fix

import scalaz._
import scalaz.syntax.std.either._
import scalaz.std.option._

trait ScalazFixTest {
  EitherT[Option, Int, String](None)
  def f1: EitherT[Option, Int, String] = ???

  ReaderT[Option, Int, String](_ => None)
  def f2: ReaderT[Option, Int, String] = ???

  WriterT[Option, Int, String](None)
  def f3: WriterT[Option, Int, String] = ???

  def f4: ContT[Option, Int, String] = ???
  def f5: StateT[Option, Int, String] = ???

  BindRec[Maybe].tailrecM[Int, String](x => Maybe.empty)(42)

  NonEmptyList.nels(1, Seq(2, 3): _*)
  NonEmptyList.nels(4, 5, 6)

  Right(3).validation
  Left(4).disjunction

  \/-("a").validation
  -\/("b").validationNel

  f1.validation

  Success(true).disjunction

  Coproduct.left[Option](List(8)).validation

  implicitly[Liskov[Int, Int]].subst[({ type l[-a] = a => Int })#l](x => x)

  IList(1).tailOption

  new BindRec[Maybe] {
    override def tailrecM[A, B](f: A => Maybe[A \/ B])(a: A): Maybe[B] = ???
    override def bind[A, B](fa: Maybe[A])(f: A => Maybe[B]) = ???
    override def map[A, B](fa: Maybe[A])(f: A => B) = ???
  }

  Foldable[IList].msuml(IList.empty[Maybe[Int]])
  Foldable[IList].msumlU(IList.empty[Maybe[Int]])
}
