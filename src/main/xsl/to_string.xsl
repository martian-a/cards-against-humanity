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
	/>
	
		
	<xsl:template match="/">
		<xsl:copy-of select="." />
	</xsl:template>
	
</xsl:stylesheet>