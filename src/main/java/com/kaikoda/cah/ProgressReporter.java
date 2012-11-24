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
