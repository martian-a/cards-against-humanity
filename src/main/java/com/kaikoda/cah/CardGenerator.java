package com.kaikoda.cah;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
public class CardGenerator {

	/**
	 * Default constructor.
	 * 
	 * @throws CardGeneratorConfigurationException when it's not possible to
	 *         construct a usable instance of CardGenerator.
	 */
	public CardGenerator() throws CardGeneratorConfigurationException {

	}

	/**
	 * Generates a printable deck of Cards Against Humanity.
	 * 
	 * @param args where to find the data file and optionally the dictionary and
	 *        target language if translation is required.
	 * @throws CardGeneratorConfigurationException when it's not possible to
	 *         construct a usable instance of CardGenerator.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws CardGeneratorConfigurationException, SAXException, IOException, ParserConfigurationException {

		CardGenerator generator = new CardGenerator();

		// Configure valid options accepted from the command-line
		Options options = CardGenerator.getOptions();

		TreeMap<String, String> params = CardGenerator.parseArgs(options, args);

		if (params != null) {

			File data = new File(params.remove("path-to-data"));

			Locale targetLanguage = null;
			if (params.containsKey("output-language")) {
				targetLanguage = Locale.forLanguageTag(params.remove("output-language"));
			}

			File dictionary = null;
			if (params.containsKey("path-to-dictionary")) {
				dictionary = new File(params.remove("path-to-dictionary"));
			}

			generator.generate(data, targetLanguage, dictionary);

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

	/**
	 * Generates a printable deck of Cards Against Humanity.
	 * 
	 * @param data
	 * @param targetLanguage
	 * @param dictionary
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public File generate(File data, Locale targetLanguage, File dictionary) throws SAXException, IOException, ParserConfigurationException {

		if (!data.exists()) {

			throw new IllegalArgumentException("File not found: " + data.getPath());

		}

		Document xml = null;
		System.out.println("Reading card data...");
		try {

			xml = Deck.parse(data);
			System.out.println("...data read.\n");

		} catch (SAXException e) {
			System.err.println("Unable to parse card data.");
			throw e;
		} catch (IOException e) {
			System.err.println("Unable to read card data.");
			throw e;
		} catch (ParserConfigurationException e) {
			System.err.println("Unable to read card data.");
			throw e;
		}

		Deck deck = new Deck(xml);

		if (targetLanguage != null && targetLanguage != deck.getLocale()) {

			System.out.println("Translating data...");
			try {

				deck.translate(targetLanguage, dictionary);
				System.out.println("...translation complete.\n");

			} catch (SAXException e) {
				System.out.println("Unable to complete translation.");
			} catch (IOException e) {
				System.out.println("Unable to complete translation.");
			} catch (TransformerException e) {
				System.out.println("Unable to complete translation.");
			} catch (ParserConfigurationException e) {
				System.out.println("Unable to complete translation.");
			}

		}

		System.out.println("Standardising blanks...");
		try {
			deck.blank();
			System.out.println("...blanks standardised.\n");
		} catch (TransformerException e) {
			System.err.println("OCD FAIL. Unable to standardise blanks.");
		} catch (SAXException e) {
			System.err.println("OCD FAIL. Unable to standardise blanks.");
		} catch (IOException e) {
			System.err.println("OCD FAIL. Unable to standardise blanks.");
		} catch (ParserConfigurationException e) {
			System.err.println("OCD FAIL. Unable to standardise blanks.");
		}

		System.out.println("Checking for duplicates...");
		try {
			deck.dedupe();
			System.out.println("...de-duping complete.\n");
		} catch (TransformerException e) {
			System.err.println("Unable to complete de-duping process.");
		} catch (SAXException e) {
			System.err.println("Unable to complete de-duping process.");
		} catch (IOException e) {
			System.err.println("Unable to complete de-duping process.");
		} catch (ParserConfigurationException e) {
			System.err.println("Unable to complete de-duping process.");
		}

		System.out.println("Generating HTML...");
		File htmlOutputLocation = null;
		try {

			String html = deck.toHtml();

			htmlOutputLocation = new File("cards_against_humanity.html");
			FileUtils.writeStringToFile(htmlOutputLocation, html, "UTF-8");

			System.out.println("...file saved:");
			System.out.println(htmlOutputLocation.getAbsolutePath() + "\n");

		} catch (SAXException e) {
			System.err.println("Unable to save cards to file.");
		} catch (IOException e) {
			System.err.println("Unable to save cards to file.");
		} catch (TransformerException e) {
			System.err.println("Unable to save cards to file.");
		} catch (ParserConfigurationException e) {
			System.err.println("Unable to save cards to file.");
		}

		System.out.println("Adding a dash of style...");
		try {

			String directoryPath = "assets";

			File outputDirectory = new File(directoryPath);
			outputDirectory.mkdir();

			// TODO: Implement solution that either copies the entire directory
			// or loops through its contents.

			String path = outputDirectory.getName() + File.separator + "style.css";
			FileUtils.copyURLToFile(this.getClass().getResource(File.separator + path), new File(path));

			path = outputDirectory.getName() + File.separator + "branding_on_black.png";
			FileUtils.copyURLToFile(this.getClass().getResource(File.separator + path), new File(path));

			path = outputDirectory.getName() + File.separator + "branding_on_white.png";
			FileUtils.copyURLToFile(this.getClass().getResource(File.separator + path), new File(path));

			path = outputDirectory.getName() + File.separator + "branding_on_black_cards.png";
			FileUtils.copyURLToFile(this.getClass().getResource(File.separator + path), new File(path));

			System.out.println("...file saved:\n");
			System.out.println(outputDirectory.getAbsolutePath());

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Unable to style.  Do it yourself.");
		}

		System.out.println("Card generation complete.");

		return htmlOutputLocation;
	}

}
