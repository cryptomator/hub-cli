# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

The changelog starts with the version 2.0.0.0.
Changes to prior versions can be found on the [GitHub release page](https://github.com/cryptomator/hub-cli/releases).

## [2.0.0.0](https://github.com/cryptomator/hub-cli/compare/1.3.2.0...HEAD) (2026-10-13)

### Changed
* Send the device ID (`Hub-Device-ID` header) to Hub when retrieving a vault key ([#20](https://github.com/cryptomator/hub-cli/pull/20))
* Building from source requires GraalVM JDK 25 instead of 21 ([#31](https://github.com/cryptomator/hub-cli/pull/31))
* Updated dependencies ([#22](https://github.com/cryptomator/hub-cli/pull/22)):
  * `io.github.coffeelibs:tiny-oauth2-client` from 0.8.0 to 0.8.1
  * `com.fasterxml.jackson.core:jackson-databind` from 2.16.1 to 2.21.7
  * `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` from 2.16.1 to 2.21.7
  * `org.cryptomator:cryptolib` from 2.1.2 to 2.3.0
  * `com.nimbusds:nimbus-jose-jwt` from 9.37.3 to 10.10
  * `org.slf4j:slf4j-api` from 2.0.12 to 2.0.20
  * `org.slf4j:slf4j-jdk14` from 2.0.12 to 2.0.20
  * `info.picocli:picocli` from 4.7.5 to 4.7.7

### Fixed
* `vault create` and `vault update` failed on Hub 2.0 and later, because emergency key shares were not sent; `vault update` keeps an existing emergency access configuration unchanged (b242db5052bd724bf880c64fc8c2bfbf2f967b4d)
