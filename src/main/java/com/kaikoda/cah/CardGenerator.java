package com.kaikoda.cah;

import java.io.IOException;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A utility for generating a printable deck of Cards Against Humanity.
 * 
 * @author Sheila Thomson
 * 
 */
public class CardGenerator implements ErrorListener {

	private static final String PATH_TO_DEDUPING_XSL = "/xsl/remove_duplicates.xsl";
	private static final String PATH_TO_TRANSLATION_XSL = "/xsl/translate.xsl";

	private DocumentBuilder documentBuilder;

	private TransformerFactory transformerFactory;

	/**
	 * Default constructor.
	 * 
	 * @throws CardGeneratorConfigurationException
	 *             when it's not possible to construct a usable instance of
	 *             CardGenerator.
	 */
	public CardGenerator() throws CardGeneratorConfigurationException {

		// Prepare for building DOM documents
		this.setDocumentBuilder();

		// Create a transformer to execute the transformation
		this.setTransformerFactory();

	}

	public void error(TransformerException exception) throws TransformerException {
		// TODO: implement verbosity levels
		System.err.println(exception.getMessage());
	}

	public void fatalError(TransformerException exception) {
		// TODO: implement verbosity levels
		System.err.println(exception.getMessage());
		System.exit(1);
	}

	/**
	 * @return the DOM Document builder used by this instance of CardGenerator.
	 */
	public DocumentBuilder getDocumentBuilder() {
		return this.documentBuilder;
	}

	public void warning(TransformerException exception) {
		// TODO: implement verbosity levels
		System.err.println(exception.getMessage());
	}

	/**
	 * Translates the card data from one language into another (if required).
	 * 
	 * @param xml
	 *            untranslated data for the cards.
	 * @param params
	 *            the target language and dictionary to use.
	 * @return the translated card data.
	 * @throws TransformerException
	 *             if an unrecoverable error occurs during the translation.
	 * @throws IOException
	 *             if the file containing the XSLT for carrying out the
	 *             translation can't be found or read.
	 * @throws SAXException
	 *             if the XSLT for executing the translation can't be parsed.
	 */
	protected Document translate(Source xml, TreeMap<String, String> params) throws TransformerException, SAXException, IOException {

		Source xsl = this.getXsl(CardGenerator.PATH_TO_TRANSLATION_XSL);

		// Translate the card data and return the result
		return this.transform(xml, xsl, params);

	}

	/**
	 * Removes duplicates in the card data.
	 * 
	 * @param xml
	 *            card data.
	 * @return de-duped card data.
	 * @throws TransformerException
	 *             if an unrecoverable error occurs during the translation.
	 * @throws IOException
	 *             if the file containing the XSLT for carrying out the
	 *             translation can't be found or read.
	 * @throws SAXException
	 *             if the XSLT for executing the translation can't be parsed.
	 */
	protected Document dedupe(Source xml) throws TransformerException, SAXException, IOException {

		Source xsl = this.getXsl(CardGenerator.PATH_TO_DEDUPING_XSL);

		// Remove duplicates in the card data and return the result
		return this.transform(xml, xsl, null);

	}
	
	private Transformer getTransformer(Source xsl, TreeMap<String, String> params) throws TransformerConfigurationException {

		// Use the transformer factory to create a new transformer
		Transformer transformer = transformerFactory.newTransformer(xsl);

		// Specify that errors encountered during the transformation should
		// be handled by the card generator itself.
		transformer.setErrorListener(this);

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

		// Return configured transformer
		return transformer;

	}

	private Source getXsl(String path) throws SAXException, IOException {
		return new DOMSource(documentBuilder.parse(this.getClass().getResourceAsStream(path)));
	}

	private void setDocumentBuilder() throws CardGeneratorConfigurationException {

		// Prepare for DOM Document building
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setExpandEntityReferences(false);
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setValidating(false);
		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		try {
			this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new CardGeneratorConfigurationException("Unable to configure document builder.");
		}

	}

	private void setTransformerFactory() throws CardGeneratorConfigurationException {

		// Specify that Saxon should be used as the transformer instead of the
		// system default
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

		// Create a transformer factory
		this.transformerFactory = TransformerFactory.newInstance();

	}

	private Document transform(Source xml, Source xsl, TreeMap<String, String> params) throws TransformerException {

		// Create a container to hold the result of the transformation
		documentBuilder.reset();
		Document document = documentBuilder.newDocument();
		DOMResult result = new DOMResult(document);

		Transformer transformer = this.getTransformer(xsl, params);

		transformer.transform(xml, result);

		return document;

	}

}
