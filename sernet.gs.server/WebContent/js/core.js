/**
 * @author Joseph
 */
/**
 * Overlabel
 */
( function( jQuery ) {
	 
    // plugin definition
    jQuery.fn.overlabel = function( options ) {
 
        // build main options before element iteration
        var opts = jQuery.extend( {}, jQuery.fn.overlabel.defaults, options );
 
        var selection = this.filter( 'label[for]' ).map( function() {
 
            var label = jQuery( this );
            var id = label.attr( 'for' );
            var field = document.getElementById( id );
			
			var tag_name = field.tagName.toLowerCase();
 
            if ( !field || tag_name == 'select') return;
 
            // build element specific options
            var o = jQuery.meta ? jQuery.extend( {}, opts, label.data() ) : opts;
 
            label.addClass( o.label_class );
 
            var hide_label = function() { label.css( o.hide_css ) };
            var show_label = function() { this.value || label.css( o.show_css ) };
 
            jQuery( field )
                 .parent().addClass( o.wrapper_class ).end()
                 .focus( hide_label ).blur( show_label ).each( hide_label ).each( show_label );
 
            return this;
 
        } );
 
        return opts.filter ? selection : selection.end();
    };
	
	    // publicly accessible defaults
    jQuery.fn.overlabel.defaults = {
        label_class:   'overlabel-apply',
        wrapper_class: 'overlabel-wrapper',
        hide_css:      { 'text-indent': '-90000px' },
        show_css:      { 'text-indent': '0px', 'cursor': 'text' },
        filter:        false
    };
 
} )( jQuery );
jQuery(document).ready(function(){
	
	 // Loginbox Overlabels
	
    var labels = jQuery('.tx-felogin-pi1 label');
    labels.overlabel();
    labels.show();
	
	//Verinice.pro form Overlabels
	var labels_verinicepro = jQuery('fieldset.fachlicherkontakt label');
	labels_verinicepro.overlabel();
	labels_verinicepro.show();
	labels_verinicepro.css({
		'color' : '#999999'
	});
	
	var labels_verinicepro = jQuery('fieldset.rechnungskontakt label');
	labels_verinicepro.overlabel();
	labels_verinicepro.show();
	labels_verinicepro.css({
		'color' : '#999999'
	});
	
	
	//Searchbox Overlabel
	
	var search_overlabel = {
        label_class:   'overlabel-apply',
        wrapper_class: 'overlabel-wrapper',
        hide_css:      { 'text-indent': '90000px' },
        show_css:      { 'text-indent': '0px', 'cursor': 'text' },
        filter:        false
    };
	
	var labels = jQuery('#searchfield label');
	labels.overlabel(search_overlabel);
	labels.show();
	labels.css({
		'color' : '#999999'
	});
	
	try {
		if (jQuery(".tx-powermail-pi1").find('.tx-powermail-pi1_formwrap').length > 0) {
			jQuery('#captcha_reload').click(function(){
		        jQuery.ajax({
		            type: "GET",
		            url: 'typo3conf/ext/captcha/captcha/captcha.php',
		            success: function(data){
		                var img_src = jQuery('img.powermail_captcha').attr('src');
		                var timestamp = new Date().getTime();
		                jQuery('img.powermail_captcha').attr('src', img_src + '?' + timestamp);
		            }
		        });
		    });
		}
	}catch (e){}
	
	try{
		createLink();
		jQuery('#dl_system_wrap').accordion();
	}catch(e){}
	
	jQuery(".col2_content_wrap, .col2_special_wrap, #col2_contentslide").each(function(){
		if(jQuery(this).height() > 250) {
			jQuery(this).css('background-position','top');
		}
	});
	
});

function createLink(){
	if(jQuery('#dl_system_wrap').length > 0 ) {
	   
	   jQuery('div.dl_link_title').each(function(){
	   		jQuery(this).prepend('<input class="dl_checkbox" type="radio" name="dl_link" value="test">');
       		jQuery(this).find('input.dl_checkbox').attr('value', jQuery(this).find('a').attr('href'));
			var linkText = jQuery(this).find('a').html();
       		jQuery(this).find('a').remove();
			jQuery(this).append(linkText);
    	});
		jQuery('form.dl_download_form').each(function(){
			jQuery(this).append('<input class="dl_submit" type="image" src="/fileadmin/templates/images/button-download.gif" alt="Download" />');
			jQuery(this).find('input.dl_checkbox').each(function(){
				jQuery(this).click(function(){
					jQuery(this).parents('form:eq(0)').attr('action',jQuery(this).attr('value'));
					jQuery(this).parents('form:eq(0)').removeAttr('onsubmit');
				});
			});
		});
	}
}

function createDlAccordion (){
	jQuery('.csc-default').each(function(){
		if(jQuery(this).find('.sb_download_flexWrap').length > 0 ) {
		   jQuery(this).addClass('sb_dl_wrap')
		   jQuery(this).find('.csc-header h1').addClass('sb_dl_header');
		   jQuery(this).find('.sb_download_flexWrap').addClass('sb_dl_content');
		   
		   jQuery(this).find('div.sb_download_flex').each(function(){
           		jQuery(this).find('.sb_dl_checkbox').attr('value', jQuery(this).find('.sb_dl_link a').attr('href'));
           		jQuery(this).find('.sb_dl_link').remove();
        	});
			
			jQuery(this).find('.sb_download_flexWrap').each(function(){
            	jQuery(this).hide();
        	});
		    jQuery('.sb_dl_wrap').each(function(){
        		var content = jQuery(this).find('.sb_dl_content');
        		jQuery(this).find('.sb_dl_header').click(function(){
            		if(!content.hasClass('open')) {
               			jQuery('.sb_dl_content.open').removeClass('open').slideUp();
                		content.addClass('open').slideDown();
            		}
		        });
   			});
		}
	});
}
