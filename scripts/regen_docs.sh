#!/bin/bash
if [[ ! -d "gh_docs" ]]; then 
  mkdir gh_docs
fi
(cd Jump/Doxygen && doxygen Doxyfile)
(cd Jump/Doxygen && doxygen Doxyfile)

