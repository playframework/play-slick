resolvers += Resolver.url("scalasbt",  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.3.0")

//used for publishing: addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")
