package elkaproj.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * First screen, allows the player to choose their name.
 */
public class GuiPlayerNameView extends JPanel implements KeyListener {

    private static final Set<Character> ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".chars().mapToObj(x -> (char)x).collect(Collectors.toSet());

    private final char[] playerName = "\0\0\0\0\0".toCharArray();
    private final HashMap<String, JTextField> textFields = new HashMap<>();
    private final JButton ok;

    /**
     * Initializes the new player view.
     * @param actionListener Listener to handle events.
     */
    public GuiPlayerNameView(ActionListener actionListener) {
        super();

        // set layout
        this.setLayout(new GridBagLayout());
        this.setBackground(Color.BLACK);
        this.setForeground(Color.WHITE);

        // add items
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1.0;
        JLabel title = new JLabel("@newgame.title", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(36f));
        this.add(title, c);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setBackground(Color.BLACK);
        for (int i = 0; i < 5; i++) {
            JTextField text = new JTextField("", 1);
            text.setName(String.valueOf(i));
            textFields.put(text.getName(), text);

            UpdateListener ul = new UpdateListener(this, text);
            text.getDocument().addDocumentListener(ul);
            text.addKeyListener(this);

            text.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, Color.WHITE));
            text.setBackground(Color.BLACK);
            text.setForeground(Color.WHITE);
            text.setFont(text.getFont().deriveFont(32f));
            text.setHorizontalAlignment(JTextField.CENTER);

            inputPanel.add(text, c);

            if (i != 4)
                inputPanel.add(Box.createRigidArea(new Dimension(5, 0)));

            if (i == 0)
                text.grabFocus();
        }

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1.0;
        this.add(inputPanel, c);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.NORTH;
        this.ok = new JButton("@newgame.ok");
        this.ok.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.WHITE),
                BorderFactory.createMatteBorder(0, 24, 0, 24, Color.BLACK)));
        this.ok.setBackground(Color.BLACK);
        this.ok.setForeground(Color.WHITE);
        this.ok.setFont(this.ok.getFont().deriveFont(32f));
        this.ok.setActionCommand(GuiRootFrame.COMMAND_CONFIRM_PLAYERNAME);
        this.ok.addActionListener(actionListener);
        this.add(this.ok, c);
    }

    /**
     * Computes the so-far entered player name.
     * @return Player-selected player name.
     */
    public String getPlayerName() {
        if (this.playerName[0] == '\0')
            return null;

        int end = 0;
        for (; end < this.playerName.length; end++)
            if (this.playerName[end] == '\0')
                break;

        return new String(this.playerName, 0, end);
    }

    private void updateName(int pos, char character) {
        this.playerName[pos] = character;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) { }

    @Override
    public void keyPressed(KeyEvent keyEvent) { }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
            this.ok.doClick();

        if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            JTextField textField = (JTextField) keyEvent.getComponent();
            int pos = Integer.parseInt(textField.getName());

            if (pos > 0) {
                String name = String.valueOf(pos - 1);
                JTextField nf = this.textFields.get(name);
                nf.grabFocus();
            }
        }
    }

    private static class UpdateListener implements DocumentListener {

        private final GuiPlayerNameView view;
        private final JTextField textField;

        public UpdateListener(GuiPlayerNameView view, JTextField textField) {
            this.view = view;
            this.textField = textField;
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent){
            this.processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent){
            this.processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent){
            this.processUpdate();
        }

        private void processUpdate() {
            int pos = Integer.parseInt(this.textField.getName());
            String text = this.textField.getText();

            if (text == null || text.equals("")) {
                this.view.updateName(pos, '\0');

                return;
            }

            boolean pendingUpdate = false;
            if (text.length() > 1) {
                text = text.substring(0, 1);
                pendingUpdate = true;
            }

            char first = text.charAt(0);
            if (Character.isLowerCase(first)) {
                first = Character.toUpperCase(first);
                text = String.valueOf(first);
                pendingUpdate = true;
            }

            if (!ALPHABET.contains(first)) {
                text = "";
                pendingUpdate = true;
            }

            if (pos < 4 && text.length() > 0) {
                String name = String.valueOf(pos + 1);
                JTextField nf = this.view.textFields.get(name);
                nf.grabFocus();
            }

            this.view.updateName(pos, text.charAt(0));
            if (pendingUpdate)
                SwingUtilities.invokeLater(new TextUpdater(this.textField, text));
        }

        private static class TextUpdater implements Runnable {

            private final JTextField textField;
            private final String text;

            public TextUpdater(JTextField textField, String text) {
                this.textField = textField;
                this.text = text;
            }

            @Override
            public void run() {
                this.textField.setText(this.text);
            }
        }
    }
}
