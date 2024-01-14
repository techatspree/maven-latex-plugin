#!/bin/sh -

# USAGE: 
# without argument defaults to `code`
# with single argument giving the name of the clone of code. 
# Currently we use `CODE` for the flatpack version. 
# This is used by eu.simuline:qMngmnt:0.0.6

# Explanation 
# code --extensions-dir <dir>
#     Set the root path for extensions.
# code --list-extensions
#     List the installed extensions.
# code --show-versions
#     Show versions of installed extensions, when using --list-extension.
# code --install-extension (<extension-id> | <extension-vsix-path>)
#     Installs an extension.
# code --uninstall-extension (<extension-id> | <extension-vsix-path>)
#     Uninstalls an extension.
# code --enable-proposed-api (<extension-id>)
#     Enables proposed API features for extensions. 
#     Can receive one or more extension IDs to enable individually.

case $# in 
  0)
  code="code"
  ;;
  1)
  code=$1
  ;;
  *)
  echo "expected 0 or 1 arguments found $#."
  exit
esac
echo $code

echo "latex and friends"
# latex and friends 
$code --force --install-extension james-yu.latex-workshop
$code --force --install-extension mathematic.vscode-latex
# lua 
# [lua]: Couldn't find message for key config.runtime. ...
#code --force --install-extension sumneko.lua

# bib
$code --force --install-extension phr0s.bib
$code --force --install-extension twday.bibmanager
$code --force --install-extension zfscgy.bibtex-helper

# nothing found for tikz
# metapost 
$code --force --install-extension fjebaker.vscode-metapost
# gnuplot is separate below 
# TBD: zotero

# gnuplot
$code --force --install-extension marioschwalbe.gnuplot
$code --force --install-extension fizzybreezy.gnuplot

# svg
$code --force --install-extension jock.svg
$code --force --install-extension simonsiefke.svg-preview

# spellchecker
$code --force --install-extension valentjn.vscode-ltex

# perl (e.g. to configure latexmk)
$code --force --install-extension d9705996.perl-toolbox

