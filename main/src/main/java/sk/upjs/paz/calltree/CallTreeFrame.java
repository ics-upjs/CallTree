package sk.upjs.paz.calltree;

import java.util.*;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JSplitPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JButton;

import sk.upjs.paz.calltree.CallTreeBuilder.CallTreeState;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.Toolkit;

/**
 * Frame visualizing a call tree builder.
 */
@SuppressWarnings("serial")
class CallTreeFrame extends JFrame {

	/**
	 * List of all tree nodes in order in which they appear in the call tree
	 * builder history.
	 */
	private final List<TreeNode> nodes;

	/**
	 * List of all tree nodes that are roots of call trees.
	 */
	private final List<TreeNode> roots;

	/**
	 * List of all marked methods that appeared in the history.
	 */
	private final List<String> methodTypes;

	/**
	 * Queue of wait semaphores.
	 */
	private final Queue<Semaphore> semaphores;

	private JPanel contentPane;
	private CallTreePanel callTreePanel;
	private JButton continueButton;
	private JComboBox<TreeNode> callTreeRootCombo;
	private JCheckBox waitTimeCheckBox;
	private JSlider waitTimeSlider;
	private javax.swing.Timer waitTimer;
	private CallTreePreviewPanel callTreePreview;
	private JScrollPane callTreeScrollPane;
	private MethodCallDetailPanel detailPanel;

