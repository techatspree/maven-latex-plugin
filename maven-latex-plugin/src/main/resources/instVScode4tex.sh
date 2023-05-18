#!/bin/sh -

echo "latex and friends"
# latex and friends 
code --force --install-extension james-yu.latex-workshop
code --force --install-extension mathematic.vscode-latex
# bib
code --force --install-extension phr0s.bib
code --force --install-extension twday.bibmanager
# nothing found for tikz
# metapost 
code --force --install-extension fjebaker.vscode-metapost
# gnuplot is separate below 
# TBD: zotero

# gnuplot
code --force --install-extension marioschwalbe.gnuplot
code --force --install-extension fizzybreezy.gnuplot

# svg
code --force --install-extension jock.svg
code --force --install-extension simonsiefke.svg-preview

# spellchecker
code --force --install-extension valentjn.vscode-ltex

# perl (e.g. to configure latexmk)
code --force --install-extension d9705996.perl-toolbox

