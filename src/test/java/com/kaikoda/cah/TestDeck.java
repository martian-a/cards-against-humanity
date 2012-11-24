package com.kaikoda.cah;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Sheila Thomson
 */
public class TestDeck {

	/**
	 * American card data.
	 */
	private static final File DECK_DATA_USA = new File(TestDeck.class.getResource("/data/control/cards/usa.xml").getFile());

	/**
	 * English dictionary.
	 */
	private static final File DICTIONARY_DATA_ENGLISH = new File(TestDeck.class.getResource("/data/control/dictionaries/english.xml").getFile());

	/**
	 * A reusable instance of a document builder configured for use by Deck.
	 */
	private static DocumentBuilder documentBuilder;

	/**
	 * For declaring what kind of exception is expected, when one is expected.
	 */
	@Rule
	public ExpectedException exception = ExpectedException.none();

	/**
	 * @throws CardGeneratorConfigurationException if it's not possible to
	 *         construct a usable instance of CardGenerator.
	 * @throws ParserConfigurationException
	 */
	@BeforeClass
	public static void setupOnce() throws CardGeneratorConfigurationException, ParserConfigurationException {
		documentBuilder = Deck.newDocumentBuilder();
	}

	/**
	 * Configure the test environment prior to each test.
	 * 
	 * @throws IOException when it's not possible to read the contents of an
	 *         input file.
	 * @throws SAXException when it's not possible to parse the contents of an
	 *         input file.
	 * @throws ParserConfigurationException
	 */
	@Before
	public void setup() throws SAXException, IOException, ParserConfigurationException {

		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreComments(true);

	}

	/**
	 * Check that it's possible to construct a usable instance of Deck.
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	@Test
	public void testDeck_constructor() throws SAXException, IOException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument(DECK_DATA_USA));

		// Check that an instance of deck has been created
		assertNotNull(customDeck);

	}

	/**
	 * Check that it's not possible to construct an instance of Deck without
	 * data.
	 */
	@Test
	public void testDeck_constructor_noCardData() {

		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Data required.");

		new Deck(null);

	}

	@Test
	public void testDeckBlank_elementBlanks() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		// Retrieve test card data containing blanks marked-up using <blank />.
		File xml = this.getFile("/data/test/cards/blanks_element.xml");

		// Build the cards
		Deck customDeck = new Deck(this.getDocument(xml));
		customDeck.blank();
		Document result = customDeck.getData();

		// Retrieve test card data that contains the same cards but no
		// duplicates.
		Document expected = this.getDocument("/data/control/cards/blanks_element.xml");

