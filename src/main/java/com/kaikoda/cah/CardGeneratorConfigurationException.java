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
