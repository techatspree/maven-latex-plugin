%prologues := 0;% default
prologues := 3;% at the moment needed to keep text in boxes 
%outputtemplate:="%{jobname}.%{charcode}";% default
%outputtemplate:="%{jobname}%{charcode}.mps";% for latex 
%outputformat:="eps";% default
input metauml;
%defaultscale := 0.8;
%defaultscale := 10pt/fontsize defaultscale;
beginfig(1);
%defaultscale := 8pt/fontsize defaultscale;
%defaultscale := 0.8;

Actor.devel("developer");
Instance.editor("source editor")("*.tex", "*.mp", "...");
Instance.sviewer("source viewer")("*.pdf", "*.mps", "...");
%Instance.tviewer("target viewer")();
Instance.lviewer("log viewer")("*.log", "*.blg", "...");
Instance.console("console")();
devel.n + (-100, 50) = editor.sw;
devel.n + ( +50, 50) = sviewer.s;
devel.s - (   0, 50) = console.n;
devel.w - (  50,  0) = lviewer.e;
item(iAssoc)("typing")(obj.sw = 0.5[devel.n,editor.se]);
%item(iAssoc)("typing")(obj.ne = 0.5[devel.n,editor.sw]);
%item(iAssoc)("command")(obj.se = 0.5[devel.s,console.n]);
%item(iAssoc)("command")(obj.se = 0.5[devel.s,console.n]);

%label.top("test", (100,0));
drawObjects(devel, editor, sviewer, lviewer, console);%, tviewer, console

link(inheritance)(devel.n -- editor.se);
link(inheritance)(editor.sw -- devel.n);

link(realization)(sviewer.s -- devel.n);
link(realization)(lviewer.e -- devel.w);

link(realization)(0.7[console.n,console.ne] -- devel.s);
link(inheritance)(devel.s -- 0.7[console.n,console.nw]);


%currentpicture := currentpicture scaled 0.2;
endfig;
end