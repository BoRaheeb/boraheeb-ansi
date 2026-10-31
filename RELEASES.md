# Release history

Notes on published BoRaheeb ANSI releases. The published tags and artifacts are
immutable and are not changed by these notes.

## 1.0.0

| Property | Value |
| --- | --- |
| Version | 1.0.0 |
| Git tag | `v1.0.0` (annotated) |
| Tagged commit | `68873233de78a92a6681df5d12351912fac5adc1` |
| Maven coordinates | `com.boraheeb:boraheeb-ansi:1.0.0` |
| Java compatibility | Java 8+ (`--release 8`) |

### Dates

All times are UTC. The three dates describe different events.

| Event | Date and time (UTC) | Source |
| --- | --- | --- |
| Tagged commit created | 2026-10-24 15:18:16 | Git commit date |
| Tag `v1.0.0` created | 2026-10-24 15:20:25 | Git tagger date; the GitHub Release record was created at the same time |
| GitHub Release published | 2026-10-30 09:54:25 | GitHub Releases API (`published_at`) |
| Maven Central metadata last updated | 2026-10-30 14:08:36 | Central `maven-metadata.xml` (`lastUpdated`) |

The Central timestamp is the repository metadata's last-updated time. It is not
a separate record of when the release first became available.

### Distribution channels and artifacts

| Channel | Location |
| --- | --- |
| GitHub Release | <https://github.com/BoRaheeb/boraheeb-ansi/releases/tag/v1.0.0> |
| Maven Central | <https://central.sonatype.com/artifact/com.boraheeb/boraheeb-ansi/1.0.0> |

Both channels list the same eight file names.

Primary release artifacts:

- `boraheeb-ansi-1.0.0.jar`
- `boraheeb-ansi-1.0.0-sources.jar`
- `boraheeb-ansi-1.0.0-javadoc.jar`
- `boraheeb-ansi-1.0.0.pom`

Detached GPG signatures, one per primary artifact:

- `boraheeb-ansi-1.0.0.jar.asc`
- `boraheeb-ansi-1.0.0-sources.jar.asc`
- `boraheeb-ansi-1.0.0-javadoc.jar.asc`
- `boraheeb-ansi-1.0.0.pom.asc`

### Changes

This is the first stable release. For the feature summary, see the
[GitHub Release notes](https://github.com/BoRaheeb/boraheeb-ansi/releases/tag/v1.0.0).

### Verification

Performed after publication:

- Both channels were reachable and listed the files above.
- All eight signature files (four per channel) verify as good signatures with
  GnuPG. The public key `5D56889E98F7A7360C44D66EAC93E5872F86EB67` was imported
  from a public keyserver into a temporary keyring, so this confirms that each
  signature matches its file and that key. It does not establish trust in the
  key.
- The Maven Central files are byte-identical to the corresponding local
  `target/` files from the project's release build. The GitHub Release files
  are not.

Stated by the GitHub Release notes and not independently verified: the build
used Maven 3.9.16 and BellSoft JDK 21.0.11, and 714 tests passed.

### Build provenance note

The published 1.0.0 artifacts come from two different builds that are not all
byte-identical. The differences are release metadata and generation timestamps,
not source code.

- The GitHub Release files contain the POM from the `v1.0.0` tag
  (`pom.xml`, 6,815 bytes).
- The Maven Central files contain a later POM (7,448 bytes) that adds the Maven
  Central publishing configuration (`central-publishing-maven-plugin` 0.12.0).
  That configuration was committed after the tag, in commit `1fbf137`, and the
  POM has not changed since.
- The compiled `com/boraheeb/ansi/Ansi.class` is identical in both main JARs.
  The published `Ansi.java` and `package-info.java` in both sources JARs are
  identical to the files in the `v1.0.0` tag.
- Between the two channels, the main JAR and the sources JAR differ only in
  their embedded `pom.xml`. The Javadoc JAR has the same file list, and its
  HTML files differ only in the generation timestamp comment and the
  `dc.created` date.
- The `branding/` directory was added after the tag (commit `07ec55f`). It is
  not part of the published artifacts.

| Primary artifact or signature | Same bytes on both channels |
| --- | --- |
| `boraheeb-ansi-1.0.0.jar` | No |
| `boraheeb-ansi-1.0.0-sources.jar` | No |
| `boraheeb-ansi-1.0.0-javadoc.jar` | No |
| `boraheeb-ansi-1.0.0.pom` | No |
| The four `.asc` signatures | No |

SHA-256 values of the four primary artifacts (the `.asc` signature files are not hashed here):

| File | GitHub Release | Maven Central |
| --- | --- | --- |
| `boraheeb-ansi-1.0.0.jar` | `45565142bc2042770d68530bd2564eb2df4e8db90d36f57b1cfbd1d3dffe2529` | `7486601fde5ed44b984f667d805007e406828d7dba742183b6b21441bed40614` |
| `boraheeb-ansi-1.0.0-sources.jar` | `8ee5c4c41d987e1e8d8c5061103d61026fbf3388e83a4fa025a6ab978bed0ad5` | `af3fe2b73728ac335ac4d9b764ebea24cb1bb1bb1e1fdf4a0faa7cba2ea5f891` |
| `boraheeb-ansi-1.0.0-javadoc.jar` | `77fcd47164c5e29a0338291d13e3d75da6822168db7f6176954108b57c250fad` | `4a74e0142975edb13870f7420d6ceb6786315be0f5e5b9f3fb9606dfc5fe5476` |
| `boraheeb-ansi-1.0.0.pom` | `33b09cd41aacfe0f01e9b6f208863a0a99057e94109b9c5e8cfbafafe99c9a83` | `db1930a8d983460526b19e7362c8d3442ab3f4b29494bb71d7b3787f92809002` |

This was not verified: the exact build environment of either build, and whether
either release build was run from a detached checkout of `v1.0.0`. The
generation timestamp in the GitHub Release Javadoc JAR (2026-10-24 18:05 AST,
UTC+3 on the build machine) is earlier than the tagged commit's time (18:18
+03:00). This shows only that the Javadoc was generated before the tagged commit
existed, not which source revision it used.

The `v1.0.0` tag and the published artifacts remain unchanged. Later releases
should commit all build and publishing configuration before the release tag is
created, and should publish the same files to every channel.
