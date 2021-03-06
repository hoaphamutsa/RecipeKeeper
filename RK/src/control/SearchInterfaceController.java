package control;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Addresses;
import model.AlertBox;
import model.ConfirmBox;
import model.Constants;
import model.ReadData;
import model.Recipe;
import model.RecipeList;

/**
 * Controller for List View
 * this class can be initialized by main controller (WelcomeController) by clicking "Find a Recipe" button
 * or by ReadController by clicking "<" button
 * 
 * SearchInterfaceController will receive a ArrayList<Recipe> and all Recipes' name on a ListView.
 * When user checks out a Recipe, this array list is still saved in background in case the user wants
 * to go back to their search result again.
 * 
 * forward Button EventHandler (line 231 - 276) explains how "backward" and "open" are handled
 * 	(similar implementations)  
 * 
 * @author Loc Nguyen
 * @author Hoa Pham
 */
public class SearchInterfaceController implements Initializable {

	@FXML // fx:id="motherPane"
	private BorderPane motherPane;

	@FXML // fx:id="repList"
	private ListView<String> repList;

	@FXML // fx:id="open"
	private Button open;

	@FXML // fx:id="delRep"
	private Button delRep;

	@FXML // fx:id="backward"
	private Button backward;

	@FXML // fx:id="forward"
	private Button forward;

	// list of recipe names
	private ObservableList<String> recipeNames = FXCollections.observableArrayList();

	// Data passed from welcome screen
	private ArrayList<Recipe> readingData;

	// passing this recipe between views
	private Recipe selectedRecipe;
	
	// constant values
	private static Constants constants = new Constants();

	// minimum size of the window
	private static final int[] MIN_SIZES = constants.getMinSizes();

	/**
	 * user's accessing history
	 */
	private Addresses history = new Addresses();

	/**
	 * constructor
	 */
	public SearchInterfaceController() {
		super();
	}

	/**
	 * constructor
	 * @param result
	 */
	public SearchInterfaceController(ArrayList<Recipe> result) {
		initData(result);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		/**
		 * open a recipe in read mode
		 */
		open.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				int selectedIndex = repList.getSelectionModel().getSelectedIndex();
				if (selectedIndex >= 0) {
					try {
						// get directory from constant class
						String fxmlFileDir = constants.getReadDirectory();
						String cssFileDir = constants.getCssDirectory();
						FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileDir));
						Parent root = loader.load();
						URL location = new URL(loader.getLocation().toString());

						/**
						 * push directory of this view to backward stack 
						 * so the user can go back to their search results
						 * if they wish to
						 */
						history.getBackward().push(constants.getSearchDirectory());
						// controller setup
						ReadController controller = loader.getController();
						controller.initFlowingData(history, readingData.get(selectedIndex), readingData);
//						controller.setHistory(history);
//						controller.initData(readingData.get(selectedIndex));
						controller.initialize(location, loader.getResources());

						// set scene
						Scene editWindow = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
						editWindow.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
						
						// set stage
						Stage originalStage = (Stage) motherPane.getScene().getWindow();
						originalStage.setTitle(readingData.get(selectedIndex).getName() + " - View Mode");
						originalStage.setScene(editWindow);
						originalStage.show();

						// center the stage
						Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
						originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
						originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);

					} catch (IOException ioe) {
						AlertBox.display("Warning", "File not found.");
					}
				} else {
					AlertBox.display("Warning", "No item selected");
				}
			}
		});

		/**
		 * Delete a recipe from model FOREVER. 
		 * Restoring by reset the closest commits 
		 * (read the commits carefully before reseting)
		 * refresh the project every time this button is clicked
		 */
		delRep.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					if (recipeNames.size() < 1) throw new RuntimeException();
					int selectedIndex = repList.getSelectionModel().getSelectedIndex();
					if (selectedIndex >= 0) {
						boolean confirm = ConfirmBox.display("Notice", "Are you sure to delete this recipe?");
						if (confirm) {
							Recipe itemToBeRemoved = readingData.get(selectedIndex);
							RecipeList data = ReadData.readRecipes();
							data.rmvRecipe(itemToBeRemoved);
							repList.getItems().remove(selectedIndex);
						}
						else return;
					}
					else {
						AlertBox.display("Warning", "No Item Selected");
					}
				} catch (RuntimeException e) {
					AlertBox.display("Warning", "Recipe List is empty");
				}
			}
		});

		/**
		 * backward listener
		 * there is one case for back ward to handle: return to main view
		 */
		backward.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					if (history.getBackward().isEmpty()) {
						return;
					}
					else {
						String fxmlFileDir = history.getBackward().pop();
						String cssFileDir = constants.getCssDirectory();
						Parent root = FXMLLoader.load(getClass().getResource(fxmlFileDir));
						Scene homeWindow = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
						homeWindow.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
						Stage originalStage = (Stage) motherPane.getScene().getWindow();

						originalStage.setTitle("Recipe Keeper");
						originalStage.setScene(homeWindow);
						originalStage.show();

						// center the stage
						Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
						originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
						originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);
					}

				} catch (IOException ioe) {
					AlertBox.display("Warning", "Oops! Something wrong happened.");
				}
			}
		});

		// disable forward button if there is no addresses accessed
