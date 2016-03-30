#### Changes in this pull request

- 
- 
- 

#### Fulfills  [EXAMPLE-1](https://jira.datalogics.com/browse/EXAMPLE-1)

#### Checklist for approving this pull request

(PR creator amend this with more conditions if necessary)

- [ ] **Maven version is set to the next minor or major version** if appropriate according to [Semantic Versioning](http://semver.org). You can check how the version differs from the previous release with: ``git fetch upstream && git diff --color-words='[^<>[:space:]]+'  upstream/master pom.xml | grep --color=never '<version>' | head -1``
- [ ] **Release notes** file has been updated as necessary. Include only _publically visible changes_. Set the first version heading to the ``-SNAPSHOT`` version _with no date_.
- [ ] There are **unit tests** for new/changed code, or a good explanation why this was not possible.
- [ ] All **CI builders** have indicated success (Give them a few minutes to notice the pull request.)
- [ ] The **Pull request title** has the JIRA issue numbers separated by spaces (if any), a space, and then a short, but descriptive summary.
- [ ] **Commit messages** are well formed: [A note about Git commit messages](http://www.tpope.net/node/106)
- [ ] New public packages, classes, and methods are **documented**. (Strongly consider documenting private classes and methods.)

#### Blockers

Add a checkbox for yourself with your **&#64;name** to this list if you are holding this PR open for review. PR Shepherd, please hold off merging this PR until these are all checked:

- [x] _&lt;add your name here with an **@**>_
