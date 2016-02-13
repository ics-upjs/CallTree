package sk.upjs.paz.calltree;

import java.awt.EventQueue;
import java.util.concurrent.Semaphore;

/**
 * Basic method for storing data required to visualize a call tree.
 */
public class CallTree {

	/**
	 * Configuration object for visualization of call trees.
	 */
	private static final Config config = new Config();

	/**
	 * Builder of call trees.
	 */
	private static final CallTreeBuilder callTreeBuilder = new CallTreeBuilder();

	/**
	 * Frame visualizing all recorded call trees.
	 */
	private static volatile CallTreeFrame visualizationFrame;

	/**
	 * Runnable that realizes update of visualization frame in event dispatch
	 * thread.
	 */
	private static final Runnable updateCallTreeRunnable = new Runnable() {
		public void run() {
			updateCallTreeFrameInEDT();
		}
	};

	/**
	 * Returns the configuration object that can be used to configure
	 * visualization.
	 * 
	 * @return configuration object for call tree visualization
	 */
	public static Config getConfig() {
		return config;
	}

	/**
	 * Marks that a new method call started.
	 * 
	 * @param args
	 *            arguments to be associated with the started method call.
	 */
	public static void markCall(Object... args) {
		if (args == null) {
			args = new Object[1];
		}

		callTreeBuilder.markCall(args);
		updateCallTreeFrame();
		waitForConfirmation();
	}

	/**
	 * Stores a log message and associates the message with a method execution.
	 * 
	 * @param message
	 *            the log message
	 */
	public static void log(String message) {
		callTreeBuilder.log(message, null);
		updateCallTreeFrame();
	}

	/**
	 * Stores a log message and associates the message with a method execution.
	 * 
	 * @param message
	 *            the log message
	 * 
	 * @param args
	 *            arguments to be associated with the message
	 */
	public static void log(String message, Object... args) {
		if (args == null) {
			args = new Object[1];
		}

		callTreeBuilder.log(message, args);
		updateCallTreeFrame();
	}

	/**
	 * Stores return value returned by a method execution.
	 * 
	 * @param value
	 *            return value
	 * @return the value
	 */
	public static int markReturn(int value) {
		callTreeBuilder.markReturn(value);
		updateCallTreeFrame();
		waitForConfirmation();
		return value;
	}

	/**
	 * Stores return value returned by a method execution.
	 * 
	 * @param value
	 *            return value
	 * @return the value
	 */
	public static byte markReturn(byte value) {
		callTreeBuilder.markReturn(value);
		updateCallTreeFrame();
		waitForConfirmation();
		return value;
	}

	/**
	 * Stores return value returned by a method execution.
	 * 
	 * @param value
	 *            return value
	 * @return the value
	 */
	public static short markReturn(short value) {
		callTreeBuilder.markReturn(value);
		updateCallTreeFrame();
		waitForConfirmation();
		return value;
	}

	/**
	 * Stores return value returned by a method execution.
	 * 
	 * @param value
	 *            return value
	 * @return the value
	 */
	public static long markReturn(long value) {
		callTreeBuilder.markReturn(value);
		updateCallTreeFrame();
		waitForConfirmation();
		return value;
	}

	/**
	 * Stores return value returned by a method execution.
	 * 
	 * @param value
	 *            return value
	 * @return the value
	 */
	public static char markReturn(char value) {
		callTreeBuilder.markReturn(value);
		updateCallTreeFrame();
		waitForConfirmation();
		return value;
	}

	/**
	 * Stores return value returned by a method execution.
	 * 
	 * @param value
	 *            return value
	 * @return the value
	 */
	public static boolean markReturn(boolean value) {
		callTreeBuilder.markReturn(value);
		updateCallTreeFrame();
		waitForConfirmation();
		return value;
	}

	/**
	 * Stores return value returned by a method execution.
	 * 
	 * @param <T>
	 *            the type of value.
	 * @param value
	 *            the return value
	 * @return the value
	 */
	public static <T> T markReturn(T value) {
		callTreeBuilder.markReturn(value);
		updateCallTreeFrame();
		waitForConfirmation();
		return value;
	}

	/**
	 * Stores return value returned by a method execution.
	 * 
	 * @param value
	 *            return value
	 * @return the value
	 */
	public static double markReturn(double value) {
		callTreeBuilder.markReturn(value);
		updateCallTreeFrame();
		waitForConfirmation();
		return value;
	}

	/**
	 * Stores return value returned by a method execution.
	 * 
	 * @param value
	 *            return value
	 * @return the value
	 */
	public static float markReturn(float value) {
		callTreeBuilder.markReturn(value);
		updateCallTreeFrame();
		waitForConfirmation();
		return value;
	}

	/**
	 * Marks that method execution was completed.
	 */
	public static void markReturn() {
		callTreeBuilder.markReturn();
		updateCallTreeFrame();
		waitForConfirmation();
	}

	/**
	 * Resets call tree builder and removes all recorded call trees.
	 */
	public static void reset() {
		callTreeBuilder.reset();
		updateCallTreeFrame();
	}

	/**
	 * Updates the call tree visualization.
	 */
	private static void updateCallTreeFrame() {
		config.lockChanges();
		EventQueue.invokeLater(updateCallTreeRunnable);
	}

	/**
	 * Updates the call tree visualization in the event dispatch thread.
	 */
	private static void updateCallTreeFrameInEDT() {
		// create frame, if it was not already created
		if (visualizationFrame == null) {
			visualizationFrame = new CallTreeFrame();
			visualizationFrame.setVisible(true);
		}

		visualizationFrame.updateState(callTreeBuilder.getState());
	}

	/**
	 * Waits for permission to continue execution. If this method is invoked
	 * from the event dispatch thread, it exits immediately.
	 */
	private static void waitForConfirmation() {
		if (EventQueue.isDispatchThread())
			return;

		final Semaphore semaphore = new Semaphore(0);
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				if (visualizationFrame == null) {
					semaphore.release();
				} else {
					visualizationFrame.addConfirmationRequest(semaphore);
				}
			}
		});

		try {
			semaphore.acquire();
		} catch (InterruptedException ignore) {

		}
	}
}
