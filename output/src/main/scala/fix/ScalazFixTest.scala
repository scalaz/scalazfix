package fix

import scalaz._
import scalaz.syntax.std.either._
import scalaz.std.option._

trait ScalazFixTest {
  EitherT[Option, Int, String](None)
  def f1: EitherT[Option, Int, String] = ???

  ReaderT[Int, Option, String](_ => None)
  def f2: ReaderT[Option, Int, String] = ???

  WriterT[Option, Int, String](None)
  def f3: WriterT[Option, Int, String] = ???

  def f4: ContT[Option, Int, String] = ???
  def f5: StateT[Option, Int, String] = ???

  BindRec[Maybe].tailrecM[Int, String](x => Maybe.empty)(42)

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
    override def tailrecM[A, B](f: A => Maybe[A \/ B])(a: A): Maybe[B] = ???
    override def bind[A, B](fa: Maybe[A])(f: A => Maybe[B]) = ???
    override def map[A, B](fa: Maybe[A])(f: A => B) = ???
  }

  Foldable[IList].psum(IList.empty[Maybe[Int]])
  Foldable[IList].psum(IList.empty[Maybe[Int]])

  {
    val f = FreeT.point[Maybe, IList, Int](5)

    f.hoist(NaturalTransformation.refl)
    f.hoist(NaturalTransformation.refl)
    f.interpret(NaturalTransformation.refl)
    f.interpret(NaturalTransformation.refl)
  }

  scalaz.std.boolean.emptyOrPoint[Maybe, Int](true)(9)
  scalaz.std.boolean.emptyOrPointNT[Maybe](false)
}
