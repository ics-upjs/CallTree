package sk.upjs.calltree;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Panel displaying a global view on a call tree.
 */
@SuppressWarnings("serial")
class CallTreePreviewPanel extends JPanel {

	/**
	 * Panel visualizing a call tree whose global view is displayed by this
	 * panel.
	 */
	private CallTreePanel source = null;

	/**
	 * Scrollpane that controls which part of the call tree is displayed in the
	 * source panel.
	 */
	private JScrollPane scroller = null;

	/**
	 * Bounds of the preview area.
	 */
	private Rectangle previewArea = null;

	/**
	 * Bounds of the focus (detailed view) area.
	 */
	private Rectangle focusRect = null;

	/**
	 * Scale of the global view.
	 */
	private double scale;

	/**
	 * Create the panel.
	 */
	public CallTreePreviewPanel() {
		// install handlers for mouse events
		MouseAdapter adapter = new MouseAdapter() {
			private boolean focusDraging = false;
			private int lastX;
			private int lastY;

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (!focusRect.contains(e.getX(), e.getY())) {
						centerFocus(e.getX(), e.getY());
					}

					focusDraging = true;
					lastX = e.getX();
					lastY = e.getY();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if ((focusDraging) && (focusRect != null)) {
					int x = e.getX();
					int y = e.getY();
					if (focusRect.contains(x, y)) {
						int dx = x - lastX;
						int dy = y - lastY;
						centerFocus(focusRect.x + dx + focusRect.width / 2, focusRect.y + dy + focusRect.height / 2);
					}
					lastX = x;
					lastY = y;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				focusDraging = false;
			}
		};

		addMouseMotionListener(adapter);
		addMouseListener(adapter);
	}

	/**
	 * Returns the panel visualizing a call tree whose global view is displayed
	 * by this panel.
	 */
	public CallTreePanel getSource() {
		return source;
	}

	/**
	 * Sets panel visualizing a call tree whose global view is displayed by this
	 * panel.
	 */
	public void setSource(CallTreePanel source) {
		this.source = source;
	}

	/**
	 * Sets JScrollPane that controls which part of a call tree is displayed.
	 */
	public void setScroller(JScrollPane scroller) {
		this.scroller = scroller;
	}

	/**
	 * Changes detailed view on call tree in such a way that a point at given
	 * coordinates in the global view is displayed in the center of the detailed
	 * view.
	 */
	private void centerFocus(int x, int y) {
		if ((scroller == null) || (previewArea == null) || (focusRect == null))
			return;

		// normalize x, y
		x -= previewArea.x;
		y -= previewArea.y;

		// calculate coordinates of top left corder
		x -= focusRect.width / 2;
		y -= focusRect.height / 2;
		x = Math.max(x, 0);
		y = Math.max(y, 0);

		// update position of viewport
		if ((previewArea.width > 0) && (previewArea.height > 0) && (scale > 0)) {
			scroller.getViewport().setViewPosition(new Point((int) Math.round(x / scale), (int) Math.round(y / scale)));
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (source == null) {
			previewArea = null;
			focusRect = null;
			scale = 0;
			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		int width = getWidth();
		int height = getHeight();
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		scale = Math.min(width / (double) sourceWidth, height / (double) sourceHeight);

		// background
		if (previewArea == null)
			previewArea = new Rectangle();

		previewArea.width = (int) Math.round(sourceWidth * scale);
		previewArea.height = (int) Math.round(sourceHeight * scale);
		previewArea.x = (width - previewArea.width) / 2;
		previewArea.y = (height - previewArea.height) / 2;

		g2.setColor(Color.white);
		g2.fill(previewArea);
		g2.setColor(Color.DARK_GRAY);
		g2.draw(previewArea);

		// enable antialiasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// call tree
		TreeNode root = source.getRoot();
		if (root != null) {
			Graphics2D sg2 = (Graphics2D) g2.create();
			sg2.translate(previewArea.x, previewArea.y);
			sg2.scale(scale, scale);
			try {
				root.paintPreview(sg2, source.getConfig());
			} finally {
				sg2.dispose();
			}
		}

		// focus
		if (scroller != null) {
			focusRect = scroller.getViewport().getViewRect();
			focusRect.x = previewArea.x + (int) Math.round(scale * focusRect.x);
			focusRect.y = previewArea.y + (int) Math.round(scale * focusRect.y);
			focusRect.width = (int) Math.round(scale * focusRect.width);
			focusRect.height = (int) Math.round(scale * focusRect.height);
			g2.setColor(Color.red);
			g2.draw(focusRect);
		} else {
			focusRect = null;
		}
	}
}
