name := "computer-database-sample"

PlayKeys.playOmnidoc := false

// Force htmlunit downgrade, as 2.17 seems to have a bug that cause the tests
// to fail
libraryDependencies += "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.44.0" force()
