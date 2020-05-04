<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:d="http://www.eclipse.org/birt/2005/design"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" version="1.0" encoding="UTF-8" media-type="text"
		omit-xml-declaration="yes" indent="no" />
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
   <xsl:template match="d:parameters|d:cubes|d:styles|d:page-setup|d:body|d:data-sources" />
    
    
    <xsl:template match="d:oda-data-set" >
datasetname: <xsl:value-of select="@name" />
id: <xsl:value-of select="@id" />
type: <xsl:value-of select="@extensionID"/>

----------------------------------------------------------
query: <xsl:value-of select="d:xml-property[@name='queryText']/text()"/>
----------------------------------------------------------
     <xsl:apply-templates/>

    </xsl:template>
    
    <xsl:template match="d:joint-data-set" >

joinTable: <xsl:value-of select="@name" /> id: <xsl:value-of select="@id" />
joint type:  <xsl:value-of select="d:list-property[@name='joinConditions']/d:structure/d:property[@name='joinType']/text()"/> :: <xsl:value-of select="d:list-property[@name='joinConditions']/d:structure/d:property[@name='joinOperator']/text()"/> |
<xsl:value-of select="d:list-property[@name='joinConditions']/d:structure/d:property[@name='leftDataSet']/text()"/> == <xsl:value-of select="d:list-property[@name='joinConditions']/d:structure/d:property[@name='rightDataSet']/text()"/> |
<xsl:value-of select="d:list-property[@name='joinConditions']/d:structure/d:expression[@name='leftExpression']/text()"/> == <xsl:value-of select="d:list-property[@name='joinConditions']/d:structure/d:expression[@name='rightExpression']/text()"/> |
<xsl:apply-templates/>



    </xsl:template>
    

<xsl:template match="d:structure[@name='cachedMetaData']" >
Fields:
    
<xsl:for-each select="d:list-property/*">
<xsl:value-of select="d:property[@name='position']/text()"/>:<xsl:value-of select="d:property[@name='dataType']/text()"/>:<xsl:value-of select="d:property[@name='name']/text()"/>:
</xsl:for-each>
    
</xsl:template>


<xsl:template match="d:list-property[@name='computedColumns']" >
Computed Fields:
    
<xsl:for-each select="d:structure">
<xsl:value-of select="d:property[@name='name']/text()"/>:<xsl:value-of select="d:expression[@name='expression']/text()"/> '

</xsl:for-each>
    
</xsl:template>



<xsl:template match="text()" />
</xsl:stylesheet>