package sk.upjs.calltree;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import sk.upjs.calltree.MethodCall.LogRecord;

import java.awt.Color;
import java.util.List;

/**
 * Panel visualizing details of a method call.
 */
@SuppressWarnings("serial")
class MethodCallDetailPanel extends JPanel {

	/**
	 * Tree node that is displayed in this detail panel.
	 */
	private TreeNode node;
	private JLabel methodNameLabel;
	private JLabel classNameLabel;
	private JLabel titleLabel;
	private JTextPane infoPane;

	/**
	 * Create the panel.
	 */
	public MethodCallDetailPanel() {
		initComponents();
		refreshInfo();
	}

	public TreeNode getNode() {
		return node;
	}

	public void setNode(TreeNode node) {
		if (node == this.node)
			return;

		this.node = node;
		refreshInfo();
	}

	public void refreshInfo() {
		if (node == null) {
			titleLabel.setText("");
			methodNameLabel.setText("");
			classNameLabel.setText("");
			infoPane.setText("");
			return;
		}

		titleLabel.setText(node.buildMethodTitle(null));
		methodNameLabel.setText(node.getMethodCall().getMethodName());
		classNameLabel.setText(node.getMethodCall().getClassName());
		printLogs(node.getMethodCall().getLogs(node.childCount()));
	}

	private void printLogs(List<LogRecord> logs) {

		// Style for values
		SimpleAttributeSet valuesStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(valuesStyle, new Color(0, 0, 192));

		// Style for lines
		SimpleAttributeSet infoStyle = new SimpleAttributeSet();
		StyleConstants.setItalic(infoStyle, true);

		// Style for log
		SimpleAttributeSet logStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(logStyle, new Color(63, 127, 95));

		// Style for call
		SimpleAttributeSet callStyle = new SimpleAttributeSet();
		StyleConstants.setBold(callStyle, true);

		// Style for return
		SimpleAttributeSet returnStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(returnStyle, new Color(127, 0, 85));
		StyleConstants.setBold(returnStyle, true);

		infoPane.setText("");
		Document doc = infoPane.getDocument();
		try {
			MethodCall nodeCall = node.getMethodCall();
			int lIndex = 0;
			List<MethodCall> calls = nodeCall.getMethodCalls();
			for (int mcc = 0; mcc <= node.childCount(); mcc++) {
				// logs before the call
				while ((lIndex < logs.size()) && (logs.get(lIndex).numberOfChildren <= mcc)) {
					LogRecord log = logs.get(lIndex);

					// intro of log
					doc.insertString(doc.getLength(), "Log", infoStyle);
					if (log.lineNumber >= 0) {
						doc.insertString(doc.getLength(), " (line " + log.lineNumber + ")", infoStyle);
					}
					doc.insertString(doc.getLength(), ":", infoStyle);

					// message of log
					if (log.message != null) {
						doc.insertString(doc.getLength(), log.message, logStyle);
					}
					if (log.values != null) {
						doc.insertString(doc.getLength(), " [" + valuesToString(log.values) + "]", valuesStyle);
					}
					doc.insertString(doc.getLength(), "\n", logStyle);

					lIndex++;
				}

				// call
				if (mcc < node.childCount()) {
					MethodCall mc = calls.get(mcc);
					doc.insertString(doc.getLength(), "Method call", infoStyle);
					if (mc.getLineNumber() >= 0) {
						doc.insertString(doc.getLength(), " (line " + mc.getLineNumber() + ")", infoStyle);
					}
					doc.insertString(doc.getLength(), ":", infoStyle);

					doc.insertString(doc.getLength(), mc.getMethodName() + "(", callStyle);
					doc.insertString(doc.getLength(), valuesToString(mc.getArguments()), valuesStyle);
					doc.insertString(doc.getLength(), ")\n", callStyle);
				}
			}

			// return
			if (nodeCall.hasReturnValue()) {
				doc.insertString(doc.getLength(), "return", returnStyle);
				doc.insertString(doc.getLength(), " " + nodeCall.getReturnValue(), valuesStyle);
			}

		} catch (Exception ignore) {

		}
	}

	/**
	 * Formats array of objects.
	 */
	private String valuesToString(Object[] values) {
		if (values == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (Object o : values) {
			if (sb.length() != 0) {
				sb.append(", ");
			}

			if (o == null) {
				sb.append("null");
			} else {
				sb.append(o.toString());
			}
		}

		return sb.toString();
	}

	private void initComponents() {
		JPanel panel = new JPanel();

		JPanel panel_1 = new JPanel();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(panel_1, Alignment.LEADING, 0, 0, Short.MAX_VALUE).addComponent(panel,
										Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 226, Short.MAX_VALUE))
				.addGap(0)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)));

		JLabel label = new JLabel("Execution log:");
		label.setFont(new Font("Tahoma", Font.BOLD, 12));

		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_1.createSequentialGroup().addGap(5).addComponent(label).addContainerGap(251,
						Short.MAX_VALUE))
				.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE));
		gl_panel_1.setVerticalGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup().addComponent(label)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)));

		infoPane = new JTextPane();
		infoPane.setEditable(false);
		scrollPane.setViewportView(infoPane);
		panel_1.setLayout(gl_panel_1);

		JLabel lblNewLabel = new JLabel("Class:");
		lblNewLabel.setForeground(Color.DARK_GRAY);

		JLabel lblNewLabel_1 = new JLabel("Method:");
		lblNewLabel_1.setForeground(Color.DARK_GRAY);

		titleLabel = new JLabel("New label");
		titleLabel.setForeground(new Color(0, 0, 128));
		titleLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

		classNameLabel = new JLabel("New label");

		methodNameLabel = new JLabel("New label");
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(gl_panel
				.createSequentialGroup()
				.addGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(gl_panel.createSequentialGroup()
						.addGap(6)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup().addComponent(lblNewLabel_1)
										.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(methodNameLabel))
								.addGroup(gl_panel.createSequentialGroup()
										.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 43,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(classNameLabel))))
						.addGroup(gl_panel.createSequentialGroup().addContainerGap().addComponent(titleLabel,
								GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)))
				.addContainerGap()));
		gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panel.createSequentialGroup().addGap(3).addComponent(titleLabel)
								.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 14,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(classNameLabel))
								.addGap(4).addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblNewLabel_1).addComponent(methodNameLabel))
								.addContainerGap()));
		panel.setLayout(gl_panel);
		setLayout(groupLayout);
	}
}
