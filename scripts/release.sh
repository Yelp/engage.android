#!/bin/bash

source require_clean_work_tree.sh
require_clean_work_tree

git tag -a "$1" -m "$1" && git commit --allow-empty -m "$1"
