            hotfix                            +--------->+
                                              ^          |
                     1.0.0-beta1      1.0.0   |          |                    1.0.1
    master/release     >----------------+------          v--------------------->+
                       |                |   1.0.1       1.0.1-beta1             |
                       |                |                                       |
                       |                |                                       |
           develop +--------------------v---------------------------------------v--->
                        project
                        version
                        1.1.0

(Die Codebeipiele nehmen an, dass origin auf die verinice. interne Repo gesetzt ist)

# Release-Phase
## Pre-Release
1. Zum Beginn der Release-Phase für Version `$version` den `release/$version` Branch erstellen

       git fetch
       git switch develop
       git switch -c release/$version

2. Nachdem der `release`-Branch angelegt wurde, ist die Minor-Version im
   `develop`-Branch zu erhöhen. Siehe hierzu Versioning in [README.md](../README.md).

## Zum Release
1. `release/$version` nach `master` fast forward only (`--ff-only`) mergen. Dies sollte mittels Pull-Request
   geschehen.

   Ist kein fast forward möglich wurde etwas falsch gemacht.

   Nach dem Merge is die Release-Version zu taggen

       git fetch
       git tag -s $version origin/master
       git push origin $version

2. Tag `$version` nach `develop` mergen, wahrscheinlich ist hier kein `--ff-only`
   möglich, z. B. wenn während der Release-Phase in `develop` gearbeitet wurde.

       git fetch
       git switch -c merge-release-$version-to-develop origin/develop
       git merge $version
       git push -u origin merge-release-$version-to-develop

   `merge-release-$version-to-develop` kann jetzt mit einem PR `--ff-only` gemerget werden.
   Sollte sich `develop` verändert haben, muss der merge wiederholt werden. Bis ein fast
   forward merge möglich ist.

3. Tag `$version` nach `eval` mergen, wahrscheinlich ist hier durch die
   eval-Patches kein `--ff-only` möglich.

       git fetch
       git switch -c merge-release-$version-to-eval origin/eval
       git merge $version
       git push origin merge-release-$version-to-eval

   Wenn gemerget wurde muss die eval-Version ebenfalls getaggt werden.

       git fetch
       git checkout origin/eval
       git tag -s eval-$version
       git push origin eval-$version

# Publish auf GitHub

    git fetch
    git remote add github git@github.com:SerNet/verinice.git
    git push github origin/master $version

# Hotfixes
Hier ist beschrieben wie ein Hotfix releaset wird. Also ein Bugfix
der nicht erst über den `develop` Branch in das nächste Release mit aufgenommen werden soll.

1. `hotfix` von `release/$version` abzweigen wieder `release/$version` mergen.
    
       git switch release/$version
       git switch -c hotfix/vn-x-?

2. Zum Release siehe "Zum Release".
