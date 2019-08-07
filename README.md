# Project description

This project aims at creating a minimalistic Spring-Boot application meant to interact with Node
in order to transform into pdf any webpage.

The objective is to be able to run reporting against well-known technologies like HTML, JavaScript and CSS
as opposed to solutions relying on less common and less known technologies.

# Runtime Requirements

You must have a relatively recent version of Node.js installed on your machine. Download it from https://nodejs.org/

You need Maven 3.6 or later installed on your machine. Download it from https://maven.apache.org/

# Run instructions

```
mvn spring-boot:run
```

# Other information

- The application is configured by default to run against port 8081.
To try it out, use something like Postman to send a POST request to:
http://localhost:8081/pdfgen/generate
The POST request must have a JSON Payload similar to the following:
```
{
    "urls": [
        "http://localhost:8081/pdfgen/static/multipage.html",
        "http://localhost:8081/pdfgen/static/waitable.html",
        "https://www.google.ie"
    ],
    "footer": {
        "templateText": "Page ${page} of ${pages}",
        "fontSize": 0.3,
        "bottomMargin": 0.5,
        "leftMargin": 0.5,
        "rightMargin": 0.5
    }
}
```

- The values of `fontSize`, `bottomMargin`, `leftMargin` and `rightMargin` are in Centimeters.
- The `waitable.html` page example introduces a very long delay. This is intentional for that particular page. Remove it to see speedier results.
