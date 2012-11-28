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
import java.util.Locale;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.kaikoda.cah.ProgressReporter.ProgressReporterMode;


/**
 * A utility for generating a printable deck of Cards Against Humanity.
 * 
 * @author Sheila Thomson
 */
public class CardGenerator {

	protected enum CardGeneratorProduct {		
		HTML, PDF, XML;		
	}

	/**
	 * A utility for providing feedback to the user of this application.
	 */
	ProgressReporter progressReporter;

	/**
	 * Default constructor.
	 * 
	 * @throws CardGeneratorConfigurationException when it's not possible to
	 *         construct a usable instance of CardGenerator.
	 */
	public CardGenerator() throws CardGeneratorConfigurationException {
		this.progressReporter = new ProgressReporter();
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
	 * @throws ParseException if there are any problems encountered while
	 *         parsing the command line tokens.
	 */
	public static void main(String[] args) throws CardGeneratorConfigurationException, ParseException, SAXException, IOException, ParserConfigurationException {

		ProgressReporterMode verbosity = ProgressReporterMode.NORMAL;

		CardGenerator generator = new CardGenerator();
		generator.setVerbosity(verbosity);

		// Configure valid options accepted from the command-line
		CardGeneratorOptions options = new CardGeneratorOptions();

		// Interpret the arguments supplied at runtime.
		TreeMap<String, String> params = options.parse(args);

		// Check whether a level of verbosity has been specified.
		if (params.containsKey("verbosity")) {
			verbosity = ProgressReporterMode.valueOf(params.get("verbosity").toUpperCase());
			generator.setVerbosity(verbosity);
		}

		// Check whether help has been requested.
		if (params.containsKey("help")) {

			if (verbosity.equals(ProgressReporterMode.NORMAL)) {

				// Print a list of the options that can be used with
				// CardGenerator.
				System.out.println("\n" + params.get("help"));

			}

		} else {

			File data = new File(params.remove("path-to-data"));
			
			CardGeneratorProduct product = CardGeneratorProduct.HTML;
			if (params.containsKey("product")) {
				product = CardGeneratorProduct.valueOf(params.remove("product").toUpperCase());
			}
			
			Locale targetLanguage = null;
			if (params.containsKey("output-language")) {
				targetLanguage = Locale.forLanguageTag(params.remove("output-language"));
			}

			File dictionary = null;
			if (params.containsKey("path-to-dictionary")) {
				dictionary = new File(params.remove("path-to-dictionary"));
			}

			generator.generate(data, targetLanguage, dictionary, product);

		}
	}
	
	public File generate(File data, Locale targetLanguage, File dictionary) throws SAXException, IOException, ParserConfigurationException {
		return this.generate(data, targetLanguage, dictionary, CardGeneratorProduct.HTML);
	}
	
	/**
	 * Generates a printable deck of Cards Against Humanity.
	 * 
	 * @param data
	 * @param targetLanguage
	 * @param dictionary
	 * @param product 
	 * @return a pointer to the main file output.
	 * @throws SAXException when there's a problem parsing the card data or dictionary.
	 * @throws IOException when there's a problem reading the card data or dictionary or when saving the output.
	 * @throws ParserConfigurationException when there's a problem configuring
	 *         the data parser.
	 */
	public File generate(File data, Locale targetLanguage, File dictionary, CardGeneratorProduct product) throws SAXException, IOException, ParserConfigurationException {

		if (!data.exists()) {

			throw new IllegalArgumentException("File not found: " + data.getPath());

		}

		Document xml = null;
		this.feedback("Reading card data...");
		try {

			xml = Deck.parse(data);
			this.feedback("...data read.\n");

		} catch (SAXException e) {
			this.feedback("Unable to parse card data.", true);
			throw e;
		} catch (IOException e) {
			this.feedback("Unable to read card data.", true);
			throw e;
		} catch (ParserConfigurationException e) {
			this.feedback("Unable to read card data.", true);
			throw e;
		}

		Deck deck = new Deck(xml);
		deck.setErrorListener(this.progressReporter);

		if (targetLanguage != null && targetLanguage != deck.getLocale()) {

			this.feedback("Translating data...");
			try {

				deck.translate(targetLanguage, dictionary);
				this.feedback("...translation complete.\n");

			} catch (SAXException e) {
				this.feedback("Unable to complete translation.");
			} catch (IOException e) {
				this.feedback("Unable to complete translation.");
			} catch (TransformerException e) {
				this.feedback("Unable to complete translation.");
			} catch (ParserConfigurationException e) {
				this.feedback("Unable to complete translation.");
			}

		}

		this.feedback("Standardising blanks...");
		try {
			deck.blank();
			this.feedback("...blanks standardised.\n");
		} catch (TransformerException e) {
			this.feedback("OCD FAIL. Unable to standardise blanks.", true);
		} catch (SAXException e) {
			this.feedback("OCD FAIL. Unable to standardise blanks.", true);
		} catch (IOException e) {
			this.feedback("OCD FAIL. Unable to standardise blanks.", true);
		} catch (ParserConfigurationException e) {
			this.feedback("OCD FAIL. Unable to standardise blanks.", true);
		}

		this.feedback("Checking for duplicates...");
		try {
			deck.dedupe();
			this.feedback("...de-duping complete.\n");
		} catch (TransformerException e) {
			this.feedback("Unable to complete de-duping process.", true);
		} catch (SAXException e) {
			this.feedback("Unable to complete de-duping process.", true);
		} catch (IOException e) {
			this.feedback("Unable to complete de-duping process.", true);
		} catch (ParserConfigurationException e) {
			this.feedback("Unable to complete de-duping process.", true);
		}

		File productLocation = null;
		if (product.equals(CardGeneratorProduct.XML)) {
			productLocation = this.generateXml(deck);
		} else {
			productLocation = this.generateHTML(deck);
		}

		this.feedback("Card generation complete.");

		return productLocation;
	}
	
	public ProgressReporterMode getVerbosity() {
		return this.progressReporter.getMode();
	}
	
	public void setVerbosity(ProgressReporterMode verbosity) {
		this.progressReporter.setMode(verbosity);
	}

	/**
	 * Provide feedback to the user of this application (not including errors).
	 * 
	 * @param message a message to the user.
	 */
	private void feedback(String message) {
		this.feedback(message, false);
	}

	/**
	 * Provide feedback to the user of this application (including errors).
	 * 
	 * @param message a message to the user.
	 * @param isError true if the message should be reported as an error. Is
	 *        false by default.
	 */
	private void feedback(String message, boolean isError) {
		this.progressReporter.feedback(message, isError);
	}

	private File generateHTML(Deck deck) {
		
		this.feedback("Generating HTML...");
		File htmlOutputLocation = null;
		try {

			String html = deck.toHtml();

			htmlOutputLocation = new File("cards_against_humanity.html");
			FileUtils.writeStringToFile(htmlOutputLocation, html, "UTF-8");

			this.feedback("...file saved:");
			this.feedback(htmlOutputLocation.getAbsolutePath() + "\n");

		} catch (SAXException e) {
			this.feedback("Unable to save cards to file.", true);
		} catch (IOException e) {
			this.feedback("Unable to save cards to file.", true);
		} catch (TransformerException e) {
			this.feedback("Unable to save cards to file.", true);
		} catch (ParserConfigurationException e) {
			this.feedback("Unable to save cards to file.", true);
		}

		this.feedback("Adding a dash of style...");
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

			this.feedback("...file saved:");
			this.feedback(outputDirectory.getAbsolutePath());

		} catch (IOException e) {
			e.printStackTrace();
			this.feedback("Unable to style.  Do it yourself.", true);
		}
		
		return htmlOutputLocation;
		
	}

	private File generateXml(Deck deck) {

		this.feedback("Generating XML...");
		File productOutputLocation = null;

			String xml = deck.toString();
			
			try {
				
				productOutputLocation = new File("cards_against_humanity.xml");
				FileUtils.writeStringToFile(productOutputLocation, xml, "UTF-8");
				
			} catch (IOException e) {
				this.feedback("Unable to save cards to file.", true);
			}

			this.feedback("...file saved:");
			this.feedback(productOutputLocation.getAbsolutePath() + "\n");

		
		return productOutputLocation;
	}

}
