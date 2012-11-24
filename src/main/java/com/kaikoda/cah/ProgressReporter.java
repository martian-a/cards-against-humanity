package com.kaikoda.cah;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class ProgressReporter implements ErrorListener {

	public void error(TransformerException exception) throws TransformerException {

		System.err.println(exception.getMessage());

	}

	public void fatalError(TransformerException exception) throws TransformerException {

		System.err.println(exception.getMessage());

	}

	public void warning(TransformerException exception) throws TransformerException {

		System.out.println(exception.getMessage());

	}

}
