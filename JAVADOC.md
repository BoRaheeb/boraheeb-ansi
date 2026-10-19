# API documentation

Prefer a full **BellSoft JDK 21.0.11** (including `javadoc`) for the JDK 21
Standard Doclet layout. **Maven 3.9.16** and this JDK are verified versions,
not necessarily the only compatible versions. Set `JAVA_HOME` to the selected
JDK and check `mvn --version` to confirm Maven uses it.

The library and documented Java API remain Java 8 compatible through
`--release 8`. The documentation toolchain does not change the library's
minimum runtime Java version.

Run from the project root:

```sh
mvn clean verify
mvn javadoc:javadoc
mvn javadoc:jar
```

- `mvn clean verify` compiles, tests, and packages the Java 8-compatible library.
- `mvn javadoc:javadoc` generates HTML API documentation.
- `mvn javadoc:jar` creates the separate Javadoc archive.

Open `target/site/apidocs/index.html` in a browser. The public API page is
`target/site/apidocs/com/boraheeb/ansi/Ansi.html`. Copy the entire `apidocs`
directory when sharing it so its local styles, scripts, and icons stay together.

The separate archive is
`target/boraheeb-ansi-1.0.0-SNAPSHOT-javadoc.jar`. Extract it and open its
`index.html` to browse it. Documentation is not included in the production JAR.
`mvn clean` removes generated documentation. Javadoc is not bound to the
default build lifecycle, so `mvn clean verify` only builds and tests the library.

Maven Javadoc Plugin **3.12.0** uses the Java Standard Doclet, UTF-8 input and
HTML output, `-Xdoclint:all`, and failure on errors or warnings.

JDK 21.0.11 previously produced an internal `AssertionError` during package
discovery with `-subpackages com.boraheeb.ansi`. Explicit source-file arguments
succeeded. The current POM uses `sourcepath` set to
`${project.build.sourceDirectory}` (`src/main/java`) and `sourceFileIncludes`
matching `**/Ansi.java` and `**/package-info.java`. This selects the production
sources in `com.boraheeb.ansi` and excludes tests. Retain these settings instead
of reintroducing `subpackages`.

The active theme is **Dracula**, with the root [theme-dracula.css](theme-dracula.css)
as the only custom stylesheet. Maven's `addStylesheets` passes `--add-stylesheet`
to the Standard Doclet. In the verified JDK 21 output, HTML loads:

1. `stylesheet.css` (standard Javadoc styling).
2. `theme-dracula.css` (the custom theme).
3. `script-dir/jquery-ui.min.css` (standard search widget styling).

These paths are relative to `target/site/apidocs/`. The custom stylesheet is
copied there unchanged; generated documentation does not need access to the
project-root copy. Do not edit generated CSS.

The theme declares `Consolas, 'Courier New', monospace` for body, block, and code
fonts. Search and interactive navigation require JavaScript. External Java
API/reference links require internet access.
The ANSI examples are displayed as code; their terminal rendering depends on the
terminal. Different JDK doclets or browsers may need a fresh visual check.

Verification during this documentation update (2026-10-20, Windows, Maven 3.9.16,
BellSoft JDK 21.0.11): all three commands above succeeded. All **714 JUnit tests**
passed with **0 failures, 0 errors, and 0 skipped**; build and Javadoc logs
contained **0 warnings or errors**. The Javadoc JAR contains the HTML and Dracula
stylesheet; the production JAR contains no documentation HTML or CSS.

Known generated-resource limitation: `stylesheet.css` imports
`resources/fonts/dejavu.css`, but that file is absent from the current HTML
output and Javadoc JAR. The theme declares the local font stack above; repairing
the missing generated font resource remains outside this documentation update.
Visual rendering was not rechecked during this update.
