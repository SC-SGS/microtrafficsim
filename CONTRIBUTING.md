# Contributing to microtrafficsim

👍🎉 First off, thanks for your interest in contributing to this project! 🎉👍

The following is a set of (style) guidelines for contributing to microtrafficsim.
Feel free to propose changes to this document in a pull request.


## Table of contents

[Code of Conduct](#code-of-conduct)

[Git Commit Messages](#git-commit-messages)

[Gitflow Workflow](#gitflow-workflow)

[Releases](#releases)

* [Release Checklist](#release-checklist)

* [Semantic Versioning](#semantic-versioning)

* [Release for macOS](#release-for-macos)

[Java](#java)

* [File Style](#file-style)

* [Coding Conventions](#coding-conventions)

* [Documentation](#documentation)

* [Project Conventions](#project-conventions)

---

## Code of Conduct

This project and everyone participating in it is governed by the [Code of Conduct](CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code.


## [Git Commit Messages][website_git_commit_messages]

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


## [Gitflow Workflow][website_gitflow_workflow]

This project uses [Gitflow Workflow][website_gitflow_workflow], a nice and clear way to work effectively.

This means we are using the following branches:
* `master`: the official release (using tags)
* `develop`: branch for active development
* `release/<tag>`: temporary branch off `develop` for bug fixes and docs before getting merged into `master`
* `feature/<name>`: branches for specific feature development
* `hotfix/<name>`: branches for bug fixes branched off `master`
* `fix/<name>`: branches for bug fixes branched off `develop`


## Releases

You can copy and use the following template for new releases.
In general, needed information should be optained from commit messages.
```
## Downloads

[Download for Windows](download_link_windows)
[Download as App for macOS](download_link_macOS)
[Download as executable jar](download_link_jar)
Download for Linux and others: see below


## General info

<optional>


## New features

<Short and interesting description about new features of this release.>


## Bug fixes

<Detailed description about fixed bugs of this release.>


[download_link_windows]: <download link>
[download_link_macOS]: <download link>
[download_link_jar]: <download link>

```


### Release Checklist

Please check the following points for a new release.
* Is the distribution and release version correct in [build.gradle](build.gradle) files?
* Are all tests passing?
* Has `release` been merged into `master`?
* Is a new teaser picture needed/recommended?
* Are all distribution files added?
    * `./gradlew :microtrafficsim-ui:distAll` creates the distribution files, which then can be found in `microtrafficsim-ui/build/distributions`.
    * the `executable jar file` can be found in `microtrafficsim-ui/build/libs`
    * It can help to call `./gradlew clean` before executing the build and dist commands.


### [Semantic Versioning][website_semantic_versioning]

"Given a version number `MAJOR.MINOR.PATCH`, increment the:
* `MAJOR` version when you make incompatible API changes,
* `MINOR` version when you add functionality in a backwards-compatible manner, and
* `PATCH` version when you make backwards-compatible bug fixes.
Additional labesl for pre-release and build metadata are available as extensions to the `MAJOR.MINOR.PATCH`."

For more information, see [the original site][website_semantic_versioning].


### Release for macOS

In case you are releasing for macOS, the current version of this simulation is only working with Java 8 (tested with `1.8.0_172`).
For correct building, the following code lines should be set
```gradle
bundleJRE = true
jreHome = "${System.env.JAVA_HOME}"
```
in `microtrafficsim-ui/build.gradle`.
Hence you have to set your `JAVA_HOME` to a correct `JDK`.
This setup is tested with `macOS 10.13.4`.
If you don't do this, the main-ui could probably start but bugs like deadlock behaviour when loading a new map may occur.
Setting `bundleJRE = false` follows into errors when multiple `JDKs` are installed.
To make the life of end users easier, just bundle the `JRE` as described above.


## Java

The following conventions and suggestions should be followed.
They help a lot keeping overview and the code clear.


### File Style

* Maximum line width is `100`.

  _This is a good trade off between `120` and `80`.
  Humans have trouble reading the code with increasing line width.
  In general, more than `80` is not recommended, but Java is a very verbose language._

* Use `4 spaces` for indention (p.s.: [could help your salary](https://stackoverflow.blog/2017/06/15/developers-use-spaces-make-money-use-tabs)!).


### Coding Conventions

* Make __visibility as closest__ as possible.

  _Usually, you tend to not bother with visibility, but visibility helps a lot with getting nice and persistent interfaces._

* __Use `getter`/`setter`__ instead of direct access, even for private usage.

  _This is unhandy in Java, but important for maintenance.
  Changing the implementation of a class should tend to make no difference for users of this class.
  Furthermore, debugging with breakpoints is much more easier when you only have to make one breakpoint instead of many at different positions.
  Same argument counts for synchronization code snippets.
  [The code is getting inlined](https://stackoverflow.com/questions/23931546/java-getter-and-setter-faster-than-direct-access)._

  `getter` starts with `get`, `setter` starts with `set`.
  Those `getter` returning a boolean expression may start differently, but with a verb.
  Corresponding fields are named like adjectives/states.
  Field names adapt to their corresponding `getter`/`setter`, not the other way around.
  ```diff
  - count()
  + getCount()

  - boolean isRunning = false;
  + boolean running = true;
  + boolean isRunning() { return running; }
  ```

* Use white spaces around binary operators.
  Exceptions can be made for special cases to improve readability (see below).

  ```diff
  - int e = a*b;
  + int e = a * b;
  - int e = a * b + c * d;
  + int e = a*b + c*d;
  ```

* Use control structures with `curly brackets` and the keyword `else` after the closing bracket for nice commenting.

  _Using control structures without `curly brackets` are easy to write, but usually very uncomfortable to read (especially inside other control structures).
  Most of the time code is read, not written, so `curly brackets` should be used._

  ```java
  // BAD: may confuse
  for (int i = 0; i < n; i++)
      for (int j = 0; j < n; j++)
          // Are these comment lines ignored?
          // Can't remember without my IDE...
          if (isRunning)
              doSomething();
          else
              doSomethingElse();
          doAnything(); // NOT in the loop, but seems to be due to wrong indention


  // GOOD: clear and easy to read
  for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
          // no problem with comments
          if (isRunning) {
              doSomething();
          }
          // `else` after closing bracket for nice commenting
          else {
              doSomethingElse();
          }
      }
  }
  doAnything();
  ```


### Documentation

* Separate class sections with `/*****/` (whole line).
  Take the following code snippet for inspiration.
  ```java
  public class Vehicle {
    private Color color;

  /********************************************************/
    // group 0, e.g. constructors and factory methods

    public Vehicle(Color color) {
      this.color = color;
    }

    public static Vehicle getRedVehicle() {
      return new Vehicle(Color.RED);
    }

  /********************************************************/
    // group 1, e.g. getter/setter

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

  /********************************************************/
    // group 2, e.g. Nagel-Schreckenberg-Model

    public void accelerate() {
      // ...
    }

    public void brake() {
      // ...
    }

    public void dawdle() {
      // ...
    }

    public void move() {
      // ...
    }

  /********************************************************/
    // group 3 (e.g. private classes)
  }
  ```

* Use annotations where expected (e.g. `@Override`).


### Project Conventions

* Prefer package/folder/file management over class mangement if `meaningful`.  
  __BUT:__ Think in an intuitive, handy and `deterministic`(!) way and don't take structuring and subfolding too far.

  Always ask yourself:  
  `How would most of the people search for this class?`  
  Someone without knowing your whole project structure should be able to find a file at the first try.  
  `In every folder, there should be only one option to continue searching (-> determinism).`

  _Take a math API for instance.
  It is okay to put basic classes like vector and matrix classes in __ONE__ package called `math`, because that's what it is.
  Someone searching for these classes will find them easily in this package even if there are a lot more classes in it.
  Think of creating a subfolder for your matrices and another subfolder for graph data structures.
  Is a matrix describing graph structures belonging to the matrix or the graph folder?
  It would be annoying to look for such a file in such a folder structure due to non-intuitive subfoldering._



[website_git_commit_messages]: https://chris.beams.io/posts/git-commit
[website_gitflow_workflow]: https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow
[website_semantic_versioning]: https://semver.org
