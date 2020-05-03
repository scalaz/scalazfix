package scalaz

import scalafix.v1._

import scala.meta.Term.ApplyType
import scala.meta._

class ScalazFix extends SemanticRule("ScalazFix") {

  private[this] val monadTransSwitch3 = List(
    "package.ReaderT",
    "package.ContT",
    "package.StateT",
    "EitherT",
    "WriterT",
  ).map("scalaz/" + _)

  private[this] val monadTransSwitch3ApplyType = monadTransSwitch3.map(_ + ".")
  private[this] val monadTransSwitch3TypeApply = monadTransSwitch3.map(_ + "#")

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case x: ApplyType if monadTransSwitch3ApplyType.contains(x.fun.symbol.value) && x.targs.size == 3 =>
        val replaced = x.copy(targs = x.targs(1) :: x.targs(0) :: x.targs(2) :: Nil).toString
        Patch.replaceTree(x, replaced)
      case x: Type.Apply if monadTransSwitch3TypeApply.contains(x.tpe.symbol.value) && x.args.size == 3 =>
        val replaced = x.copy(args = x.args(1) :: x.args(0) :: x.args(2) :: Nil).toString
        Patch.replaceTree(x, replaced)
      case x: Term.Apply if x.fun.symbol.value == "scalaz/BindRec#tailrecM()." && x.args.size == 1 =>
        x.fun match {
          case y: Term.Apply if x.fun.symbol.value == "scalaz/BindRec#tailrecM()." && y.args.size == 1 =>
            Patch.replaceTree(
              x,
              Term
                .Apply(
                  fun = Term.Apply(
                    fun = y.fun,
                    args = x.args
                  ),
                  args = y.args
                )
                .toString
            )
          case _ =>
            Patch.empty
        }
      case x: Term.Apply if x.fun.symbol.value == "scalaz/NonEmptyList.nels()." && x.args.size >= 2 =>
        x.args.last match {
          case y: Term.Repeated if x.args.size == 2 =>
            Patch.replaceTree(
              x,
              s"NonEmptyList.fromSeq(${x.args.head}, ${y.expr})"
            )
          case _ =>
            Patch.replaceTree(
              x,
              s"NonEmptyList.nel(${x.args.head}, scalaz.IList(${x.args.tail.mkString(", ")}))"
            )
        }
      case x: Term.Select if x.name.symbol.value == "scalaz/syntax/std/EitherOps#validation()." =>
        Patch.replaceTree(x, x.copy(name = x.name.copy("toValidation")).toString)
      case x: Term.Select if x.name.symbol.value == "scalaz/syntax/std/EitherOps#disjunction()." =>
        Patch.replaceTree(x, x.copy(name = x.name.copy("toDisjunction")).toString)
    }.asPatch
  }

}
