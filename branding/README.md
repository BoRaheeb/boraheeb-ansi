# BoRaheeb ANSI branding

Visual identity assets for BoRaheeb ANSI. They are for documentation, the
repository page, and project listings only. They are not part of the library
and are not packaged into any Maven artifact.

## The mark

The icon reads as a terminal prompt: a `>` prompt, a `_` cursor, and the `]`
bracket from ANSI escape syntax, each in a different Dracula colour to stand
for styled output. The three are vector shapes, not text. They share one
stroke weight and have fully rounded ends, joins, and inner bends, with a
pill-shaped cursor, for a soft, jelly-like look. They sit on a raised,
rounded tile with a thin border, inside a 512 × 512 square.

## Files

| File | Size | Background | Use |
| --- | --- | --- | --- |
| `logo.svg` | 492 × 208 | Dark plate | Default logo; reads on any background |
| `logo-dark.svg` | 436 × 160 | Transparent | Light wordmark for dark pages (GitHub dark mode) |
| `logo-light.svg` | 436 × 160 | Transparent | Dark wordmark for light pages (GitHub light mode) |
| `icon.svg` | 512 × 512 | Transparent corners | Master icon, scalable |
| `icon-32.png` | 32 × 32 | Transparent corners | Favicons, small UI |
| `icon-64.png` | 64 × 64 | Transparent corners | Small avatars |
| `icon-128.png` | 128 × 128 | Transparent corners | Medium avatars, listings |
| `icon-256.png` | 256 × 256 | Transparent corners | High-DPI avatars |
| `icon-512.png` | 512 × 512 | Transparent corners | Organisation or profile avatar |
| `social-preview.png` | 1280 × 640 | Dark | GitHub social preview (Settings → General → Social preview) |

The root `README.md` uses `logo-light.svg` and switches to `logo-dark.svg` when
the reader's system prefers a dark colour scheme.

## Palette

Based on the Dracula palette.

| Role | Hex |
| --- | --- |
| Background, logo plate | `#282A36` |
| Tile fill | `#343746` |
| Tile border | `#6272A4` |
| Wordmark "BoRaheeb" (dark backgrounds) | `#F8F8F2` |
| Wordmark "ANSI" (dark backgrounds) | `#FF79C6` |
| Wordmark "ANSI" (light backgrounds) | `#C2307F` |
| Prompt `>` | `#8BE9FD` |
| Cursor `_` | `#BD93F9` |
| Bracket `]` | `#50FA7B` |

Dracula pink `#FF79C6` is too light to read on white, so `logo-light.svg`
uses a deeper pink for "ANSI". The icon keeps its dark tile everywhere.

## Typography

The wordmark is set in JetBrains Mono Bold, stacked on two lines: "BoRaheeb"
above "ANSI". The social preview's smaller lines use JetBrains Mono Regular. The letters are stored as vector outlines in the
SVG files, so no font is loaded and no font files are added by this folder.
The outlines come from the JetBrains Mono 2.304 files already in
`javadoc-theme/resources/fonts/` (SIL Open Font License 1.1, see `OFL.txt`
and `AUTHORS.txt` there).

## Usage notes

- Keep clear space around the logo of at least half the icon's height.
- Do not recolour the cursor bands, stretch the mark, or reset the wordmark
  in another typeface.
- On light backgrounds use `logo-light.svg`; on dark backgrounds use
  `logo-dark.svg`; when the background is unknown use `logo.svg`.
- Maven Central and MVNRepository do not read logos from a POM, so these
  assets will not appear on those sites automatically.