//		if (history.getForward().isEmpty()) {
//			forward.setDisable(true);
//		}

		/**
		 * forward event handler
		 * there is one case for forward to handle: list View -> read view
		 */
		forward.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					if (history.getForward().isEmpty()) return;
					else {
						// pop directory from forward stack
						String fxmlFileDir = history.getForward().pop();
						String cssFileDir = constants.getCssDirectory();
						// use FXMLLoader so the controller can be obtained later
						FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileDir));
						Parent root = loader.load();
						URL location = new URL(loader.getLocation().toString());

						// controller setup
						Object controller = loader.getController();
						if (controller instanceof ReadController) {
							history.getBackward().push(constants.getSearchDirectory());
							((ReadController) controller).initFlowingData(history, selectedRecipe, readingData);
							((ReadController) controller).initialize(location, loader.getResources());

							// set scene
							Scene recipeListView = new Scene(root, MIN_SIZES[0], MIN_SIZES[1]);
							recipeListView.getStylesheets().add(getClass().getResource(cssFileDir).toExternalForm());
							
							//set Stage
							Stage originalStage = (Stage) motherPane.getScene().getWindow();
							originalStage.setScene(recipeListView);
							originalStage.setTitle(selectedRecipe.getName() + " - View Mode");
							originalStage.show();

							// center the stage
							Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
							originalStage.setX((primScreenBounds.getWidth() - originalStage.getWidth()) / 2);
							originalStage.setY((primScreenBounds.getHeight() - originalStage.getHeight()) / 2);
						}
					}
				} 
				catch (IOException ioe) {
					AlertBox.display("Warning", "Oops! Something wrong happened.");
				}
				catch (Exception e) {
					AlertBox.display("Warning", "Oops! Something wrong happened.");
				}
			}	
		});

	}

	/**
	 * Initialize data
	 * @param result
	 */
	public void initData(ArrayList<Recipe> result) {
		for (Recipe r : result) {
			recipeNames.add(r.getName());
		}
		this.readingData = result;
		// for 
		repList.setItems(recipeNames);
		repList.setStyle("-fx-background-color: #FFFFFF; -fx-accent: #ff6c00; -fx-focus-color: #ff6c00;");
		motherPane.setStyle("-fx-background-color: #FFFFFF");
		open.setStyle("-fx-background-color: #ff9900");
	}

	/**
	 * Initialize flowing data
	 * @param history
	 * @param r
	 * @param repList
	 */
	public void initFlowingData(Addresses history, Recipe r, ArrayList<Recipe> repList) {
		this.history = history;
		this.selectedRecipe = r;
		
		for (Recipe rep : repList) {
			recipeNames.add(rep.getName());
		}
		this.readingData = repList;
		// for
		this.repList.setItems(recipeNames);
		this.repList.setStyle("-fx-background-color: #FFFFFF; -fx-accent: #ff6c00; -fx-focus-color: #ff6c00;");
		motherPane.setStyle("-fx-background-color: #FFFFFF");
		open.setStyle("-fx-background-color: #ff9900");
	}
	
	/**
	 * @return history of this scene
	 */
	public Addresses getHistory() {
		return history;
	}

	/**
	 * set history for this scene
	 * @param history
	 */
	public void setHistory(Addresses history) {
		this.history = history;
	}

	/**
	 * 
	 * @return recipe being selected
	 */
	public Recipe getSelectedRecipe() {
		return selectedRecipe;
	}

	/**
	 * set the previously selected recipe
	 * @param selectedRecipe
	 */
	public void setSelectedRecipe(Recipe selectedRecipe) {
		this.selectedRecipe = selectedRecipe;
	}
	
	/**
	 * @return list of recipes being read
	 */
	public ArrayList<Recipe> getReadingData() {
		return readingData;
	}
	
	/**
	 * set list of recipe being read 
	 * @param readingData
	 */
	public void setReadingData(ArrayList<Recipe> readingData) {
		this.readingData = readingData;
	}
}
