/* The following functions are part of a minimal JS library I wrote for VNDB.org */

var expanded_icon = '▾';
var collapsed_icon = '▸';

var http_request = false;
function ajax(url, func, async) {
  if(!async && http_request)
    http_request.abort();
  var req = (window.ActiveXObject) ? new ActiveXObject('Microsoft.XMLHTTP') : new XMLHttpRequest();
  if(req == null)
    return alert("Your browser does not support the functionality this website requires.");
  if(!async)
    http_request = req;
  req.onreadystatechange = function() {
    if(!req || req.readyState != 4 || !req.responseText)
      return;
    if(req.status != 200)
      return alert('Whoops, error! :(');
    func(req);
  };
  url += (url.indexOf('?')>=0 ? ';' : '?')+(Math.floor(Math.random()*999)+1);
  req.open('GET', url, true);
  req.send(null);
}

function byId(n) {
  return document.getElementById(n)
}
function byName(){
  var d = arguments.length > 1 ? arguments[0] : document;
  var n = arguments.length > 1 ? arguments[1] : arguments[0];
  return d.getElementsByTagName(n);
}
function byClass() { // [class], [parent, class], [tagname, class], [parent, tagname, class]
  var par = typeof arguments[0] == 'object' ? arguments[0] : document;
  var t = arguments.length == 2 && typeof arguments[0] == 'string' ? arguments[0] : arguments.length == 3 ? arguments[1] : '*';
  var c = arguments[arguments.length-1];
  var l = byName(par, t);
  var ret = [];
  for(var i=0; i<l.length; i++)
    if(hasClass(l[i], c))
      ret[ret.length] = l[i];
  return ret;
}

/* wrapper around DOM element creation
 * tag('string') -> createTextNode
 * tag('tagname', tag(), 'string', ..) -> createElement(), appendChild(), ..
 * tag('tagname', { class: 'meh', title: 'Title' }) -> createElement(), setAttribute()..
 * tag('tagname', { <attributes> }, <elements>) -> create, setattr, append */
function tag() {
  if(arguments.length == 1)
    return typeof arguments[0] != 'object' ? document.createTextNode(arguments[0]) : arguments[0];
  var el = typeof document.createElementNS != 'undefined'
    ? document.createElementNS('http://www.w3.org/1999/xhtml', arguments[0])
    : document.createElement(arguments[0]);
  for(var i=1; i<arguments.length; i++) {
    if(arguments[i] == null)
      continue;
    if(typeof arguments[i] == 'object' && !arguments[i].appendChild) {
      for(attr in arguments[i]) {
        if(attr == 'style')
          el.setAttribute(attr, arguments[i][attr]);
        else
          el[ attr == 'class' ? 'className' : attr == 'for' ? 'htmlFor' : attr ] = arguments[i][attr];
      }
    } else
      el.appendChild(tag(arguments[i]));
  }
  return el;
}
function addBody(el) {
  if(document.body.appendChild)
    document.body.appendChild(el);
  else if(document.documentElement.appendChild)
    document.documentElement.appendChild(el);
  else if(document.appendChild)
    document.appendChild(el);
}
function setContent() {
  setText(arguments[0], '');
  for(var i=1; i<arguments.length; i++)
    if(arguments[i] != null)
      arguments[0].appendChild(tag(arguments[i]));
}
function setText(obj, txt) {
  if(obj.textContent != null)
    obj.textContent = txt;
  else
    obj.innerText = txt;
}

function listClass(obj) {
  var n = obj.className;
  if(!n)
    return [];
  return n.split(/ /);
}
function hasClass(obj, c) {
  var l = listClass(obj);
  for(var i=0; i<l.length; i++)
    if(l[i] == c)
      return true;
  return false;
}
function setClass(obj, c, set) {
  var l = listClass(obj);
  var n = [];
  if(set) {
    n = l;
    if(!hasClass(obj, c))
      n[n.length] = c;
  } else {
    for(var i=0; i<l.length; i++)
      if(l[i] != c)
        n[n.length] = l[i];
  }
  obj.className = n.join(' ');
}


// Dropdown Search

function dsInit(obj, url, trfunc, serfunc, retfunc, parfunc) {
  obj.setAttribute('autocomplete', 'off');
  obj.onkeydown = dsKeyDown;
  obj.onblur = function() { setTimeout(function () { setClass(byId('ds_box'), 'hidden', true) }, 250) };
  obj.ds_returnFunc = retfunc;
  obj.ds_trFunc = trfunc;
  obj.ds_serFunc = serfunc;
  obj.ds_parFunc = parfunc;
  obj.ds_searchURL = url;
  obj.ds_selectedId = 0;
  obj.ds_dosearch = null;
  if(!byId('ds_box'))
    addBody(tag('div', {id: 'ds_box', 'class':'hidden'}, tag('b', 'Loading...')));
}

