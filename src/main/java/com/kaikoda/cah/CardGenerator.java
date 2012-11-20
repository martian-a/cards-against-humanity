package com.kaikoda.cah;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A utility for generating a printable deck of Cards Against Humanity.
 * 
 * @author Sheila Thomson
 */
public class CardGenerator implements ErrorListener {

	/**
	 * XSLT for removing duplicates in the card data.
	 */
	private static final String PATH_TO_DEDUPING_XSL = "/xsl/remove_duplicates.xsl";

	/**
	 * XSLT for creating an HTML5 set of Cards Against Humanity.
	 */
	private static final String PATH_TO_HTML_XSL = "/xsl/html5.xsl";

	/**
	 * XSLT for translating card data from one culture to another.
	 */
	private static final String PATH_TO_TRANSLATION_XSL = "/xsl/translate.xsl";

	/**
	 * For building DOM Documents from XML.
	 */
	private DocumentBuilder documentBuilder;

	/**
	 * For creating Transformers.
	 */
	private TransformerFactory transformerFactory;

	/**
	 * Default constructor.
	 * 
	 * @throws CardGeneratorConfigurationException when it's not possible to
	 *         construct a usable instance of CardGenerator.
	 */
	public CardGenerator() throws CardGeneratorConfigurationException {

		// Prepare for building DOM documents
		this.setDocumentBuilder();

		// Create a transformer to execute the transformation
		this.setTransformerFactory();

	}

	/**
	 * Generates a printable deck of Cards Against Humanity.
	 * 
	 * @param args where to find the data file and optionally the dictionary and
	 *        target language if translation is required.
	 * @throws CardGeneratorConfigurationException when it's not possible to
	 *         construct a usable instance of CardGenerator.
	 */
	public static void main(String[] args) throws CardGeneratorConfigurationException {

		CardGenerator generator = new CardGenerator();

		// Configure valid options accepted from the command-line
		Options options = CardGenerator.getOptions();

		TreeMap<String, String> params = CardGenerator.parseArgs(options, args);

		if (params != null) {

			File data = new File(params.remove("path-to-data"));

			if (!data.exists()) {

				System.err.println("File not found: " + data.getPath());

			} else {

				generator.generate(data, params);

			}
		}

	}

	/**
	 * @return A collection containing the options accepted by this application
	 *         from the command-line.
	 */
	private static Options getOptions() {

		// Configure valid options accepted from the command-line
		Options options = new Options();

		// The name of the file or directory to be processed
		options.addOption("f", true, "path to the card data file.");

		// The location of the distro folder
		options.addOption("d", true, "path to the dictionary to use for translating.");

		// The abbreviated name of the schema that the input file/s conform/s
		// to, eg. jats, npg, aj
		options.addOption("l", true, "language code, as per http://www.w3.org/International/articles/language-tags/");

		// A request for help with using the application
		options.addOption("h", "help", false, "list all parameters that can be specified at runtime");

		return options;
	}

	/**
	 * Prints an annotated list of the options that are valid for use with this
	 * application.
	 * 
	 * @param options the list of options
	 */
	private static void help(Options options) {

		System.out.println("\nThe options that can be used with this application are:\n");

		@SuppressWarnings("unchecked")
		Collection<Option> optionsCollection = options.getOptions();
		Iterator<Option> validOptions = optionsCollection.iterator();
		while (validOptions.hasNext()) {
			Option option = validOptions.next();

			System.out.println("-" + option.getOpt() + "\t" + option.getDescription());
		}

	}

