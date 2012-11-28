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

import java.util.Observable;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * Provides feedback on the progress of a CardGenerator.
 * 
 * @author Sheila Thomson
 */
public class ProgressReporter extends Observable implements ErrorListener {

	public enum ProgressReporterMode {
		CALLBACK, NORMAL, SILENT;
	}

	private ProgressReporterMode mode;

	public ProgressReporter() {
		this(ProgressReporterMode.NORMAL);
	}

	public ProgressReporter(ProgressReporterMode verbosity) {
		this.mode = verbosity;
	}

	public void error(TransformerException exception) throws TransformerException {

		this.feedback(exception.getMessage(), true);

	}

	public void fatalError(TransformerException exception) throws TransformerException {

		this.feedback(exception.getMessage(), true);

	}

	/**
	 * Provides feedback on the progress of a CardGenerator.
	 * 
	 * @param message the progress to report.
	 */
	public void feedback(String message) {
		this.feedback(message, false);
	}

	/**
	 * Provides feedback on the progress of the CardGenerator.
	 * 
	 * @param message the progress to report.
	 * @param isError true if the message is to be reported as an error; false
	 *        by default.
	 */
	public void feedback(String message, boolean isError) {
				
		if (this.mode.equals(ProgressReporterMode.SILENT)) {

			// I ain't sayin' nuffink
			return;

		}

		if (this.mode.equals(ProgressReporterMode.CALLBACK)) {

			// TODO: Notify observer/listener
			
			// Nothing more to do.
			return;

		}

		if (isError) {
			System.err.println(message);
		} else {
			System.out.println(message);
		}

	}

	public ProgressReporterMode getMode() {
		return this.mode;
	}

	public void setMode(ProgressReporterMode verbosity) {
		this.mode = verbosity;
	}

	public void warning(TransformerException exception) throws TransformerException {

		this.feedback(exception.getMessage());

	}

}
