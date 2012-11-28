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
	
	<xsl:param name="path-to-dictionary" select="'../data/dictionaries/english.xml'" as="xs:string" />
	<xsl:param name="output-language" select="''" as="xs:string" />
	
	<xsl:variable name="dictionary" select="document($path-to-dictionary)/*" as="element()?" />
	
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
	
	<xsl:template match="/">
		<xsl:variable name="source-language" select="lower-case(normalize-space(game/@xml:lang))" as="xs:string?" />
		<xsl:variable name="target-language" as="xs:string?">
			<xsl:choose>
				<!-- If no target language has been specified, assume it's to be the same as the source langauge -->
				<xsl:when test="normalize-space($output-language) = ''"><xsl:value-of select="$source-language" /></xsl:when>
				<xsl:otherwise>
					<!-- target language specified -->
					<xsl:value-of select="lower-case(normalize-space($output-language))" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>			
		
		<xsl:variable name="entries" as="element()*">
			<xsl:if test="$source-language != '' and $target-language != ''">
				<xsl:sequence select="$dictionary//entry[term/lower-case(@xml:lang) = $source-language and term/lower-case(@xml:lang) = $target-language]" />
			</xsl:if>
		</xsl:variable>
		
		<xsl:apply-templates select="game" mode="translate">
			<xsl:with-param name="entries" select="$entries" as="element()*" tunnel="yes" />
			<xsl:with-param name="source-language" select="$source-language" as="xs:string?" tunnel="yes" />
			<xsl:with-param name="target-language" select="$target-language" as="xs:string?" tunnel="yes" />
		</xsl:apply-templates>
					
	</xsl:template>

	<xsl:template match="game" mode="translate">	
		<xsl:param name="entries" as="element()*" tunnel="yes" />
		<xsl:param name="target-language" as="xs:string?" tunnel="yes" />		
		
		<xsl:element name="{name()}">			
			<xsl:attribute name="xml:lang">
				<xsl:choose>
					<xsl:when test="$target-language != '' and count($entries) &gt; 0">
						<xsl:value-of select="$target-language" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$target-language" />
					</xsl:otherwise>						
				</xsl:choose>
			</xsl:attribute>			
			<xsl:copy-of select="@*[not(name() = 'xml:lang')]" />
			<xsl:copy-of select="licence" />
			<xsl:apply-templates select="deck" mode="translate" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="deck| suit | card | blank" mode="translate">		
		<xsl:element name="{name()}">
			<xsl:copy-of select="@*" />
			<xsl:apply-templates mode="translate" />
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="text()" mode="translate">
		<xsl:param name="entries" as="element()*" tunnel="yes" />
		<xsl:param name="source-language" as="xs:string?" tunnel="yes" />
		<xsl:param name="target-language" as="xs:string?" tunnel="yes" />
		
		<xsl:if test="normalize-space(.) != ''">
			<xsl:value-of select="cah:translate(., $entries, $source-language, $target-language)" />
		</xsl:if>			
	</xsl:template>
	
	<xsl:function name="cah:translate" as="xs:string">
		<xsl:param name="in" as="xs:string" />
		<xsl:param name="entries" as="element()*" />
		<xsl:param name="from" as="xs:string?" />
		<xsl:param name="to" as="xs:string?" />
		
		<xsl:variable name="result">
			<xsl:choose>
				<xsl:when test="normalize-space($from) != '' and normalize-space($to) != '' and count($entries) &gt; 0">
					<xsl:for-each select="$entries[1]">				
						<xsl:variable name="remaining-entries" select="$entries[position() != 1]" as="element()*" />
						<xsl:value-of select="cah:translate(replace($in, term[lower-case(@xml:lang) = $from], term[lower-case(@xml:lang) = $to]), $remaining-entries, $from, $to)" />
					</xsl:for-each>		
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$in" />
				</xsl:otherwise>
			</xsl:choose>			
		</xsl:variable>
				
		<xsl:value-of select="$result" />
	</xsl:function>
	
</xsl:stylesheet>