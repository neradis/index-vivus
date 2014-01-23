package de.fusionfactory.index_vivus.services;

/**
 * Created with IntelliJ IDEA.
 * User: Eric Kurzhals
 * Date: 23.01.14
 * Time: 17:02
 */
public class FulltextSearchException extends Exception {
	private final int code;

	public FulltextSearchException(int code, String message) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}