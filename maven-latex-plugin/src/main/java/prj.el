(jdee-project-file-version "1.0")
(jdee-set-variables

; general 
 '(jdee-sourcepath 
   (quote ("~/OpenSource/maven-latex-plugin/maven-latex-plugin.git/trunk/maven-latex-plugin/src/main/java")))
 '(jdee-global-classpath 
   (quote ("~/OpenSource/maven-latex-plugin/maven-latex-plugin.git/trunk/maven-latex-plugin/target/classes")))

; jdk 
 '(jdee-jdk (quote "1.8"))
 '(jdee-jdk-registry (quote (("1.8" . "/usr/lib64/jvm/javaLatest/"))))
 '(jdee-jdk-doc-url "file:///home/ernst/.emacs.d/jdee-master/doc/html/jde-ug/jde-ug.html")

; compilation 
 '(jdee-compiler (quote ("javac" "")))
 '(jdee-compile-enable-kill-buffer t)
 '(jdee-compile-option-encoding "UTF-8")
 '(jdee-compile-option-sourcepath (quote ("~/OpenSource/maven-latex-plugin/maven-latex-plugin.git/trunk/maven-latex-plugin/src/main/java")))
 '(jdee-compile-option-command-line-args (quote ("-Xlint")))
 '(jdee-compile-option-depend nil)
 '(jdee-compile-option-deprecation t)
; '(jdee-compile-option-debug (t t t))
 '(jdee-compile-option-directory "~/OpenSource/maven-latex-plugin/maven-latex-plugin.git/trunk/maven-latex-plugin/target/classes")



 '(jdee-server-dir "~/.emacs.d/jdee-server-master/target")
)
