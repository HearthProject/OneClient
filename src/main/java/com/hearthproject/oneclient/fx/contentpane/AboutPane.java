package com.hearthproject.oneclient.fx.contentpane;

import com.hearthproject.oneclient.Main;
import com.hearthproject.oneclient.fx.contentpane.base.ButtonDisplay;
import com.hearthproject.oneclient.fx.contentpane.base.ContentPane;
import com.hearthproject.oneclient.util.OperatingSystem;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Calendar;

public class AboutPane extends ContentPane {
	@FXML
	private VBox credits;
	@FXML
	private VBox stuffBox;

	public AboutPane() {
		super("gui/contentpanes/about.fxml", "About", "about.png", ButtonDisplay.BELOW_DIVIDER);
	}

	@Override
	public void onStart() {
		stuffBox.prefWidthProperty().bind(Main.mainController.contentBox.widthProperty());
		stuffBox.prefHeightProperty().bind(Main.mainController.contentBox.heightProperty());
		if (credits.getChildren().isEmpty()) {
			String yearString = "2017";
			int year = Calendar.getInstance().get(Calendar.YEAR);
			if (year > 2017) {
				yearString += "-" + year;
			}
			Text text = new Text("©" + yearString + " - HEARTH PROJECT");
			text.setStyle("-fx-fill: #FFFFFF; -fx-font-family:  'Lato', sans-serif; -fx-font-size: 12;");
			Hyperlink hyperlink = new Hyperlink("http://hearthproject.uk/");
			hyperlink.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family:  'Lato', sans-serif; -fx-font-size: 12;");
			hyperlink.setOnAction(e -> openHearthSite());
			hyperlink.focusTraversableProperty().setValue(false);
			Text creditsText = new Text("\nCredits:");
			creditsText.setStyle("-fx-fill: #FFFFFF; -fx-font-family:  'Lato', sans-serif; -fx-font-size: 14; -fx-font-weight: bold");
			credits.getChildren().addAll(text, hyperlink, creditsText);
			addCredit("modmuss50 - Lead Developer", "https://twitter.com/modmuss50");
			addCredit("Prospector - UX Manager", "https://twitter.com/ProfProspector");
			addCredit("primetoxinz - Various Contributions", "https://github.com/primetoxinz");
			addCredit("loading.io - Splash Loading .gif", "https://loading.io/");
			addCredit("Yannick - Home, About, Download, Instances, and Update icons", "https://www.flaticon.com/authors/yannick");
			addCredit("Gregor Cresnar - Package icon", "https://www.flaticon.com/authors/gregor-cresnar");
			addCredit("Egor Rumyantsev - Settings icon", "https://www.flaticon.com/authors/egor-rumyantsev");
			addCredit("Icomoon - Log Out icon", "https://www.flaticon.com/authors/icomoon");
		}
	}

	public void addCredit(String credit, String url) {
		Hyperlink hyperlink = new Hyperlink(credit);
		hyperlink.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family:  'Lato', sans-serif; -fx-font-size: 12;");
		hyperlink.setOnAction((actionEvent) -> OperatingSystem.browseURI(url));
		hyperlink.focusTraversableProperty().setValue(false);
		credits.getChildren().add(hyperlink);
	}

	public void openHearthSite() {
		OperatingSystem.browseURI("http://hearthproject.uk/");
	}

	@Override
	public void refresh() {

	}
}
