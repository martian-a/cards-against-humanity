<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:cah="http://cah.kaikoda.com"
	version="2.0" 
	exclude-result-prefixes="#all">
	
	<xsl:output 
		method="xml"
		version="1.0"
		omit-xml-declaration="no"
		encoding="UTF-8"
		media-type="text/xml"
		indent="yes" 		 
		doctype-public="-//Kaikoda//DTD CAH Cards 1.0//EN" 
		doctype-system="../schema/cards.dtd"
	/>
	
		
	<xsl:template match="game">	
		<xsl:element name="game">
			<xsl:copy-of select="@*" />
			<xsl:apply-templates />
		</xsl:element>			
	</xsl:template>
	
	<xsl:template match="deck">
		<xsl:element name="deck">
			<xsl:copy-of select="@*" />
			
			<xsl:for-each-group select="suit" group-by="@color">				
				<xsl:element name="suit">
					<xsl:attribute name="color" select="current-grouping-key()" />
					<xsl:for-each-group select="//suit[@color = current-grouping-key()]/card" group-by=".">
						<card><xsl:apply-templates /></card>
					</xsl:for-each-group>
				</xsl:element>
			</xsl:for-each-group>
		</xsl:element>	
	</xsl:template>
	
	<xsl:template match="text()">
		<xsl:value-of select="." />
	</xsl:template>
	
	<xsl:template match="blank">
		<xsl:copy-of select="." />
	</xsl:template>
	
</xsl:stylesheet>