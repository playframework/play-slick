resolvers += Resolver.url("scalasbt",  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.0")

//addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")
