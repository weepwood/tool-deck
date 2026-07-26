#!/usr/bin/env sh
set -eu

GRADLE_VERSION="8.13"
INSTALL_ROOT="${GRADLE_USER_HOME:-$HOME/.gradle}/tooldeck-bootstrap"
GRADLE_HOME="$INSTALL_ROOT/gradle-$GRADLE_VERSION"
ARCHIVE="$INSTALL_ROOT/gradle-$GRADLE_VERSION-bin.zip"
URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$INSTALL_ROOT"
  echo "Downloading Gradle $GRADLE_VERSION..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ARCHIVE" "$URL"
  else
    echo "curl or wget is required to bootstrap Gradle." >&2
    exit 1
  fi
  unzip -q -o "$ARCHIVE" -d "$INSTALL_ROOT"
  rm -f "$ARCHIVE"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
