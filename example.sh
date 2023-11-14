#!/bin/sh
set -e

# This example assumes that you build the native executable. If you didn't, please
# run `mvnd clean package -Pnative -DskipTests` first.
if [ ! -x "target/hub" ]; then
  echo "hub binary not found" >&2
  exit 1
fi

function hub() {
  target/hub $@
}

export HUB_CLI_API_BASE=http://localhost:8080/api
export HUB_CLI_CLIENT_ID=cryptomatorhub-cli
export HUB_CLI_CLIENT_SECRET=top-secret
export HUB_CLI_ACCESS_TOKEN=$(hub login client-credentials)

export HUB_CLI_P12_FILE=cli.p12
export HUB_CLI_P12_PASSWORD=test
if [ ! -f ${HUB_CLI_P12_FILE} ]; then
  HUB_CLI_SETUP_CODE=$(hub setup)
  echo "setup code: ${HUB_CLI_SETUP_CODE}"
fi

hub list-users
hub list-groups
hub list-vaults

# hub create-vault --name=test --path=.
VAULT_ID="d86d09b2-1480-4f1e-9a61-c2a04de95042" #$(read -p "enter vault id: ")
# hub get-recoverykey --vault-id=${VAULT_ID}
hub update-vault --vault-id=${VAULT_ID} --description="updated!"

USER_ID="30790a56-d9e3-4b01-9b0d-dba93b38df9e" #$(read -p "enter admin user id: ")
hub add-user --vault-id=${VAULT_ID} --user-id=${USER_ID} --role=OWNER