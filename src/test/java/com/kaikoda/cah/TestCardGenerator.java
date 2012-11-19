package com.kaikoda.cah;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Sheila Thomson
 * 
 */
public class TestCardGenerator {

	/**
	 * A reusable instance of CardGenerator.
	 */
	private CardGenerator generator;

	/**
	 * Configure the test environment prior to each test.
	 * 
	 * @throws CardGeneratorConfigurationException
	 *             if it's not possible to construct a usable instance of
	 *             CardGenerator.
	 */
	@Before
	public void setup() throws CardGeneratorConfigurationException {
		
		this.generator = new CardGenerator();
		
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreComments(true);
		
	}

	/**
	 * Check that it's possible to construct a usable instance of CardGenerator.
	 */
	@Test
	public void testCardGenerator_constructor() {
		assertNotNull(generator);
	}

	/**
	 * Check that it's possible to retrieve the document builder from the card
	 * generator.
	 */
	@Test
	public void testCardGeneratorGetDocumentBuilder() {

		assertTrue(generator.getDocumentBuilder() instanceof DocumentBuilder);

	}

	/**
	 * Check that the Card Generator doesn't fail when params is null.
	 * 
	 * @throws SAXException
	 *             if an error occurs while building one of the test or control
	 *             documents.
	 * @throws IOException
	 *             if an error occurs while reading one of the test or control
	 *             documents.
	 * @throws TransformerException
	 *             is an unrecoverable error occurs during the transformation.
	 */
	@Test
	public void testCardGeneratorTranslate_params_null() throws SAXException, IOException, TransformerException {

		// Retrieve the test card data for British CAH.
		DocumentBuilder documentBuilder = generator.getDocumentBuilder();
		Source xml = new DOMSource(documentBuilder.parse(this.getClass().getResourceAsStream("/data/test/cards/uk.xml")));

		// Build the cards
		Document result = generator.translate(xml, null);

		// Retrieve the control card data for British CAH.
		Document expected = documentBuilder.parse(this.getClass().getResourceAsStream("/data/control/cards/uk.xml"));

		// Check that the result is untranslated (as no dictionary has been
		// specified).
		assertXMLEqual(expected, result);

	}

	/**
	 * Check that a null parameter value is ignored.
	 * 
	 * @throws SAXException
	 *             if an error occurs while building one of the test or control
	 *             documents.
	 * @throws IOException
	 *             if an error occurs while reading one of the test or control
	 *             documents.
	 * @throws TransformerException
	 *             is an unrecoverable error occurs during the transformation.
	 */
	@Test
	public void testCardGeneratorTranslate_params_nullValue() throws SAXException, IOException, TransformerException {

		// Retrieve the test card data for British CAH.
		DocumentBuilder documentBuilder = generator.getDocumentBuilder();
		Source xml = new DOMSource(documentBuilder.parse(this.getClass().getResourceAsStream("/data/test/cards/uk.xml")));

		// Build the parameter list
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("path-to-dictionary", this.getClass().getResource("/data/control/dictionaries/english.xml").getPath());
		params.put("output-language", null);

		// Build the cards
		Document result = generator.translate(xml, params);

		// Retrieve the control card data for American CAH.
		Document expected = documentBuilder.parse(this.getClass().getResourceAsStream("/data/control/cards/usa.xml"));

		// Check that the result is the same cards, translated into American
		// English (which is the default output language).
		assertXMLEqual(expected, result);

	}

	/**
	 * Check that the Card Generator falls back to the default output language
	 * when none other is specified (and an English dictionary is available).
	 * 
	 * @throws SAXException
	 *             if an error occurs while building one of the test or control
	 *             documents.
	 * @throws IOException
	 *             if an error occurs while reading one of the test or control
	 *             documents.
	 * @throws TransformerException
	 *             is an unrecoverable error occurs during the transformation.
	 */
	@Test
	public void testCardGeneratorTranslate_params_defaultOutputLangage() throws SAXException, IOException, TransformerException {

		// Retrieve the test card data for British CAH.
		DocumentBuilder documentBuilder = generator.getDocumentBuilder();
		Source xml = new DOMSource(documentBuilder.parse(this.getClass().getResourceAsStream("/data/test/cards/uk.xml")));

		// Build the parameter list
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("path-to-dictionary", this.getClass().getResource("/data/control/dictionaries/english.xml").getPath());

		// Build the cards
		Document result = generator.translate(xml, params);

		// Retrieve the control card data for American CAH.
		Document expected = documentBuilder.parse(this.getClass().getResourceAsStream("/data/control/cards/usa.xml"));

		// Check that the result is the same cards, translated into American
		// English (which is the default output language).
		assertXMLEqual(expected, result);

	}

	/**
	 * Check that the Card Generator correctly translates from American to
	 * British English.
	 * 
	 * @throws SAXException
	 *             if an error occurs while building one of the test or control
	 *             documents.
	 * @throws IOException
	 *             if an error occurs while reading one of the test or control
	 *             documents.
	 * @throws TransformerException
	 *             is an unrecoverable error occurs during the transformation.
	 */
	@Test
	public void testCardGeneratorTranslate_english() throws SAXException, IOException, TransformerException {

		// Retrieve the test card data for American CAH.
		DocumentBuilder documentBuilder = generator.getDocumentBuilder();
		Source xml = new DOMSource(documentBuilder.parse(this.getClass().getResourceAsStream("/data/test/cards/usa.xml")));

		// Build the parameter list
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("path-to-dictionary", this.getClass().getResource("/data/control/dictionaries/english.xml").getPath());
		params.put("output-language", "en-gb");

		// Build the cards
		Document result = generator.translate(xml, params);

		// Retrieve the test card data for British CAH.
		Document expected = documentBuilder.parse(this.getClass().getResourceAsStream("/data/control/cards/uk.xml"));

		// Check that the result is the same cards, translated into British
		// English (which is the default output language).
		assertXMLEqual(expected, result);

	}

	/**
	 * Check that the Card Generator correctly removes duplicates entries in the
	 * card data.
	 * 
	 * @throws SAXException
	 *             if an error occurs while building one of the test or control
	 *             documents.
	 * @throws IOException
	 *             if an error occurs while reading one of the test or control
	 *             documents.
	 * @throws TransformerException
	 *             is an unrecoverable error occurs during the transformation.
	 */
	@Test
	public void testCardGeneratorDedupe() throws SAXException, IOException, TransformerException {

		// Retrieve test card data containing multiple decks and duplicate cards
		DocumentBuilder documentBuilder = generator.getDocumentBuilder();
		documentBuilder.reset();
		Source xml = new DOMSource(documentBuilder.parse(this.getClass().getResourceAsStream("/data/test/cards/duplicates.xml")));

		// Build the cards
		Document result = generator.dedupe(xml);

		// Retrieve test card data that contains the same cards but no duplicates.
		documentBuilder.reset();
		Document expected = documentBuilder.parse(this.getClass().getResourceAsStream("/data/control/cards/no_duplicates.xml"));
		
		// Check that the result is the same cards, but in just two decks and
		// with no duplicates.
		assertXMLEqual(expected, result);

	}

}
