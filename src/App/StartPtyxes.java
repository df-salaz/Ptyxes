package App;

import java.sql.SQLException;

import javafx.application.Application;
import javafx.stage.Stage;

public class StartPtyxes extends Application {
	private static final DatabaseHelper databaseHelper = new DatabaseHelper();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			databaseHelper.connectToDatabase(); // Connect to the database
			new FirstPage(databaseHelper).show(primaryStage);;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}
