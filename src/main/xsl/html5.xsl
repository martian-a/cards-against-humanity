<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:cah="http://cah.kaikoda.com"
	version="2.0" 
	exclude-result-prefixes="#all">
	
	<xsl:output indent="yes" encoding="UTF-8" method="xml" media-type="text/html" doctype-public="HTML" omit-xml-declaration="yes" />
	
	<xsl:template match="/">	
		<xsl:result-document>
			<html>
				<xsl:apply-templates select="game/@xml:lang" />
				<xsl:apply-templates select="game" mode="meta-data" />
				<xsl:apply-templates select="game" mode="content" />
			</html>
		</xsl:result-document>		
	</xsl:template>
	
	<xsl:template match="game" mode="meta-data">
		<head>
			<title>Cards Against Humanity</title>
			<style type="text/css"><![CDATA[
				td {
					width: 4.75cm;
					height: 4.75cm;
				}
				.black .page {
					background-color: black;
					color: white;
				}
				.white .page {
					background-color: white;
					color: black;
				}
			]]></style>
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
				<div class="page">				
					<table>
						<tbody>	
							<!-- Group into rows -->
							<xsl:for-each-group select="current-group()" group-starting-with="card[(position() mod 4) = 1]">													
								<tr>
									<xsl:apply-templates select="current-group()" />
								</tr>
							</xsl:for-each-group>
						</tbody>	
					</table>
				</div>
		</xsl:for-each-group>
		</section>
	</xsl:template>
	
	<xsl:template match="card">
		<td>
			<xsl:value-of select="." />
		</td>
	</xsl:template>
	
</xsl:stylesheet>