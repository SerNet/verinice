// name = string equal to the name of the instance of the object
// defaultExpiration = number of units to make the default expiration date for the cookie
// expirationUnits = 'seconds' | 'minutes' | 'hours' | 'days' | 'months' | 'years' (default is 'days')
// defaultDomain = string, default domain for cookies; default is current domain minus the server name
// defaultPath = string, default path for cookies; default is '/'
function Cookiemanager(name,defaultExpiration,expirationUnits,defaultDomain,defaultPath) {
	// remember our name
	this.name = name;
	// get the default expiration
	this.defaultExpiration = this.getExpiration(defaultExpiration,expirationUnits);
	// set the default domain to defaultDomain if supplied; if not, set it to document.domain
	// if document.domain is numeric, otherwise strip off the server name and use the remainder
	this.defaultDomain = (defaultDomain)?defaultDomain:(document.domain.search(/[a-zA-Z]/) == -1)?document.domain:document.domain.substring(document.domain.indexOf('.') + 1,document.domain.length);
	// set the default path
	this.defaultPath = (defaultPath)?defaultPath:'/';
	// initialize an object to hold all the document's cookies
	this.cookies = new Object();
	// initialize an object to hold expiration dates for the doucment's cookies
	this.expiration = new Object();
	// initialize an object to hold domains for the doucment's cookies
	this.domain = new Object();
	// initialize an object to hold paths for the doucment's cookies
	this.path = new Object();
	// set an onlunload function to write the cookies
	window.onunload = new Function (this.name+'.setDocumentCookies();');
	// get the document's cookies
	this.getDocumentCookies();
	}
// gets an expiration date for a cookie as a GMT string
// expiration = integer expressing time in units (default is 7 days)
// units = 'miliseconds' | 'seconds' | 'minutes' | 'hours' | 'days' | 'months' | 'years' (default is 'days') 
Cookiemanager.prototype.getExpiration = function(expiration,units) {
	// set default expiration time if it wasn't supplied
	expiration = (expiration)?expiration:7;
	// supply default units if units weren't supplied
	units = (units)?units:'days';
	// new date object we'll use to get the expiration time
	var date = new Date();
	// set expiration time according to units supplied
	switch(units) {
		case 'years':
			date.setFullYear(date.getFullYear() + expiration);
			break;
		case 'months':
			date.setMonth(date.getMonth() + expiration);
			break;
		case 'days':
			date.setTime(date.getTime()+(expiration*24*60*60*1000));
			break;
		case 'hours':
			date.setTime(date.getTime()+(expiration*60*60*1000));
			break;
		case 'minutes':
			date.setTime(date.getTime()+(expiration*60*1000));
			break;
		case 'seconds':
			date.setTime(date.getTime()+(expiration*1000));
			break;
		default:
			date.setTime(date.getTime()+expiration);
			break;
		}
	// return expiration as GMT string
	return date.toGMTString();
	}
// gets all document cookies and populates the .cookies property with them
Cookiemanager.prototype.getDocumentCookies = function() {
	var cookie,pair;
	// read the document's cookies into an array
	var cookies = document.cookie.split(';');
	// walk through each array element and extract the name and value into the cookies property
	var len = cookies.length;
	for(var i=0;i < len;i++) {
		cookie = cookies[i];
		// strip leading whitespace
		while (cookie.charAt(0)==' ') cookie = cookie.substring(1,cookie.length);
		// split name/value pair into an array
		pair = cookie.split('=');
		// use the cookie name as the property name and value as the value
		this.cookies[pair[0]] = pair[1];
		}
	}
// sets all document cookies
Cookiemanager.prototype.setDocumentCookies = function() {
	var expires = '';
	var cookies = '';
	var domain = '';
	var path = '';
	for(var name in this.cookies) {
		// see if there's a custom expiration for this cookie; if not use default
		expires = (this.expiration[name])?this.expiration[name]:this.defaultExpiration;
		// see if there's a custom path for this cookie; if not use default
		path = (this.path[name])?this.path[name]:this.defaultPath;
		// see if there's a custom domain for this cookie; if not use default
		domain = (this.domain[name])?this.domain[name]:this.defaultDomain;
		// add to cookie string
		cookies = name + '=' + this.cookies[name] + '; expires=' + expires + '; path=' + path + '; domain=' + domain;
		document.cookie = cookies;
		}
	return true;
	}
// gets cookie value
// cookieName = string, cookie name
Cookiemanager.prototype.getCookie = function(cookieName) {
	var cookie = this.cookies[cookieName]
	return (cookie)?cookie:false;
	}
// stores cookie value, expiration, domain and path
// cookieName = string, cookie name
// cookieValue = string, cookie value
// expiration = number of units in which the cookie should expire
// expirationUnits = 'miliseconds' | 'seconds' | 'minutes' | 'hours' | 'days' | 'months' | 'years' (default is 'days')
// domain = string, domain for cookie
// path = string, path for cookie
Cookiemanager.prototype.setCookie = function(cookieName,cookieValue,expiration,expirationUnits,domain,path) {
	this.cookies[cookieName] = cookieValue;
	// set the expiration if it was supplied 
	if (expiration) this.expiration[cookieName] = this.getExpiration(expiration,expirationUnits);
	// set path if it was supplied
	if (domain) this.domain[cookieName] = domain;
	if (path) this.path[cookieName] = path;
	return true;
	}

var cookieManager = new Cookiemanager('cookieManager',1,'years');
