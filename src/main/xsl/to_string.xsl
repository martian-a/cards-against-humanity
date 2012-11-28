<?xml version="1.0" encoding="UTF-8"?>
<!--
* Cards Against Humanity Card Generator
* Copyright (C) 2012  Sheila Thomson
* 
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->
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