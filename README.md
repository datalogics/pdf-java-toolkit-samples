# Datalogics PDF Java Toolkit Sample repository

This repository contains samples for use with [Datalogics PDF Java Toolkit](http://www.datalogics.com/products/pdf/pdfjavatoolkit/).

Please note that even though the samples are MIT licensed, you still require
a license from Datalogics for PDF Java Toolkit in order to run these samples. Please
sign up for an evaluation [here](http://www.datalogics.com/products/pdf/pdfjavatoolkit/eval/)
or [contact us](http://www.datalogics.com/company/contact-us/) to learn more before
evaluating Datalogics PDF Java Toolkit.

## Requirements

* Java SE 1.7
* Maven 3.3.9

## Using with an evaluation version of PDFJT

The evaluation version of PDFJT has license management, and a different artifact name: ``pdfjt-lm``. Switching to use this version of PDFJT is provided with Maven profiles.

### License file

Evaluation copies will come with a license file, with a name ending in ``.l4j``.

You'll have to place your license file in the top-level directory of the samples, so that it is in the current directory when running samples.

### Maven

To use license-managed PDFJT, specify ``-Dpdfjt-lm`` on the command line. This will automatically activate and deactivate the correct profiles:

```
$ mvn verify -Dpdfjt-lm
```

### Eclipse

In the project properties for **pdf-java-toolkit-samples**, select the **Maven** settings, and under **Active Maven Profiles (comma-separated):** enter ``pdfjt-lm,!pdfjt-non-lm``.

It will ask if you want to update the product configuration. Click **Yes**.

The samples were developed and tested using [Mars 4.5.0](https://eclipse.org/mars/)

## Contributing

See ``CONTRIBUTING.md``
