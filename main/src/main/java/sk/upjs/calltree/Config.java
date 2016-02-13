package sk.upjs.calltree;

import java.awt.*;

/**
 * Configuration settings for call tree visualization. Any changes of
 * configuration must be realized before building a call tree.
 */
public class Config {

	/**
	 * Collection of precomputed colors for a basic color.
	 */
	private static class ColorCollection {
		final Color basicColor;
		final Color lighter;
		final Color darker;

		public ColorCollection(Color color) {
			basicColor = color;
			float hsbVals[] = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
			lighter = Color.getHSBColor(hsbVals[0], hsbVals[1], 0.5f * (1f + hsbVals[2]));
			darker = Color.getHSBColor(hsbVals[0], hsbVals[1], 0.8f * hsbVals[2]);
		}
	}

	/**
	 * Padding of box displaying a method call and its execution.
	 */
	private int boxPadding = 7;

	/**
	 * Horizontal space between neighboring call subtrees.
	 */
	private int hSpace = 9;

	/**
	 * Vertical space between box and child call subtrees.
	 */
	private int vSpace = 30;

	/**
	 * Padding of the visualization pane.
	 */
	private int globalPadding = 10;

	/**
	 * Font for text in the visualization pane.
	 */
	private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

	/**
	 * List of colors used to visually distinguish different methods in a call
	 * tree.
	 */
	private ColorCollection[] methodColors;

	/**
	 * Color collection for selected node.
	 */
	private ColorCollection selectedColor;

	/**
	 * Color for printing returned values.
	 */
	private Color returnValueColor;

	/**
	 * Indicates that configuration changes are not allowed.
	 */
	private boolean locked = false;

	/**
	 * Constructs default configuration.
	 */
	public Config() {
		setReturnValueColor(Color.blue);
		setSelectedColor(Color.GRAY);
		setMethodColors(new Color[] { new Color(244, 244, 244), new Color(222, 184, 135), new Color(255, 246, 143),
				new Color(245, 245, 220), new Color(127, 255, 212) });
	}

	/**
	 * Checks whether configuration changes are allowed.
	 */
	private void checkLock() {
		if (locked)
			throw new RuntimeException("Configuration can be changed only before building of a call tree.");
	}

	/**
	 * Locks any configuration changes.
	 */
	synchronized void lockChanges() {
		locked = true;
	}

	/**
	 * Returns padding of box displaying a method call and its execution.
	 * 
	 * @return the box padding in pixels.
	 */
	public synchronized int getBoxPadding() {
		return boxPadding;
	}

	/**
	 * Sets padding of box displaying a method call and its execution.
	 * 
	 * @param boxPadding
	 *            the desired padding.
	 */
	public synchronized void setBoxPadding(int boxPadding) {
		checkLock();
		this.boxPadding = boxPadding;
	}

	/**
	 * Returns horizontal space between neighboring call subtrees.
	 * 
	 * @return the horizontal space in pixels.
	 */
	public synchronized int getHSpace() {
		return hSpace;
	}

	/**
	 * Sets horizontal space between neighboring call subtrees.
	 * 
	 * @param hSpace
	 *            the desired horizontal space.
	 */
	public synchronized void setHSpace(int hSpace) {
		checkLock();
		this.hSpace = hSpace;
	}

	/**
	 * Returns vertical space between box and child call subtrees.
	 * 
	 * @return the vertical space in pixels.
	 */
	public synchronized int getVSpace() {
		return vSpace;
	}

	/**
	 * Sets vertical space between box and child call subtrees.
	 * 
	 * @param vSpace
	 *            the desired vertical space.
	 */
	public synchronized void setVSpace(int vSpace) {
		checkLock();
		this.vSpace = vSpace;
	}

	/**
	 * Returns padding of the visualization pane.
	 * 
	 * @return the padding.
	 */
	public synchronized int getGlobalPadding() {
		return globalPadding;
	}

