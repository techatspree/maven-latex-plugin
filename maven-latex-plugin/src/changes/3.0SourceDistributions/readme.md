<!-- markdownlint-disable no-trailing-spaces -->
<!-- markdownlint-disable no-inline-html -->
# Source Distributions 

The idea of source distributions is to deliver all files needed to compile. 
It must be ensured that the sources delivered really compile. 
To that end, they are compiled just as for a pdf/dvi or any other 'binary' distribution. 
It must be ensured that enough information is submitted to the reveiver of the source distribution, 
that (s)he can reconstruct. 
In particular, the part of the pom affecting the latex plugin is stored, 
this is just to be read from the class Settings. 
For source distributions it is assumed that the goal cfg is used for compilation. 
Thus the pom is sufficient to know the binaries created. 
There is a config which specifies whether the binaries are to be delivered also for check, 
but the core are the sources. 

The switch -recorder is set so that an fls file is created. 
This is used to find all dependencies. 
Caution: there are gaps. 
It must be checked that dependencies are essentially in the tex distribution 
which must be for the moment TeX Live and in folder of the latex main file, possibly recursively. 
Details are given in the [Section on Reproducibility](#ssRepro). 
Then all these files are copied to the target folder. 
For the distribution TeX Live, there is an update once per year, so it suffices to store the year. 
For MiKTeX, which is not supported in a first version, 
updates are quite irregular. 
So the versions of the converters must be given. 
An according file is also delivered. 
Note that only the converters in the pom are taken into account. 


## On Reproducibility <a id='ssRepro'></a>

This folder contains material to reach reproducibility. 
The [fls file](./manualWithoutDistri.fls) is modified, removing files with path `/usr/share/texmf`
which seems the location of the distribution. 

It can be reconstructed, provided TeX Live, 
by `kpsewhich latex.ltx` just eliminating three containment levels from the path. 

Then from the fls file all output is removed. 
Then removed all files starting with `/home/ernst/.cache/texmf/`. 

Observe that fls files all have a line starting with `PWD` indicating the current working directory. 
This is the location of the according latex main file. 
Input and output files startig `./` and `../` are given relative to this path. 

Then all input is removed which is in the current directory (starting `./`). 
These are the files in a subfolder of the folder where the latex main file is located. 
In our case, there are also files starting with `../`. 
This is not really typical for usage in a maven plugin. 
An example is 

```
INPUT ../../../build.xml
```

One can prove easily for use as a maven project, that this is still in the project, 
by starting with `PWD` and searching the pom.xml file 
which is relative in the path `../../../pom.xml`. 
So to be inside the project, only up to 3 levels upward are allowed. 

The remaining files in the fls file are the following: 

```
INPUT /var/lib/texmf/web2c/luahbtex/lualatex.fmt
INPUT /var/lib/texmf/fonts/map/pdftex/updmap/pdftex.map
INPUT /etc/texmf/web2c/mktex.cnf
```

It must be clarified whether these may break reproducibility. 

If not, one can just package all files in the project occurring in the fls file as input. 
These files are really sufficient to rerun latex with same result. 
Maybe one shall not allow prefix `../`. 
This could be avoided using links. 
That way, one could just package part of the tex input folder, 
typically `./src/site/tex`, where `.` is the maven project base folder. 

