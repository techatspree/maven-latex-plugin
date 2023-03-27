#!/bin/sh -
# This script shall be run after change of version. 
# from the root directory where pom.xml is located 
pwd
cp src/test/resources/integration/target/./manualLatexMavenPlugin.pdf     src/main/resources/docsCmp/manualLatexMavenPlugin.pdf 
cp src/test/resources/integration/target/./dvi/dviFormat.pdf              src/main/resources/docsCmp/dvi/dviFormat.pdf
cp src/test/resources/integration/target/./latex/latexEngines.pdf         src/main/resources/docsCmp/latex/latexEngines.pdf
cp src/test/resources/integration/target/./pythontex/pythontexInOut.pdf   src/main/resources/docsCmp/pythontex/pythontexInOut.pdf
cp src/test/resources/integration/target/./recorderOption/recorder.pdf    src/main/resources/docsCmp/recorderOption/recorder.pdf
cp src/test/resources/integration/target/./robustIdxGlos/robustsample.pdf src/main/resources/docsCmp/robustIdxGlos/robustsample.pdf 
cp src/test/resources/integration/target/./xfig/xfigFormat.pdf            src/main/resources/docsCmp/xfig/xfigFormat.pdf