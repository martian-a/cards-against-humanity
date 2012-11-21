<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:cah="http://cah.kaikoda.com"
	xmlns:html="http://www.w3.org/TR/html5/"
	version="2.0" 
	exclude-result-prefixes="xsl xs cah html">
	
	<xsl:output 
		method="xml"
		indent="yes"
		encoding="UTF-8"
		omit-xml-declaration="no"
		version="1.0"
		media-type="text/xml"
	/>
	
	<xsl:template match="/">	
			<xsl:text disable-output-escaping="yes">
&lt;!DOCTYPE html&gt;
</xsl:text>
			<html>
				<xsl:apply-templates select="game/@xml:lang" />
				<xsl:apply-templates select="game" mode="meta-data" />
				<xsl:apply-templates select="game" mode="content" />
			</html>						
	</xsl:template>
	
	<xsl:template match="game" mode="meta-data">
		<head>
			<title>Cards Against Humanity</title>
			<link href="style.css" rel="stylesheet" type="text/css" media="all" />
		</head>
	</xsl:template>
	
	<xsl:template match="game" mode="content">
		<body>
			<xsl:apply-templates select="deck[@color = 'white']" />
			<xsl:apply-templates select="deck[@color = 'black']" />
		</body>
	</xsl:template>
	
	<xsl:template match="@xml:lang">
		<xsl:copy-of select="." />
		<xsl:attribute name="lang" select="." />
	</xsl:template>
	
	<xsl:template match="deck">
		<xsl:variable name="color" select="@color" />
		<xsl:variable name="total-cards-per-row" select="4" as="xs:decimal" />
		<xsl:variable name="total-rows-per-page" select="5" as="xs:decimal" />
		<xsl:variable name="total-cards-per-page" select="$total-cards-per-row * $total-rows-per-page" as="xs:decimal" />

		<section class="{$color}">
			<!-- Group into pages -->
			<xsl:for-each-group select="card" group-starting-with="card[(position() mod $total-cards-per-page) = 1]">			
				<!-- Group into rows -->
				<xsl:for-each-group select="current-group()" group-starting-with="card[(position() mod 4) = 1]">													
					<xsl:apply-templates select="current-group()" />
				</xsl:for-each-group>
		</xsl:for-each-group>
		</section>
	</xsl:template>
	
	<xsl:template match="card">
		<div class="card">
			<div class="content">
				<p><xsl:value-of select="." /></p>
			</div>
		</div>
	</xsl:template>
	
</xsl:stylesheet>