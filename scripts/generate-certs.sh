#!/usr/bin/env bash
#
# Generates a self-signed plugin signing certificate + key, following
# https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
#
# Output goes to <repo>/build/certificates/, which is inside the already
# git-ignored build/ directory, so none of the generated files can be
# committed by accident.
#
# NOTE: running `./gradlew clean` deletes build/ — copy the generated files
# out (or keep them only as a source for the GitHub secrets) if you need them
# to survive a clean. The durable artifacts are the secrets, not these files.
#
# Usage:
#   bash scripts/generate-certs.sh [DAYS] [SUBJECT]
#
#   DAYS    certificate validity in days (default: 1825 = 5 years)
#   SUBJECT X.509 subject (default: "/CN=Piece of Code")
#
# Environment overrides:
#   CERT_PASSPHRASE   passphrase for the private key. If unset the script
#                     prompts interactively (confirms once). Setting it makes
#                     the run fully non-interactive (e.g. for CI).
#   CERT_SUBJECT      alternative to the SUBJECT positional argument.
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="$REPO_ROOT/build/certificates"
DAYS="${1:-1825}"
SUBJECT="${2:-${CERT_SUBJECT:-/CN=Piece of Code}}"

mkdir -p "$OUT_DIR"
cd "$OUT_DIR"

# Never silently overwrite an existing private key / certificate.
if [[ -f private.pem || -f chain.crt ]]; then
  echo "Certificate files already exist in $OUT_DIR." >&2
  echo "Move them away (or delete them) first, then re-run." >&2
  exit 1
fi

# Resolve the passphrase.
if [[ -z "${CERT_PASSPHRASE:-}" ]]; then
  if [[ ! -t 0 ]]; then
    echo "No TTY available and CERT_PASSPHRASE is not set." >&2
    echo "Set CERT_PASSPHRASE to run non-interactively." >&2
    exit 1
  fi
  read -rsp "Passphrase for the private key: " PASSPHRASE
  echo
  read -rsp "Confirm passphrase: " PASSPHRASE_CONFIRM
  echo
  if [[ "$PASSPHRASE" != "$PASSPHRASE_CONFIRM" ]]; then
    echo "Passphrases do not match." >&2
    exit 1
  fi
  if (( ${#PASSPHRASE} < 4 )); then
    echo "Passphrase must be at least 4 characters." >&2
    exit 1
  fi
  export CERT_PASSPHRASE="$PASSPHRASE"
  unset PASSPHRASE PASSPHRASE_CONFIRM
fi

echo "Output directory: $OUT_DIR"
echo

# 1. Generate an encrypted RSA key in the traditional PEM form IPGP's
#    signPlugin expects. (OpenSSL 3.5 no longer accepts the doc's genpkey
#    -aes-256-cbc / -passout syntax; genrsa -traditional produces the same
#    key the doc's genpkey + openssl rsa two-step intended, in one step, and
#    -aes256 keeps it passphrase-protected so PRIVATE_KEY_PASSWORD matters.)
openssl genrsa \
  -aes256 \
  -traditional \
  -passout env:CERT_PASSPHRASE \
  -out private.pem \
  4096

# 2. Generate the self-signed certificate chain with the requested validity.
#    MSYS_NO_PATHCONV stops Git Bash from rewriting the leading "/" of
#    -subj into a Windows path (no-op on Linux/macOS).
MSYS_NO_PATHCONV=1 openssl req \
  -key private.pem \
  -new \
  -x509 \
  -days "$DAYS" \
  -subj "$SUBJECT" \
  -passin env:CERT_PASSPHRASE \
  -out chain.crt

echo
echo "Generated:"
ls -1 private.pem chain.crt
echo

echo "Validity of chain.crt:"
openssl x509 -in chain.crt -noout -subject -dates
echo

# Encode the two files GitHub secrets need (single-line Base64; IPGP's
# signPlugin auto-decodes values before use).
base64 -w 0 private.pem > PRIVATE_KEY.b64
base64 -w 0 chain.crt   > CERTIFICATE_CHAIN.b64

echo "Next steps:"
echo "  1. Paste the contents of $(pwd)/PRIVATE_KEY.b64 into the JETBRAINS_PLUGINS_PRIVATE_KEY organization secret."
echo "  2. Paste the contents of $(pwd)/CERTIFICATE_CHAIN.b64 into the JETBRAINS_PLUGINS_CERTIFICATE_CHAIN organization secret."
echo "  3. Set the JETBRAINS_PLUGINS_PRIVATE_KEY_PASSWORD organization secret to the passphrase you entered."
echo
echo "Keep private.pem safe; never commit it."
