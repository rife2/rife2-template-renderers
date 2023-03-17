[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Nexus Snapshot](https://img.shields.io/nexus/s/com.uwyn.rife2/rife2-renderers?server=https%3A%2F%2Fs01.oss.sonatype.org%2F)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/uwyn/rife2/rife2-renderers/)
[![GitHub CI](https://github.com/rife2/rife2-template-renderers/actions/workflows/gradle.yml/badge.svg)](https://github.com/rife2/rife2-template-renderers/actions/workflows/gradle.yml)
[![Tests](https://rife2.com/tests-badge/badge/com.uwyn.rife2/rife2-renderers)](https://github.com/rife2/rife2-template-renderers/actions/workflows/gradle.yml)

# [RIFE2](https://rife2.com/) Template Renderers

This project provides a collection of template renderers.

## Date/Time Renderers

| Renderer                         | Description                                                     |
|:---------------------------------|:----------------------------------------------------------------|
| `rife.render.BeatTime`           | Renders the current time in Swatch Internet (.beat) Time format |
| `rife.render.DateIso`            | Renders the current date in ISO 8061 format                     |
| `rife.render.DateTimeIso`        | Renders the current date and time in ISO 8061 format            |
| `rife.render.DateTimeRfc2822`    | Renders the current date and time in RFC 2822 format            |
| `rife.render.DateTimeUtc`        | Renders the current UTC date and time in ISO 8061 format        |
| `rife.render.TimeIso`            | Renders the current time in ISO 8061 format                     |
| `rife.render.Year`               | Renders the current year                                        |

## Encoding Renderers

| Renderer                         | Description                                            |
|:---------------------------------|:-------------------------------------------------------|
| `rife.render.EncodeBase64`       | Encodes a template value to Base64                     |
| `rife.render.EncodeHtml`         | Encodes a template value to HTML                       |
| `rife.render.EncodeHtmlEntities` | Encodes a template value to HTML decimal entities      |
| `rife.render.EncodeJs`           | Encodes a template value to JavaScript/ECMAScript      |
| `rife.render.EncodeJson`         | Encodes a template value to JSON                       |
| `rife.render.EncodeQp`           | Converts a template value to a quoted-printable string |
| `rife.render.EncodeUnicode`      | Encodes a template value to Unicode escape codes       |
| `rife.render.EncodeUrl`          | URL-encodes a template value                           |
| `rife.render.EncodeXml`          | Encodes a template value to XML                        |

## Format Renderers

| Renderer                         | Description                                                      |
|:---------------------------------|:-----------------------------------------------------------------|
| `rife.render.formatCreditcard`   | Formats a template credit card number value to the last 4 digits |
| `rife.render.ShorteUrl`          | Shortens a template value using [is./gd](https://is.gd/)         |

## Text Renderers

| Renderer                    | Description                                                   |
|:----------------------------|:--------------------------------------------------------------|
| `rife.render.Capitalize`    | Capitalizes a template value                                  |
| `rife.render.Lowercase`     | Converts a template value to lowercase                        |
| `rife.render.Rot13`         | Translates a template value to/from ROT13                     |
| `rife.render.SwapCase`      | Swap case of a template value                                 |
| `rife.render.Trim`          | Removes leading and trailing whitespace from a template value |
| `rife.render.Uncapitalize`  | Un-capitalizes a template value                               |
| `rife.render.Uppercase`     | Converts a template value to uppercase                        |

## Usage in Templates

In RIFE2, template renders are used as follows:

```plain
<!--v render:rife.render.RendererName/-->
```

or

```plain
{{v render:rife.render.RendererName/}}
```

To specify the value ID when applicable, use:

```plain
<!--v render:rife.render.RendererName:valueId/-->
{{v render:rife.render.RendererName:valueId/}}
```

For example, to capitalize a template `foo` value:

```plain
<!--v render:rife.render.Capitalize:foo/-->
{{v render.rife.render.Capitalize:foo/}}
```
