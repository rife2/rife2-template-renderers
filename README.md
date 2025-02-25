[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![bld](https://img.shields.io/badge/2.2.1-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/rife2/rife2-template-renderers)](https://github.com/rife2/rife2-template-renderers/releases/latest)
[![Release](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/releases/com/uwyn/rife2/rife2-renderers/maven-metadata.xml?color=blue)](https://repo.rife2.com/#/releases/com/uwyn/rife2/rife2-renderers)
[![Maven Central](https://img.shields.io/maven-central/v/com.uwyn.rife2/rife2-renderers)](https://central.sonatype.com/artifact/com.uwyn.rife2/rife2-renderers/)
[![Nexus Snapshot](https://img.shields.io/nexus/s/com.uwyn.rife2/rife2-renderers?server=https%3A%2F%2Fs01.oss.sonatype.org%2F)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/uwyn/rife2/rife2-renderers/)
[![GitHub CI](https://github.com/rife2/rife2-template-renderers/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/rife2-template-renderers/actions/workflows/bld.yml)
[![Tests](https://rife2.com/tests-badge/badge/com.uwyn.rife2/rife2-renderers)](https://github.com/rife2/rife2-template-renderers/actions/workflows/gradle.yml)

# [RIFE2](https://rife2.com/) Template Renderers

This project provides a collection of useful template renderers.

## Date/Time Renderers

| Renderer                                                                                                          | Description                                                     |
|:------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------|
| [rife.render.BeatTime](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.BeatTime)               | Renders the current time in Swatch Internet (.beat) Time format |
| [rife.render.DateIso](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.DateIso)                 | Renders the current date in ISO 8061 format                     |
| [rife.render.DateTimeIso](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.DateTimeIso)         | Renders the current date and time in ISO 8061 format            |
| [rife.render.DateTimeRfc2822](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.DateTimeRfc2822) | Renders the current date and time in RFC 2822 format            |
| [rife.render.TimeIso](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.TimeIso)                 | Renders the current time in ISO 8061 format                     |
| [rife.render.Year](https://github.com/rife2/rife2-template-renderers/wiki/rife.rennder.Year)                      | Renders the current year                                        |

## Encoding Renderers

| Renderer                                                                                                                | Description                                             |
|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------|
| [rife.render.EncodeBase64](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.EncodeBase64)             | Encodes a template value to Base64                      |
| [rife.render.EncodeHtml](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.EncodeHtml)                 | Encodes a template value to HTML                        |
| [rife.render.EncodeHtmlEntities](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.EncodeHtmlEntities) | Encodes a template value to HTML decimal entities       |
| [rife.render.EncodeJs](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.EncodeJs)                     | Encodes a template value to JavaScript/ECMAScript       |
| [rife.render.EncodeJson](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.EncodeJson)                 | Encodes a template value to JSON                        |
| [rife.render.EncodeUnicode](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.EncodeUnicode)           | Encodes a template value to Unicode escape codes        |
| [rife.render.EncodeUrl](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.EncodeUrl)                   | URL-encodes a template value                            |
| [rife.render.EncodeXml](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.EncodeXml)                   | Encodes a template value to XML                         |

## Format Renderers

| Renderer                                                                                                            | Description                                                      |
|:--------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------|
| [rife.render.Abbreviate](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Abbreviate)             | Abbreviates a template value                                     |
| [rife.render.formatCreditcard](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.FormatCreditCard) | Formats a template credit card number value to the last 4 digits |
| [rife.render.Mask](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Mask)                         | Masks characters of a template value                             |
| [rife.render.Normalize](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Normalize)               | Normalizes a template value for inclusion in a URL path          |
| [rife.render.QrCode](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.QrCode)                     | Generates an SVG QR Code from a template value                   |
| [rife.render.ShortenUrl](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.ShortenUrl)             | Shortens a template value URL                                    |
| [rife.render.Uptime](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Uptime)                     | Renders the server uptime in various customizable formats        |


## Text Renderers

| Renderer                                                                                                          | Description                                                           |
|:------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------|
| [rife.render.Capitalize](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Capitalize)           | Capitalizes a template value                                          |
| [rife.render.CapitalizeWords](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.CapitalizeWords) | Capitalizes words of a template value                                 |
| [rife.render.Lowercase](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Lowercase)             | Converts a template value to lowercase                                |
| [rife.render.Rot13](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Rot13)                     | Translates a template value to/from ROT13                             |
| [rife.render.SwapCase](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.SwapCase)               | Swaps case of a template value                                        |
| [rife.render.Trim](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Trim)                       | Removes leading and trailing whitespace from a template value         |
| [rife.render.Uncapitalize](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Uncapitalize)       | Uncapitalizes a template value                                        |
| [rife.render.Uppercase](https://github.com/rife2/rife2-template-renderers/wiki/rife.render.Uppercase)             | Converts a template value to uppercase                                |

## Documentation

Read more in the [documentation](https://github.com/rife2/rife2-template-renderers/wiki).
