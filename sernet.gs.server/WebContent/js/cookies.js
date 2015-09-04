/*
 * This script was developed by the Einfach für Alle web site (http://www.einfach-fuer-alle.de/artikel/fontsize/).
 * It was modified to set only the fontSize cookie and not all the others as it did before
 * Default domain definition was also changed to not use wildcard domain names
 *
 * $Id: cookies.js 3579 2007-12-10 19:35:13Z fsuter $
 */

function Cookiemanager() {
	this.name = 'cookieManager';
	this.defaultExpiration = this.getExpiration();
	this.defaultDomain = window.location.hostname;
	this.defaultPath = '/';
	this.cookies = new Object();
	this.expiration = new Object();
	this.domain = new Object();
	this.path = new Object();
	window.onunload = new Function (this.name+'.setDocumentCookies();');
	this.getDocumentCookies();
}

Cookiemanager.prototype.getExpiration = function() {
	var date = new Date();
    date.setTime(date.getTime()+(7*24*60*60*1000));
	return date.toGMTString();
}

Cookiemanager.prototype.getDocumentCookies = function() {
	var cookie, pair;
	var cookies = document.cookie.split(';');
	var len = cookies.length;
	for(var i = 0;i < len;i++) {
		cookie = cookies[i];
		while (cookie.charAt(0) == ' ') cookie = cookie.substring(1,cookie.length);
		pair = cookie.split('=');
		this.cookies[pair[0]] = pair[1];
	}
}

Cookiemanager.prototype.setDocumentCookies = function() {
	var cookies = '';
	if (this.cookies['fontSize']) {
		cookies = 'fontSize=' + this.cookies['fontSize'] + '; expires=' + this.expiration['fontSize'] + '; path=' + this.path['fontSize'] + '; domain=' + this.domain['fontSize'];
		if (cookies != '') {
			document.cookie = cookies;
        }
	}
	return true;
}

Cookiemanager.prototype.getCookie = function() {
	return (this.cookies['fontSize']) ? this.cookies['fontSize'] : false;
}

Cookiemanager.prototype.setCookie = function(cookieValue) {
    if (!isNaN(cookieValue)) {
    	this.cookies['fontSize'] = parseInt(cookieValue);
    	this.expiration['fontSize'] = this.getExpiration();
    	this.domain['fontSize'] = this.defaultDomain;
    	this.path['fontSize'] = this.defaultPath;
    	return true;
    }
}

var cookieManager = new Cookiemanager();