	/**
	 * Converts an array of arguments supplied at run-time, checks them for
	 * validity and returns a list of valid option choices.
	 * 
	 * @param options a list of valid options.
	 * @param args an unchecked list expressing option choices.
	 * @return a valid list of the options chosen.
	 */
	private static TreeMap<String, String> parseArgs(Options options, String[] args) {

		File inputLocation = null;
		File dictionaryLocation = null;
		String targetLanguage = null;

		// Parse runtime options from the command-line
		CommandLineParser parser = new GnuParser();
		try {

			CommandLine line = parser.parse(options, args);

			// Check if help has been requested
			if (line.hasOption("h")) {

				// Provide help
				CardGenerator.help(options);

				// No futher processing required
				return null;

			}

			// Retrieve the input file
			if (line.hasOption("f")) {
				inputLocation = new File(line.getOptionValue("f"));
			}

			// Retrieve the dictionary
			if (line.hasOption("d")) {
				dictionaryLocation = new File(line.getOptionValue("d"));
			}

			// Retrieve the dictionary
			if (line.hasOption("l")) {
				targetLanguage = line.getOptionValue("l");
			}

		} catch (ParseException e) {
			System.err.println(e.getMessage());
			return null;
		}

		if (inputLocation == null) {
			System.err.println("Nothing to process; no file or directory was specified.");
			return null;
		}

		TreeMap<String, String> params = new TreeMap<String, String>();
		if (dictionaryLocation != null) {
			params.put("path-to-dictionary", dictionaryLocation.getAbsolutePath());
		}
		if (targetLanguage != null) {
			params.put("output-language", targetLanguage);
		}
		if (inputLocation != null) {
			params.put("path-to-data", inputLocation.getAbsolutePath());
		}

		return params;
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
	 * Generates a printable deck of Cards Against Humanity.
	 * 
	 * @param input card data
	 * @param params if translation is required, the target language and where
	 *        to find the dictionary.
	 */
	public void generate(File input, TreeMap<String, String> params) {

		Document xml = null;
		System.out.print("Reading card data...");
		try {

			documentBuilder.reset();
			xml = this.documentBuilder.parse(input);
			System.out.print("...data read.\n");

		} catch (SAXException e) {
			System.err.println("Unable to read card data.");
		} catch (IOException e) {
			System.err.println("Unable to read card data.");
		}

		if (xml == null) {
			System.exit(1);
		}

		System.out.print("Translating data...");
		try {
			xml = this.translate(xml, params);
			System.out.print("...translation complete.\n");
		} catch (TransformerException e) {
			System.err.println("Unable to complete translation.");
		} catch (SAXException e) {
			System.err.println("Unable to complete translation.");
		} catch (IOException e) {
			System.err.println("Unable to complete translation.");
		}

		System.out.print("Removing duplicates...");
		try {
			xml = this.dedupe(xml);
			System.out.print("...de-duping complete.\n");
		} catch (TransformerException e) {
			System.err.println("Unable to complete de-duping process.");
		} catch (SAXException e) {
			System.err.println("Unable to complete de-duping process.");
		} catch (IOException e) {
			System.err.println("Unable to complete de-duping process.");
		}

		System.out.print("Generating HTML...");
		try {

			String html = this.toHtml(xml);

			File outputLocation = new File("cards_against_humanity.html");
			FileUtils.writeStringToFile(outputLocation, html);

			System.out.print("...file saved:\n");
			System.out.println(outputLocation.getAbsolutePath());

		} catch (SAXException e) {
			System.err.println("Unable to save cards to file.");
		} catch (IOException e) {
			System.err.println("Unable to save cards to file.");
		} catch (TransformerException e) {
			System.err.println("Unable to save cards to file.");
		}

		System.out.print("Adding a dash of style...");
		try {

			File outputLocation = new File("style.css");
			FileUtils.copyURLToFile(this.getClass().getResource("/css/style.css"), outputLocation);

			System.out.print("...file saved:\n");
			System.out.println(outputLocation.getAbsolutePath());

		} catch (IOException e) {
			System.err.println("Unable to style.  Do it yourself.");
		}

		System.out.println("Card generation complete.");
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
	 * Removes duplicates in the card data.
	 * 
	 * @param xml card data.
	 * @return de-duped card data.
	 * @throws TransformerException if an unrecoverable error occurs during the
	 *         translation.
	 * @throws IOException if the file containing the XSLT for carrying out the
	 *         translation can't be found or read.
	 * @throws SAXException if the XSLT for executing the translation can't be
	 *         parsed.
	 */
	protected Document dedupe(Document xml) throws TransformerException, SAXException, IOException {

		Source xsl = this.getXsl(CardGenerator.PATH_TO_DEDUPING_XSL);

		// Remove duplicates in the card data and return the result
		return this.transformToDocument(xml, xsl, null);

	}

	/**
	 * Generates an HTML5 version of the card data provided.
	 * 
	 * @param xml card data.
	 * @return the cards as HTML5.
	 * @throws TransformerException if an unrecoverable error occurs during the
	 *         translation.
	 * @throws IOException if the file containing the XSLT for carrying out the
	 *         translation can't be found or read.
	 * @throws SAXException if the XSLT for executing the translation can't be
	 *         parsed.
	 */
	protected String toHtml(Document xml) throws SAXException, IOException, TransformerException {

		Source xsl = this.getXsl(CardGenerator.PATH_TO_HTML_XSL);

		// Translate the card data and return the result
		String result = this.transformToString(xml, xsl, null);

		return result;

	}

	/**
	 * Translates the card data from one language into another (if required).
	 * 
	 * @param xml untranslated data for the cards.
	 * @param params the target language and dictionary to use.
	 * @return the translated card data.
	 * @throws TransformerException if an unrecoverable error occurs during the
	 *         translation.
	 * @throws IOException if the file containing the XSLT for carrying out the
	 *         translation can't be found or read.
	 * @throws SAXException if the XSLT for executing the translation can't be
	 *         parsed.
	 */
	protected Document translate(Document xml, TreeMap<String, String> params) throws TransformerException, SAXException, IOException {

		Source xsl = this.getXsl(CardGenerator.PATH_TO_TRANSLATION_XSL);

		// Translate the card data and return the result
		return this.transformToDocument(xml, xsl, params);

	}

	/**
	 * Creates an instance of Transformer primed for use with the XSLT and
	 * parameters supplied.
	 * 
	 * @param xsl the XSLT that will be used to execute transformations.
	 * @param params a list of parameters to be supplied to the XSLT.
	 * @return an instance of Transformer that can execute transformations using
	 *         the XSLT and parameters supplied.
	 * @throws TransformerConfigurationException when it's not possible to
	 *         configure the Transformer as required.
	 */
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

	/**
	 * Returns an XSLT stylesheet as an instance of Source, for use in
	 * transformations.
	 * 
	 * @param path a pointer to a file containing the stylesheet required.
	 * @return the stylesheet requested.
	 * @throws SAXException when something goes wrong while building the Source.
	 * @throws IOException when something goes wrong while reading the file that
	 *         contains the XSLT.
	 */
	private Source getXsl(String path) throws SAXException, IOException {
		documentBuilder.reset();
		return new DOMSource(documentBuilder.parse(this.getClass().getResourceAsStream(path)));
	}

	/**
	 * Creates and configures a re-usable instance of DocumentBuilder.
	 * 
	 * @throws CardGeneratorConfigurationException when it's not possible to
	 *         configure the DocumentBuilder as required.
	 */
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

	/**
	 * Creates and configures a re-usable factory for generating instances of
	 * Transformer.
	 * 
	 * @throws CardGeneratorConfigurationException when it's not possible to
	 *         configure the TransformerFactory as required.
	 */
	private void setTransformerFactory() throws CardGeneratorConfigurationException {

		// Specify that Saxon should be used as the transformer instead of the
		// system default
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

		// Create a transformer factory
		this.transformerFactory = TransformerFactory.newInstance();

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
	 */
	private void transform(Source xml, Source xsl, Result result, TreeMap<String, String> params) throws TransformerException {

		Transformer transformer = this.getTransformer(xsl, params);
		transformer.transform(xml, result);

	}

	/**
	 * Transforms XML using the XSLT stylesheet and parameters specified, return
	 * the result as a DOM Document.
	 * 
	 * @param xml the XML to be transformed.
	 * @param xsl the XSLT stylesheet to use for the transformation.
	 * @param params a list of parameters for configuring the XSLT stylesheet
	 *        prior to the transformation.
	 * @return the result of the transformation as a DOM Document
	 * @throws TransformerException when it's not possible to complete the
	 *         transformation.
	 */
	private Document transformToDocument(Document xml, Source xsl, TreeMap<String, String> params) throws TransformerException {

		// Create a container to hold the result of the transformation
		documentBuilder.reset();
		Document document = documentBuilder.newDocument();
		DOMResult result = new DOMResult(document);

		this.transform(new DOMSource(xml), xsl, result, params);

		return document;
	}

	/**
	 * Transforms XML using the XSLT stylesheet and parameters specified, return
	 * the result as a String.
	 * 
	 * @param xml the XML to be transformed.
	 * @param xsl the XSLT stylesheet to use for the transformation.
	 * @param params a list of parameters for configuring the XSLT stylesheet
	 *        prior to the transformation.
	 * @return the result of the transformation as a String
	 * @throws TransformerException when it's not possible to complete the
	 *         transformation.
	 */
	private String transformToString(Document xml, Source xsl, TreeMap<String, String> params) throws TransformerException {

		// Create a container to hold the result of the transformation
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);

		this.transform(new DOMSource(xml), xsl, result, params);

		return writer.toString();
	}

}
