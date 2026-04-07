
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.rmi.Naming;

public class VotingClient {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				VotingSystem service = (VotingSystem) Naming.lookup("rmi://localhost:1099/VotingService");
				new VotingClientWindow(service).setVisible(true);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Unable to connect to RMI server: " + ex.getMessage(), "Connection error",
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	private static final class VotingClientWindow extends JFrame {
		private final JTextField voterIdField = new JTextField();
		private final JTextField candidateField = new JTextField();
		private final JTextArea outputArea = new JTextArea();

		private VotingClientWindow(VotingSystem service) {
			super("Voting System RMI Demo");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setSize(820, 620);
			setLocationRelativeTo(null);
			setLayout(new BorderLayout(12, 12));
			((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
			final VotingSystem remote = service;

			JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
			formPanel.add(new JLabel("Voter ID"));
			formPanel.add(voterIdField);
			formPanel.add(new JLabel("Candidate Name"));
			formPanel.add(candidateField);

			JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10));
			JButton registerButton = new JButton("Register Voter");
			JButton voteButton = new JButton("Cast Vote");
			JButton listButton = new JButton("Show Candidate List");
			JButton countButton = new JButton("Vote Count");
			JButton resetButton = new JButton("Reset Election");
			JButton refreshButton = new JButton("Refresh Status");

			buttonPanel.add(registerButton);
			buttonPanel.add(voteButton);
			buttonPanel.add(listButton);
			buttonPanel.add(countButton);
			buttonPanel.add(resetButton);
			buttonPanel.add(refreshButton);

			JPanel left = new JPanel(new BorderLayout(10, 10));
			left.add(formPanel, BorderLayout.NORTH);
			left.add(buttonPanel, BorderLayout.CENTER);

			outputArea.setEditable(false);
			outputArea.setLineWrap(true);
			outputArea.setWrapStyleWord(true);
			JScrollPane scrollPane = new JScrollPane(outputArea);
			outputArea.setText("Connected to RMI service. Use the buttons to exercise remote calls.");

			add(left, BorderLayout.WEST);
			add(scrollPane, BorderLayout.CENTER);

			registerButton.addActionListener(event -> runRemote(() -> {
				int voterId = readVoterId();
				int result = remote.register(voterId);
				if (result == 0) {
					append("Registered voter " + voterId);
				} else {
					append("Voter already exists: " + voterId);
				}
			}));

			voteButton.addActionListener(event -> runRemote(() -> {
				int voterId = readVoterId();
				String candidate = readCandidate();
				int result = remote.castvote(candidate, voterId);
				if (result == 0) {
					append("Vote recorded for existing candidate: " + candidate.toUpperCase());
				} else if (result == 1) {
					append("New candidate added and vote recorded: " + candidate.toUpperCase());
				} else if (result == 2) {
					append("Voter is not registered.");
				} else if (result == 3) {
					append("This voter has already voted.");
				} else {
					append("Unable to cast vote.");
				}
			}));

			listButton.addActionListener(event -> runRemote(() -> {
				String[][] list = remote.candidatelist();
				if (list.length == 0) {
					append("No candidates yet.");
					return;
				}
				StringBuilder builder = new StringBuilder();
				builder.append("Candidate list:\n");
				for (String[] row : list) {
					if (row[0] == null) {
						continue;
					}
					builder.append(row[0]).append(" -> ").append(row[1]).append(" votes\n");
				}
				append(builder.toString().trim());
			}));

			countButton.addActionListener(event -> runRemote(() -> {
				String candidate = readCandidate();
				int count = remote.votecount(candidate);
				if (count < 0) {
					append("Candidate not found: " + candidate.toUpperCase());
				} else {
					append(candidate.toUpperCase() + " currently has " + count + " vote(s).");
				}
			}));

			resetButton.addActionListener(event -> runRemote(() -> {
				remote.reset();
				append("Election reset successfully.");
			}));

			refreshButton.addActionListener(event -> runRemote(() -> {
				String[][] list = remote.candidatelist();
				append("Refreshed. Candidates tracked: " + list.length);
			}));
		}

		private void runRemote(RemoteAction action) {
			try {
				action.run();
			} catch (Exception ex) {
				append("Error: " + ex.getMessage());
			}
		}

		private int readVoterId() {
			return Integer.parseInt(voterIdField.getText().trim());
		}

		private String readCandidate() {
			return candidateField.getText().trim();
		}

		private void append(String message) {
			outputArea.append(message + "\n");
			outputArea.setCaretPosition(outputArea.getDocument().getLength());
		}

		private interface RemoteAction {
			void run() throws Exception;
		}
	}
}
