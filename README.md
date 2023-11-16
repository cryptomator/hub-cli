# hub-cli

Cryptomator Hub CLI is a command line tool for managing Cryptomator Hub.

## Sponsors

Cryptomator Hub CLI is sponsored by

<table>
  <tbody>
    <tr>
      <td><a href="https://poverty-action.org/"><img src="https://poverty-action.org/themes/custom/ipa/assets/imgs/ipa-logo.svg" alt="Innovations for Poverty Action" height="80" class=""></a></td>
    </tr>
  </tbody>
</table>

## Setup

If Keycloak of your Cryptomator Hub instance was not created with the Cryptomator Hub CLI option, you need to:
1. Create new a client by importing the following config: [cryptomatorhub-cli.json](https://github.com/cryptomator/hub-cli/files/13366389/cryptomatorhub-cli.json)
2. Export the secret of the imported client to the system where you intend to use hub-cli
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

The complete list of commands:
```
hub
├─ login
│  ├─ client-credentials
│  ├─ authorization-code
├─ setup
├─ vault
│  ├─ create
│  ├─ update
│  ├─ add-user
│  ├─ add-group
│  ├─ remove-user / remove-group
│  ├─ recoverykey
├─ user
└─ group
```

## Installation

Download native binaries of Cryptomator Hub CLI from https://github.com/cryptomator/hub-cli/releases or clone and build Cryptomator Hub CLI using Maven (instructions below).

## Shell completion

Cryptomator Hub CLI supports shell auto-completion for bash and ZSH. To archive this, download the `HubCli_completion.sh` file from releases and apply it by sourcing, for example.

See https://picocli.info/man/3.x/autocomplete.html#_install_completion_script for more options.

## Building

### Prerequisites

* GraalVM JDK 21
* Maven 3
* System toolchain (see [GraalVM docs](https://www.graalvm.org/latest/reference-manual/native-image/#prerequisites))

### Run Maven

```
mvn clean install -Pnative
```

This will build an executable (`hub`) under `target`.

