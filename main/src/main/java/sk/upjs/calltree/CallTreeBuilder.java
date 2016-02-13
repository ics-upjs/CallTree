package sk.upjs.calltree;

import java.util.*;

/**
 * Internal builder of call trees.
 */
class CallTreeBuilder {

	/**
	 * A class representing a state of the call tree builder.
	 */
	class CallTreeState {
		final List<MethodCall> history;
		final MethodCall activeCall;

		CallTreeState(List<MethodCall> history, MethodCall activeCall) {
			this.history = history;
			this.activeCall = activeCall;
		}
	}

	/**
	 * Synchronization lock used when arbitrary call tree is modified.
	 */
	private final Object changeLock = new Object();

	/**
	 * Roots of all recorded call trees.
	 */
	private final List<MethodCall> roots;

	/**
	 * List of all recorded method calls in order in which they were created
	 * (invoked).
	 */
	private final List<MethodCall> history;

	/**
	 * Last known executed method call.
	 */
	private MethodCall activeMethod;

	/**
	 * Last known configuration of the call stack.
	 */
	private StackTraceElement[] callstack = null;

	/**
	 * Index of the root method call in the call tree.
	 */
	private int callTreeRootIndex;

	/**
	 * Mapping that associates method calls to stack trace elements.
	 */
	private final ArrayList<MethodCall> methodCallsOnStack;

	/**
	 * Constructs a new call tree builder.
	 */
	public CallTreeBuilder() {
		methodCallsOnStack = new ArrayList<MethodCall>();
		roots = new ArrayList<MethodCall>();
		history = new ArrayList<MethodCall>();
	}

	/**
	 * Resets the call tree builder.
	 */
	public void reset() {
		synchronized (changeLock) {
			roots.clear();
			callstack = null;
			activeMethod = null;
			methodCallsOnStack.clear();
		}
	}

	/**
	 * Logs a message.
	 * 
	 * @param message
	 *            content of the logged message
	 * @param args
	 *            additional values to store as a part of the logged message
	 */
	public void log(String message, Object[] args) {
		synchronized (changeLock) {
			StackTraceElement[] cs = getCallstack();
			MethodCall mc = getCurrentMethodCall(cs);
			if (mc == null) {
				throw new CallTreeMarkingException(
						"Invalid use of calls for building a call tree (maybe the current method call was not marked by markCall).");
			}

			mc.log(message, args, cs[cs.length - 1].getLineNumber());
			activeMethod = mc;
		}
	}

	/**
	 * Records the return value of the current method call.
	 * 
	 * @param value
	 *            the return value to be recorded.
	 */
	public void markReturn(Object value) {
		synchronized (changeLock) {
			MethodCall mc = getCurrentMethodCall(getCallstack());
			if (mc == null) {
				throw new CallTreeMarkingException(
						"Invalid use of calls for building a call tree (maybe the current method call was not marked by markCall).");
			}

			mc.markReturn(value);
			activeMethod = mc.getParent();
		}
	}

	/**
	 * Records the current method call is completed.
	 */
	public void markReturn() {
		synchronized (changeLock) {
			MethodCall mc = getCurrentMethodCall(getCallstack());
			if (mc == null) {
				throw new CallTreeMarkingException(
						"Invalid use of calls for building a call tree (maybe the current method call was not marked by markCall).");
			}
			activeMethod = mc.getParent();
		}
	}

	/**
	 * Records a new call of a monitored method.
	 */
	public void markCall(Object[] args) {
		// take snapshot of the current callstack and remove all "artefacts",
		// the top of callstack has the highest index
		StackTraceElement[] currentCallStack = getCallstack();

		synchronized (changeLock) {
			if (matchPreviousCallStack(currentCallStack)) {
				// find the first difference
				int branchIdx = branchIndex(currentCallStack);
				if (branchIdx < callTreeRootIndex) {
					throw new CallTreeMarkingException("Invalid use of calls for building a call tree.");
				}

				// build tree elements for the new branch of method calls
				MethodCall[] newBranch = buildSubtreeForBranch(branchIdx, currentCallStack);
				if (newBranch.length == 0) {
					throw new CallTreeMarkingException(
							"Invalid use of calls for building a call tree (probably, markCall is not the first statement of method "
									+ currentCallStack[branchIdx].getMethodName() + ")");
				}

				// update callstack records
				methodCallsOnStack.subList(branchIdx + 1, methodCallsOnStack.size()).clear();
				for (MethodCall mc : newBranch) {
					methodCallsOnStack.add(mc);
				}
				callstack = currentCallStack;
				activeMethod = methodCallsOnStack.get(methodCallsOnStack.size() - 1);
			} else {
				// start to build a new call tree
				StackTraceElement current = currentCallStack[currentCallStack.length - 1];
				StackTraceElement previous = (currentCallStack.length >= 2)
						? currentCallStack[currentCallStack.length - 2] : null;

				MethodCall mc = new MethodCall(current, previous, true, null, history.size());
				roots.add(mc);
				history.add(mc);

				// update callstack records
				callstack = currentCallStack;
				methodCallsOnStack.clear();
				for (int i = 0; i < currentCallStack.length - 1; i++)
					methodCallsOnStack.add(null);
				methodCallsOnStack.add(mc);
				callTreeRootIndex = currentCallStack.length - 1;
				activeMethod = mc;
			}

			// set init arguments
			methodCallsOnStack.get(methodCallsOnStack.size() - 1).setArguments(args);
		}
	}

