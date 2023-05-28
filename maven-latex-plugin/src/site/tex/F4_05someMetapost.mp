%prologues := 0;% default
%%prologues := 3;
%outputtemplate:="%{jobname}.%{charcode}";% default
%outputtemplate:="%{jobname}%{charcode}.mps";% for latex 
%outputformat:="eps";% default
input metauml;
beginfig(1);
  % states
  Begin.beginAll;
  %End.endAll;
  % State Standby
  State.Stopped("STOPPED")();
  Stopped.w = beginAll.e + (20, 0);
  State.Playing("PLAYING")();
  %Playing.w = Stopped.e + (60, 0);
  State.Paused("PAUSED")();
  Stopped.n = 0.5[Paused.n, Playing.n] + (0, 70);
  Playing.w = Paused.e + (120, 0);
  %endAll.w  = Standby.e  + (20,0);

  Note.A("This is my aleph", btex $\aleph$ etex);
  A.e = beginAll.w + (-20, 0);

  drawObjects(beginAll, Stopped, Playing, Paused, A);

  % feedback; links after draw: bad

  % links between states 
  link(transition)(beginAll.e -- Stopped.w);
  link(transition)(Stopped.e  -- Playing.n);
  item.play(iAssoc)("[play()]")
    (play.sw = 0.5[Stopped.e, Playing.n]);
  link(transition)(Playing.nw  -- Stopped.se);
  item.stopPlaying(iAssoc)("[stop()]")
    (stopPlaying.ne = 0.5[Playing.nw, Stopped.se]);
  link(transition)(Playing.w + (0, +10) -- Paused.e  + (0, +10));
  item.pause(iAssoc)("[pause()]")
    (pause.s = 0.5[Playing.w, Paused.e] + (0, 10));
  link(transition)(Paused.e  + (0, -10) -- Playing.w + (0, -10));
  item.playPaused(iAssoc)("[play()]")
    (playPaused.n = 0.5[Paused.e, Playing.w] + (0, -10));
  link(transition)(Paused.ne -- Stopped.sw);
  item.stopPaused(iAssoc)("[stop()]")
    (stopPaused.nw = 0.5[Paused.ne, Stopped.sw]);
endfig;
end

