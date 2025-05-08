# Updating module-info.java

If you need to update module-info.java in the future because a new package has been added, then do the following:

```bash
rm -rf /tmp/modinfo   # clear any leftover from the failed runs
jdeps --ignore-missing-deps --generate-module-info /tmp/modinfo \
    ~/.m2/repository/org/ejml/ejml-java9module/0.45.2-SNAPSHOT/ejml-java9module-0.45.2-SNAPSHOT.jar
mkdir -p main/ejml-java9module/module
cp /tmp/modinfo/ejml.java9module/module-info.java main/ejml-java9module/module/module-info.java
```
Then edit the desktop dependency to make it optional at runtime for headless environments:
```
requires static java.desktop;
```

# Verify Publish Worked

Run after `./gradlew pTML`. Set the version once, then paste each check.

```bash
VERSION=0.45.2-SNAPSHOT
REPO="$HOME/.m2/repository/org/ejml/ejml-java9module/$VERSION"
JAR="$REPO/ejml-java9module-$VERSION.jar"
```

1) `ls "$REPO"/ejml-java9module-$VERSION{.jar,.pom,.module,-sources.jar,-javadoc.jar}`
   * PASS: all 5 paths listed, no `No such file`.
2) `ls "$REPO"/*.asc | wc -l`
   * PASS: count ≥ 5. (Skip for unsigned snapshots.)
3) `jar --file "$JAR" --list | grep -x module-info.class`
   * PASS: prints `module-info.class`. FAIL: empty.
4) `jar --file "$JAR" --describe-module | head -1`
   * PASS: starts with `ejml.java9module`, does not contain `automatic`.
5) `grep "<artifactId>ejml-" "$REPO/ejml-java9module-$VERSION.pom"`
   * PASS: only ejml-java9module (the artifact itself)
6) jar --file "$REPO"/ejml-java9module-$VERSION-sources.jar --list | grep -c '\.java$'   # > 0
   jar --file "$REPO"/ejml-java9module-$VERSION-javadoc.jar --list | grep -x index.html   # release builds only