	/**
	 * Create the frame.
	 */
	public CallTreeFrame() {
		waitTimer = new javax.swing.Timer(Integer.MAX_VALUE, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmContinueButtonClicked();
			}
		});
		waitTimer.setRepeats(false);

		initComponents();
		waitTimeSliderChanged();

		callTreePanel.setPanels(callTreePreview, detailPanel);
		callTreePreview.setSource(callTreePanel);
		callTreePreview.setScroller(callTreeScrollPane);

		nodes = new ArrayList<TreeNode>();
		roots = new ArrayList<TreeNode>();
		methodTypes = new ArrayList<String>();
		semaphores = new LinkedList<Semaphore>();
	}

	/**
	 * Updates visualization according to current state of a call tree builder.
	 */
	public void updateState(final CallTreeState state) {
		// check whether received update corresponds to the previous update
		if (state.history.size() < nodes.size()) {
			deleteCallTrees();
		}

		boolean matchPreviousHistory = true;
		for (int i = 0; i < nodes.size(); i++)
			if (nodes.get(i).getMethodCall() != state.history.get(i)) {
				matchPreviousHistory = false;
				break;
			}

		if (!matchPreviousHistory) {
			deleteCallTrees();
		}

		// create new tree nodes if necessary
		TreeNode displayedRoot = callTreePanel.getRoot();
		boolean refreshDisplayRoot = false;
		boolean newRoots = false;
		for (int i = nodes.size(); i < state.history.size(); i++) {
			MethodCall mc = state.history.get(i);

			// compute method type index for marked method calls
			int methodTypeIndex = -1;
			if (mc.isMarked()) {
				String id = mc.getClassName() + "." + mc.getMethodName();
				methodTypeIndex = methodTypes.indexOf(id);
				if (methodTypeIndex == -1) {
					methodTypeIndex = methodTypes.size();
					methodTypes.add(id);
				}
			}

			TreeNode tn = new TreeNode(mc, methodTypeIndex);
			nodes.add(tn);
			if (mc.isRoot()) {
				roots.add(tn);
				newRoots = true;
			} else {
				if (displayedRoot == roots.get(roots.size() - 1))
					refreshDisplayRoot = true;

				nodes.get(mc.getParent().getIndex()).addChild(tn);
			}
		}

		// set callstack flag for method calls that are on callstack
		for (TreeNode node : nodes)
			node.setOnCallstack(false);

		MethodCall nodePointer = state.activeCall;
		while (nodePointer != null) {
			nodes.get(nodePointer.getIndex()).setOnCallstack(true);
			nodePointer = nodePointer.getParent();
		}

		// if there are new roots, update list of available call trees
		if (newRoots) {
			callTreeRootCombo.setModel(new DefaultComboBoxModel<TreeNode>(roots.toArray(new TreeNode[0])));
			callTreeRootComboChanged();
		}

		// refresh visual content
		if (refreshDisplayRoot)
			callTreePanel.relayoutTree();

		callTreePanel.repaint();
		detailPanel.refreshInfo();
	}

	public void addConfirmationRequest(Semaphore semaphore) {
		semaphores.add(semaphore);
		continueButton.setEnabled(!semaphores.isEmpty());

		if (waitTimeCheckBox.isSelected()) {
			if (waitTimeSlider.getValue() == 0) {
				confirmContinueButtonClicked();
			} else {
				waitTimer.restart();
			}
		}
	}

	/**
	 * Removes all call trees
	 */
	private void deleteCallTrees() {
		nodes.clear();
		roots.clear();
		methodTypes.clear();
	}

	private void initComponents() {
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(CallTreeFrame.class.getResource("/sk/upjs/paz/calltree/images/tree.png")));
		setTitle("Call Tree Visualization");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 757, 486);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JPanel topPanel = new JPanel();
		topPanel.setBorder(null);

		JSplitPane splitPane1 = new JSplitPane();
		splitPane1.setResizeWeight(0.8);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(topPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(splitPane1, GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(topPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(splitPane1, GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)));
		topPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		topPanel.add(panel, BorderLayout.WEST);

		JLabel lblDisplayedCalltree = new JLabel("Call tree:");
		panel.add(lblDisplayedCalltree);

		callTreeRootCombo = new JComboBox<TreeNode>();
		panel.add(callTreeRootCombo);
		callTreeRootCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callTreeRootComboChanged();
			}
		});

		JPanel panel_1 = new JPanel();
		topPanel.add(panel_1, BorderLayout.EAST);

		waitTimeCheckBox = new JCheckBox("Wait (0.5 s)");
		waitTimeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				waitTimeCheckboxChanged();
			}
		});
		panel_1.add(waitTimeCheckBox);

		waitTimeSlider = new JSlider();
		waitTimeSlider.setValue(10);
		waitTimeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				waitTimeSliderChanged();
			}
		});
		waitTimeSlider.setMaximum(50);
		panel_1.add(waitTimeSlider);

		continueButton = new JButton("Continue");
		panel_1.add(continueButton);
		continueButton.setEnabled(false);
		continueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmContinueButtonClicked();
			}
		});

		JSplitPane splitPane2 = new JSplitPane();
		splitPane2.setResizeWeight(0.8);
		splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);

		callTreePreview = new CallTreePreviewPanel();

		detailPanel = new MethodCallDetailPanel();
		detailPanel.setBorder(null);

		callTreeScrollPane = new JScrollPane();
		callTreePanel = new CallTreePanel();
		callTreeScrollPane.setViewportView(callTreePanel);

		splitPane2.setRightComponent(callTreePreview);
		splitPane2.setLeftComponent(callTreeScrollPane);

		splitPane1.setRightComponent(detailPanel);
		splitPane1.setLeftComponent(splitPane2);

		contentPane.setLayout(gl_contentPane);
	}

	private void confirmContinueButtonClicked() {
		waitTimer.stop();

		if (!semaphores.isEmpty()) {
			semaphores.poll().release();
		}

		continueButton.setEnabled(!semaphores.isEmpty());
		if (!semaphores.isEmpty() && waitTimeCheckBox.isSelected()) {
			waitTimer.restart();
		}
	}

	private void callTreeRootComboChanged() {
		int idx = callTreeRootCombo.getSelectedIndex();
		if ((idx >= 0) && (idx < roots.size())) {
			callTreePanel.setRoot(roots.get(callTreeRootCombo.getSelectedIndex()));
		}
	}

	private void waitTimeSliderChanged() {
		int delayValue = waitTimeSlider.getValue();
		StringBuilder sb = new StringBuilder();
		sb.append("Delay ");
		if (delayValue % 10 == 0) {
			sb.append(delayValue / 10);
		} else {
			sb.append(delayValue / 10.0);
		}
		sb.append(" sec.");
		waitTimeCheckBox.setText(sb.toString());
		waitTimer.setInitialDelay(waitTimeSlider.getValue() * 100);
	}

	private void waitTimeCheckboxChanged() {
		if (!waitTimeCheckBox.isSelected()) {
			waitTimer.stop();
		} else {
			if (!semaphores.isEmpty()) {
				waitTimer.restart();
			}
		}
	}
}
