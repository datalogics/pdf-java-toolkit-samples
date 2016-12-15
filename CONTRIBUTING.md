# Contributing

Contributions to the PDF Java Toolkit Samples project are welcome! We're setting high standards for this project, and so this document will help you create contributions that meet the standards of the project, and make pull request reviews go smoothly.

## Platform

Currently, all of the developers are using Macs. Some of the scripts assume the availability of Bash and Sed, but they're not tools you'd use everyday. Things should work on Windows, but that's not deeply tested.

## Recommended tools

You can use whatever editing tools or IDE you want, but Eclipse is strongly recommended. Eclipse will check your style as you go, and has settings that will help you conform to the coding style.

* Maven 3.3.9
* JDK 1.7
* Eclipse with [M2Eclipse](http://eclipse.org/m2e/) installed.
    * [JAutodoc](http://jautodoc.sourceforge.net/) also recommended. Makes writing Javadoc much easier.
    * [m2e-code-quality](http://m2e-code-quality.github.io/m2e-code-quality/). Installing from the marketplace doesn't work for now, so add the [m2e-code-quality site](http://m2e-code-quality.github.io/m2e-code-quality/site) in **Help**->**Install New Software…**.
        * Install only the following items:
            * **Checkstyle configuration plugin for M2Eclipse**. This will also install the Eclipse Checkstyle Plug-In automatically.
            * **FindBugs configuration plugin for M2Eclipse**. This will also install the FindBugs Feature automatically.
            * **PMD Configuration plugin for M2Eclipse**. This will also install the PMD plug-in automatically.
    * When writing tests, it's good to see if they cover the work you did. Check out [EclEmma](http://www.eclemma.org), which lets you test for coverage in Eclipse, based on the [JaCoCo](http://www.eclemma.org/jacoco/) library.
        * Note that EclEmma may [indicate a lack of coverage when an exception is thrown](http://www.eclemma.org/faq.html#trouble05). This is a known issue.

#### Installing the plugins all at once

Sometimes the above instructions don't work so well. Sometimes the m2e-code-quality has trouble resolving dependencies. In that case there's an alternative.

There is a file ``eclipse-plugins-neon.p2f`` in the main project director. It contains specifications for the following:

* JAutodoc
* m2e-code-quality
* EclEmma
* m2e connectors for the Maven Dependency Plugin and build-helper-maven-plugin (these usually could be installed with help from Eclipse)
* Supporting plugins for PMD, FindBugs and Checkstyle

To use it:

1. choose **File->Import...**, then open up the **Install** group, then choose the **Install Software Items from File**. Click **Next**.
2. Click **Browse...**, and navigate to the ``eclipse-plugins-neon.p2f`` file and select it.
3. Click **Contact all update sites during install to find required software**
4. Click **Next**
5. Review things and click **Next** again
6. Accept the licensing agreement and click **Finish**
7. Tell it that it's okay to install unsigned software.
8. Let it restart Eclipse.

This has been tested with Eclipse Neon.

## Supplied tools

### Checked exceptions

Because some of the underlying libraries throw checked exceptions, it becomes tedious to supply descriptions for them all. There is a script, ``fix_throws_javadoc.sh``, which changes all the ``@throws`` tags to use the canonical descriptions. Please use and extend this script as necessary.

## Coding style

Coding style for Java is based on the Google Code Style with some minor modifications. See the ``JAVA-CODING-STYLE.md`` document.

There are settings in Eclipse to help format the code to the style guidelines as you write code, but the definitive standard is the Checkstyle program.

### Checkstyle

Code formatting is checked with [Checkstyle](http://checkstyle.sourceforge.net). Code that is merged into the repo *must* conform to the checks done during the Maven build. The last word on style correctness is that the ``mvn validate`` phase completes successfully.

- Checkstyle issues warnings. You can see these in Eclipse if you use the Checkstyle plugin mentioned above. Because they're warnings, they won't cause problems building or running code or tests while your work is in progress.
- The Maven ``validate`` phase will **fail** if there are style errors, and Maven will not even continue to the compile phase.

### Exception documentation

Writing Javadoc for ``@throws`` is tedious and we end up with inconsistent messages. You'll note that we have ``fix_throws_javadoc.sh``, which calls ``fix_throws_javadoc.sed``.

Instead of using ad-hoc messages for ``@throws``, put in just the exception name, and do:

```bash
$ sh fix_throws_javadoc.sh
```

This should add standard messages for your ``@throws`` documentation tags. If you're using a heretofore unused exception, then add a good standard documentation snippet to ``fix_throws_javadoc.sed``.

## Commit messages

- See [A Note About Git Commit Messages](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html).
- Make sure the subject is concise, specific, and meaningful. Developers tend to read the list of subjects, not the commit bodies.
    - If it's necessary to read the body of the message, the subject isn't specific enough.
    - If it's necessary to read the code changes to understand what's going on, the message didn't explain enough.
- Small commits are totally okay. Almost preferred.

## Running tests

### Command line

* Use ``mvn clean verify`` to ensure that all of the unit tests and code quality tests run.
* Use ``mvn clean verify -P integration-tests`` to also include the integration tests.
    * At present, this includes a test that all the samples will (merely) run when their ``main`` is called with an empty argument array.

### IDE

(Using Eclipse as the example here)

Running all the tests from the package will likely result in a long test run, as all of the tests will run twice (once in a suite, once on their own) and the lengthy integration tests will run without a font cache.

It is recommended to run either of the test suites:

* ``AllUnitTests.java`` is likely the one you'll run most often. This suite runs just the unit tests.
* ``AllIntegrationTests.java`` will run the integration tests.

## Pull requests

- The destination branch of your pull request is usually ``develop``, but if there is extended work, make a feature branch.

## Reviews

Code reviews will be done by a developer who works on the project regularly. Expect the following kind of feedback from a code review.

- The code must build and pass tests. A reviewer and/or CI system will run ``mvn clean verify`` on the code, and it must build successfully.
   - This includes a Checkstyle pass. Any Checkstyle warnings will cause a failure in the ``validate`` phase, and you'll be asked to correct them.
   - The unit tests on both the code and the samples must pass.
   - The CI server will run integration tests. These include tests that each sample will run when its ``main`` method is run, with an empty argument array.
- There may be a review of the code coverage of the library and sample unit tests. Expect the reviewer to check that your new code is covered by some executing code somewhere.
    - Coverage need not be absolute.
    - We're not using any kind of tools to enforce coverage.
    - But expect that if you write a function, something in the tests should at least be calling it.
