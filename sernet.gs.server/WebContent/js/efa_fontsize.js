/*
 * This script was developed by the Einfach für Alle web site (http://www.einfach-fuer-alle.de/artikel/fontsize/).
 * The version below was slightly modified to include a reset button (original script has only bigger and smaller)
 * It was also modified to move some parts to the TYPO3 plugin so that they could be written dynamically
 * (for example the control buttons can be created using IMAGE content objects)
 *
 * $Id: efa_fontsize.js 3579 2007-12-10 19:35:13Z fsuter $
 */
function Efa_Fontsize06(increment,bigger,reset,smaller,def) {
	this.w3c = (document.getElementById);
	this.ms = (document.all);
	this.userAgent = navigator.userAgent.toLowerCase();
	this.isMacIE = ((this.userAgent.indexOf('msie') != -1) && (this.userAgent.indexOf('mac') != -1) && (this.userAgent.indexOf('opera') == -1));
	this.isOldOp = ((this.userAgent.indexOf('opera') != -1)&&(parseFloat(this.userAgent.substr(this.userAgent.indexOf('opera')+5)) <= 7));

	if ((this.w3c || this.ms) && !this.isOldOp && !this.isMacIE) {
		this.name = "efa_fontSize06";
		this.cookieName = 'fontSize';
		this.increment = increment;
		this.def = def;
		this.defPx = Math.round(16*(def/100))
		this.base = 1;
		this.pref = this.getPref();
		this.testHTML = '<div id="efaTest" style="position:absolute;visibility:hidden;line-height:1em;">&nbsp;</div>';
		this.biggerLink = this.getLinkHtml(1,bigger);
		this.resetLink = this.getLinkHtml(0,reset);
		this.smallerLink = this.getLinkHtml(-1,smaller);
	} else {
		this.biggerLink = '';
		this.resetLink = '';
		this.smallerLink = '';
		this.efaInit = new Function('return true;');
	}

	this.allLinks = this.biggerLink + this.resetLink + this.smallerLink;
}

Efa_Fontsize06.prototype.efaInit = function() {
		document.writeln(this.testHTML);
		this.body = (this.w3c)?document.getElementsByTagName('body')[0].style:document.all.tags('body')[0].style;
		this.efaTest = (this.w3c)?document.getElementById('efaTest'):document.all['efaTest'];
		var h = (this.efaTest.clientHeight)?parseInt(this.efaTest.clientHeight):(this.efaTest.offsetHeight)?parseInt(this.efaTest.offsetHeight):999;
		if (h < this.defPx) this.base = this.defPx/h;
		this.body.fontSize = Math.round(this.pref*this.base) + '%';
}

Efa_Fontsize06.prototype.getLinkHtml = function(direction,properties) {
	var html = properties[0] + '<a href="#" onclick="efa_fontSize06.setSize(' + direction + '); return false;"';
	html += (properties[2])?'title="' + properties[2] + '"':'';
	html += (properties[3])?'class="' + properties[3] + '"':'';
	html += (properties[4])?'id="' + properties[4] + '"':'';
	html += (properties[5])?'name="' + properties[5] + '"':'';
	html += (properties[6])?'accesskey="' + properties[6] + '"':'';
	html += (properties[7])?'onmouseover="' + properties[7] + '"':'';
	html += (properties[8])?'onmouseout="' + properties[8] + '"':'';
	html += (properties[9])?'onfocus="' + properties[9] + '"':'';
	return html += '>'+ properties[1] + '<' + '/a>' + properties[10];
}

Efa_Fontsize06.prototype.getPref = function() {
	var pref = this.getCookie();
	if (pref) return parseInt(pref);
	else return this.def;
}

Efa_Fontsize06.prototype.setSize = function(direction) {
	this.pref = (direction)?this.pref+(direction*this.increment):this.def;
	this.setCookie(this.pref);
	this.body.fontSize = Math.round(this.pref*this.base) + '%';
}

Efa_Fontsize06.prototype.getCookie = function() {
	var cookie = cookieManager.getCookie();
	return (cookie)?cookie:false;
}

Efa_Fontsize06.prototype.setCookie = function(cookieValue) {
	return cookieManager.setCookie(cookieValue);
}
