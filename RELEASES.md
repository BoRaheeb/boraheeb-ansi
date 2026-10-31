# Release history

Notes on published BoRaheeb ANSI releases. The published tags and artifacts are
immutable and are not changed by these notes.

## 1.0.0

Git tag: `v1.0.0` (annotated), commit `6887323`.
Maven coordinates: `com.boraheeb:boraheeb-ansi:1.0.0`.

### Build provenance note

The published 1.0.0 POM does not match the POM in the `v1.0.0` tag. The
differences are release metadata only, not source code.

- The Maven Central publishing configuration
  (`central-publishing-maven-plugin` 0.12.0) was committed after the tag, in
  commit `1fbf137`. The published POM includes it; the tagged POM does not.
- The published `Ansi.java` and `package-info.java` in the sources JAR are
  identical to the files in the `v1.0.0` tag.
- The `branding/` directory was added after the tag (commit `07ec55f`). It is
  not part of the published artifacts.

Verification performed after publication:

- The published POM, JAR, sources JAR, Javadoc JAR, and their four `.asc`
  signatures are byte-identical to the corresponding local build outputs.
- All four signatures verify as good signatures for the release key
  `5D56889E98F7A7360C44D66EAC93E5872F86EB67`.

This was not verified: the exact build environment, and whether the release
build was run from a detached checkout of `v1.0.0`.

The `v1.0.0` tag and the published artifacts remain unchanged. Later releases
should commit all build and publishing configuration before the release tag is
created.
