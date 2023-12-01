#!/bin/sh
set -e

# This example assumes that you build the native executable. If you didn't, please
# run `mvnd clean package -Pnative -DskipTests` first.
if [ ! -x "target/hub" ]; then
  echo "hub binary not found" >&2
  exit 1
fi

function hub() {
  target/hub "$@"
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

if [ ! -e vault ]; then
  VAULT_ID=$(hub vault create --name=vault --path=.)
  echo -n "created vault ${VAULT_ID} with recovery key: "
  hub vault recoverykey --vault-id=${VAULT_ID}
else
  hub vault list | jq .
  read -p "enter vault id: " VAULT_ID
fi
hub vault update --vault-id=${VAULT_ID} --description="updated by hub-cli at $(date "+%Y-%m-%d %H:%M:%S")"

echo "this is your admin's id:"
hub user list | jq --raw-output  '.[] | select(.name=="admin") | .id'

if [[ -n "${USER_ID}" ]]; then
  hub vault add-user --vault-id=${VAULT_ID} --user-id=${USER_ID} --role=OWNER
  exit 0;
elif [[ -n "${GROUP_ID}" ]]; then
  hub vault add-group --vault-id=${VAULT_ID} --group-id=${GROUP_ID} --role=OWNER
  exit 0;
fi

while true; do
  CHOICE="x"
  read -p "add [u]ser or [g]roup as owner or [r]emove user/group? " CHOICE
  if [[ "${CHOICE}" == "u" ]]; then
    echo -n "users: "
    hub user list | jq .
    read -p "enter user id: " USER_ID
    hub vault add-user --vault-id=${VAULT_ID} --user-id=${USER_ID} --role=OWNER
  elif [[ "${CHOICE}" == "g" ]]; then
    echo -n "groups: "
    hub group list | jq .
    read -p "enter group id: " GROUP_ID
    hub vault add-group --vault-id=${VAULT_ID} --group-id=${GROUP_ID} --role=OWNER
  elif [[ "${CHOICE}" == "r" ]]; then
    echo -n "groups: "
    hub group list | jq .
    echo -n "users: "
    hub user list | jq .
    read -p "enter user or group id: " AUTHORITY_ID
    hub vault remove-member --vault-id=${VAULT_ID} --member-id=${AUTHORITY_ID}
  else
    break
  fi
done