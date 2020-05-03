package scalaz

import scalafix.v1._

import scala.meta.Term.ApplyType
import scala.meta._

final case class ReplaceMethod(symbol: String, newMethod: String)

object ReplaceDeprecatedMethod {
  val values: List[ReplaceMethod] = List(
    ReplaceMethod("scalaz/syntax/std/EitherOps#validation().", "toValidation"),
    ReplaceMethod("scalaz/syntax/std/EitherOps#disjunction().", "toDisjunction"),
    ReplaceMethod("scalaz/`\\/`#validationNel().", "toValidationNel"),
    ReplaceMethod("scalaz/`\\/`#validation().", "toValidation"),
    ReplaceMethod("scalaz/EitherT#validation().", "toValidation"),
    ReplaceMethod("scalaz/Validation#disjunction().", "toDisjunction"),
    ReplaceMethod("scalaz/Coproduct#validation().", "toValidation"),
    ReplaceMethod("scalaz/Liskov#subst().", "substCt"),
    ReplaceMethod("scalaz/Foldable#msuml().", "psum"),
    ReplaceMethod("scalaz/FoldableParent#msumlU().", "psum"),
    ReplaceMethod("scalaz/FreeT#hoistN().", "hoist"),
    ReplaceMethod("scalaz/FreeT#hoistM().", "hoist"),
    ReplaceMethod("scalaz/FreeT#interpretS().", "interpret"),
    ReplaceMethod("scalaz/FreeT#interpretT().", "interpret"),
    ReplaceMethod("scalaz/std/BooleanFunctions#emptyOrPure().", "emptyOrPoint"),
    ReplaceMethod("scalaz/std/BooleanFunctions#emptyOrPureNT().", "emptyOrPointNT"),
  )
}

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
      case x: Term.Select =>
        ReplaceDeprecatedMethod.values.find(_.symbol == x.name.symbol.value) match {
          case Some(r) =>
            Patch.replaceTree(x, x.copy(name = x.name.copy(r.newMethod)).toString)
          case None =>
            if (x.name.symbol.value == "scalaz/IList#tailOption().") {
              Patch.replaceTree(x, s"${x.qual}.tailMaybe.toOption")
            } else {
              Patch.empty
            }
        }
      case x: Defn.Def
          if List(
            x.name.value == "tailrecM",
            x.tparams.size == 2,
            x.paramss.size == 2,
            x.paramss.forall(_.size == 1)
          ).forall(identity) =>
        List(
          Patch.addLeft(x.paramss(0).head.tokens.head, x.paramss(1).head.toString),
          Patch.addLeft(x.paramss(1).head.tokens.head, x.paramss(0).head.toString),
          Patch.removeTokens(x.paramss(0).flatMap(_.tokens.toList)),
          Patch.removeTokens(x.paramss(1).flatMap(_.tokens.toList)),
        ).asPatch
    }.asPatch
  }

}
