#!/bin/bash
# The purpose of this script is to sync the GitHub pdf-java-toolkit-samples
# repository with the Octocat version of the repository.
FORK_NAME="datalogics"

OCTOCAT_SAMPLES_REPO="git@octocat.dlogics.com:${FORK_NAME}/pdf-java-toolkit-samples.git"
GITHUB_SAMPLES_REPO="git@github.com:${FORK_NAME}/pdf-java-toolkit-samples.git"

# Make sure local repository has remotes called "upstream" and "github"
# "upstream" remote should point to the OCTOCAT_SAMPLES_REPO url
# "github" remote should point to the GITHUB_SAMPLES_REPO url
UPSTREAM_REMOTE_URL=$(git config --get remote.upstream.url 2>&1)

if [[ ($? != 0) || ($UPSTREAM_REMOTE_URL != $OCTOCAT_SAMPLES_REPO) ]]; then
    echo "ERROR: Either the 'upstream' remote has not been set or is not pointing to the correct repository URL."
    exit 1
fi

GITHUB_REMOTE_URL=$(git config --get remote.github.url 2>&1)

if [[ ($? != 0) || ($GITHUB_REMOTE_URL != $GITHUB_SAMPLES_REPO) ]]; then
    echo "ERROR: Either the 'github' remote has not been set or is not pointing to the correct repository URL."
    exit 1
fi

echo "Fetching from upstream remote at ${UPSTREAM_REMOTE_URL}..."
git fetch upstream

GITHUB_BRANCH_TO_UPDATE="develop"
echo "Pushing changes from Octocat upstream/develop to the GitHub pdf-java-toolkit-samples repository's ${GITHUB_BRANCH_TO_UPDATE} branch..."
git push github upstream/develop:refs/heads/${GITHUB_BRANCH_TO_UPDATE}
