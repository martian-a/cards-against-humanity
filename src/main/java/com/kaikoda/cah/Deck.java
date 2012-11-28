/*
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
 */
package com.kaikoda.cah;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Observable;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Represents a deck of Cards Against Humanity.
 * 
 * @author Sheila Thomson
 *
 */
public class Deck extends Observable {

	/**
	 * XSLT for removing duplicates in the card data.
	 */
	protected static final String PATH_TO_DEDUPING_XSL = "/xsl/remove_duplicates.xsl";

	/**
	 * XSLT for creating an HTML5 set of Cards Against Humanity.
	 */
	protected static final String PATH_TO_HTML_XSL = "/xsl/html5.xsl";

	/**
	 * XSLT for translating card data from one culture to another.
	 */
	protected static final String PATH_TO_STRING_XSL = "/xsl/to_string.xsl";

	/**
	 * XSLT for translating card data from one culture to another.
	 */
	protected static final String PATH_TO_TRANSLATION_XSL = "/xsl/translate.xsl";

	/**
	 * The card data for this deck.
	 */
	private Document data;

	private ErrorListener errorListener;

	/**
	 * Default constructor. Stores the card data.
	 * 
	 * @param xml the card data for this deck.
	 */
	public Deck(Document xml) {

		this.setData(xml);

		if (this.data == null) {
			throw new IllegalArgumentException("Data required.");
		}

	}

	/**
	 * Creates and configures a re-usable instance of DocumentBuilder.
	 * 
	 * @throws ParserConfigurationException when it's not possible to configure
	 *         the DocumentBuilder as required.
	 */
	public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {

		// Prepare for DOM Document building
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setExpandEntityReferences(true);
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setValidating(false);
		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		return documentBuilderFactory.newDocumentBuilder();

	}

