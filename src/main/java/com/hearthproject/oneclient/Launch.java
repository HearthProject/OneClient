package com.hearthproject.oneclient;

import com.hearthproject.oneclient.util.logging.OneClientLogging;

import javax.swing.*;
import java.io.IOException;

public class Launch {
	public static void main(String[] args) throws IOException {
		try {
			Class.forName("javafx.stage.Stage");
		} catch (ClassNotFoundException e) {
			OneClientLogging.error(e);
			JOptionPane.showMessageDialog(null,
				"JavaFX wasn't found installed on this computer, this is usually because you are using OpenJDK that does not include it, to fix this error install openjfx for your distribution."
					+ "\nOn debian based distributions this can fixed by running"
					+ "\n   sudo apt-get install openjfx"
					+ "\n   or by installing oracle java"
					+ "\nIf you are not using OpenJDK something has gone badly wrong, please contact the OneClient Developers",
				"A cataclysmic error has occurred!",
				JOptionPane.ERROR_MESSAGE);
		}
		Main.main(args);
	}

}
