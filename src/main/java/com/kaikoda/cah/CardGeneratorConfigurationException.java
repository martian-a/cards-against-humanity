package com.kaikoda.cah;

/**
 * Thrown when it's not possible to construct a usable instance of
 * CardGenerator.
 * 
 * @author Sheila Thomson
 */
public class CardGeneratorConfigurationException extends Exception {

	/**
	 * Version ID for serialisation.
	 */
	private static final long serialVersionUID = 2843031643211993028L;

	/**
	 * @param string a custom message summarising the exception.
	 */
	public CardGeneratorConfigurationException(String string) {
		super(string);
	}

}