	public static Document parse(File xml) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilder builder = Deck.newDocumentBuilder();
		return builder.parse(xml);
	}

	public static Document parse(String xml) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilder builder = Deck.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(xml)));
	}

	/**
	 * Returns an XSLT stylesheet as an instance of Source, for use in
	 * transformations.
	 * 
	 * @param path a pointer to a file containing the stylesheet required.
	 * @return the stylesheet requested.
	 * @throws SAXException when something goes wrong while building the Source.
	 * @throws IOException when something goes wrong while reading the file that
	 *         contains the XSLT.
	 * @throws ParserConfigurationException
	 */
	protected static Source getXsl(String path) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilder builder = Deck.newDocumentBuilder();
		return new DOMSource(builder.parse(Deck.class.getResourceAsStream(path)));
	}

	/**
	 * Creates and configures a Transformer.
	 * 
	 * @throws TransformerConfigurationException
	 */
	protected static Transformer newTransformer(Source xsl) throws TransformerConfigurationException {

		// Specify that Saxon should be used as the transformer instead of the
		// system default
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

		// Create a transformer factory
		TransformerFactory factory = TransformerFactory.newInstance();

		if (xsl != null) {
			return factory.newTransformer(xsl);
		}

		return factory.newTransformer();

	}

	/**
	 * Replace underscore blanks with <blank />
	 */
	public void blank() throws SAXException, IOException, ParserConfigurationException, TransformerException {

		// Check that the data contains a blank represented as a sequence of
		// underscores.
		if (this.isBlanked()) {

			// No underscore blanks found; nothing further to do.
			return;

		}

		// Convert the current data into an XML String.
		String xml = this.toString();

		// Find all instances of more than one consecutive underscore and
		// replace each sequence with <blank />
		Pattern regexp = Pattern.compile("__+");
		Matcher matcher = regexp.matcher(xml);
		String result = matcher.replaceAll("<blank />");

		// Convert the newly blanked XML String back into a DOM Document.
		Document document = Deck.parse(result);

		// Replace the stored data with the newly blanked version.
		this.setData(document);

		// De-dupe in case duplicates created as a result of blanking
		this.dedupe();

	}

	/**
	 * Merge duplicate cards.
	 * 
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void dedupe() throws TransformerException, ParserConfigurationException, SAXException, IOException {

		// Retrieve the XSLT stylesheet that will execute the merge
		Source xsl = Deck.getXsl(Deck.PATH_TO_DEDUPING_XSL);

		// Create a container to hold the result of the transformation
		DocumentBuilder documentBuilder = Deck.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		DOMResult result = new DOMResult(document);

		// Remove duplicates in the card data and return the result
		this.transform(new DOMSource(this.data), xsl, result, null);

		// Replace the stored data with the newly de-duped version
		this.setData(document);

	}

	public Document getData() {
		return this.data;
	}

	public ErrorListener getErrorListener() {
		return this.errorListener;
	}

	public Locale getLocale() {

		// Retrieve the root element
		Element game = this.data.getDocumentElement();

		// Retrieve the game language
		String xmlLang = game.getAttribute("xml:lang");

		if (xmlLang.equals("")) {
			throw new IllegalArgumentException("The language of the card deck isn't specified.");
		}

		return Locale.forLanguageTag(xmlLang);

	}

	public boolean hasErrorListener() {
		if (this.errorListener != null) {
			return true;
		}
		return false;
	}

	/**
	 * Check whether the data contains any unconverted underscore blanks.
	 * 
	 * @return true if the data contains any blanks represented by multiple
	 *         underscores instead of a single <blank /> element. False if all
	 *         blanks are represented by a <blank /> element.
	 */
	public boolean isBlanked() {

		String xml = this.toString();

		Pattern regexp = Pattern.compile("__+");
		Matcher matcher = regexp.matcher(xml);

		if (matcher.find()) {
			return false;
		}

		return true;
	}

	/**
	 * Can be used to specify a Listener for reporting exceptions generated
	 * during a Transformation.
	 * 
	 * @param listener an instance of ErrorListener.
	 */
	public void setErrorListener(ErrorListener listener) {
		this.errorListener = listener;
	}

	/**
	 * @return the current data as an HTML5 String.
	 */
	public String toHtml() throws SAXException, IOException, ParserConfigurationException, TransformerException {

		// Create a container to hold the result of the transformation
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);

		Source xsl = Deck.getXsl(Deck.PATH_TO_HTML_XSL);

		// Transform the current data into an XML string.
		this.transform(new DOMSource(this.data), xsl, result, null);

		// Return the XML String
		return writer.toString();

	}

	/**
	 * @return the current data as an XML String.
	 */
	@Override
	public String toString() {

		String xmlString = "";

		// Create a container to hold the result of the transformation
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);

		try {

			// Transform the current data into an XML string.
			this.transform(new DOMSource(this.data), null, result, null);

			// Return the XML String
			xmlString = writer.toString();

		} catch (TransformerException e) {
			xmlString = this.serialise();
		} catch (SAXException e) {
			xmlString = this.serialise();
		} catch (IOException e) {
			xmlString = this.serialise();
		} catch (ParserConfigurationException e) {
			xmlString = this.serialise();
		}

		return xmlString;

	}

	/**
	 * Translate from original language into target language
	 */
	public void translate(Locale targetLanguage, File dictionary) throws SAXException, IOException, TransformerException, ParserConfigurationException {

		// Check that a target language has been specified.
		if (targetLanguage == null) {
			throw new IllegalArgumentException("New language not specified.");
		}

		// Check that the data is not already in the target language
		if (targetLanguage.equals(this.getLocale())) {

			// No translation required.
			return;

		}

		// Check that a dictionary has been specified
		if (dictionary == null) {
			throw new IllegalArgumentException("Dictionary required.");
		}

		// Check that the dictionary exists
		if (!dictionary.exists()) {
			throw new IllegalArgumentException("Dictionary not found.");
		}

		// Retrieve the XSLT stylesheet that will execute the translation
		Source xsl = Deck.getXsl(Deck.PATH_TO_TRANSLATION_XSL);

		// Create a container to hold the result of the transformation
		DocumentBuilder documentBuilder = Deck.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		DOMResult result = new DOMResult(document);

		// Build the parameter list
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("path-to-dictionary", dictionary.getAbsolutePath());
		params.put("output-language", targetLanguage.toLanguageTag());

		// Translate the card data and return the result
		this.transform(new DOMSource(this.data), xsl, result, params);

		// Replace the stored data with the newly translated version
		this.setData(document);

		// De-dupe in case duplicates created as a result of translation
		this.dedupe();

	}

	/**
	 * Transforms XML using the XSLT stylesheet and parameters specified.
	 * 
	 * @param xml the XML to be transformed.
	 * @param xsl the XSLT stylesheet to use for the transformation.
	 * @param result a container to hold the result of the transformation.
	 * @param params a list of parameters for configuring the XSLT stylesheet
	 *        prior to the transformation.
	 * @throws TransformerException when it's not possible to complete the
	 *         transformation.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	protected void transform(Source xml, Source xsl, Result result, TreeMap<String, String> params) throws TransformerException, SAXException, IOException, ParserConfigurationException {

		if (xsl == null) {
			xsl = Deck.getXsl(Deck.PATH_TO_STRING_XSL);
		}

		// Use the transformer factory to create a new transformer
		Transformer transformer = Deck.newTransformer(xsl);
		if (this.hasErrorListener()) {
			transformer.setErrorListener(this.getErrorListener());
		}

		// Pass parameters through to the XSLT
		if (params != null) {

			// Loop through all the parameters by name
			for (String name : params.keySet()) {

				// Use the name to retrieve the value
				String value = params.get(name);

				// If the parameter value isn't null, pass it through
				if (value != null) {
					transformer.setParameter(name, value);
				}
			}
		}

		transformer.transform(xml, result);

	}

	private String serialise() {

		// Unable to create XML string, fall back.
		DOMImplementationLS domImplementation = (DOMImplementationLS) this.data.getImplementation();
		LSSerializer lsSerializer = domImplementation.createLSSerializer();
		return lsSerializer.writeToString(this.data);

	}

	private void setData(Document xml) {

		// If there is new data, replace the old data with it.
		if (xml != null) {
			this.data = xml;
		}

	}

}
