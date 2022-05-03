#!/bin/sh -
# This script shall be run after change of version. 
# from the root directory where pom.xml is located 
pwd
cp src/test/resources/integration/target/./manualLatexMavenPlugin.pdf     src/main/resources/docsCmp/manualLatexMavenPlugin.pdf 
cp src/test/resources/integration/target/./dvi/dviFormat.pdf              src/main/resources/docsCmp/dvi/dviFormat.pdf
cp src/test/resources/integration/target/./robustIdxGlos/robustsample.pdf src/main/resources/docsCmp/robustIdxGlos/robustsample.pdf 
cp src/test/resources/integration/target/./xfig/xfigFormat.pdf            src/main/resources/docsCmp/xfig/xfigFormat.pdf