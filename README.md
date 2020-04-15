Prerequisites: mvn and JDK 1.8 installed.

Running spring project: 
``` mvn spring-boot:run ```

REST API description:
POST /submit - submit an xml with pages. Submitted pages are stored in-memory using title as a key. Example:
``` curl --location --request POST 'localhost:9090/submit' \
--header 'Content-Type: application/xml' \
--header 'Accept: application/xml' \
--data-raw '<?xml version="1.0" encoding="UTF-8" ?>
<mediawiki xmlns="http://www.mediawiki.org/xml/export-0.10/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.10/ http://www.mediawiki.org/xml/export-0.10.xsd" version="0.10" xml:lang="en">
<page>
  <title>AccessibleComputing</title>
  <ns>0</ns>
  <id>10</id>
  <redirect title="Computer accessibility" />
  <revision>
    <id>854851586</id>
    <parentid>834079434</parentid>
    <timestamp>2018-08-14T06:47:24Z</timestamp>
    <contributor>
      <username>Godsy</username>
      <id>23257138</id>
    </contributor>
    <comment>remove from category for seeking instructions on rcats</comment>
    <model>wikitext</model>
    <format>text/x-wiki</format>
    <text xml:space="preserve">#REDIRECT [[Computer accessibility]]

{{R from move}}
{{R from CamelCase}}
{{R unprintworthy}} Accessible computing</text>
    <sha1>42l0cvblwtb4nnupxm6wo000d27t6kf</sha1>
  </revision>
</page>
</mediawiki>
' 
```
Example with data from file:
```
curl --location --request POST 'localhost:9090/submit' \
--header 'Content-Type: application/xml' \
--header 'Accept: application/xml' \
--data-binary file.xml
```

GET {title}/context - get the context of a page with {title}. Example: 
```
  curl --location --request GET 'localhost:9090/AccessibleComputing/context'
```
GET {title}/structure - get the structure(sections, subsections) of a page.

GET {title}/links - get the links of a page.