function dsKeyDown(ev) {
  var c = document.layers ? ev.which : document.all ? event.keyCode : ev.keyCode;
  var obj = this;

  if(c == 9) // tab
    return true;

  // do some processing when the enter key has been pressed
  if(c == 13) {
    var frm = obj;
    while(frm && frm.nodeName.toLowerCase() != 'form')
      frm = frm.parentNode;
    if(frm) {
      var oldsubmit = frm.onsubmit;
      frm.onsubmit = function() { return false };
      setTimeout(function() { frm.onsubmit = oldsubmit }, 100);
    }

    if(obj.ds_selectedId != 0)
      obj.value = obj.ds_serFunc(byId('ds_box_'+obj.ds_selectedId).ds_itemData, obj);
    if(obj.ds_returnFunc)
      obj.ds_returnFunc(obj);

    setClass(byId('ds_box'), 'hidden', true);
    setContent(byId('ds_box'), tag('b', 'Loading...'));
    obj.ds_selectedId = 0;
    if(obj.ds_dosearch) {
      clearTimeout(obj.ds_dosearch);
      obj.ds_dosearch = null;
    }

    return false;
  }

  // process up/down keys
  if(c == 38 || c == 40) {
    var l = byName(byId('ds_box'), 'tr');
    if(l.length < 1)
      return true;

    // get new selected id
    if(obj.ds_selectedId == 0) {
      if(c == 38) // up
        obj.ds_selectedId = l[l.length-1].id.substr(7);
      else
        obj.ds_selectedId = l[0].id.substr(7);
    } else {
      var sel = null;
      for(var i=0; i<l.length; i++)
        if(l[i].id == 'ds_box_'+obj.ds_selectedId) {
          if(c == 38) // up
            sel = i>0 ? l[i-1] : l[l.length-1];
          else
            sel = l[i+1] ? l[i+1] : l[0];
        }
      obj.ds_selectedId = sel.id.substr(7);
    }

    // set selected class
    for(var i=0; i<l.length; i++)
      setClass(l[i], 'selected', l[i].id == 'ds_box_'+obj.ds_selectedId);
    return true;
  }

  // perform search after a timeout
  if(obj.ds_dosearch)
    clearTimeout(obj.ds_dosearch);
  obj.ds_dosearch = setTimeout(function() {
    dsSearch(obj);
  }, 500);

  return true;
}

function dsSearch(obj) {
  var box = byId('ds_box');
  var val = obj.ds_parFunc ? obj.ds_parFunc(obj.value) : obj.value;

  clearTimeout(obj.ds_dosearch);
  obj.ds_dosearch = null;

  // hide the ds_box div
  if(val.length < 2) {
    setClass(box, 'hidden', true);
    setContent(box, tag('b', 'Loading...'));
    obj.ds_selectedId = 0;
    return;
  }

  // position the div
  var ddx=0;
  var ddy=obj.offsetHeight;
  var o = obj;
  do {
    ddx += o.offsetLeft;
    ddy += o.offsetTop;
  } while(o = o.offsetParent);

  box.style.position = 'absolute';
  box.style.left = ddx+'px';
  box.style.top = ddy+'px';
  box.style.width = obj.offsetWidth+'px';
  setClass(box, 'hidden', false);

  // perform search
  ajax(obj.ds_searchURL + encodeURIComponent(val), function(hr) {
    dsResults(hr, obj);
  });
}

function dsResults(hr, obj) {
  var lst = hr.responseXML.getElementsByTagName('item');
  var box = byId('ds_box');
  if(lst.length < 1) {
    setContent(box, tag('b', 'No results'));
    obj.selectedId = 0;
    return;
  }

  var tb = tag('tbody', null);
  for(var i=0; i<lst.length; i++) {
    var id = lst[i].getAttribute('id');
    var tr = tag('tr', {id: 'ds_box_'+id, ds_itemData: lst[i]} );
    setClass(tr, 'selected', obj.selectedId == id);

    tr.onmouseover = function() {
      obj.ds_selectedId = this.id.substr(7);
      var l = byName(box, 'tr');
      for(var j=0; j<l.length; j++)
        setClass(l[j], 'selected', l[j].id == 'ds_box_'+obj.ds_selectedId);
    };
    tr.onmousedown = function() {
      obj.value = obj.ds_serFunc(this.ds_itemData, obj);
      if(obj.ds_returnFunc)
        obj.ds_returnFunc();
      setClass(box, 'hidden', true);
      obj.ds_selectedId = 0;
    };

    obj.ds_trFunc(lst[i], tr);
    tb.appendChild(tr);
  }
  setContent(box, tag('table', tb));

  if(obj.ds_selectedId != 0 && !byId('ds_box_'+obj.ds_selectedId))
    obj.ds_selectedId = 0;
}



