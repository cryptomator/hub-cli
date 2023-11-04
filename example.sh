#!/bin/sh
set -e

# This example assumes that you build the native executable. If you didn't, please
# run `mvnd clean package -Pnative -DskipTests` first.
if [ ! -x "target/classes/hub" ]; then
  echo "hub binary not found" >&2
  exit 1
fi

function hub() {
  target/classes/hub $@
}

export HUB_CLI_API_BASE=http://localhost:8080/api
export HUB_CLI_CLIENT_ID=cli
export HUB_CLI_CLIENT_SECRET=test
export HUB_CLI_ACCESS_TOKEN=$(hub login client-credentials)
HUB_CLI_SETUP_CODE=$(hub setup --p12-file=cli.p12 --p12-password=test)

echo $HUB_CLI_SETUP_CODE