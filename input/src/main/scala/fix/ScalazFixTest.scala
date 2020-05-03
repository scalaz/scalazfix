/*
rule = ScalazFix
 */
package fix

import scalaz._
import scalaz.syntax.std.either._

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
}
