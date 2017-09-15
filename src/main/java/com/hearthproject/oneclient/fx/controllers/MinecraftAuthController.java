package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.nodes.ContentPaneButton;
import com.hearthproject.oneclient.hearth.api.HearthApi;
import com.hearthproject.oneclient.hearth.api.json.Role;
import com.hearthproject.oneclient.hearth.api.json.User;
import com.hearthproject.oneclient.util.MiscUtil;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.AuthStore;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.net.URL;
import java.util.Optional;

public class MinecraftAuthController {

	private static YggdrasilUserAuthentication authentication;
	private static HBox mainHbox;
	private static boolean isAtemptingLogin = false;
	public TextField username;
	public PasswordField password;
	public Stage stage;
	public Button buttonLogin;
	public CheckBox checkboxPasswordSave;

	public static void load() {
		authentication = (YggdrasilUserAuthentication) (new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1")).createUserAuthentication(Agent.MINECRAFT);
		Optional<AuthStore> authStore = getAuthStore();
		if (authStore.isPresent()) {
			isAtemptingLogin = true;
			updateGui();
			OneClientLogging.info("Logging in with saved details");
			if (authStore.get().authStorage != null) {
				authentication.loadFromStorage(authStore.get().authStorage);
			}
			try {
				doLogin(true);
			} catch (Exception e) {
				if(authentication.isLoggedIn() && !authentication.canPlayOnline()){
					OneClientLogging.error(e);

					Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
					alert.setTitle("Failed to re-authenticate");
					alert.setHeaderText("Failed to re-authenticate");
					alert.setContentText("Do you want to run in offline mode?");

					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == ButtonType.OK){
						OneClientLogging.info("Launching in offline mode");
					} else {
						doLogout();
					}
				} else {
					OneClientLogging.logUserError(e, "Failed to login, you will need to re-log in");
				}

				isAtemptingLogin = false;
				updateGui();
			}
		} else {
			isAtemptingLogin = false;
			updateGui();
		}
	}

