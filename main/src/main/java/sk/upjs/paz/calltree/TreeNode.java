package sk.upjs.paz.calltree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

/**
 * Node in a call tree that corresponds to a method call.
 */
class TreeNode {

	/**
	 * Stroke with width 2
	 */
	private static final Stroke DOUBLE_STROKE = new BasicStroke(2);

	/**
	 * Stroke with width 1
	 */
	private static final Stroke SIMPLE_STROKE = new BasicStroke(1);

	/**
	 * Method call represented by this tree node.
	 */
	private final MethodCall methodCall;

	/**
	 * Indicates whether underlying method call is currently on callstack.
	 */
	private boolean onCallstack;

	/**
	 * Indicates that the node is selected.
	 */
	private boolean selected;

	/**
	 * Type of method in view of the whole recorded history.
	 */
	private final int typeIndex;

	/**
	 * List of child tree nodes.
	 */
	private final List<TreeNode> children = new ArrayList<TreeNode>();

	/**
	 * Bounding box of the whole call tree starting in this tree node.
	 */
	private final Rectangle boundingBox = new Rectangle();

	/**
	 * Rectangle that specifies a box representing this node of a call tree.
	 */
	private final Rectangle nodeBox = new Rectangle();

	/**
	 * Title displayed in this node.
	 */
	private String methodTitle;

	/**
	 * Constructs a new tree node representing a method call (execution).
	 * 
	 * @param methodCall
	 *            method call represented by this tree node.
	 * 
	 * @param typeIndex
	 *            type of method in the whole recorded history
	 */
	public TreeNode(MethodCall methodCall, int typeIndex) {
		this.methodCall = methodCall;
		this.typeIndex = typeIndex;
	}

	/**
	 * Returns method call represented by this tree node.
	 * 
	 * @return
	 */
	public MethodCall getMethodCall() {
		return methodCall;
	}

	/**
	 * Returns whether this tree node is currently located on call stack.
	 */
	public boolean isOnCallstack() {
		return onCallstack;
	}

	/**
	 * Sets whether this tree node is currently located on call stack.
	 */
	public void setOnCallstack(boolean onCallstack) {
		this.onCallstack = onCallstack;
	}

	/**
	 * Returns whether the node is selected.
	 * 
	 * @return true, if the node is selected, false otherwise
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets whether the node is selected.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Returns the y-coordinate of the tree rooted in this node.
	 */
	public int getTop() {
		return boundingBox.y;
	}

	/**
	 * Returns the x-coordinate of the tree rooted in this node.
	 */
	public int getLeft() {
		return boundingBox.x;
	}

	/**
	 * Returns width of the tree rooted in this node.
	 */
	public int getWidth() {
		return boundingBox.width;
	}

	/**
	 * Returns height of the tree rooted in this node.
	 */
	public int getHeight() {
		return boundingBox.height;
	}

	/**
	 * Adds a child node.
	 */
	public void addChild(TreeNode tn) {
		children.add(tn);
	}

	/**
	 * Returns the number of children.
	 */
	public int childCount() {
		return children.size();
	}

	/**
	 * Realizes a measure pass.
	 */
	public void measure(FontMetrics fm, Config config) {
		boundingBox.width = 0;
		boundingBox.height = 0;

		// measure children
		for (TreeNode child : children)
			child.measure(fm, config);

		// calculate dimensions for displaying subtrees
		boolean first = true;
		for (TreeNode child : children) {
			boundingBox.width += child.getWidth();
			if (first) {
				first = false;
			} else {
				boundingBox.width += config.getHSpace();
			}

			boundingBox.height = Math.max(boundingBox.height, child.getHeight());
		}

		// measure box for displaying the node
		measureBox(fm, config);

		// calculate final dimension of the tree
		boundingBox.width = Math.max(boundingBox.width, nodeBox.width);
		boundingBox.height += nodeBox.height;
		boundingBox.height += fm.getHeight();
		if (!children.isEmpty())
			boundingBox.height += config.getVSpace();
	}