		// Check that the result is the same cards, but in just two decks and
		// with no duplicates.
		assertXMLEqual(expected, result);

	}

	@Test
	public void testDeckBlank_mixedBlanks() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		// Retrieve test card data containing blanks marked-up using a mix of
		// underscores and <blank />.
		File xml = this.getFile("/data/test/cards/blanks_mixed.xml");

		// Build the cards
		Deck customDeck = new Deck(this.getDocument(xml));
		customDeck.blank();
		Document result = customDeck.getData();

		// Retrieve test card data that contains the same cards but no
		// duplicates.
		Document expected = this.getDocument("/data/control/cards/blanks_element.xml");

		// Check that the result is the same cards, but in just two decks and
		// with no duplicates.
		assertXMLEqual(expected, result);

	}

	@Test
	public void testDeckBlank_underscoreBlanks() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		// Retrieve test card data containing blanks marked-up using
		// underscores.
		File xml = this.getFile("/data/test/cards/blanks_underscore.xml");

		// Build the cards
		Deck customDeck = new Deck(this.getDocument(xml));
		customDeck.blank();
		Document result = customDeck.getData();

		// Retrieve test card data that contains the same cards but no
		// duplicates.
		Document expected = this.getDocument("/data/control/cards/blanks_element.xml");

		// Check that the result is the same cards, but in just two decks and
		// with no duplicates.
		assertXMLEqual(expected, result);

	}

	/**
	 * Check that it's possible to retrieve a document builder from Deck.
	 * 
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testDeckGetDocumentBuilder() throws ParserConfigurationException {

		assertTrue(Deck.newDocumentBuilder() instanceof DocumentBuilder);

	}

	@Test
	public void testDeckGetLocale() throws SAXException, IOException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument("/data/test/cards/netherlands.xml"));

		Locale expected = Locale.forLanguageTag("en-nl");
		Locale result = customDeck.getLocale();

		assertEquals(expected, result);
		assertEquals("en-NL", result.toLanguageTag());

	}

	@Test
	public void testDeckGetLocale_notSpecified() throws SAXException, IOException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument("/data/test/cards/no_language.xml"));

		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("The language of the card deck isn't specified.");

		customDeck.getLocale();

	}

	/**
	 * Check that blanks represented a <blank /> element are preserved.
	 * 
	 * @throws SAXException when there's an unrecoverable problem while parsing
	 *         the data.
	 * @throws IOException when it's not possible to read the card data.
	 * @throws ParserConfigurationException when it's not possible to create a
	 *         correctly configured instance of the parser.
	 */
	@Test
	public void testDeckIsBlanked_inputElementBlanks() throws SAXException, IOException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument("/data/test/cards/blanks_element.xml"));
		assertEquals(true, customDeck.isBlanked());

	}

	/**
	 * Check that blanks represented by a sequence of consecutive underscores
	 * are replaced with a single <blank /> element.
	 * 
	 * @throws SAXException when there's an unrecoverable problem while parsing
	 *         the data.
	 * @throws IOException when it's not possible to read the card data.
	 * @throws ParserConfigurationException when it's not possible to create a
	 *         correctly configured instance of the parser.
	 */
	@Test
	public void testDeckIsBlanked_inputMixedBlanks() throws SAXException, IOException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument("/data/test/cards/blanks_mixed.xml"));
		assertEquals(false, customDeck.isBlanked());

	}

	/**
	 * Check that blanks represented by a sequence of consecutive underscores
	 * are replaced with a single <blank /> element.
	 * 
	 * @throws SAXException when there's an unrecoverable problem while parsing
	 *         the data.
	 * @throws IOException when it's not possible to read the card data.
	 * @throws ParserConfigurationException when it's not possible to create a
	 *         correctly configured instance of the parser.
	 */
	@Test
	public void testDeckIsBlanked_inputUnderscoreBlanks() throws SAXException, IOException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument("/data/test/cards/blanks_underscore.xml"));
		assertEquals(false, customDeck.isBlanked());

	}

	@Test
	public void testDeckNewTransformer_nullXsl() throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException {

		// Create a deck, any deck.
		Deck customDeck = new Deck(this.getDocument("/data/test/cards/usa.xml"));

		// Request a new instance of Transformer, without any XSLT
		Transformer result = Deck.newTransformer(null);

		// Check that an instance has been returned
		assertNotNull(result);

	}

	@Test
	public void testDeckParse_fileIn() throws IOException, SAXException, ParserConfigurationException {

		File xml = this.getFile("/data/test/cards/uk.xml");
		assertNotNull(xml);

		Document result = Deck.parse(xml);
		assertNotNull(result);

		assertEquals("game", result.getDocumentElement().getNodeName());

	}

	@Test
	public void testDeckParse_stringIn() throws IOException, SAXException, ParserConfigurationException {

		String xml = this.getXmlString("/data/test/cards/uk.xml");
		assertNotNull(xml);

		Document result = Deck.parse(xml);
		assertNotNull(result);

		assertEquals("game", result.getDocumentElement().getNodeName());

	}

	@Test
	public void testDeckToHtml() throws IOException, SAXException, ParserConfigurationException, TransformerException {

		File file = this.getFile("/data/control/cards/netherlands.xml");
		Document xml = this.getDocument(file);
		String expected = this.getXmlString("/data/control/cards/netherlands.html");

		Deck customDeck = new Deck(xml);
		String result = customDeck.toHtml();

		assertXMLEqual(expected, result);

	}

	@Test
	public void testDeckToString() throws IOException, SAXException, ParserConfigurationException {

		File file = this.getFile("/data/control/cards/netherlands.xml");
		Document xml = this.getDocument(file);
		String expected = this.getXmlString(file);

		Deck customDeck = new Deck(xml);
		String result = customDeck.toString();

		assertXMLEqual(expected, result);

	}

	@Test
	public void testDeckTransform_nullParamValue() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		// Retrieve the control card data for Dutch English CAH (so can assume
		// all pre-processing complete).
		Document xml = this.getDocument("/data/control/cards/netherlands.xml");

		Deck customDeck = new Deck(xml);
		customDeck.setErrorListener(new ProgressReporter());

		// Create a container to hold the result of the transformation
		DocumentBuilder documentBuilder = Deck.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		DOMResult result = new DOMResult(document);

		// Build the parameter list
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("path-to-dictionary", TestDeck.DICTIONARY_DATA_ENGLISH.getAbsolutePath());
		params.put("output-language", null);

		customDeck.transform(new DOMSource(xml), Deck.getXsl(Deck.PATH_TO_TRANSLATION_XSL), result, params);

		// Retrieve the control card data for American CAH.
		Document expected = xml;

		// Check that the result is the same cards, translated into American
		// English (which is the default output language).
		assertXMLEqual(expected, document);

	}

	@Test
	public void testDeckTranslate() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument(TestDeck.DECK_DATA_USA));
		assertEquals(Locale.forLanguageTag("en-us"), customDeck.getLocale());

		customDeck.translate(Locale.forLanguageTag("en-gb"), TestDeck.DICTIONARY_DATA_ENGLISH);
		assertEquals(Locale.forLanguageTag("en-gb"), customDeck.getLocale());

	}

	@Test
	public void testDeckTranslate_dictionaryNotFound() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument("/data/test/cards/netherlands.xml"));
		customDeck.setErrorListener(new ProgressReporter());

		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Dictionary not found.");

		customDeck.translate(Locale.forLanguageTag("en-gb"), new File("non-existant.file"));

	}

	@Test
	public void testDeckTranslate_nullDictionary() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument("/data/test/cards/netherlands.xml"));
		customDeck.setErrorListener(new ProgressReporter());

		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Dictionary required.");

		customDeck.translate(Locale.forLanguageTag("en-gb"), null);

	}

	@Test
	public void testDeckTranslate_nullTargetLanguage() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument("/data/test/cards/netherlands.xml"));
		customDeck.setErrorListener(new ProgressReporter());

		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("New language not specified.");

		customDeck.translate(null, TestDeck.DICTIONARY_DATA_ENGLISH);

	}

	@Test
	public void testDeckTranslate_sourceLocaleUnknown() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		Deck customDeck = new Deck(this.getDocument("/data/test/cards/no_language.xml"));

		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("The language of the card deck isn't specified.");

		customDeck.translate(Locale.forLanguageTag("en-gb"), TestDeck.DICTIONARY_DATA_ENGLISH);

	}

	/**
	 * Check that the language and data remain unchanged if the target language
	 * is the same as the current language.
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testDeckTranslate_targetLanguageIsSourceLanguage() throws SAXException, IOException, TransformerException, ParserConfigurationException {

		Document expected = this.getDocument("/data/test/cards/netherlands.xml");
		Deck customDeck = new Deck(expected);
		customDeck.setErrorListener(new ProgressReporter());

		Locale targetLanguage = Locale.forLanguageTag("en-nl");
		assertEquals(targetLanguage, customDeck.getLocale());

		customDeck.translate(targetLanguage, TestDeck.DICTIONARY_DATA_ENGLISH);
		Document result = customDeck.getData();

		assertEquals(targetLanguage, customDeck.getLocale());
		assertXMLEqual(expected, result);
	}

	/**
	 * Builds a DOM Document from the file specified.
	 * 
	 * @param input a file containing XML.
	 * @return the XML as a DOM Document.
	 * @throws SAXException when something goes wrong while building the DOM
	 *         Document.
	 * @throws IOException when something goes wrong while reading or writing to
	 *         the file.
	 * @throws ParserConfigurationException
	 */
	private Document getDocument(File input) throws SAXException, IOException, ParserConfigurationException {

		TestDeck.documentBuilder.reset();

		return TestDeck.documentBuilder.parse(input);
	}

	/**
	 * Builds a DOM Document from a file at the path specified.
	 * 
	 * @param path a path to a file containing XML.
	 * @return the XML as a DOM Document.
	 * @throws SAXException when something goes wrong while building the DOM
	 *         Document.
	 * @throws IOException when something goes wrong while reading or writing to
	 *         the file.
	 * @throws ParserConfigurationException
	 */
	private Document getDocument(String path) throws SAXException, IOException, ParserConfigurationException {
		return this.getDocument(this.getFile(path));
	}

	/**
	 * Finds a file that matches the path specified.
	 * 
	 * @param path a potential path to the file sought.
	 * @return the file sought.
	 */
	private File getFile(String path) {
		return new File(this.getClass().getResource(path).getFile());
	}

	/**
	 * Builds an XML String from the file specified.
	 * 
	 * @param input a file containing XML.
	 * @return the XML as an XML String.
	 * @throws IOException when something goes wrong while reading the input
	 *         file.
	 */
	private String getXmlString(File input) throws IOException {

		return FileUtils.readFileToString(input, "UTF-8");

	}

	/**
	 * Builds an XML String from the file at the location specified.
	 * 
	 * @param input a file containing XML.
	 * @return the XML as an XML String.
	 * @throws IOException when something goes wrong while reading the input
	 *         file.
	 */
	private String getXmlString(String input) throws IOException {
		return this.getXmlString(this.getFile(input));
	}

}
