# scalaz migration

[![Build Status](https://travis-ci.com/scalaz/scalazfix.svg?branch=master)](https://travis-ci.com/github/scalaz/scalazfix)

<https://scalacenter.github.io/scalafix/docs/rules/external-rules.html>

`project/scalafix.sbt`

```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.15")
```

sbt shell

```
> scalafixEnable
> scalafix dependency:ScalazFix@org.scalaz:scalazfix:0.1.0
> test:scalafix dependency:ScalazFix@org.scalaz:scalazfix:0.1.0
```
