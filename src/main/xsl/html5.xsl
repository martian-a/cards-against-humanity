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
		media-type="application/xhtml+xml"
	/>
	
	<xsl:variable name="blank"><xsl:text>________</xsl:text></xsl:variable>
	
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
			<meta charset="UTF-8" />
			<title>Cards Against Humanity</title>
			<link href="assets/style.css" rel="stylesheet" type="text/css" media="all" />
		</head>
	</xsl:template>
	
	<xsl:template match="game" mode="content">
		<body>
			<xsl:apply-templates select="deck" />
		</body>
	</xsl:template>
	
	<xsl:template match="deck">
		<section class="deck">
			<xsl:apply-templates select="suit[@color = 'white']" />
			<xsl:apply-templates select="suit[@color = 'black']" />
		</section>
	</xsl:template>
	
	<xsl:template match="@xml:lang">
		<xsl:copy-of select="." />
		<xsl:attribute name="lang" select="." />
	</xsl:template>
	
	<xsl:template match="suit">
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
		<xsl:variable name="total-characters" select="string-length(.) + (count(blank) * string-length($blank))" as="xs:integer" />
		<xsl:variable name="total-blanks" select="count(blank)" as="xs:integer" />
		
		<div class="card{if ($total-blanks &gt; 1) then ' has-instructions' else ''}{if ($total-blanks &gt; 1) then concat(' blanks-', $total-blanks) else ''}">			
			<div class="content">
				<p>
					<xsl:if test="$total-characters &gt; 80">
						<xsl:attribute name="class">
							<xsl:choose>
								<xsl:when test="$total-characters &gt; 150">far-too-long</xsl:when>
								<xsl:when test="$total-characters &gt; 125">too-long</xsl:when>
								<xsl:when test="$total-characters &gt; 110">very-very-long</xsl:when>
								<xsl:when test="$total-characters &gt; 90">very-long</xsl:when>
								<xsl:otherwise>long</xsl:otherwise>
							</xsl:choose>							
						</xsl:attribute>
					</xsl:if>
					<xsl:apply-templates />
				</p>			
			</div>
			<xsl:if test="$total-blanks &gt; 1">
				<div class="instructions">
					<xsl:if test="$total-blanks &gt; 2">							
						<p class="draw">Draw <span class="total"><xsl:value-of select="$total-blanks - 1" /></span></p>
					</xsl:if>
					<p class="pick">Pick <span class="total"><xsl:value-of select="$total-blanks" /></span></p>
				</div>		
			</xsl:if>	
		</div>
	</xsl:template>
	
	<xsl:template match="blank">
		<xsl:value-of select="$blank" />
	</xsl:template>
	
	<xsl:template match="text()">
		<xsl:value-of select="." />
	</xsl:template>
	
</xsl:stylesheet>