# hub-cli

Cryptomator Hub CLI is a command line tool for managing Cryptomator Hub.

## Sponsors

Cryptomator Hub CLI is sponsored by

<table>
  <tbody>
    <tr>
      <td><a href="https://poverty-action.org/"><img src="" alt="Innovations for Poverty Action" height="80" class=""></a></td>
    </tr>
  </tbody>
</table>

## Setup

The following steps must be performed in Cryptomator Hub (Keycloak) if it was not created with the Cryptomator Hub CLI option:

1. Create a Cryptomator Hub CLI client with the following configuration [cryptomatorhub-cli.json](https://github.com/cryptomator/hub-cli/files/13362616/cryptomatorhub-cli.json)
2. Add the `view-clients` permission to the `syncer` user

## Usage

```
Usage: hub [-hV] [COMMAND]
Manage Cryptomator Hub instances via CLI.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  login  Login to the hub instance and retrieve an access token.
  setup  Initialize key pairs and registers with Hub. Prints this user's setup
           code to STDOUT on success.
  vault  Manage vaults.
  group  Manage groups.
  user   Manage users.
```

## Installation

Download native binaries of Cryptomator Hub CLI from https://github.com/cryptomator/hub-cli/releases or clone and build Cryptomator Hub CLI using Maven (instructions below).

## Shell completion

Cryptomator Hub CLI supports shell auto-completion for bash and ZSH. To archive this, download the `HubCli_completion.sh` file from releases and apply it by sourcing, for example.

See https://picocli.info/man/3.x/autocomplete.html#_install_completion_script for more options.

## Building

### Dependencies

* GraalVM JDK 21
* Maven 3

### Run Maven

```
mvn clean install -Pnative
```

This will build an executable (`hub`) under `target`.

