package elkaproj.ui;

import elkaproj.game.GameController;
import elkaproj.game.IGameLifecycleHandler;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * Root menu bar of the game. Provides key options for managing the gameplay.
 */
public class GuiMenuBar extends JMenuBar implements IGameLifecycleHandler {

    private final JMenuItem mGamePause;

    /**
     * Creates a new menu bar for the game.
     *
     * @param actionListener Listener handling this menu's action events.
     * @param gameController Game controller to handle certain events from.
     */
    public GuiMenuBar(ActionListener actionListener, GameController gameController) {
        super();

        // -- FILE
        JMenu mFile = new JMenu("@menu.file.label");

        JMenuItem mFileExit = new JMenuItem("@menu.file.items.exit");
        mFileExit.addActionListener(actionListener);
        mFileExit.setActionCommand(GuiRootFrame.COMMAND_EXIT);
        mFile.add(mFileExit);

        this.add(mFile);

        // -- GAME
        JMenu mGame = new JMenu("@menu.game.label");

        this.mGamePause = new JMenuItem("@menu.game.items.pause");
        this.mGamePause.addActionListener(actionListener);
        this.mGamePause.setActionCommand(GuiRootFrame.COMMAND_PAUSE_RESUME);
        mGame.add(this.mGamePause);

        JMenuItem mGameStop = new JMenuItem("@menu.game.items.stop");
        mGameStop.addActionListener(actionListener);
        mGameStop.setActionCommand(GuiRootFrame.COMMAND_STOP);
        mGame.add(mGameStop);

        JMenuItem mGameReset = new JMenuItem("@menu.game.items.reset");
        mGameReset.addActionListener(actionListener);
        mGameReset.setActionCommand(GuiRootFrame.COMMAND_RESET);
        mGame.add(mGameReset);

        JMenuItem mGameScoreboard = new JMenuItem("@menu.game.items.scoreboard");
        mGameScoreboard.addActionListener(actionListener);
        mGameScoreboard.setActionCommand(GuiRootFrame.COMMAND_SCOREBOARD);
        mGame.add(mGameScoreboard);

        JMenuItem mGameAuthors = new JMenuItem("@menu.game.items.authors");
        mGameAuthors.addActionListener(actionListener);
        mGameAuthors.setActionCommand(GuiRootFrame.COMMAND_AUTHORS);
        mGame.add(mGameAuthors);

        this.add(mGame);

        gameController.addLifecycleHandler(this);
    }

    @Override
    public void onGamePaused() {
        this.mGamePause.setText("@menu.game.items.resume");
    }

    @Override
    public void onGameResumed() {
        this.mGamePause.setText("@menu.game.items.pause");
    }
}