/* What follows is specific to manned.org */

// Search box
searchRedir = false;
dsInit(byId('q'), '/xml/search.xml?q=', function(item, tr) {
    tr.appendChild(tag('td', item.getAttribute('name'), tag('i', '('+item.getAttribute('section')+')')));
  },
  function(item) {
    searchRedir = true;
    location.href = '/'+item.getAttribute('name')+'.'+item.getAttribute('section').substr(0,1);
    return item.getAttribute('name')+'('+item.getAttribute('section')+')';
  },
  function() {
    if(!searchRedir) {
      var frm=byId('q');
      while(frm && frm.nodeName.toLowerCase() != 'form')
        frm = frm.parentNode;
      frm.submit();
    }
  }
);



// Efficiently pack an array of booleans into a string. (Uses something like
// base32) Note: The resulting array after decoding may have a few more
// elements than it had before decoding. These will be false.
var bsCharacters = "abcdefghijklmnopqrstuvwxyz234567";

function bsEncode(a) {
  var v = 0;
  var b = 0;
  var r = '';
  for(var i=0; i<a.length; i++) {
    v = (v<<1) + (a[i]?1:0);
    if(++b == 5) {
      r += bsCharacters.charAt(v);
      v = b = 0;
    }
  }
  if(!a.length || b > 0)
    r += bsCharacters.charAt(v<<(5-b));
  return r;
}

function bsDecode(s) {
  var a = [];
  for(var i=0; i<s.length; i++) {
    var n = s.charCodeAt(i);
    n -= n >= 97 ? 97 : 24;
    a.push(!!((n>>4)&1), !!((n>>3)&1), !!((n>>2)&1), !!((n>>1)&1), !!(n&1));
  }
  return a;
}


/* Structure of VARS.mans:
  [
    ["System", "Full name", "short", [
        [ "package", "version", [
            [ "section", "locale"||null ],
            ...
          ],
          oldvisible // <- this is only set by JS
        ],
        ...
      ],
      oldvisible // <- this is only set by JS
    ],
    ...
  ]
*/

navShowLocales  = false;
navHasLocale    = false;

function navCreate(nav) {
  setText(nav, '');

  view = navSerialize();
  navHasLocale = false;
  var dl = tag('dl', null);

  for(var i=0; i<VARS.mans.length; i++) {
    var sys = VARS.mans[i];

    var isold = i > 0 && VARS.mans[i-1][0] == sys[0];
    if(typeof sys[4] === 'undefined')
      sys[4] = !isold;

    var pkgnum = 0;
    var dd = tag('dd', null);

    if(sys[4])
      for(var j=0; j<sys[3].length; j++)
        if(navCreatePkg(nav, view, dd, sys, j))
          pkgnum++;

    if(!isold || sys[4])
      dl.appendChild(tag('dt', sys[1],
        isold || !VARS.mans[i+1] || VARS.mans[i+1][0] != sys[0] ? null : tag('a',
          {href:'#', _sysn: sys[0], _sysi:i, 'class':'expand',
           title: "Show/hide historical releases.",
           onclick: function() {
            for(var j=this._sysi+1; j<VARS.mans.length && VARS.mans[j][0] == this._sysn; j++)
              VARS.mans[j][4] = !VARS.mans[j][4];
            navCreate(nav);
            return false
          }}, VARS.mans[i+1][4] ? expanded_icon : collapsed_icon)
      ));

    if(sys[4] && pkgnum > 0)
      dl.appendChild(dd);
  }

  navCreateLinks(nav);
  nav.appendChild(dl);
}


