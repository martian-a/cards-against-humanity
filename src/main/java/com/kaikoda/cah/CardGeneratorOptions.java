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
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * A collection containing the options accepted by this application from the
 * command-line.
 * 
 * @author Sheila Thomson
 */
public class CardGeneratorOptions {

	/**
	 * A configured collection of the options available.
	 */
	private Options options;

	/**
	 * Default constructor. Pre-configured with valid options for use with a
	 * Cards Against Humanity Card Generator.
	 */
	public CardGeneratorOptions() {
		this.setOptions();
	}

	/**
	 * @return a collection containing all the options that can be used with the
	 *         Card Generator.
	 */
	@SuppressWarnings("unchecked")
	public Collection<Option> getOptions() {
		return this.options.getOptions();
	}

	/**
	 * Converts an array of arguments supplied at run-time, checks them for
	 * validity and returns a the valid option data.
	 * 
	 * @param args an unchecked list expressing option choices.
	 * @return a valid list of the options chosen.
	 * @throws ParseException if there are any problems encountered while
	 *         parsing the command line tokens.
	 */
	public TreeMap<String, String> parse(String[] args) throws ParseException {

		TreeMap<String, String> params = new TreeMap<String, String>();

		// Parse runtime options from the command-line
		CommandLineParser parser = new GnuParser();

		CommandLine line = parser.parse(this.options, args);

		// Check whether a level of verbosity has been specified
		if (line.hasOption("v")) {
			
			// Record the level specified
			params.put("verbosity", line.getOptionValue("v"));
			
		}
		
		// Check if help has been requested
		if (line.hasOption("h")) {

			// Provide help
			params.put("help", this.getHelp());

			// No further processing required
			return params;

		}

		File inputLocation = null;
		File dictionaryLocation = null;
		String targetLanguage = null;
		String product = null;
		
		// Retrieve the input file
		if (line.hasOption("f")) {
			inputLocation = new File(line.getOptionValue("f"));
		}

		// Retrieve the dictionary
		if (line.hasOption("d")) {
			dictionaryLocation = new File(line.getOptionValue("d"));
		}

		// Retrieve the locale code
		if (line.hasOption("l")) {
			targetLanguage = line.getOptionValue("l");
		}
		
		// Retrieve the product required
		if (line.hasOption("p")) {
			product = line.getOptionValue("p");
		}

		// Check whether an input location has been specified (required).
		if (inputLocation == null) {
			throw new IllegalArgumentException("Nothing to process; no file or directory was specified.");
		}
		
		// Add the input location to the parsed option data.
		params.put("path-to-data", inputLocation.getAbsolutePath());
		
		// Check whether a dictionary has been specified
		if (dictionaryLocation != null) {

			// Add the dictionary to the parsed option data
			params.put("path-to-dictionary", dictionaryLocation.getAbsolutePath());

		}

		// Check whether a target language has been specified.
		if (targetLanguage != null) {

			// Add the target language to the parsed option data
			params.put("output-language", targetLanguage);

		}
		
		// Check whether a product has been specified
		if (product != null) {

			// Add the product to the parsed option data
			params.put("product", product);

		}

		// Return the parsed option data
		return params;
	}

	/**
	 * Prints an annotated list of the options that are valid for use with this
	 * application.
	 * 
	 * @return a list of all the Card Generator options.
	 */
	private String getHelp() {

		String help = "The options that can be used with this application are:\n";

		Collection<Option> optionsCollection = this.getOptions();
		Iterator<Option> validOptions = optionsCollection.iterator();
		while (validOptions.hasNext()) {
			Option option = validOptions.next();

			help = help + "\n-" + option.getOpt() + "\t" + option.getDescription() + "\n";
		}

		return help;
	}

	/**
	 * 
	 */
	private void setOptions() {

		// Configure valid options accepted from the command-line
		options = new Options();

		// The name of the file to be processed
		options.addOption("f", true, "path to the card data file.");

		// The location of the dictionary
		options.addOption("d", true, "path to the dictionary to use for translating.");

		// The target language
		options.addOption("l", true, "locale, representing the culture that you would like the cards translated into.  Should be a code compatible with IETF BCP 47, Tags for Identifying Languages.  eg. en-GB (British English), en-NL (Dutch English), nl-NL (Dutch Dutch), en-x-pirate (Pirate English)");
		
		// The product required
		options.addOption("p", true, "the product required: html (default), xml or pdf");
		
		// Verbosity
		// TODO: Implement an enum representing the reporting modes available
		// TODO: Update to match enum values.
		options.addOption("v", true, "verbosity, how much feedback you'd like on the progress of the Card Generator: normal (default) or silent");

		// A request for help with using the application
		options.addOption("h", "help", false, "list all parameters that can be specified at runtime");

	}

}
