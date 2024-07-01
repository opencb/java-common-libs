#!/bin/bash

## Navigate to the root folder where the pom.xml is
cd "$(dirname "$0")"/../../../ || exit 2

## Use the first argument passed as the branch. If it is not passed, I exit
if [[ -n $1 ]]; then
  GIT_BRANCH=$1
else
   exit 2
fi

# If the branch exists in the opencga repository, I return it
if [ "$(git ls-remote https://github.com/opencb/opencga.git "$GIT_BRANCH" )" ] ; then
  echo "$GIT_BRANCH";
  exit 0;
fi

## Read the opencga version from the pom.xml
BUILD_VERSION=$(mvn help:evaluate -Dexpression=opencga.version -q -DforceStdout)

## We remove the -SNAPSHOT if it exists
CLEAN_BUILD_VERSION=$(echo "$BUILD_VERSION" | cut -d "-" -f 1)

## Read the numbers separately to compose the name of the branch
MAJOR=$(echo "$CLEAN_BUILD_VERSION" | cut -d "." -f 1)
MINOR=$(echo "$CLEAN_BUILD_VERSION" | cut -d "." -f 2)
PATCH=$(echo "$CLEAN_BUILD_VERSION" | cut -d "." -f 3)

## it's a HOTFIX. Count the number of points to know if it is a hotfix
COUNT=$(echo "$CLEAN_BUILD_VERSION" | grep -o '\.' | wc -l )
if [ "$COUNT" -gt 2 ]; then
  echo "release-$MAJOR.$MINOR.$PATCH.x"
  exit 0
fi

## It's develop branch
if [[ "$PATCH" ==  "0" ]]; then
  echo "develop"
  exit 0
else #Is release branch
  echo "release-$MAJOR.$MINOR.x"
  exit 0
fi