function navCreatePkg(nav, view, dd, sys, n) {
  var pkg = sys[3][n];

  var isold = n > 0 && sys[3][n-1][0] == pkg[0];
  if(isold && !pkg[3])
    return false;

  var mannum = 0;
  var pdd = tag('dd', null);
  for(var i=0; i<pkg[2].length; i++) {
    var man = pkg[2][i];
    var txt = man[0] + (man[1] ? '.'+man[1] : '');
    if(man[2] != VARS.hash && man[1])
      navHasLocale = true;
    if(man[2] == VARS.hash || (navShowLocales || !man[1])) {
      if(i > 0)
        pdd.appendChild(tag(' '));
      pdd.appendChild(man[2] == VARS.hash ? tag('b', txt) : tag('a', {href:'/'+VARS.name+'/'+man[2]+'?v='+view}, txt));
      mannum++;
    }
  }

  if(mannum > 0) {
    dd.appendChild(tag('dt', tag('a', {href:'/browse/'+sys[2]+'/'+pkg[0]+'/'+pkg[1]}, pkg[0]),
      isold || !sys[3][n+1] || sys[3][n+1][0] != pkg[0] ? null : tag('a',
        {href:'#', _pkgn: pkg[0], _pkgi:n, 'class':'expand',
         title: 'Show/hide historical versions of this package',
         onclick: function() {
          for(var j=this._pkgi+1; j<sys[3].length && sys[3][j][0] == this._pkgn; j++)
            sys[3][j][3] = !sys[3][j][3];
          navCreate(nav);
          return false
        }}, sys[3][n+1][3] ? expanded_icon : collapsed_icon),
      tag('i', pkg[1])));
    dd.appendChild(pdd);
    return true;
  }
  return false;
}


function navCreateLinks(nav) {
  var t = (navShowLocales ? expanded_icon : collapsed_icon) + 'locales';
  nav.appendChild(!navHasLocale ? tag('i', {'class':'global'}, t) : tag('a',
    { 'class': 'global',
      href:    '#',
      title:   'Show/hide manuals in a non-standard locale.',
      onclick: function() { navShowLocales = !navShowLocales; navCreate(nav); return false }
    }, t
  ));
}


// Serializes the current navigation view into a short string. The string is a
// bsEncode()ed bit array, creates as follows:
//   array.push(navShowLocales);
//   for(each system that has an expand button)
//     array.push(is the butten expanded or not);
//   for(each system)
//     for(each package that has an expand button)
//       array.push(is the button expanded or not);
// Obviously, this means that the serialized view depends on the number and
// order of systems and packages. The order is stable, the number may change
// with database updates.
function navSerialize() {
  var a = [navShowLocales];
  for(var i=0; i<VARS.mans.length; i++)
    if(i+1 < VARS.mans.length && VARS.mans[i+1][0] == VARS.mans[i][0] && (i == 0 || VARS.mans[i-1][0] != VARS.mans[i][0]))
      a.push(!!VARS.mans[i+1][4]);
  for(var i=0; i<VARS.mans.length; i++)
    for(var j=0; j<VARS.mans[i][3].length; j++)
      if(j+1 < VARS.mans[i][3].length && VARS.mans[i][3][j+1][0] == VARS.mans[i][3][j][0] && (j == 0 || VARS.mans[i][3][j-1][0] != VARS.mans[i][3][j][0]))
        a.push(!!VARS.mans[i][3][j+1][3]);
  return bsEncode(a).replace(/(.)a+$/, '$1');
}

// And the reverse of the above.
function navLoad(s) {
  var a = bsDecode(s);
  navShowLocales = !!a.shift();
  for(var i=0; i<VARS.mans.length; i++)
    if(i > 0 && VARS.mans[i-1][0] == VARS.mans[i][0])
      VARS.mans[i][4] = i > 1 && VARS.mans[i-2][0] == VARS.mans[i-1][0] ? VARS.mans[i-1][4] : !!a.shift();
  for(var i=0; i<VARS.mans.length; i++)
    for(var j=0; j<VARS.mans[i][3].length; j++)
      if(j > 0 && VARS.mans[i][3][j-1][0] == VARS.mans[i][3][j][0])
        VARS.mans[i][3][j][3] = j > 1 && VARS.mans[i][3][j-2][0] == VARS.mans[i][3][j-1][0] ? VARS.mans[i][3][j-1][3] : !!a.shift();
}


if(byId('nav')) {
  navLoad(VARS.view||'');
  navCreate(byId('nav'));
}



// The "more..." links on the homepage.
if(byId('systems')) {
  var f = function() {
    var l = byName(this.parentNode, 'a', 'hidden');
    for(var i=0; i<l.length; i++)
      setClass(l[i], 'hidden', false);
    setClass(this, 'hidden', true);
    return false
  };
  var l = byClass(byId('systems'), 'a', 'more');
  for(var i=0; i<l.length; i++)
    l[i].onclick = f;
}