	public static void openLoginGui() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL fxmlUrl = classLoader.getResource("gui/mc_auth.fxml");
			if (fxmlUrl == null) {
				OneClientLogging.logger.error("An error has occurred loading mc_auth.fxml!");
				return;
			}
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(fxmlUrl);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
			Parent root = fxmlLoader.load(fxmlUrl.openStream());
			Stage stage = new Stage();
			stage.setTitle("One Client - Login to minecraft");
			stage.getIcons().add(new Image("images/icon.png"));
			stage.setResizable(false);
			stage.initOwner(Main.stage);
			stage.initModality(Modality.WINDOW_MODAL);
			Scene scene = new Scene(root, 600, 300);
			scene.getStylesheets().add("gui/css/theme.css");
			stage.setScene(scene);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(Main.stage);
			stage.show();
			MinecraftAuthController controller = fxmlLoader.getController();
			controller.stage = stage;
			controller.buttonLogin.setDefaultButton(true);
			controller.showLoginGui();
		} catch (Exception e) {
			OneClientLogging.error(e);
		}
	}

	public static YggdrasilUserAuthentication getAuthentication() {
		return authentication;
	}

	private static void doLogin(boolean save) throws Exception {
		try {
			OneClientLogging.info("Logging into minecraft");
			authentication.logIn();
			if (authentication.isLoggedIn()) {
				OneClientLogging.info("Logged into minecraft successfully");
			}
		} catch (AuthenticationException e) {
			throw e;
		}
		if (HearthApi.enable) {
			OneClientLogging.info("Logging into hearth");
			HearthApi.getHearthAuthentication().login(authentication);
			save(save, authentication.getSelectedProfile().getName());
		}
		updateGui();
	}

	public static void doLogout(){
		if(authentication != null){
			authentication.logOut();
		}
		updateGui();
	}

	private static Optional<AuthStore> getAuthStore() {
		FileInputStream inputStream = null;
		ObjectInputStream objectInputStream = null;
		try {
			if (getAuthStoreFile().exists()) {
				inputStream = new FileInputStream(getAuthStoreFile());
				objectInputStream = new ObjectInputStream(inputStream);
				AuthStore authStore = (AuthStore) objectInputStream.readObject();
				objectInputStream.close();
				inputStream.close();
				return Optional.of(authStore);
			}
		} catch (Exception e) {
			//10/10 here
			try {
				if (objectInputStream != null) {
					objectInputStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e2) {
				OneClientLogging.error(e2);
			}
			//If invalid, delete it
			if (getAuthStoreFile().exists()) {
				getAuthStoreFile().delete();
			}
			OneClientLogging.error(e);
			Optional.empty();
		}
		return Optional.empty();
	}

	public static void save(boolean save, String username) {
		try {
			if (save) {
				FileOutputStream fileOutputStream = new FileOutputStream(getAuthStoreFile());
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				AuthStore authStore = new AuthStore();
				authStore.username = username;
				if (authentication != null) {
					authStore.authStorage = authentication.saveForStorage();
					authStore.playerName = authentication.getUserID();
				}
				objectOutputStream.writeObject(authStore);
				objectOutputStream.close();
				fileOutputStream.close();
			} else {
				if (getAuthStoreFile().exists()) {
					getAuthStoreFile().delete();
				}
			}
		} catch (Exception e) {
			OneClientLogging.error(e);
		}

	}

	public static void loadGuiElements(HBox hBox) {
		mainHbox = hBox;
		updateGui();
	}

	public static void updateGui() {
		MiscUtil.runLaterIfNeeded(() -> {
			mainHbox.getChildren().clear();
			mainHbox.setSpacing(3);
			if (authentication != null && authentication.canPlayOnline()) {
				ImageView imageView = new ImageView();
				imageView.setFitHeight(64);
				imageView.setFitWidth(64);
				try {
					imageView.setImage(new Image(new URL("https://crafatar.com/renders/head/" + authentication.getSelectedProfile().getId()).openStream()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				mainHbox.getChildren().add(imageView);
				Text usernameLabel = new Text();
				usernameLabel.setStyle("-fx-fill: #FFFFFF; -fx-font-family:  'Lato', sans-serif; -fx-font-size: 20;");
				usernameLabel.setText(authentication.getSelectedProfile().getName());
				mainHbox.setAlignment(Pos.CENTER);
				mainHbox.getChildren().add(usernameLabel);
				if(HearthApi.enable && HearthApi.getHearthAuthentication().getAuthentication() != null){
					try {
						User user = HearthApi.getHearthAuthentication().getUser();
						if(user.roles != null){
							for(Role role : user.roles){
								ImageView roleImage = new ImageView();
								roleImage.setFitHeight(16);
								roleImage.setFitWidth(32);
								mainHbox.getChildren().add(roleImage);
								try {
									roleImage.setImage(new Image(new URL(role.iconUrl).openStream()));
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					} catch (UnirestException e) {
						e.printStackTrace();
					}
				}
			} else if (authentication != null && authentication.isLoggedIn() && !authentication.canPlayOnline()) {
				Text textLabel = new Text();
				textLabel.setStyle("-fx-fill: #FFFFFF; -fx-font-family:  'Lato', sans-serif; -fx-font-size: 20;");
				textLabel.setText("Offline mode");
				mainHbox.setAlignment(Pos.CENTER);
				mainHbox.getChildren().add(textLabel);
			} else if (!isAtemptingLogin) {
				ContentPaneButton loginButton = new ContentPaneButton("", ButtonDisplay.TOP);
				loginButton.setText("Login");
				loginButton.prefWidthProperty().bind(Main.mainController.sideBox.widthProperty());
				loginButton.setOnAction(event -> {
					openLoginGui();
				});
				mainHbox.getChildren().add(loginButton);
			} else {
				Text textLabel = new Text();
				textLabel.setStyle("-fx-fill: #FFFFFF; -fx-font-family:  'Lato', sans-serif; -fx-font-size: 20;");
				textLabel.setText("Logging in...");
				mainHbox.setAlignment(Pos.CENTER);
				mainHbox.getChildren().add(textLabel);
			}
		});
	}

	public static boolean isUserValid() {
		return authentication.isLoggedIn();
	}

	public static boolean isUserOnline(){
		return authentication.canPlayOnline();
	}

	public static File getAuthStoreFile() {
		return new File(Constants.getRunDir(), "authstore.dat");
	}

	public void login(ActionEvent actionEvent) {
		stage.hide();
		try {
			isAtemptingLogin = true;
			updateGui();
			authentication.setUsername(username.getText());
			authentication.setPassword(password.getText());
			doLogin(checkboxPasswordSave.isSelected());
			save(checkboxPasswordSave.isSelected(), username.getText());
			stage.close();
		} catch (Exception e) {
			isAtemptingLogin = false;
			updateGui();
			OneClientLogging.logUserError(e, "Failed to log in");
		}
	}

	public void showLoginGui() {
		try {
			Optional<AuthStore> authStore = getAuthStore();
			if (authStore.isPresent()) {
				username.setText(authStore.get().username);
				checkboxPasswordSave.setSelected(true);
			} else {
				checkboxPasswordSave.setSelected(false);
			}
		} catch (Exception e) {
			OneClientLogging.error(e);
			checkboxPasswordSave.setSelected(false);
		}
		stage.show();
	}

	public void onLinkClick() {
		try {
			Desktop.getDesktop().browse(new URL("https://github.com/HearthProject/OneClient").toURI());
		} catch (Exception e) {
			OneClientLogging.error(e);
		}
	}

	public static void setAccessToken(YggdrasilUserAuthentication authentication, String newToken) throws NoSuchFieldException, IllegalAccessException {
		Field field = authentication.getClass().getDeclaredField("accessToken");
		field.setAccessible(true);
		field.set(authentication, newToken);
	}


}
