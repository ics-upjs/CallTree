package sample;

import sk.upjs.calltree.*;
import sk.upjs.jpaz2.*;

public class FractalTurtle extends Turtle {

	/**
	 * Draws a Koch's curve defined by level and length.
	 * 
	 * @param level
	 *            the level of fractal.
	 * @param length
	 *            the length of fractal.
	 */
	public void kochCurve(int level, int length) {
		CallTree.markCall(level, length);
		
		if (level <= 1) {
			step(length);
		} else {
			kochCurve(level - 1, length / 3);
			turn(-60);
			kochCurve(level - 1, length / 3);
			turn(120);
			kochCurve(level - 1, length / 3);
			turn(-60);
			kochCurve(level - 1, length / 3);
		}
	}

	public static void main(String[] args) {
		// create a drawing pane
		WinPane pane = new WinPane();

		// create a turtle in the center of the pane
		FractalTurtle koch = new FractalTurtle();
		pane.add(koch);
		koch.center();

		// draw fractal
		koch.kochCurve(4, 130);
	}

}