	/**
	 * Returns current state of this call tree builder.
	 */
	public CallTreeState getState() {
		synchronized (changeLock) {
			return new CallTreeState(new ArrayList<MethodCall>(history), activeMethod);
		}
	}

	/**
	 * Returns the current method call according to content of the callstack.
	 */
	private MethodCall getCurrentMethodCall(StackTraceElement[] currentCallStack) {
		// check whether current callstack matches prefix of last recorded
		// callstack
		if (currentCallStack.length > callstack.length) {
			throw new CallTreeMarkingException(
					"Invalid use of calls for building a call tree (the current method call was not marked by markCall).");
		}
		boolean matchOK = true;
		for (int i = 0; i < currentCallStack.length - 1; i++) {
			if (!currentCallStack[i].equals(callstack[i])) {
				matchOK = false;
				break;
			}
		}
		matchOK = matchOK && equalIgnoreLineNumber(currentCallStack[currentCallStack.length - 1],
				callstack[currentCallStack.length - 1]);

		if (!matchOK) {
			throw new CallTreeMarkingException(
					"Invalid use of calls for building a call tree (maybe the current method call was not marked by markCall).");
		}

		// return an underlying method call instance
		return methodCallsOnStack.get(currentCallStack.length - 1);
	}

	/**
	 * Checks whether the given callstack corresponds to the calltree that is
	 * currently in the building process.
	 */
	private boolean matchPreviousCallStack(StackTraceElement[] currentCallStack) {
		if (callstack == null)
			return false;

		// check whether all callstack elements before the first marked
		// callstack element are the same
		int idx = 0;
		while (methodCallsOnStack.get(idx) == null) {
			if (idx >= currentCallStack.length)
				return false;

			if (!currentCallStack[idx].equals(callstack[idx]))
				return false;

			idx++;
		}

		// the only allowed change for the first marked callstack elements is
		// line number
		if (idx >= currentCallStack.length)
			return false;

		return equalIgnoreLineNumber(currentCallStack[idx], callstack[idx]);
	}

	/**
	 * Returns an index in the callstack where branching of the call tree is
	 * detected.
	 */
	private int branchIndex(StackTraceElement[] currentCallStack) {
		int idx = 0;
		while (idx < callstack.length) {
			if (idx >= currentCallStack.length) {
				return idx - 2;
			}

			if (equalExceptLineNumber(callstack[idx], currentCallStack[idx])) {
				if (currentCallStack.length - 1 == idx)
					return idx - 1;
				else
					return idx;
			}

			if (!callstack[idx].equals(currentCallStack[idx])) {
				return idx - 1;
			}
			idx++;
		}

		if (callstack.length == currentCallStack.length)
			return callstack.length - 2;
		else
			return callstack.length - 1;
	}

	/**
	 * Builds a subtree of call tree that corresponds to a newly recognized
	 * branch in the call tree.
	 */
	private MethodCall[] buildSubtreeForBranch(int branchIndex, StackTraceElement[] currentCallStack) {
		MethodCall[] result = new MethodCall[Math.max(currentCallStack.length - (branchIndex + 1), 0)];

		MethodCall parent = methodCallsOnStack.get(branchIndex);
		for (int i = branchIndex + 1; i < currentCallStack.length; i++) {
			MethodCall mc = new MethodCall(currentCallStack[i], currentCallStack[i - 1],
					i == currentCallStack.length - 1, parent, history.size());

			parent.addCall(mc);
			parent = mc;
			history.add(mc);
			result[i - (branchIndex + 1)] = mc;
		}

		return result;
	}

	/**
	 * Builds a callstack.
	 */
	private static StackTraceElement[] getCallstack() {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		int artefactsCount = 4;
		StackTraceElement[] result = new StackTraceElement[stacktrace.length - artefactsCount];

		int readIdx = stacktrace.length - 1;
		for (int i = 0; i < result.length; i++) {
			result[i] = stacktrace[readIdx];
			readIdx--;
		}
		return result;
	}

	/**
	 * Returns whether two stacktrace elements differ only in line numbers.
	 */
	private static boolean equalExceptLineNumber(StackTraceElement st1, StackTraceElement st2) {
		return (equalsStrings(st1.getClassName(), st2.getClassName()))
				&& (equalsStrings(st1.getMethodName(), st2.getMethodName()))
				&& (equalsStrings(st1.getFileName(), st2.getFileName())
						&& (st1.getLineNumber() != st2.getLineNumber()));
	}

	/**
	 * Returns whether two stacktrace elements are equal in the case when line
	 * numbers are not considered.
	 */
	private static boolean equalIgnoreLineNumber(StackTraceElement st1, StackTraceElement st2) {
		return (equalsStrings(st1.getClassName(), st2.getClassName()))
				&& (equalsStrings(st1.getMethodName(), st2.getMethodName()))
				&& (equalsStrings(st1.getFileName(), st2.getFileName()));
	}

	/**
	 * Returns whether two strings are equal or both are null.
	 */
	private static boolean equalsStrings(String s1, String s2) {
		if (s1 == null) {
			return s1 == s2;
		}

		return s1.equals(s2);
	}
}
