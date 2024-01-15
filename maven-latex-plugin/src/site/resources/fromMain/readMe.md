<!-- markdownlint-disable no-trailing-spaces -->
<!-- markdownlint-disable no-inline-html -->

# Folder `fromMain` explained 

This folder contains only the file `instVScode4tex` 
which is nothing but a link to `instVScode4tex.sh` which does not exist, 
at least in version control. 

The background is, that this folder `fromMain` is populated in phase `validate`. 
In particular, then `instVScode4tex.sh` arises. 

A browser will download `instVScode4tex.sh` but show `instVScode4tex`. 
In the PDF manual, the link to `instVScode4tex.sh` resp. `instVScode4tex` 
shall show the file, so that `instVScode4tex` is required as link target, 
whereas `instVScode4tex.sh` is the file for injection. 