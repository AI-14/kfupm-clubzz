/**
 * Author: AMAAN IZHAR
 * Controller class that handles all the login functions.
 */

package controllers.login;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.loggedincache.GlobalUserName;
import models.PasswordEncryptDecrypt;
import models.db.DBConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginMainController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private JFXTextField userNameTextField;

    @FXML
    private JFXPasswordField passwordTextField;

    @FXML
    private JFXButton logInButton, guestButton;

    @FXML
    private ImageView kfupmLogoImageView;

    @FXML
    private Label titleLabel;

    public static final String globalUserName = "1111";
    public Connection conn;
    public PasswordEncryptDecrypt pEncDec;

    /**
     * Method that initializes the following:
     * 1. Aligns title to the center.
     * 2. Creating object of encryption class.
     * 3. Open connection to database.
     * It handles all the exceptions.
     *
     * @param location - The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources - The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleLabel.setAlignment(Pos.CENTER);
        pEncDec = new PasswordEncryptDecrypt();
        try {
            Task<Void> task = new Task<Void>() {
                @Override
                public Void call() {
                    DBConnection dbConnection = new DBConnection();
                    conn = dbConnection.getDBConnection();
                    if (conn == null) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Something unexpected happened. Please close the application and try again.");
                        alert.show();
                    }
                    return null;
                }
            };
            Thread thread = new Thread(task);
            thread.start();
            task.setOnSucceeded(e -> {
                System.out.println("Connected to AWS");
            });
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something unexpected happened. Please close the application and try again.");
            alert.show();
        }
    }

    /**
     * Method for handling login validation and closing the database connection.
     */
    @FXML
    void logInButtonAction() {
        String username = userNameTextField.getText();
        String password = passwordTextField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please enter username/password.");
            alert.show();
        } else if (username.length() > 10 || password.length() > 12) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Username should not exceed 10 characters.\nPassword should not exceed 12 characters");
            alert.show();
        } else {
            String encPassword = pEncDec.getEncryptedPassword(password); // Converting the original password into the encrypted one.
            String getUserInfo = "SELECT USERNAME, PWORD, ROLE FROM CREDS WHERE USERNAME=? AND PWORD=?;";
            PreparedStatement pstmt;
            ResultSet rs;

            // Access the username and password (encrytped) from the database.
            try {
                pstmt = conn.prepareStatement(getUserInfo);
                pstmt.setString(1, username);
                pstmt.setString(2, encPassword);
                rs = pstmt.executeQuery();
                if (!rs.next()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Username does not exists.");
                    alert.show();
                } else {
                    do {
                        // Based on the role integer type, render corresponding user view.
                        int role = rs.getInt(3);
                        switch (role) {
                            case 1:
                                loadUserScreen("../../views/sysadmin/SysAdminMain.fxml", "System Admin");
                                break;
                            case 2:
                                GlobalUserName.globalId = username;
                                loadUserScreen("../../views/clubadmin/ClubAdminMain.fxml", "Club Admin");
                                break;
                            case 3:
                                GlobalUserName.globalId = username;
                                loadUserScreen("../../views/clubmember/ClubMemberMain.fxml", "Club Member");

                                break;
                            case 4:
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Wait");
                                alert.setContentText("You are not currently a club member so you may only log in as guest.");
                                alert.show();;
                                break;
                        }
                    } while (rs.next());
                    //conn.close(); // Take care of this in specific situation in future.
                }
            } catch (NullPointerException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something unexpected happened. Please close the application and try again.");
                alert.show();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getMessage());
                alert.show();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something unexpected happened. Please close the application and try again.");
                alert.show();
            }
        }
    }

    /**
     * Method for displaying the user view and closing the login window.
     * @param userViewPath - the path that leads to a specific fxml view.
     * @param title - a string that sets up the title of the corresponding user on his stage.
     */
    public void loadUserScreen(String userViewPath, String title) {
        try {
            Stage currStage = (Stage) logInButton.getScene().getWindow();
            currStage.close();

            Parent root = FXMLLoader.load(getClass().getResource(userViewPath));
            Stage newStage = new Stage();
            Scene scene = new Scene(root, 700, 500);
            newStage.setTitle(title);
            newStage.setScene(scene);
            newStage.resizableProperty().setValue(Boolean.FALSE);
            newStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in loading user view.");
            alert.show();
        }
    }

    /**
     * Method for loading guest view (where guests can do their own functions).
     */
    @FXML
    void guestButtonAction() {
        try {
            Stage currStage = (Stage) guestButton.getScene().getWindow();
            currStage.close();

            Parent root = FXMLLoader.load(getClass().getResource("../../views/guest/GuestMain.fxml"));
            Stage newStage = new Stage();
            Scene scene = new Scene(root, 700, 500);
            newStage.setTitle("Guest");
            newStage.setScene(scene);
            newStage.resizableProperty().setValue(Boolean.FALSE);
            newStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in loading [Guest] view.");
            alert.show();
        }
    }
}