	/**
	 * Measures the box representing this tree node.
	 */
	private void measureBox(FontMetrics fm, Config config) {
		methodTitle = buildMethodTitle(config);
		int titleWidth = fm.stringWidth(methodTitle);
		int titleHeight = fm.getHeight();

		nodeBox.width = titleWidth + 2 * config.getBoxPadding();
		nodeBox.height = titleHeight + 2 * config.getBoxPadding();
		if (nodeBox.width < nodeBox.height * 1.5) {
			nodeBox.width = (int) Math.round(nodeBox.height * 1.5);
		}
	}

	/**
	 * Realizes the layout pass.
	 */
	public void layout(int left, int top, FontMetrics fm, Config config) {
		boundingBox.x = left;
		boundingBox.y = top;

		nodeBox.x = boundingBox.x + (boundingBox.width - nodeBox.width) / 2;
		nodeBox.y = boundingBox.y + fm.getHeight();

		top += nodeBox.height + fm.getHeight() + config.getVSpace();
		for (TreeNode child : children) {
			child.layout(left, top, fm, config);
			left += child.getWidth() + config.getHSpace();
		}
	}

	/**
	 * Returns whether tree rooted in this node contains a point at given
	 * coordinates.
	 */
	public boolean containsPointInTree(int x, int y) {
		return boundingBox.contains(x, y);
	}

	/**
	 * Returns whether point at given coordinates is located within visual box
	 * of this tree node.
	 */
	public boolean containsPointInBox(int x, int y) {
		return nodeBox.contains(x, y);
	}

	/**
	 * Returns child that is a root of a tree containing a given point.
	 */
	public TreeNode getChildContainingPoint(int x, int y) {
		for (TreeNode child : children)
			if (child.containsPointInTree(x, y))
				return child;

		return null;
	}

	/**
	 * Returns position where this node has to be attached to its parent.
	 */
	public Point getAnchor() {
		return new Point(nodeBox.x + nodeBox.width / 2, nodeBox.y);
	}

	/**
	 * Paints the tree rooted in this node.
	 */
	public void paint(Graphics2D g2, Config config) {
		// do not realize paintings of trees that are not visible
		Rectangle clip = g2.getClipBounds();
		if (clip != null) {
			if (!boundingBox.intersects(clip))
				return;
		}

		// draw connections to children
		Point nodeAnchor = new Point(nodeBox.x + nodeBox.width / 2, nodeBox.y + nodeBox.height);

		for (TreeNode child : children) {
			child.paint(g2, config);
			Point anchor = child.getAnchor();
			if (child.isOnCallstack()) {
				g2.setStroke(DOUBLE_STROKE);
				g2.setColor(Color.RED);
			} else {
				g2.setColor(Color.BLACK);
			}

			g2.drawLine(nodeAnchor.x, nodeAnchor.y, anchor.x, anchor.y);
			g2.setStroke(SIMPLE_STROKE);
		}

		// draw box
		Paint bgPaint;
		if (selected)
			bgPaint = config.createSelectedBgPaint(nodeBox);
		else
			bgPaint = config.createMethodCallBgPaint(typeIndex, nodeBox);

		g2.setPaint(bgPaint);
		RoundRectangle2D boxShape = new RoundRectangle2D.Double(nodeBox.x, nodeBox.y, nodeBox.width, nodeBox.height,
				config.getBoxPadding(), config.getBoxPadding());
		g2.fill(boxShape);

		if (isOnCallstack()) {
			g2.setStroke(DOUBLE_STROKE);
			g2.setPaint(Color.RED);
		} else {
			g2.setPaint(Color.GRAY);
		}
		g2.draw(boxShape);
		g2.setStroke(SIMPLE_STROKE);

		// draw title of the box
		FontMetrics fm = g2.getFontMetrics();
		Rectangle2D titleBounds = fm.getStringBounds(methodTitle, g2);
		if (selected) {
			g2.setColor(Color.white);
		} else {
			g2.setColor(Color.black);
		}
		g2.drawString(methodTitle, nodeBox.x + (int) ((nodeBox.width - titleBounds.getWidth()) / 2),
				nodeBox.y + (nodeBox.height + fm.getHeight()) / 2 - fm.getDescent());

		// draw marker, if this node contains log records
		if (methodCall.hasLogs()) {
			g2.setColor(Color.red);
			int bp = config.getBoxPadding();
			g2.fill(new Ellipse2D.Double(nodeBox.getMaxX() - bp, nodeBox.y + bp, bp / 2, bp / 2));
		}

		// ask children to draw return value
		for (TreeNode child : children) {
			Point anchor = child.getAnchor();
			if (anchor.x < nodeAnchor.x) {
				child.drawReturnValue(g2, child.nodeBox.x, anchor.x, config);
			} else {
				child.drawReturnValue(g2, anchor.x, child.nodeBox.x + child.nodeBox.width, config);
			}
		}

		// if this node is root, it draws its return value now
		if (methodCall.isRoot()) {
			drawReturnValue(g2, nodeBox.x, nodeBox.x + nodeBox.width, config);
		}
	}

