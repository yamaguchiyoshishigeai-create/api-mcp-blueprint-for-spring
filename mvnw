#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPS="$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$PROPS" ]; then
  echo "[ERROR] Maven wrapper properties not found: $PROPS" >&2
  exit 1
fi

DISTRIBUTION_URL=$(awk -F= '/^distributionUrl=/{print $2}' "$PROPS")
if [ -z "$DISTRIBUTION_URL" ]; then
  echo "[ERROR] distributionUrl is not defined in $PROPS" >&2
  exit 1
fi

FILE_NAME=${DISTRIBUTION_URL##*/}
MAIN_NAME=$(printf '%s' "$FILE_NAME" | sed 's/-bin\.zip$//')
MAVEN_USER_HOME=${MAVEN_USER_HOME:-"$HOME/.m2"}
TARGET_ROOT="$MAVEN_USER_HOME/wrapper/dists/$MAIN_NAME"
MAVEN_HOME="$TARGET_ROOT/$MAIN_NAME"
MVN_CMD="$MAVEN_HOME/bin/mvn"

if [ ! -x "$MVN_CMD" ]; then
  mkdir -p "$TARGET_ROOT"
  ZIP_FILE="$TARGET_ROOT/$FILE_NAME"
  if [ ! -f "$ZIP_FILE" ]; then
    echo "Downloading Maven from $DISTRIBUTION_URL"
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "$DISTRIBUTION_URL" -o "$ZIP_FILE"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "$DISTRIBUTION_URL" -O "$ZIP_FILE"
    else
      echo "[ERROR] curl or wget is required to download Maven" >&2
      exit 1
    fi
  fi
  TMP_DIR="$TARGET_ROOT/extract"
  rm -rf "$TMP_DIR"
  mkdir -p "$TMP_DIR"
  unzip -q "$ZIP_FILE" -d "$TMP_DIR"
  EXTRACTED_DIR=$(find "$TMP_DIR" -maxdepth 1 -type d ! -path "$TMP_DIR" | head -n 1)
  rm -rf "$MAVEN_HOME"
  mv "$EXTRACTED_DIR" "$MAVEN_HOME"
  rm -rf "$TMP_DIR"
fi

exec "$MVN_CMD" "$@"
