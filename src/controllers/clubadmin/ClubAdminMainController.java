package controllers.clubadmin;

import com.jfoenix.controls.JFXButton;
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
import models.db.DBConnection;
import models.loggedincache.GlobalUserName;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ClubAdminMainController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Label clubTitleLabel;

    @FXML
    private JFXButton addEditProject;

    @FXML
    private JFXButton addMemberButton;

    @FXML
    private JFXButton computeStatButton;

    @FXML
    private JFXButton approveButton;

    @FXML
    private JFXButton signOutButton;

    public Connection conn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            try {
                Task<Void> task = new Task<Void>() {
                    @Override
                    public Void call() {
                        DBConnection dbConnection = new DBConnection();
                        conn = dbConnection.getDBConnection();
                        if (conn != null) {
                            System.out.println("Connected");
                        } else {
                            clubTitleLabel.setText("Connection Error: No Club found!");
                            clubTitleLabel.setTextFill(Color.RED);
                            clubTitleLabel.setAlignment(Pos.CENTER);
                        }
                        return null;
                    }
                };

                Thread thread = new Thread(task);
                thread.start();
                task.setOnSucceeded(e -> {
                    displayClubTitle();
                });
            }
            catch (RuntimeException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something unexpected happened. Please close the application and try again.");
                alert.show();
            }
        } catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something unexpected happened. Please close the application and try again.");
            alert.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something unexpected happened. Please close the application and try again.");
            alert.show();
        }
    }

    public void displayClubTitle() {
        String clubNameSql = "SELECT NAME FROM CLUB WHERE ID = ?";
        String clubName = "";
        PreparedStatement pstmt;
        ResultSet rs;

        try {
            pstmt = conn.prepareStatement(clubNameSql);
            int clubid = Integer.parseInt(GlobalUserName.globalId);
            pstmt.setInt(1,clubid);
            rs = pstmt.executeQuery();
            while(rs.next()) {
                clubName = rs.getString("name");
            }
            clubTitleLabel.setText(clubName);
            //clubTitleLabel.setTextFill(Color.BLACK);
            clubTitleLabel.setAlignment(Pos.CENTER);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void addEditProjectAction() throws Exception {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("../../views/clubadmin/AddProject.fxml"));
            rootPane.getChildren().setAll(pane);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            System.out.println(e.toString());
            alert.setContentText("Error in loading [AddProject] view.");
            alert.show();
        }
    }

    @FXML
    void addMemberButtonAction() {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("../../views/clubadmin/AddMember.fxml"));
            rootPane.getChildren().setAll(pane);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in loading [AddMember] view.");
            alert.show();
        }
    }

    @FXML
    void approveButtonAction() {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("../../views/clubadmin/ApproveMembers.fxml"));
            rootPane.getChildren().setAll(pane);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in loading [MembersPerClub] view.");
            alert.show();
        }

    }

    @FXML
    void computeStatButtonAction() {

        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("../../views/clubadmin/ComputeStats.fxml"));
            rootPane.getChildren().setAll(pane);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in loading [MembersPerClub] view.");
            alert.show();
        }

    }

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
