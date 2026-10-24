# BoRaheeb ANSI

**BoRaheeb ANSI** is a lightweight, dependency-free Java library for generating ANSI SGR (Select Graphic Rendition)
escape sequences and working with terminal text colors and styles.

It provides a single static utility class, `com.boraheeb.ansi.Ansi`, with predefined styling constants,
color generation methods, and small text/color utilities.

- **Java 8+** — compiled with `--release 8` and tested on BellSoft OpenJDK 8u504.
- **No runtime dependencies** — uses the Java standard library.
- **Small API** — one public utility class, with 176 public `String` constants and 22 public method signatures.
- **Terminal-independent generation** — produces strings; visual rendering depends on the receiving terminal.

## Features

| Feature | Description |
| --- | --- |
| Text attributes | Bold, dim, italic, underline, blinking, inverse, hidden, strikethrough, overline, and selective resets |
| Standard colors | Standard and bright foreground/background colors |
| 256-color palette | Foreground, background, and underline color sequences |
| True color | RGB channels, packed `0xRRGGBB`, and CSS-style HEX inputs |
| Underline styles | Single, double, curly, dotted, and dashed (terminal-dependent) |
| Text utilities | `wrap`, `stripSgr`, and `containsSgr` |
| Color utilities | Input validation and RGB/256-color palette conversion |

Some underline styles and underline colors are terminal-dependent extensions. The library generates sequences but does not implement a terminal renderer or enable terminal support automatically.

## Installation

The project currently uses the development version **`1.0.0-SNAPSHOT`**. It is **not yet published to Maven Central**.

Build and install the library into your local Maven repository:

```sh
mvn clean install
```

Then add this dependency to another Maven project's `pom.xml`:

```xml
<dependency>
    <groupId>com.boraheeb</groupId>
    <artifactId>boraheeb-ansi</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

This works after local installation. Update the version when an official release is published.

Alternatively, build the JAR with `mvn clean package` and find it at:

```text
target/boraheeb-ansi-1.0.0-SNAPSHOT.jar
```

## Quick start

```java
import com.boraheeb.ansi.Ansi;

public class Demo {
    public static void main(String[] args) {
        // Standard styles and colors.
        System.out.println(Ansi.wrap(Ansi.BOLD + Ansi.FG_GREEN, "Success!"));

        // 256-color and true-color foregrounds.
        System.out.println(Ansi.wrap(Ansi.fg256(208), "256-color orange"));
        System.out.println(Ansi.wrap(Ansi.fgRgb(255, 165, 0), "RGB orange"));
        System.out.println(Ansi.wrap(Ansi.fgHex("#FFA500"), "HEX orange"));

        // Background color and a foreground color.
        System.out.println(Ansi.wrap(
            Ansi.bgHex("#020A6D") + Ansi.FG_WHITE,
            "Navy background"
        ));

        // Underline style and independent underline color.
        // Actual appearance varies by terminal.
        System.out.println(Ansi.wrap(
            Ansi.UNDERLINE_CURLY + Ansi.underlineRgb(255, 0, 0),
            "Curly red underline"
        ));

        // SGR detection and removal.
        String styled = Ansi.wrap(Ansi.FG_RED, "Error");
        System.out.println(Ansi.containsSgr(styled)); // true
        System.out.println(Ansi.stripSgr(styled));    // Error

        // Color conversion.
        System.out.println(Ansi.rgbTo256(255, 0, 0));    // 196
        System.out.printf("#%06X%n", Ansi.color256ToRgb(196)); // #FF0000
    }
}
```

`Ansi.wrap(...)` **always appends `Ansi.RESET`**, including when the style is `null` or empty. This prevents a wrapped style from continuing beyond the returned string.

## Color input and fallback behavior

- **256-color index:** `0`–`255`.
- **RGB channels:** each channel must be `0`–`255`.
- **Packed RGB:** `0x000000`–`0xFFFFFF`; alpha is not supported.
- **HEX input:** `#RGB`, `#RGBA`, `#RRGGBB`, or `#RRGGBBAA`. HEX alpha digits are validated but ignored.

Invalid color inputs do not throw an exception: the relevant method emits a warning to `System.err` and substitutes the documented fallback (**index `244`** or **RGB channel `180`**).

`rgbTo256(r, g, b)` selects the nearest reference color among indices `16`–`255` using squared RGB distance; configurable system colors `0`–`15` are intentionally excluded. Palette conversion is approximate, not perceptually accurate.

## Important limitations

- The appearance of ANSI output depends on the terminal. Unsupported sequences may be ignored, rendered differently, or displayed literally.
- `stripSgr` and `containsSgr` recognize ESC-based, numeric CSI SGR sequences. They are **not** general ANSI escape-sequence parsers or security sanitizers.
- The library generates terminal control strings; it does not detect terminal capabilities, manage terminal sessions, or render text itself.

## Build and testing

Requirements for building: **Maven** and a full **JDK 8 or newer**. The Maven configuration targets Java 8.

```sh
mvn clean verify
```

The current regression suite contains **714 JUnit 5 test cases**. This documentation update verified all 714 tests on BellSoft JDK **21.0.11** with Maven **3.9.16** (Windows), with no failures, errors, or skipped tests. Earlier verification also passed on BellSoft OpenJDK **8u504** and **25.0.4.1**; those JDKs were not rerun for this update. JUnit is a test-only dependency and is not included in the production JAR.

## API documentation

Use a full **BellSoft JDK 21.0.11** for the preferred **JDK 21 Standard Doclet layout**. Set `JAVA_HOME` to that JDK and confirm the Maven process uses it with `mvn --version`. Both commands below succeeded with Maven **3.9.16** during this documentation update:

```sh
mvn javadoc:javadoc
mvn javadoc:jar
```

Open `target/site/apidocs/index.html` in a browser. The documentation uses the JDK 21 Standard Doclet layout, and **Dracula** is the active visual theme: `javadoc-theme/theme-dracula.css` is loaded after the standard Javadoc `stylesheet.css`, and both are present in the output at `target/site/apidocs/`.

The Javadoc bundles **JetBrains Mono 2.304** (Regular, Bold, and Italic TrueType files in `javadoc-theme/resources/fonts/`), so no locally installed Consolas or JetBrains Mono font is required, and the pages can be viewed offline with their included assets. JetBrains Mono is a third-party font licensed under the **SIL Open Font License 1.1** (`OFL.txt` and `AUTHORS.txt` ship with the fonts); the BoRaheeb ANSI library itself remains **MIT-licensed**. The font files are included only in documentation artifacts (`target/site/apidocs/` and the Javadoc JAR), never in the production runtime JAR.

The library remains **Java 8 compatible** through `--release 8`. The JDK used to generate documentation does not determine the library's minimum runtime Java version.

For the workflow, source-discovery compatibility fix, and known generated-resource limitation, see [JAVADOC.md](JAVADOC.md).

## License

MIT License — see [LICENSE](LICENSE).
