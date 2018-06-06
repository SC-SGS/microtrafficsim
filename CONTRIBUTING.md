# Contributing to microtrafficsim

👍🎉 First off, thanks for your interest in contributing to this project! 🎉👍

The following is a set of guidelines for contributing to microtrafficsim.
Feel free to propose changes to this document in a pull request.


## Table of contents

[Code of Conduct](#code-of-conduct)

[Styleguides](#styleguides)
   * [Git Commit Messages](#git-commit-messages)
   * [Gitflow Workflow](#gitflow-workflow)
   * [Semantic Versioning](#semantic-versioning)

---

## Code of Conduct

This project and everyone participating in it is governed by the [Code of Conduct](CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code.


## Styleguides

### Git Commit Messages

These points make committing easier and clearer.
* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line up to 72 characters
* 2nd line is empty
* Consider using following flags in your commit messages:
    * `fix` when the commit contains bug fixes
    * `doc` when writing documentation
    * `test` when tests were changed/added
    * `style` when code or format style changes occur in the commit


### [Gitflow Workflow][website_gitflow_workflow]

This project uses [Gitflow Workflow][website_gitflow_workflow], a nice and clear way to work effectively.

This means we are using the following branches:
* `master`: the official release (using tags)
* `develop`: branch for active development
* `release/<tag>`: temporary branch off `develop` for bug fixes and docs before getting merged into `master`
* `feature/<name>`: branches for specific feature development
* `hotfix/<name>`: branches for bug fixes branched off `master`
* `fix/<name>`: branches for bug fixes branched off `develop`


### [Semantic Versioning][website_semantic_versioning]

"Given a version number `MAJOR.MINOR.PATCH`, increment the:
* `MAJOR` version when you make incompatible API changes,
* `MINOR` version when you add functionality in a backwards-compatible manner, and
* `PATCH` version when you make backwards-compatible bug fixes.
Additional labesl for pre-release and build metadata are available as extensions to the `MAJOR.MINOR.PATCH`."

For more information, see [the original site][website_semantic_versioning].




[website_gitflow_workflow]: https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow
[website_semantic_versioning]: https://semver.org
