/**
 * Author: AMAAN IZHAR
 * Controller that displays the main screen of the System admin and handles all the meta-functionalities of the
 * admin.
 */

package controllers.sysadmin;


import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import java.net.URL;
import java.util.ResourceBundle;

public class SysAdminMainController implements Initializable {
    @FXML
    public AnchorPane rootPane;

    @FXML
    private JFXButton myAccountButton, signOutButton, addClubButton, addClubMemberButton,
            updateClubAdminButton, computeStatsButton;

    @FXML
    private ImageView sysAdminImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    /**
     * Method to display view that adds a club.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    void addClubButtonAction() {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("../../views/sysadmin/AddClub.fxml"));
            rootPane.getChildren().setAll(pane);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setContentText("Error in loading [AddClub] view.");
            alert.show();
        }
    }

    /**
     * Method to display the view that adds club members.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    void addClubMemberButtonAction() {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("../../views/sysadmin/AddClubMember.fxml"));
            rootPane.getChildren().setAll(root);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setContentText("Error in loading [AddClubMember] view.");
            alert.show();
        }

    }

    /**
     * Method to display the view that updates club admins.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    void updateClubAdminButtonAction() {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("../../views/sysadmin/UpdateClubAdmin.fxml"));
            rootPane.getChildren().setAll(root);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setContentText("Error in loading [UpdateClubAdmin] view.");
            alert.show();
        }
    }

    /**
     * Method to display the view that displays club's statistics.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    void computeStatsButtonAction() {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("../../views/sysadmin/ComputeStats.fxml"));
            rootPane.getChildren().setAll(root);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setContentText("Error in loading [MembersPerClub] view.");
            alert.show();
        }
    }

    /**
     * Method that displays the account view to the user.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    void myAccountButtonAction() {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("../../views/sysadmin/MyAccount.fxml"));
            rootPane.getChildren().setAll(root);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setContentText("Error in loading [MyAccount] view.");
            alert.show();
        }
    }

    /**
     * Method to take the user back to login page.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    void signOutButtonAction() {
        try {
            Stage currStage = (Stage) signOutButton.getScene().getWindow();
            currStage.close();

            Parent root = FXMLLoader.load(getClass().getResource("../../views/login/LoginMain.fxml"));
            Stage newStage = new Stage();
            Scene scene = new Scene(root, 700, 500);
            newStage.setTitle("System Admin");
            newStage.setScene(scene);
            newStage.resizableProperty().setValue(Boolean.FALSE);
            newStage.show();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setContentText("Error in loading [Login] view.");
            alert.show();
        }
    }
}