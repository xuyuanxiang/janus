#!/usr/bin/env bash

rm CHANGELOG.md
conventional-changelog -p angular -i CHANGELOG.md -s -r 0
