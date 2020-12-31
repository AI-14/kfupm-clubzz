/**
 * Author: AMAAN IZHAR
 * Controller class to handle account settings. It provides a way to change password.
 */

package controllers.sysadmin;

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
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.PasswordEncryptDecrypt;
import models.db.DBConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MyAccountController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Label titleLabel, msgLabel;

    @FXML
    private JFXTextField userNameTextField;

    @FXML
    private JFXPasswordField currentPasswordTextField, newPasswordTextField, confirmNewPasswordTextField;

    @FXML
    private JFXButton updateButton, backButton, signOutButton;

    public SysAdminMainController sysAdminMainController;
    public Connection conn;
    public PasswordEncryptDecrypt pEncDec;

    /**
     * Method that initializes the following:
     * 1. Opens mysql connection to server.
     * 2. Initializes some objects.
     * 3. Displays message in case of connection failure.
     *
     * @param location  - The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources - The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sysAdminMainController = new SysAdminMainController();
        pEncDec = new PasswordEncryptDecrypt();
        titleLabel.setAlignment(Pos.CENTER);
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
                msgLabel.setText("Database Connection Successful.");
                msgLabel.setTextFill(Color.GREEN);
                msgLabel.setAlignment(Pos.CENTER);
            });
        }
        catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("NullPointerException");
            alert.setContentText("Something unexpected happened. Please close the application and try again.");
            alert.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setContentText("Something unexpected happened. Please close the application and try again.");
            alert.show();
        }
    }

    /**
     * Method for updating user's password.
     */
    @FXML
    void updateButtonAction() {
        // If user exists and passwords dont exceed length and passwords are equal.
        if(validateUsername() && !exceedsLength() && passwordsEqual()) {
            String encPassword = pEncDec.getEncryptedPassword(newPasswordTextField.getText()); // Encrypt the new password.
            String updateSql = "UPDATE CREDS SET PWORD=? WHERE USERNAME=?";

            PreparedStatement pstmt;

            try {
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setString(1, encPassword);
                pstmt.setString(2, userNameTextField.getText());
                int row = pstmt.executeUpdate();
                if(row == 1) { // Only intended row affected.
                    msgLabel.setText("Password changed successfully.");
                    msgLabel.setTextFill(Color.GREEN);
                    msgLabel.setAlignment(Pos.CENTER);
                }
                else {
                    msgLabel.setText("Error! Please try again.");
                    msgLabel.setTextFill(Color.RED);
                    msgLabel.setAlignment(Pos.CENTER);
                }
                clearFields();
            }
            catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("SQLException");
                alert.setContentText(e.getMessage());
                alert.show();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("CredentialsError");
            alert.setContentText("The following problems might have occured:\n1. Username does not exist.\n2. Password length > 12 characters.\n3. Passwords don't match");
            alert.show();
        }
    }

    /**
     * Method for validating the username.
     * @return boolean - whether the username is validated or not.
     */
    public boolean validateUsername() {
        try {
            String getUsernameSql = "SELECT USERNAME FROM CREDS WHERE USERNAME=?;";
            String username;
            PreparedStatement pstmt;
            ResultSet rs;

            pstmt = conn.prepareStatement(getUsernameSql);
            pstmt.setString(1, userNameTextField.getText());
            rs = pstmt.executeQuery();
            if(!rs.next()) {
                return false; // Username not found.
            }
            else {
                do {
                    username = rs.getString(1);
                }while(rs.next());
                return username.equals(userNameTextField.getText());
            }
        }
        catch (SQLException e) {
            return false;
        }
    }

    /**
     * Validate if password exceeds the length.
     * @return boolean value.
     */
    public boolean exceedsLength() {
        return newPasswordTextField.getText().length() > 12 || confirmNewPasswordTextField.getText().length() > 12;
    }

    /**
     * Validate if new password and confirm password are equal.
     * @return boolean value.
     */
    public boolean passwordsEqual() {
        return newPasswordTextField.getText().equals(confirmNewPasswordTextField.getText());
    }

    // Clearing all fields.
    public void clearFields() {
        userNameTextField.clear();
        currentPasswordTextField.clear();
        newPasswordTextField.clear();
        confirmNewPasswordTextField.clear();
    }

    /**
     * Method to display main sysadmin view.
     */
    @FXML
    void backButtonAction() {
        try {
            conn.close();
            AnchorPane root = FXMLLoader.load(getClass().getResource("../../views/sysadmin/SysAdminMain.fxml"));
            rootPane.getChildren().setAll(root);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setContentText("Error in loading [SysAdmin] view.");
            alert.show();
        }
    }
}