#!/usr/bin/env bash
# get_opencga_enterprise_branch.sh
#
# This script computes the correct opencga-enterprise branch to use for testing a PR in any Xetabase component repo.
# It unifies the logic previously duplicated in java-common-libs, biodata, and opencga.
#
# Inputs:
#   1. project (artifactId from pom.xml)
#   2. base_ref (target branch of the PR)
#   3. head_ref (source branch of the PR)
#
# Output:
#   Prints the resolved opencga-enterprise branch to stdout.
#   Exits with non-zero and error message if it cannot resolve a branch.

set -euo pipefail

project="$1"
base_ref="$2"
head_ref="$3"
REPO_URI="https://$ZETTA_REPO_ACCESS_TOKEN@github.com/zetta-genomics/opencga-enterprise.git"

# 1. If the branch begins with 'TASK' and exists in the opencga-enterprise repository, return it
if [[ $head_ref == TASK* ]]; then
  if [ "$(git ls-remote "$REPO_URI" "$head_ref" )" ] ; then
    echo "$head_ref";
    exit 0
  fi
fi

# 2. If the base_ref is 'develop', always map to develop
if [[ "$base_ref" == "develop" ]]; then
  echo "develop"
  exit 0
fi

# 3. release-* branch logic
if [[ "$base_ref" =~ ^release-([0-9]+)\. ]]; then
  major="${BASH_REMATCH[1]}"
  # Project-specific offset for release branch mapping
  case "$project" in
    java-common-libs)
      offset=3
      ;;
    biodata|opencga)
      offset=1
      ;;
    opencga-enterprise)
      # If the project is opencga-enterprise, use the branch as-is
      echo "$base_ref"
      exit 0
      ;;
    *)
      echo "ERROR: Unknown project '$project' for release branch mapping" >&2
      exit 1
      ;;
  esac
  new_major=$((major - offset))
  if (( new_major < 1 )); then
    echo "ERROR: Computed release branch version < 1 for $project (base_ref: $base_ref, offset: $offset)" >&2
    exit 1
  fi
  # Extract the rest of the version (minor and patch) from base_ref
  rest_version=$(echo "$base_ref" | sed -E "s/^release-[0-9]+(\..*)/\1/")
  target_branch="release-${new_major}${rest_version}"
  # Check if the exact branch exists
  if [ "$(git ls-remote --heads \"$REPO_URI\" $target_branch)" ]; then
    echo "$target_branch"
    exit 0
  else
    echo "ERROR: No matching release branch '$target_branch' found in opencga-enterprise for $project (base_ref: $base_ref, offset: $offset)" >&2
    exit 1
  fi
fi

# 4. Fallback: fail with clear error
echo "ERROR: Could not resolve opencga-enterprise branch for project '$project' (base_ref: $base_ref, head_ref: $head_ref)" >&2
exit 1