	/**
	 * Paints tree rooted in this node for preview display.
	 */
	public void paintPreview(Graphics2D g2, Config config) {
		// do not realize paintings of trees that are not visible
		Rectangle clip = g2.getClipBounds();
		if (clip != null) {
			if (!boundingBox.intersects(clip))
				return;
		}

		// draw connections to children
		Point nodeAnchor = new Point(nodeBox.x + nodeBox.width / 2, nodeBox.y + nodeBox.height);

		for (TreeNode child : children) {
			child.paintPreview(g2, config);
			Point anchor = child.getAnchor();
			if (child.isOnCallstack()) {
				g2.setStroke(DOUBLE_STROKE);
				g2.setColor(Color.RED);
			} else {
				g2.setColor(Color.BLACK);
			}

			g2.drawLine(nodeAnchor.x, nodeAnchor.y, anchor.x, anchor.y);
			g2.setStroke(SIMPLE_STROKE);
		}

		// draw box
		Paint bgPaint;
		if (selected)
			bgPaint = config.createSelectedBgPaint(nodeBox);
		else
			bgPaint = config.createMethodCallBgPreviewPaint(typeIndex, nodeBox);

		g2.setPaint(bgPaint);
		RoundRectangle2D boxShape = new RoundRectangle2D.Double(nodeBox.x, nodeBox.y, nodeBox.width, nodeBox.height,
				config.getBoxPadding(), config.getBoxPadding());
		g2.fill(boxShape);

		if (isOnCallstack()) {
			g2.setStroke(DOUBLE_STROKE);
			g2.setPaint(Color.RED);
		} else {
			g2.setPaint(Color.GRAY);
		}
		g2.draw(boxShape);
		g2.setStroke(SIMPLE_STROKE);
	}

	/**
	 * Draws return value
	 */
	private void drawReturnValue(Graphics2D g2, int x1, int x2, Config config) {
		if (!methodCall.hasReturnValue())
			return;

		Object returnValue = methodCall.getReturnValue();
		String returnValueAsString = (returnValue == null) ? "null" : returnValue.toString();

		FontMetrics fm = g2.getFontMetrics();
		Rectangle2D bounds = fm.getStringBounds(returnValueAsString, g2);
		g2.setColor(config.getReturnValueColor());
		g2.drawString(returnValueAsString, (int) (x1 + (x2 - x1 - bounds.getWidth()) / 2), nodeBox.y - 3);
	}

	@Override
	public String toString() {
		return buildMethodTitle(null);
	}

	/**
	 * Builds a title of this tree node.
	 */
	public String buildMethodTitle(Config config) {
		StringBuilder sb = new StringBuilder();
		sb.append(methodCall.getMethodName());
		sb.append("(");

		Object[] arguments = methodCall.getArguments();
		if ((arguments != null) && (arguments.length != 0)) {
			boolean firstArg = true;
			for (Object o : arguments) {
				if (!firstArg)
					sb.append(", ");
				else
					firstArg = false;

				if (o != null)
					sb.append(o.toString());
				else
					sb.append("null");
			}
		}

		sb.append(")");
		return sb.toString();
	}
}
