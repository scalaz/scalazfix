# scalaz migration

<https://scalacenter.github.io/scalafix/docs/rules/external-rules.html>

`project/scalafix.sbt`

```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.3")
```

sbt shell

### apply release version

```
> scalafixEnable
> scalafixAll dependency:ScalazFix@org.scalaz:scalazfix:0.2.0
```

### apply snapshot version

```
> scalafixEnable
> scalafixAll https://raw.githubusercontent.com/scalaz/scalazfix/master/rules/src/main/scala/scalaz/ScalazFix.scala
```
