package elkaproj.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * First screen, allows the player to choose their name.
 */
public class GuiPlayerNameView extends JPanel implements KeyListener {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Set<Character> ALPHABET_SET = ALPHABET.chars().mapToObj(x -> (char) x).collect(Collectors.toSet());
    private static final char[] ALPHABET_CHARS = ALPHABET.toCharArray();

    private final char[] playerName = "\0\0\0\0\0".toCharArray();
    private final HashMap<String, JTextField> textFields = new HashMap<>();
    private final JButton ok;
    private final boolean[] dontRefocus = new boolean[]{false, false, false, false, false};

    /**
     * Initializes the new player view.
     *
     * @param actionListener Listener to handle events.
     */
    public GuiPlayerNameView(ActionListener actionListener) {
        super();

        // set layout
        this.setFocusable(false);
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

            Action backspaceAction = text.getActionMap().get(DefaultEditorKit.deletePrevCharAction);
            text.getActionMap().put(DefaultEditorKit.deletePrevCharAction, new BackspaceActionWrapper(text, backspaceAction));

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
     *
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
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        int key = keyEvent.getKeyCode();
        JTextField textField = (JTextField) keyEvent.getComponent();

        switch (key) {
            case KeyEvent.VK_ENTER:
                this.ok.doClick();
                break;

            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_BACK_SPACE:
                if (key == KeyEvent.VK_BACK_SPACE && !textField.getText().equals(""))
                    break;

                int npos = Integer.parseInt(textField.getName());
                npos += (key == KeyEvent.VK_RIGHT ? 1 : -1);

                if (npos >= 0 && npos <= 4) {
                    String name = String.valueOf(npos);
                    JTextField nf = this.textFields.get(name);
                    nf.grabFocus();

                    if (key == KeyEvent.VK_BACK_SPACE)
                        nf.setText("");
                }

                break;

            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
                String sval = textField.getText();
                if (sval != null && sval.length() > 1)
                    break;

                char cval = sval != null && sval.length() == 1 ? sval.charAt(0) : '\0';
                int idx = charIndexOf(cval);

                if (key == KeyEvent.VK_UP)
                    idx++;
                else
                    idx--;

                if (idx >= ALPHABET_CHARS.length)
                    idx = 0;
                else if (idx < 0)
                    idx = ALPHABET_CHARS.length - 1;

                this.dontRefocus[Integer.parseInt(textField.getName())] = true;
                textField.setText(String.valueOf(ALPHABET_CHARS[idx]));
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
    }

    private static int charIndexOf(char item) {
        for (int i = 0; i < GuiPlayerNameView.ALPHABET_CHARS.length; i++)
            if (GuiPlayerNameView.ALPHABET_CHARS[i] == item)
                return i;

        return -1;
    }

    private static class UpdateListener implements DocumentListener {

        private final GuiPlayerNameView view;
        private final JTextField textField;
        private final int position;

        public UpdateListener(GuiPlayerNameView view, JTextField textField) {
            this.view = view;
            this.textField = textField;
            this.position = Integer.parseInt(this.textField.getName());
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            this.processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            this.processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            this.processUpdate();
        }

        private void processUpdate() {
            String text = this.textField.getText();

            if (text == null || text.equals("")) {
                this.view.updateName(this.position, '\0');

                if (this.position > 0) {
                    String name = String.valueOf(this.position - 1);
                    JTextField nf = this.view.textFields.get(name);

                    if (this.textField.hasFocus() && !this.view.dontRefocus[this.position])
                        nf.grabFocus();
                }

                return;
            }

            boolean pendingUpdate = false;
            if (text.length() > 1) {
                if (this.position < 4) {
                    String nextText = text.substring(1, 2);
                    JTextField nf = this.view.textFields.get(String.valueOf(this.position + 1));
                    nf.grabFocus();
                    nf.setText(nextText);
                }

                text = text.substring(0, 1);
                pendingUpdate = true;
            }

            char first = text.charAt(0);
            if (Character.isLowerCase(first)) {
                first = Character.toUpperCase(first);
                text = String.valueOf(first);
                pendingUpdate = true;
            }

            if (!ALPHABET_SET.contains(first)) {
                text = "";
                pendingUpdate = true;
            }

            if (this.position < 4 && text.length() > 0) {
                String name = String.valueOf(this.position + 1);
                JTextField nf = this.view.textFields.get(name);

                if (this.textField.hasFocus() && !this.view.dontRefocus[this.position])
                    nf.grabFocus();

                this.view.dontRefocus[this.position] = false;
            }

            this.view.updateName(this.position, text.charAt(0));
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

    private static class BackspaceActionWrapper implements Action {

        private final JTextField textField;
        private final Action proxiedAction;

        public BackspaceActionWrapper(JTextField textField, Action proxied) {
            this.textField = textField;
            this.proxiedAction = proxied;
        }

        @Override
        public Object getValue(String s) {
            return this.proxiedAction.getValue(s);
        }

        @Override
        public void putValue(String s, Object o) {
            this.proxiedAction.putValue(s, o);
        }

        @Override
        public void setEnabled(boolean b) {
            this.proxiedAction.setEnabled(b);
        }

        @Override
        public boolean isEnabled() {
            return this.proxiedAction.isEnabled();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
            this.proxiedAction.addPropertyChangeListener(propertyChangeListener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
            this.proxiedAction.removePropertyChangeListener(propertyChangeListener);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (!this.textField.getText().equals(""))
                this.proxiedAction.actionPerformed(actionEvent);
        }
    }
}
