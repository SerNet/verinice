<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:d="http://www.eclipse.org/birt/2005/design"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" version="1.0" encoding="UTF-8" media-type="text"
		omit-xml-declaration="yes" indent="no" />
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
   <xsl:template match="d:parameters|d:cubes|d:styles|d:page-setup|d:data-sources|d:joint-data-set" />
    
    
    <xsl:template match="d:table" >
    
table   : <xsl:value-of select="@name" />
id      : <xsl:value-of select="@id" />
dataset : <xsl:value-of select="d:property[@name='dataSet']/text()"/>
     <xsl:apply-templates/>

    </xsl:template>
    
    <xsl:template match="d:data" >
id: <xsl:value-of select="@id" />   field:  <xsl:value-of select="d:property[@name='resultSetColumn']/text()"/>  
    </xsl:template>
    
    <xsl:template match="d:text-data" >
id: <xsl:value-of select="@id" />   text-field:  <xsl:value-of select="d:expression[@name='valueExpr']/text()"/>  
    </xsl:template>
    




<xsl:template match="text()" />
</xsl:stylesheet>