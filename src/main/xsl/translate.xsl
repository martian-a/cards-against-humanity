<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:cah="http://cah.kaikoda.com/xsl/functions"
	version="2.0">
	
	<xsl:param name="path-to-dictionary" select="'../data/dictionaries/english.xml'" as="xs:string" />
	<xsl:param name="output-language" select="'en-us'" as="xs:string" />
	
	<xsl:variable name="dictionary" select="document($path-to-dictionary)/*" as="element()?" />
	
	<xsl:template match="game">
		<xsl:variable name="source-language" select="normalize-space(@xml:lang)" as="xs:string?" />
		<xsl:variable name="target-language" select="normalize-space($output-language)" as="xs:string?" />
		
		<xsl:variable name="entries" as="element()*">
			<xsl:if test="$source-language != '' and $target-language != ''">
				<xsl:sequence select="$dictionary//entry[term/@xml:lang = $source-language and term/@xml:lang = $target-language]" />
			</xsl:if>
		</xsl:variable>
		
		<xsl:apply-templates select="self::game" mode="translate">
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
						<xsl:value-of select="@xml:lang" />
					</xsl:otherwise>						
				</xsl:choose>
			</xsl:attribute>			
			<xsl:copy-of select="@*[not(name() = 'xml:lang')]" />
			<xsl:apply-templates mode="translate" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="deck | card" mode="translate">		
		<xsl:element name="{name()}">
			<xsl:copy-of select="@*" />
			<xsl:apply-templates mode="translate" />
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="text()" mode="translate">
		<xsl:param name="entries" as="element()*" tunnel="yes" />
		<xsl:param name="source-language" as="xs:string?" tunnel="yes" />
		<xsl:param name="target-language" as="xs:string?" tunnel="yes" />
		
		<xsl:value-of select="cah:translate(., $entries, $source-language, $target-language)" />
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
						<xsl:value-of select="cah:translate(replace($in, term[@xml:lang = $from], term[@xml:lang = $to]), $remaining-entries, $from, $to)" />
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