	/**
	 * Sets padding of the visualization pane.
	 * 
	 * @param globalPadding
	 *            the desired padding.
	 */
	public synchronized void setGlobalPadding(int globalPadding) {
		checkLock();
		this.globalPadding = globalPadding;
	}

	/**
	 * Returns font used for texts in the visualization pane.
	 * 
	 * @return the font.
	 */
	public synchronized Font getFont() {
		return font;
	}

	/**
	 * Sets font for texts in the visualization pane.
	 * 
	 * @param font
	 *            the desired font.
	 */
	public synchronized void setFont(Font font) {
		if (font == null) {
			throw new RuntimeException("Font cannot be null.");
		}

		checkLock();
		this.font = font;
	}

	/**
	 * Sets colors used to distinguish different methods in call trees.
	 * 
	 * @param colors
	 *            non-empty array of collors
	 */
	public synchronized void setMethodColors(Color[] colors) {
		if ((colors == null) || (colors.length == 0)) {
			throw new RuntimeException("Colors array must contain at least one color.");
		}

		for (Color c : colors)
			if (c == null) {
				throw new RuntimeException("Color cannot be null.");
			}

		checkLock();

		methodColors = new ColorCollection[colors.length];
		for (int i = 0; i < methodColors.length; i++)
			methodColors[i] = new ColorCollection(colors[i]);
	}

	/**
	 * Returns colors used to distinguish different methods in call trees.
	 * 
	 * @return array of colors
	 */
	public synchronized Color[] getMethodColors() {
		Color[] colors = new Color[methodColors.length];
		int idx = 0;
		for (ColorCollection cc : methodColors) {
			colors[idx] = cc.basicColor;
			idx++;
		}

		return colors;
	}

	/**
	 * Sets color used for selected nodes.
	 * 
	 * @param c
	 *            the color
	 */
	public synchronized void setSelectedColor(Color c) {
		if (c == null) {
			throw new RuntimeException("Color cannot be null.");
		}

		checkLock();
		selectedColor = new ColorCollection(c);
	}

	/**
	 * Returns color used for selected nodes.
	 * 
	 * @return color for selected nodes
	 */
	public synchronized Color getSelectedColor() {
		return selectedColor.basicColor;
	}

	/**
	 * Returns color of returned values.
	 * 
	 * @return the color of returned values.
	 */
	public synchronized Color getReturnValueColor() {
		return returnValueColor;
	}

	/**
	 * Sets color of returned values.
	 * 
	 * @param c
	 *            the color of returned values.
	 */
	public synchronized void setReturnValueColor(Color c) {
		if (c == null) {
			throw new RuntimeException("Color cannot be null.");
		}

		checkLock();
		this.returnValueColor = c;
	}

	/**
	 * Creates a background paint for box of a method call with given
	 * "category".
	 * 
	 * @return the paint.
	 */
	synchronized Paint createMethodCallBgPaint(int methodIdx, Rectangle methodCallBox) {
		if (methodIdx >= 0) {
			methodIdx = methodIdx % methodColors.length;
			ColorCollection cc = methodColors[methodIdx];
			return new GradientPaint(0, methodCallBox.y, cc.lighter, 0, methodCallBox.y + methodCallBox.height,
					cc.darker);
		} else {
			return Color.white;
		}
	}

	/**
	 * Creates a background paint for box of a method call with given "category"
	 * for preview drawing.
	 * 
	 * @return the paint.
	 */
	synchronized Paint createMethodCallBgPreviewPaint(int methodIdx, Rectangle methodCallBox) {
		if (methodIdx >= 0) {
			methodIdx = methodIdx % methodColors.length;
			ColorCollection cc = methodColors[methodIdx];
			return cc.basicColor;
		} else {
			return Color.white;
		}
	}

	/**
	 * Creates a background paint for selected box of a method call.
	 * 
	 * @return the paint.
	 */
	synchronized Paint createSelectedBgPaint(Rectangle methodCallBox) {
		return new GradientPaint(0, methodCallBox.y, selectedColor.lighter, 0, methodCallBox.y + methodCallBox.height,
				selectedColor.darker);
	}
}
