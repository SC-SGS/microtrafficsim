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

### [Git Commit Messages][website_git_commit_messages]

Commit messages are a _hugely_ important part of working with git.
They not only help other workers to understand your changes quickly, they also are the basement for new releases and their release notes.
Thus, you really should account these following rules [cited from here][website_git_commit_messages] in your commits.

First of all, commits should be `atomic`.
One commit should contain only changes of a few lines of code (or one single feature).
This method seems to be too verbose and kind of annoying, but when working with Git logs (`git log`) or GitHub's network tree, this is a huge advantage.
Branch management, releases and especially finding bugs is way easier with small commit messages.

Some points about the commit message style:
* Separate `subject` from `body` with a blank line.
  The body explains _what_ has changed and _why_, not _how_ it has changed.
  _How_ can be checked by looking at the commit changes itself.
* Line widths
    * 1st line (`subject`) up to 50 characters
    * 2nd line empty
    * Remaining (`body`) lines up to 72 characters
* Capitalize the `subject` line
  ```diff
  - fix typo ...
  + Fix typo ...
  ```
* Do not end the `subject` line with a period
  ```diff
  - Refactor brackets of some if-statements.
  + Refactor brackets of some if-statements
  ```
* Use the present tense  
  ```diff
  - Added feature
  + Add feature
  ```
* Use the imperative mood, no other language styles (no description either)  
  ```diff
  - Moves cursor to ...
  - Fixed bug ...
  - Sweet new API methods ...
  + Move cursor to ...
  + Fix bug ...
  + Add new API methods for ...
  ```
In summary, a properly formed Git commit subject line should always be able to complete the following sentence:  
  `If applied, this commit will <your subject line>`

Consider using following verbs/flags in your commit messages:
* `fix` when the commit contains bug fixes
* `doc(s)` when writing documentation
* `test(s)` when tests were changed/added
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




[website_git_commit_messages]: https://chris.beams.io/posts/git-commit
[website_gitflow_workflow]: https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow
[website_semantic_versioning]: https://semver.org
