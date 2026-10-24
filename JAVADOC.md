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
`mvn clean` removes generated documentation. The Javadoc JAR is bound to the
`package` phase, so `mvn clean verify` also builds it; `mvn javadoc:javadoc`
generates only the HTML under `target/site/apidocs/`.

Maven Javadoc Plugin **3.12.0** uses the Java Standard Doclet, UTF-8 input and
HTML output, `-Xdoclint:all`, and failure on errors or warnings.

JDK 21.0.11 previously produced an internal `AssertionError` during package
discovery with `-subpackages com.boraheeb.ansi`. Explicit source-file arguments
succeeded. The current POM uses `sourcepath` set to
`${project.build.sourceDirectory}` (`src/main/java`) and `sourceFileIncludes`
matching `**/Ansi.java` and `**/package-info.java`. This selects the production
sources in `com.boraheeb.ansi` and excludes tests. Retain these settings instead
of reintroducing `subpackages`.

The active theme is **Dracula**, kept in the dedicated `javadoc-theme/` directory:

```text
javadoc-theme/
├── theme-dracula.css
└── resources/
    └── fonts/
        ├── JetBrainsMono-Regular.ttf
        ├── JetBrainsMono-Bold.ttf
        ├── JetBrainsMono-Italic.ttf
        ├── OFL.txt
        └── AUTHORS.txt
```

- `theme-dracula.css`: the only custom stylesheet. It declares the Dracula
  color variables and three `@font-face` rules (400 normal, 700 normal, 400
  italic) for the `JetBrains Mono` family, and sets the body, block, and code
  font variables to `'JetBrains Mono', monospace`.
- `resources/fonts/`: the unmodified JetBrains Mono **2.304** TTF files taken
  from the official distribution. JetBrains Mono is a third-party font asset.
- `OFL.txt`: the verbatim SIL Open Font License 1.1 text for JetBrains Mono.
- `AUTHORS.txt`: the verbatim JetBrains Mono authors list.

The project itself stays MIT-licensed (root `LICENSE`); the font keeps its own
OFL license. Neither file replaces the other.

Maven's `addStylesheets` passes `--add-stylesheet` to the Standard Doclet. The
POM sets `javadocDirectory` to `${project.basedir}/javadoc-theme` and
`docfilessubdirs` to `true`; the plugin then locates `theme-dracula.css` there
and copies the `resources/` tree into the output unchanged. In the verified
JDK 21 output, HTML loads:

1. `stylesheet.css` (standard Javadoc styling).
2. `theme-dracula.css` (the custom theme).
3. `script-dir/jquery-ui.min.css` (standard search widget styling).

Generated layout (relative to `target/site/apidocs/`, and identical at the root
of the Javadoc JAR):

```text
theme-dracula.css
resources/fonts/JetBrainsMono-Regular.ttf
resources/fonts/JetBrainsMono-Bold.ttf
resources/fonts/JetBrainsMono-Italic.ttf
resources/fonts/OFL.txt
resources/fonts/AUTHORS.txt
```

The font URLs in `theme-dracula.css` are relative (`resources/fonts/...`) and
resolve against the stylesheet's own location, so the documentation works
offline when the `apidocs` directory is opened locally. Do not edit generated
CSS. Font files exist only in documentation output; the production JAR contains
no fonts, stylesheet, or font license files.

The theme needs no locally installed font. Search and interactive navigation require JavaScript. External Java
API/reference links require internet access.
The ANSI examples are displayed as code; their terminal rendering depends on the
terminal. Different JDK doclets or browsers may need a fresh visual check.

Verification during this documentation update (2026-10-24, Windows, Maven 3.9.16,
BellSoft JDK 21.0.11): all three commands above succeeded. All **714 JUnit tests**
passed with **0 failures, 0 errors, and 0 skipped**; build and Javadoc logs
contained **0 warnings or errors**. The Javadoc JAR contains the HTML, the Dracula
stylesheet, and the bundled font files and notices; the production JAR contains
no documentation HTML, CSS, or fonts.

Known generated-resource limitation: `stylesheet.css` imports
`resources/fonts/dejavu.css`, but that file is absent from the current HTML
output and Javadoc JAR. The Dracula theme no longer depends on DejaVu: it uses
the bundled JetBrains Mono files declared in `theme-dracula.css`.

After `mvn clean verify`, check the bundled assets:

```sh
jar tf target/boraheeb-ansi-1.0.0-SNAPSHOT-javadoc.jar
jar tf target/boraheeb-ansi-1.0.0-SNAPSHOT.jar
```

The Javadoc JAR listing must include `theme-dracula.css` and
`resources/fonts/` entries for the three `.ttf` files, `OFL.txt`, and
`AUTHORS.txt`. The production JAR listing must contain none of them. The same
files must exist under `target/site/apidocs/`. Javadoc is attached to the
`package` phase, so `mvn clean verify` builds the Javadoc JAR.
