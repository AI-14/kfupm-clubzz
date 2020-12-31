package controllers.clubmember;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import controllers.sysadmin.SysAdminMainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import models.db.DBConnection;
import models.loggedincache.GlobalUserName;
import sun.plugin.javascript.navig.Anchor;


import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClubMemberMainController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button LeaveClubBtn;

    @FXML
    private Button ViewProjectBtn;

    @FXML
    public ComboBox<String> ClubComboBox = new ComboBox<>();

    @FXML
    private Button MyAccountBtn;

    @FXML
    private Button SignOutBtn;

    @FXML
    private Label msgLabel;

    public Connection conn;
    ObservableList<String> deptNamesObservable;



    /** Initializes the window and sets up connection when
     *  the user login is detected to be of a clubmember. */

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
                            setClubComboBoxValues();

                        } else {
                            msgLabel.setText("ERROR: Database Connection");
                            msgLabel.setTextFill(Color.RED);
                            msgLabel.setAlignment(Pos.CENTER);
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

            } catch (RuntimeException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something unexpected happened. Please close the application and try again.");
                alert.show();
                }

            }  catch (NullPointerException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something unexpected happened. Please close the application and try again.");
            alert.show();

            }  catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Something unexpected happened. Please close the application and try again.");
            alert.show();
        }
    }

    /**Sets the values of the Club drop down list. Shows all clubs
    *  in which the user is still a part of or is still active. */

    public void setClubComboBoxValues() {
        try {
            String sql = "SELECT NAME FROM CLUB WHERE ID IN (" +
                         "SELECT CLUBID FROM CLUBMEMBER WHERE STUDENTID =" + GlobalUserName.globalId+ " AND STATUSID <> 1)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> deptNames = new ArrayList<>();
            while (rs.next())
                deptNames.add(rs.getString(1));
            deptNamesObservable = FXCollections.observableArrayList(deptNames);
            ClubComboBox.setItems(deptNamesObservable);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }

    }

    /** Gives a confirmation box upon clicking the LEAVE club button. If confirmed, the user will
     *  leave the selected club and that club will be deleted from their dropdown menu. */
    @FXML
    void LeaveClub() {
        GlobalUserName.clubMemberSelect = ClubComboBox.getSelectionModel().getSelectedItem();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("EXIT CLUB");
        alert.setContentText("Are you sure you want to leave the club?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            try{
                int clubID = 0;
                String sql1 = "SELECT ID FROM CLUB WHERE NAME = '"+ GlobalUserName.clubMemberSelect+"';";
                PreparedStatement pstmt1 = conn.prepareStatement(sql1);
                ResultSet rs = pstmt1.executeQuery();
                if(rs.next()) {
                    clubID += rs.getInt(1);
                }
                DBConnection dbConnection = new DBConnection();
                conn = dbConnection.getDBConnection();
                String sql = "UPDATE CLUBMEMBER SET STATUSID ="+1+" WHERE STUDENTID="+GlobalUserName.globalId+" AND CLUBID ="+clubID+";" ;;
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.executeUpdate();
                setClubComboBoxValues();

                if(deptNamesObservable.isEmpty()) {
                    String sql2 = "UPDATE CREDS SET ROLE = 4 WHERE USERNAME = ? ;";
                    String userName = GlobalUserName.globalId;

                    PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                    pstmt2.setString(1,userName);
                    pstmt2.executeUpdate();
                    Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                    alert2.setContentText("Since you are not a part of any club, you have become a guest and would" +
                                            "need to be approved by admin again. Signing out.");
                    alert2.show();
                    SignOut();
                }


            } catch (SQLException e) {
                Alert alert1 = new Alert(Alert.AlertType.ERROR);
                alert1.setContentText(e.getMessage());
                alert1.show();
            } catch (Exception e) {
                Alert alert1 = new Alert(Alert.AlertType.ERROR);
                alert1.setContentText("There was a problem in signing out.");
                alert1.show();
            }
        }
    }



    /** Loads the ProjectView Scene. */
    @FXML
    void viewProject() {
        GlobalUserName.clubMemberSelect = ClubComboBox.getSelectionModel().getSelectedItem();
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("../../views/clubmember/ProjectView.fxml"));
            rootPane.getChildren().setAll(pane);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in loading Project view.");
            alert.show();
        }
    }

    /** Managing Account details */
    @FXML
    void MyAccount() {
    }

    /**Signs out to Main Login screen. */
    @FXML
    void SignOut() throws Exception {
        conn.close();
        AnchorPane root = FXMLLoader.load(getClass().getResource("../../views/login/LoginMain.fxml"));
        SignOutBtn.getScene().setRoot(root);
    }
}
