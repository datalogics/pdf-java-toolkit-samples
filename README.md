# Datalogics PDF Java Toolkit Sample Repository

This repository contains samples for use with [Datalogics PDF Java Toolkit](http://www.datalogics.com/products/pdf/pdfjavatoolkit/).

Please note that even though the samples are MIT licensed, you still require a license from Datalogics for PDF Java Toolkit in order to run them. Please sign up for an evaluation [here](http://www.datalogics.com/products/pdf/pdfjavatoolkit/eval/) or [contact us](http://www.datalogics.com/company/contact-us/) to learn more before evaluating the product.

## Requirements

* Java SE 1.7
* Maven 3.3.9

## Using with an evaluation version of PDF Java Toolkit

The evaluation version of PDF Java Toolkit has license management, and a different artifact name: ``pdfjt-lm``. There's also a corresponding ``talkeetna-lm`` which similarly depends on ``pdfjt-lm``. Switching to use these versions of PDF Java Toolkit and Talkeetna is provided with Maven profiles.

### License file

Evaluation copies will come with a license file, with a name ending in ``.l4j``.

Move your license file to the top-level directory of the samples, so that it is in the current directory when running samples.

To use license-managed PDFJT, create a file called ``.use-pdfjt-lm`` in the top directory of this project. This will activate profiles automatically for Maven and Eclipse.

If this project was received as part of an evaluation, then the ``.use-pdfjt-lm`` file is already created.

## Tested IDEs

The samples were developed and tested using [Mars 4.5.0](https://eclipse.org/mars/)

## Using remote input PDF files with the samples

By default, the samples run with pre-packaged input PDF files that are treated as resources of the samples. However, you can use your own input PDF files, including those that are stored remotely on a shared network with the samples. As long as the shared network where the PDF files reside is mounted on your local machine it's treated a part of your local file system.

For more information on working with files in java please see [file path java tutorial](http://www.java2s.com/Tutorial/Java/0180__File/UniversalNamingConvention.htm).

## Contributing

See ``CONTRIBUTING.md``
