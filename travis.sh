#!/bin/bash

set -euo pipefail

function configureTravis {
  mkdir -p ~/.local
  curl -sSL https://github.com/SonarSource/travis-utils/tarball/v39 | tar zx --strip-components 1 -C ~/.local
  source ~/.local/bin/install
}
configureTravis
. installJDK8

export DEPLOY_PULL_REQUEST=true

export login="username"
export password=${GITHUB_UT_TOKEN}
export github_api_organization="sns-seb2-github-api-test"
export github_api_pr_repository="copy-of-jenkins"

regular_mvn_build_deploy_analyze
