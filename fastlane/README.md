fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android verify

```sh
[bundle exec] fastlane android verify
```

Test with gradle

### android build

```sh
[bundle exec] fastlane android build
```

Prepare release builds

### android submit_beta

```sh
[bundle exec] fastlane android submit_beta
```

Submit a new Beta Build to Google Play

### android submit_prod

```sh
[bundle exec] fastlane android submit_prod
```

Submit a new Prod Build to Google Play

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Deploy builds to Google Play

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
