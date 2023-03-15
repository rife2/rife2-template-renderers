[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

# [RIFE2](https://rife2.com/) Template Renderers

This project provides a set of template renderers.

- **rife.render.Capitalize**
  - Capitalizes a template value.
- **rife.render.DateIso**
  - Renders the current date in ISO 8061 format.
- **rife.render.DateTimeIso**
  - Renders the current date and time in ISO 8061 format.
- **rife.render.DateTimeRfc2822**
  - Renders the current date and time in RFC 2822 format.
- **rife.render.DateTimeUtc**
  - Renders the current UTC date and time in ISO 8061 format.
- **rife.render.EncodeBase64**
  - Encodes a template value to Base64.
- **rife.render.EncodeHtml**
  - Encodes a template value to HTML.
- **rife.render.EncodeHtmlEntities**
  - Encodes a template value to HTML decimal entities.
- **rife.render.EncodeJson**
  - Encodes a template value to JSON.
- **rife.render.EncodeQp**
  - Converts a template value to a quoted-printable string.
- **rife.render.EncodeUnicode**
  - Encodes a template value to Unicode escape codes.
- **rife.render.EncodeUrl**
  - URL-encodes a template value.
- **rife.render.EncodeXml**
  - Encodes a template value to XML.
- **rife.render.Lowercase**
  - Converts a template value to lowercase.
- **rife.render.Rot13**
  - Translates a template value to/from ROT13.
- **rife.render.SwapCase**
  - Swap case of a template value.
- **rife.render.TimeIso**
  - Renders the current time in ISO 8061 format.
- **rife.render.Trim**
  - Removes leading and trailing whitespace from a template value.
- **rife.render.Uncapitalize**
  - Un-capitalizes a template value.
- **rife.render.Uppercase**
  - Converts a template value to uppercase.
- **rife.render.Year**
  - Renders the current year.

## Usage in Templates

All rendered can be used at follows:

```
<!--v render:rife.render.RendererName:valueId/-->
```

or

```
{{v render:rife.render.RendererName:valueId/}}
```

For example, to capitalize a template `foo` value:

```
<!--v render:rife.render.Capitalize:foo/-->
```

or

```
{{v render:rife.render.Capitalize:foo}}
```
