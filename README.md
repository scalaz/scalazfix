# scalaz migration

[![Build Status](https://travis-ci.com/scalaz/scalazfix.svg?branch=master)](https://travis-ci.com/github/scalaz/scalazfix)

<https://scalacenter.github.io/scalafix/docs/rules/external-rules.html>

`project/scalafix.sbt`

```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.15")
```

sbt shell

### apply release version

```
> scalafixEnable
> scalafix dependency:ScalazFix@org.scalaz:scalazfix:0.1.1
> test:scalafix dependency:ScalazFix@org.scalaz:scalazfix:0.1.1
```

### apply snapshot version

```
> scalafixEnable
> scalafix https://raw.githubusercontent.com/scalaz/scalazfix/master/rules/src/main/scala/scalaz/ScalazFix.scala
> test:scalafix https://raw.githubusercontent.com/scalaz/scalazfix/master/rules/src/main/scala/scalaz/ScalazFix.scala
```
