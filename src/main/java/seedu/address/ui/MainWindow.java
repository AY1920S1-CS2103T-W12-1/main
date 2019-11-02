package seedu.address.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.claim.Claim;
import seedu.address.model.contact.Contact;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private boolean unknownEntry;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private ClaimListPanel claimListPanel;
    private IncomeListPanel incomeListPanel;

    private IndividualContactWindow individualContactWindow;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    public MainWindow(Stage primaryStage, Logic logic) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();

        unknownEntry = false;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillWithContacts() {
        personListPanel = new PersonListPanel(logic.getFilteredContactList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Fills up window with claims
     */
    void fillWithClaims() {
        claimListPanel = new ClaimListPanel(logic.getFilteredClaimList());
        personListPanelPlaceholder.getChildren().add(claimListPanel.getRoot());
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Fills up window with incomes
     */
    void fillWithIncomes() {
        incomeListPanel = new IncomeListPanel(logic.getFilteredIncomeList());
        personListPanelPlaceholder.getChildren().add(incomeListPanel.getRoot());
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Updates list panel with claims.
     */
    @FXML
    public void handleClaimsList() {
        claimListPanel = new ClaimListPanel(logic.getFilteredClaimList());
        personListPanelPlaceholder.getChildren().add(claimListPanel.getRoot());
        UiManager.changeState("claims");
        resultDisplay.setFeedbackToUser("All Claims Listed");
    }

    /**
     * Updates list panel with contacts.
     */
    @FXML
    public void handleContactsList() {
        personListPanel = new PersonListPanel(logic.getFilteredContactList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());
        UiManager.changeState("contacts");
        resultDisplay.setFeedbackToUser("All Contacts Listed");
    }

    /**
     * Updates list panel with incomes.
     */
    @FXML
    public void handleIncomesList() {
        incomeListPanel = new IncomeListPanel(logic.getFilteredIncomeList());
        personListPanelPlaceholder.getChildren().add(incomeListPanel.getRoot());
        UiManager.changeState("incomes");
        resultDisplay.setFeedbackToUser("All Incomes Listed");
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    /**
     * After unknown entry is handled, it becomes known.
     */
    private void handleUnknownEntry() {
        this.unknownEntry = !this.unknownEntry;
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException,
            IOException, URISyntaxException {
        try {
            if (unknownEntry) {
                CommandResult commandResult = logic.executeUnknownInput(commandText);
                if (!commandResult.isCreateShortCut()) {
                    handleUnknownEntry();
                }
                logger.info("Result: " + commandResult.getFeedbackToUser());
                resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
                return commandResult;
            } else {
                CommandResult commandResult = logic.execute(commandText);
                logger.info("Result: " + commandResult.getFeedbackToUser());
                resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

                if (commandResult.isShowHelp()) {
                    handleHelp();
                }

                if (commandResult.isExit()) {
                    handleExit();
                }

                if (commandResult.isClaim()) {
                    Claim claim = commandResult.giveClaim();
                    Model.handleClaim(claim);
                }

                if (commandResult.isContact()) {
                    Contact contact = commandResult.giveContact();
                    Model.handleContact(contact);
                }

                if (commandResult.isCreateShortCut()) {
                    handleUnknownEntry();
                }

                return commandResult;
            }
        } catch (CommandException | ParseException | IOException | URISyntaxException e) {
            logger.info("Invalid command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }

}
