package sk.upjs.paz.calltree;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

/**
 * Panel visualizing a call tree.
 */
@SuppressWarnings("serial")
class CallTreePanel extends JPanel {

	/**
	 * Configuration settings for visualization.
	 */
	private final Config config;

	/**
	 * Root of the displayed tree.
	 */
	private TreeNode root;

	/**
	 * Selected node.
	 */
	private TreeNode selectedNode;

	/**
	 * Panel that provides global view on visualized call tree.
	 */
	private CallTreePreviewPanel previewPanel;

	/**
	 * Panel that provides detailed information about a tree node.
	 */
	private MethodCallDetailPanel detailPanel;

	/**
	 * Create the panel.
	 */
	public CallTreePanel() {
		config = CallTree.getConfig();
		root = null;

		// install mouse listeners
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				TreeNode node = getNodeAt(e.getX(), e.getY());
				if ((node != null) && (node.getMethodCall().isMarked())) {
					if (selectedNode != null) {
						selectedNode.setSelected(false);
						selectedNode = null;
					}

					if (detailPanel != null)
						detailPanel.setNode(node);

					node.setSelected(true);
					selectedNode = node;
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				TreeNode node = getNodeAt(e.getX(), e.getY());
				if ((node != null) && (node.getMethodCall().isMarked())) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					setCursor(Cursor.getDefaultCursor());
				}

			}
		};

		addMouseListener(adapter);
		addMouseMotionListener(adapter);
	}

	/**
	 * Sets panels that provides global view on call tree and details of a
	 * selected tree node.
	 */
	public void setPanels(CallTreePreviewPanel previewPanel, MethodCallDetailPanel detailPanel) {
		this.previewPanel = previewPanel;
		this.detailPanel = detailPanel;
	}

	/**
	 * Returns configuration settings used by this panel for visualizing a call
	 * tree.
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * Sets root of visualized call tree.
	 */
	public void setRoot(TreeNode root) {
		if (this.root == root)
			return;

		if (selectedNode != null) {
			selectedNode.setSelected(false);
			selectedNode = null;
		}

		this.root = root;

		if (root != null) {
			root.setSelected(true);
			selectedNode = root;
		}

		if (detailPanel != null) {
			detailPanel.setNode(root);
		}

		relayoutTree();
	}

	/**
	 * Returns tree node that is root of visualized call tree.
	 */
	public TreeNode getRoot() {
		return this.root;
	}

	/**
	 * Relayout visualized tree.
	 */
	public void relayoutTree() {
		if (root == null) {
			setSize(0, 0);
			return;
		}

		this.setFont(config.getFont());
		FontMetrics fm = this.getGraphics().getFontMetrics();
		root.measure(fm, config);
		root.layout(config.getGlobalPadding(), config.getGlobalPadding(), fm, config);
		setPreferredSize(new Dimension(root.getWidth() + 2 * config.getGlobalPadding(),
				root.getHeight() + 2 * config.getGlobalPadding()));
		revalidate();
		repaint();
	}

	@Override
	public void repaint() {
		super.repaint();
		// each repaint request of call tree must repaint panel with global view
		if (previewPanel != null) {
			previewPanel.repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Rectangle clip = g.getClipBounds();
		if (clip == null)
			clip = new Rectangle(getWidth(), getHeight());

		g2.setPaint(Color.white);
		g2.fill(clip);

		if (root != null) {
			g2.setFont(config.getFont());
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			root.paint(g2, config);
		}
	}

	/**
	 * Returns tree node whose box is located at given coordinates.
	 */
	private TreeNode getNodeAt(int x, int y) {
		TreeNode traversal = root;
		while (traversal != null) {
			if (!traversal.containsPointInTree(x, y))
				return null;

			if (traversal.containsPointInBox(x, y))
				return traversal;

			traversal = traversal.getChildContainingPoint(x, y);
		}

		return null;
	}
}
