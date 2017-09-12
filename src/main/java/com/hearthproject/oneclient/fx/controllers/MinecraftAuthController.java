package com.hearthproject.oneclient.fx.controllers;

import com.hearthproject.oneclient.Constants;
import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.hearth.HearthApi;
import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.util.logging.OneClientLogging;
import com.hearthproject.oneclient.util.minecraft.AuthStore;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.util.Optional;

public class MinecraftAuthController {

	public TextField username;
	public PasswordField password;

	public Stage stage;
	public Button buttonLogin;
	public CheckBox checkboxPasswordSave;

	private static YggdrasilUserAuthentication authentication;
	public static boolean isOffline;

	public static void load(){
		authentication = (YggdrasilUserAuthentication) (new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1")).createUserAuthentication(Agent.MINECRAFT);
		Optional<AuthStore> authStore = getAuthStore();
		if(authStore.isPresent()){
			OneClientLogging.info("Logging in with saved details");
			authentication.setUsername(authStore.get().username);
			authentication.setPassword(authStore.get().password);
			try {
				doLogin();
			} catch (Exception e) {
				//TODO ask if the user wants to go offline, and handle that somehow
				OneClientLogging.logUserError(e, "Failed to login, please login again");
				openLoginGui();
			}
		} else {
			openLoginGui();
		}
	}

	public static void openLoginGui(){
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

	public void login(ActionEvent actionEvent) {
		stage.hide();
		try {
			authentication.setUsername(username.getText());
			authentication.setPassword(password.getText());
			save(checkboxPasswordSave.isSelected(), username.getText(), password.getText());
			doLogin();
			stage.close();
		} catch (Exception e) {
			OneClientLogging.logUserError(e, "Failed to log in");
		}
	}

	private static void doLogin() throws Exception {
		try {
			OneClientLogging.info("Logging into minecraft");
			authentication.logIn();
			if(authentication.isLoggedIn()){
				OneClientLogging.info("Logged into minecraft successfully");
			}
		} catch (AuthenticationException e) {
			throw e;
		}
		if(HearthApi.enable) {
			OneClientLogging.info("Logging into hearth");
			HearthApi.login(authentication);
			HearthApi.getClientPermissions();
		}
	}

	public void showLoginGui() {
		try {
			Optional<AuthStore> authStore = getAuthStore();
			if(authStore.isPresent()){
				username.setText(authStore.get().username);
				password.setText(authStore.get().password);
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

	private static Optional<AuthStore> getAuthStore(){
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
		} catch (Exception e){
			//10/10 here
			try {
				if(objectInputStream != null){
					objectInputStream.close();
				}
				if(inputStream != null){
					inputStream.close();
				}
			} catch (Exception e2){
				OneClientLogging.error(e2);
			}
			//If invalid, delete it
			if(getAuthStoreFile().exists()){
				getAuthStoreFile().delete();
			}
			OneClientLogging.error(e);
			Optional.empty();
		}
		return Optional.empty();
	}

	public static void save(boolean save, String username, String password) {
		try {
			if (save) {
				FileOutputStream fileOutputStream = new FileOutputStream(getAuthStoreFile());
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				AuthStore authStore = new AuthStore();
				authStore.username = username;
				authStore.password = password;
				if(authentication != null && authentication.isLoggedIn()){
					authStore.accessToken = authentication.getAuthenticatedToken();
					authStore.clientToken = authentication.getAuthenticationService().getClientToken();
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

	public void onLinkClick() {
		try {
			Desktop.getDesktop().browse(new URL("https://github.com/HearthProject/OneClient").toURI());
		} catch (Exception e) {
			OneClientLogging.error(e);
		}
	}

	public static File getAuthStoreFile() {
		return new File(Constants.getRunDir(), "authstore.dat");
	}

}
