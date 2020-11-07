#!/bin/bash

repos=(
  "/home/raft/Desktop/SoonToBeRaft/"
)

echo ""
echo "Getting latest for" ${#repos[@]} "repositories using pull --rebase"

for repo in "${repos[@]}"
do
  echo ""
  echo "****** Getting latest for" ${repo} "******"
  cd "${repo}"
  git checkout elections_empty_beats
  out0=$(git fetch)
  out1=$(git status)
  if [[ $out1 != *"Your branch is up to date with "* ]]; then
    echo "Not up to date"
    git reset --hard origin/elections_empty_beats
    ant
  else
    echo "Up to date"
  fi
done