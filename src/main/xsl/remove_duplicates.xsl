<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:cah="http://cah.kaikoda.com"
	version="2.0" 
	exclude-result-prefixes="#all">
	
	<xsl:output indent="yes" encoding="UTF-8" method="xml" media-type="text/xml" doctype-public="-//Kaikoda//DTD CAH Cards 1.0//EN" doctype-system="../schema/cards.dtd" />
	
	<xsl:template match="game">
	
		<xsl:element name="game">
			<xsl:copy-of select="@*" />

			<xsl:for-each-group select="deck" group-by="@color">
				<xsl:variable name="color" select="@color" />
				
				<deck color="{$color}">
					<xsl:for-each-group select="/game/deck[@color = $color]/card" group-by="text()">
						<card><xsl:value-of select="text()" /></card>
					</xsl:for-each-group>
				</deck>
			</xsl:for-each-group>
		</xsl:element>	
		
	</xsl:template>
	
</xsl:stylesheet>