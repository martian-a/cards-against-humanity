package com.kaikoda.cah;

/**
 * Thrown when it's not possible to construct a usable instance of
 * CardGenerator.
 * 
 * @author Sheila Thomson
 * 
 */
public class CardGeneratorConfigurationException extends Exception {

	/**
	 * @param string
	 *            a custom message summarising the exception.
	 */
	public CardGeneratorConfigurationException(String string) {
		super(string);
	}

	/**
	 * Version ID for serialisation.
	 */
	private static final long serialVersionUID = 2843031643211993028L;

}
