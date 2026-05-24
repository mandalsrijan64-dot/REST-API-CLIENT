#!/bin/bash
# ============================================================
#  build_and_run.sh  –  One-step compile & run
# ------------------------------------------------------------
#  Usage:
#    chmod +x build_and_run.sh
#    ./build_and_run.sh                        # interactive
#    ./build_and_run.sh London "New York"      # batch cities
# ============================================================

set -e  # exit on first error

SRC=src
OUT=out

echo ""
echo "  ┌─ Compiling Java sources ──────────────────────┐"
mkdir -p "$OUT"

javac -d "$OUT" \
      "$SRC/model/WeatherData.java"        \
      "$SRC/client/HttpClient.java"        \
      "$SRC/parser/JsonParser.java"        \
      "$SRC/service/WeatherService.java"   \
      "$SRC/display/WeatherDisplay.java"   \
      "$SRC/WeatherApp.java"

echo "  │  Compiled successfully → $OUT/"
echo "  └───────────────────────────────────────────────┘"
echo ""

# Pass any script arguments straight to the Java app
java -cp "$OUT" WeatherApp "$@"
