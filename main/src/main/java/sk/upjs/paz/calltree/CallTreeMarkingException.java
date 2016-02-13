package sk.upjs.paz.calltree;

/**
 * Thrown to indicate that calls to build a call tree do not follow rules.
 */
@SuppressWarnings("serial")
public class CallTreeMarkingException extends RuntimeException {

	public CallTreeMarkingException(String message) {
		super(message);
	}

}
