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
      case x @ ApplyType.Initial(_, t1 :: t2 :: t3 :: Nil) if monadTransSwitch3ApplyType.contains(x.fun.symbol.value) =>
        val replaced = x.copy(targs = t2 :: t1 :: t3 :: Nil).toString
        Patch.replaceTree(x, replaced)
      case x @ Type.Apply.Initial(_, t1 :: t2 :: t3 :: Nil)
          if monadTransSwitch3TypeApply.contains(x.tpe.symbol.value) =>
        val replaced = x.copy(args = t2 :: t1 :: t3 :: Nil).toString
        Patch.replaceTree(x, replaced)
      case x @ Term.Apply.Initial(_, x1 :: Nil) if x.fun.symbol.value == "scalaz/BindRec#tailrecM()." =>
        x.fun match {
          case y @ Term.Apply.Initial(_, y1 :: Nil) if x.fun.symbol.value == "scalaz/BindRec#tailrecM()." =>
            Patch.replaceTree(
              x,
              Term.Apply
                .Initial(
                  fun = Term.Apply.Initial(
                    fun = y.fun,
                    args = x1 :: Nil
                  ),
                  args = y1 :: Nil
                )
                .toString
            )
          case _ =>
            Patch.empty
        }
      case x @ Term.Apply.Initial(_, args) if x.fun.symbol.value == "scalaz/NonEmptyList.nels()." && args.size >= 2 =>
        args.last match {
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
