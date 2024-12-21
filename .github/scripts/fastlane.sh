#!/bin/bash
set -e

export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8

# Save and decrypt keystore
echo "$KEYSTORE" > appstore.keystore.asc
gpg -d --passphrase "$KEYSTORE_PASSPHRASE" --batch appstore.keystore.asc > app/appstore.keystore

# Save and decrypt keystore properties
echo "$KEYSTORE_PROPERTIES" > keystore.properties.asc
gpg -d --passphrase "$KEYSTORE_PROPERTIES_PASSPHRASE" --batch keystore.properties.asc > app/.config/keystore.properties

# Save and decrypt secrets properties
echo "$SECRETS_PROPERTIES" > secrets.properties.asc
gpg -d --passphrase "$SECRETS_PROPERTIES_PASSPHRASE" --batch secrets.properties.asc > app/.config/secrets.properties

# Save and decrypt Google services
echo "$GOOGLE_SERVICES" > google-services.json.asc
gpg -d --passphrase "$GOOGLE_SERVICES_PASSPHRASE" --batch google-services.json.asc > app/google-services.json

# Save and decrypt PlayStore services
echo "$PLAYSTORE_SERVICE_ACCOUNT" > playstore-service-account.json.asc
gpg -d --passphrase "$PLAYSTORE_SERVICE_ACCOUNT_PASSPHRASE" --batch playstore-service-account.json.asc > app/playstore-service-account.json

bundle exec fastlane deploy
