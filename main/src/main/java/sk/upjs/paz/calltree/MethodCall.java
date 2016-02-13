package sk.upjs.paz.calltree;

import java.util.*;

/**
 * A method call (and underlying execution) including its position in a call
 * tree.
 * 
 */
class MethodCall {

	/**
	 * Log record and data values attached to it.
	 */
	static class LogRecord {
		/**
		 * Number of child method calls at the moment when this record was
		 * stored.
		 */
		int numberOfChildren;

		/**
		 * Line number with log command.
		 */
		int lineNumber;

		/**
		 * Log message.
		 */
		String message;

		/**
		 * Attached values.
		 */
		Object[] values;
	}

	/**
	 * Fully qualified name of the class containing a method whose invocation is
	 * represented by this method call.
	 */
	private final String className;

	/**
	 * Name of the method whose invocation is represented by this method call.
	 */
	private final String methodName;

	/**
	 * Name of the source file containing the method whose invocation is
	 * represented by this method call.
	 */
	private final String filename;

	/**
	 * Line number in the calling method that caused this method call.
	 */
	private final int lineNumber;

	/**
	 * Indicates whether this method call is marked.
	 */
	private final boolean isMarked;

	/**
	 * Index of this method call in the history log.
	 */
	private final int historyIndex;

	/**
	 * Parent of this method call in the call tree.
	 */
	private final MethodCall parent;

	/**
	 * List of method calls that were realized by this method call.
	 */
	private List<MethodCall> children;

	/**
	 * Arguments of the method call.
	 */
	private Object[] arguments;

	/**
	 * List of recorded logs.
	 */
	private List<LogRecord> logs;

	/**
	 * Indicates whether return value was stored.
	 */
	private boolean returnValueStored;

	/**
	 * Value that was returned by this method call.
	 */
	private Object returnValue;

	/**
	 * Constructs a new method call.
	 */
	public MethodCall(StackTraceElement ste, StackTraceElement callingSte, boolean marked, MethodCall parent,
			int index) {
		this.className = ste.getClassName();
		this.methodName = ste.getMethodName();
		this.filename = ste.getFileName();
		this.lineNumber = (callingSte != null) ? callingSte.getLineNumber() : -1;
		this.isMarked = marked;
		this.parent = parent;
		this.historyIndex = index;
	}

	/**
	 * Sets arguments of this method call.
	 */
	public synchronized void setArguments(Object[] arguments) {
		if (arguments != null)
			this.arguments = arguments.clone();
		else
			this.arguments = null;
	}

	/**
	 * Returns arguments of this method call.
	 */
	public synchronized Object[] getArguments() {
		return arguments;
	}

	/**
	 * Adds a child method call that is a result of method invocation in this
	 * method call.
	 */
	public synchronized void addCall(MethodCall methodCall) {
		if (methodCall == null)
			return;

		if (children == null)
			children = new LinkedList<MethodCall>();

		children.add(methodCall);
	}

	/**
	 * Returns list of all method calls that were initiated by this method call.
	 */
	public synchronized List<MethodCall> getMethodCalls() {
		if (children == null) {
			return Collections.emptyList();
		} else {
			return new ArrayList<MethodCall>(children);
		}
	}

	/**
	 * Returns whether this method call invoked another method.
	 */
	public synchronized boolean hasMethodCalls() {
		return (children != null);
	}

	/**
	 * Records a bundle of values for this method call.
	 * 
	 * @param name
	 *            name of the bundle
	 * @param values
	 *            values in the bundle
	 */
	public synchronized void log(String message, Object[] values, int lineNumber) {
		LogRecord logRecord = new LogRecord();
		logRecord.message = message;
		logRecord.numberOfChildren = (children != null) ? children.size() : 0;
		logRecord.values = values;
		logRecord.lineNumber = lineNumber;

		if (this.logs == null)
			this.logs = new ArrayList<LogRecord>();

		this.logs.add(logRecord);
	}

	/**
	 * Records return value of the method call.
	 */
	public synchronized void markReturn(Object value) {
		if (returnValueStored)
			throw new CallTreeMarkingException(
					"Duplicated recording of the return value for method " + methodName + ".");

		returnValue = value;
		returnValueStored = true;
	}

	/**
	 * Returns whether this method call is the root of a call tree.
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * Returns index of this method call in the recorded history of method
	 * calls.
	 */
	public int getIndex() {
		return historyIndex;
	}

	/**
	 * Method call that caused invocation of this method call.
	 */
	public MethodCall getParent() {
		return parent;
	}

	/**
	 * Returns class name of the method.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Returns name of the method.
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Returns file name that stores class with the method.
	 */
	public String getFileName() {
		return filename;
	}

	/**
	 * Line number of the method call (in the method that caused execution of
	 * this method call).
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Returns whether this method was recorded as a result of a markCall.
	 */
	public boolean isMarked() {
		return isMarked;
	}

	/**
	 * Returns whether a return value was recorded for this method call.
	 * 
	 * @return true, if a return value was recorded, false otherwise.
	 */
	public synchronized boolean hasReturnValue() {
		return returnValueStored;
	}

	/**
	 * Returns the recorded return value.
	 * 
	 * @return recorded return value or null, if the value was not recorded.
	 */
	public synchronized Object getReturnValue() {
		return returnValue;
	}

	/**
	 * Returns whether there is a log associated with this method call
	 * (execution).
	 */
	public synchronized boolean hasLogs() {
		return (logs != null);
	}

	/**
	 * Returns list of log records.
	 * 
	 * @param childLimit
	 *            limit for number of children at time when log was recorded.
	 */
	public synchronized List<LogRecord> getLogs(int childLimit) {
		if (logs == null)
			return Collections.emptyList();

		List<LogRecord> result = new ArrayList<LogRecord>(logs.size());
		for (LogRecord log : logs)
			if (log.numberOfChildren <= childLimit)
				result.add(log);

		return result;
	}
}
