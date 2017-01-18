# Datalogics PDF Java Toolkit Sample repository

This repository contains samples for use with [Datalogics PDF Java Toolkit](http://www.datalogics.com/products/pdf/pdfjavatoolkit/).

Please note that even though the samples are MIT licensed, you still require
a license from Datalogics for PDF Java Toolkit in order to run these samples. Please
sign up for an evaluation [here](http://www.datalogics.com/products/pdf/pdfjavatoolkit/eval/)
or [contact us](http://www.datalogics.com/company/contact-us/) to learn more before
evaluating Datalogics PDF Java Toolkit.

## Requirements

* Java SE 1.7

## Using with an evaluation version of PDFJT

The evaluation version of PDF Java Toolkit has license management, and a different artifact name: ``pdfjt-lm``. There's also a corresponding ``talkeetna-lm`` which similarly depends on ``pdfjt-lm``. Switching to use these versions of PDF Java Toolkit and Talkeetna is provided with Maven profiles.

### License file

Evaluation copies will come with a license file, with a name ending in ``.l4j``.

Move the license file to the top-level directory of the samples, so that it is in the current directory when running samples.

To use license-managed PDFJT, create a file called ``.use-pdfjt-lm`` in the top directory of this project. This will activate profiles automatically for Maven and Eclipse.

If this project was received as part of an evaluation, then the ``.use-pdfjt-lm`` file is already created.

## Building and running from the command line

This project includes [maven-wrapper](https://github.com/datalogics/maven-wrapper), which fully encapsulates the build system required for PDF Java Toolkit. Your system only requires a Java installation to build and run the samples.

* To build with Maven, run ``./mvnw verify`` in the 'samples' folder (``.\mvnw verify`` on Windows).
  * This will create a ``target`` folder with a set of build artifacts, including the ``pdf-java-toolkit-samples-lite-0.0.1-SNAPSHOT.jar`` file containing all the samples.
* To run a sample, specify the JAR file and the fully qualified name of the sample to run. For example, to run the ``HelloWorld`` sample:
  * ``java -cp target/pdf-java-toolkit-samples-lite-0.0.1-SNAPSHOT.jar com.datalogics.pdf.samples.creation.HelloWorld``

## Tested IDEs

The samples were developed and tested using [Neon 4.6.2](https://eclipse.org/neon/)

## Using remote input PDF files with the samples

By default, the samples run with pre-packaged input PDF files that are treated as resources of the samples. However, you can use your own input PDF files 
including those that are stored remotely on a shared network with the samples. As long as the shared network where the PDF files reside is mounted on your local machine it's treated a part of your local file system.

For more information on working with files in java please see [file path java tutorial](http://www.java2s.com/Tutorial/Java/0180__File/UniversalNamingConvention.htm).

## Contributing

See ``CONTRIBUTING.md``
