# Releasing play-slick

Releasing a new version of play-slick is truly simple.

## Precondition

* An account on vegemite.

## Release steps

* Log in vegemite.
* type `sudo su - play`, and enter your account's password.
* type `cd deploy`.
* and finally type `./release --project play-slick --branch <branch-to-release> --tag <tag-name>`, where <branch-to-release> is the name of the branch you want to release and <tag-name> is the version tag.

That will start the build. The output should be similar to

```shell
play ▶ [] ~/deploy$ ./release --project play-slick --branch master --tag 5.0.0
This will release play-slick from branch master using JDK8, continue? [y/n] y
java version "1.8.0_31"
...
```

Type ENTER if the version between square brackets suits you. If it doesn't, provide the desired version to release.

Once the binaries are successfully deployed, you will be prompted if you wish to push your changes to the remote repository:

```shell
...
[info] Dropping staging repository [comtypesafe-1354] status:released, profile:com.typesafe(34c112e991655)
[info] Dropped successfully: comtypesafe-1354
[info] Setting version to '1.0.2-SNAPSHOT'.
[info] Reapplying settings...
[info] Set current project to play-slick-root (in build file:/home/play/deploy/play-slick/)
[info] [master fa73a4f] Setting version to 1.0.2-SNAPSHOT
[info]  1 file changed, 1 insertion(+), 1 deletion(-)
Push changes to the remote repository (y/n)? [y]
```

Typing `y` will create a tag in the remote repository and push a commit to update the branch version number.
