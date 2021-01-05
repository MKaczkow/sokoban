package elkaproj.ui;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * Root menu bar of the game. Provides key options for managing the gameplay.
 */
public class GameMenuBar extends JMenuBar {

    /**
     * Creates a new menu bar for the game.
     */
    public GameMenuBar(ActionListener actionListener) {
        super();

        // -- FILE
        JMenu mFile = new JMenu("@menu.file.label");

        JMenuItem mFileExit = new JMenuItem("@menu.file.items.exit");
        mFileExit.addActionListener(actionListener);
        mFileExit.setActionCommand(GameFrame.COMMAND_EXIT);
        mFile.add(mFileExit);

        this.add(mFile);

        // -- GAME
        JMenu mGame = new JMenu("@menu.game.label");

        JMenuItem mGamePause = new JMenuItem("@menu.game.items.pause");
        mGamePause.addActionListener(actionListener);
        mGamePause.setActionCommand(GameFrame.COMMAND_PAUSE_RESUME);
        mGame.add(mGamePause);

        JMenuItem mGameReset = new JMenuItem("@menu.game.items.reset");
        mGameReset.addActionListener(actionListener);
        mGameReset.setActionCommand(GameFrame.COMMAND_RESET);
        mGame.add(mGameReset);

        JMenuItem mGameScoreboard = new JMenuItem("@menu.game.items.scoreboard");
        mGameScoreboard.addActionListener(actionListener);
        mGameScoreboard.setActionCommand(GameFrame.COMMAND_SCOREBOARD);
        mGame.add(mGameScoreboard);

        JMenuItem mGameAuthors = new JMenuItem("@menu.game.items.authors");
        mGameAuthors.addActionListener(actionListener);
        mGameAuthors.setActionCommand(GameFrame.COMMAND_AUTHORS);
        mGame.add(mGameAuthors);

        this.add(mGame);
    }
}
