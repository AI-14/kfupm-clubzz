package controllers.clubadmin;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import models.db.DBConnection;
import models.loggedincache.GlobalUserName;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ApproveMembersController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private JFXButton backButton;


    @FXML
    private JFXComboBox<String> idCombobox = new JFXComboBox<>();

    @FXML
    private JFXButton approveButton;

    @FXML
    private JFXButton rejectButton;

    @FXML
    private JFXButton approveAllButton;
    public ClubAdminMainController ClubAdminMainController;
    public Connection conn;

    @FXML
    void approveAllButtonAction() {
            try {
                String updateSql = "UPDATE CLUBMEMBER SET STATUSID=? WHERE STATUSID = 3 AND CLUBID = ?";

                PreparedStatement pstmt;
                ResultSet rs = null;

                ArrayList<String> memberIds = new ArrayList<>();
                memberIds = setMemberIdComboBoxValues();

                for(String id : memberIds){
                    updateUsernameRole(Integer.parseInt(id));
                }

                // Prepare update sql statement and execute it.
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, 2);
                pstmt.setInt(2,Integer.parseInt(GlobalUserName.globalId));

                int row = pstmt.executeUpdate();

//                 Success message.
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Rows: " + row + " affected succesfully!");
                // Update club combo box and clear the fields.
                setMemberIdComboBoxValues();
                clearAllFields();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getMessage());
                alert.show();
            } catch (NullPointerException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Do not leave any field empty!");
                alert.show();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something went wrong. Try again!");
                alert.show();
            }
        }



    private void clearAllFields() {
        idCombobox.getSelectionModel().clearSelection();
    }

    private boolean fieldsEmpty() {
        return idCombobox.getSelectionModel().isEmpty();
    }

    @FXML
    void approveButtonAction() {
        if (fieldsEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a student ID.");
            alert.show();
        } else {
            try {
                String updateSql = "UPDATE CLUBMEMBER SET STATUSID = 2 WHERE CLUBID = ? AND STUDENTID = ?";

                PreparedStatement pstmt;
                ResultSet rs;

                int studentId = Integer.parseInt(idCombobox.getValue());


                // Prepare update sql statement and execute it.
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, Integer.parseInt(GlobalUserName.globalId));
                pstmt.setInt(2, studentId);

                int row = pstmt.executeUpdate();

                // Success message.
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Rows: " + row + " affected succesfully!");
//
                updateUsernameRole(studentId);
                // Update club combo box and clear the fields.

                setMemberIdComboBoxValues();
                clearAllFields();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getMessage());
                alert.show();
            } catch (NullPointerException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Do not leave any field empty!");
                alert.show();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something went wrong. Try again!");
                alert.show();
            }

        }
    }

    public void updateUsernameRole(int studentID){
        String updateSql = "UPDATE CREDS SET ROLE=3 WHERE USERNAME=?";
        try {
            PreparedStatement pstmt;
            String username = String.valueOf(studentID); //Converting studentID to string.
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setString(1, username);
            int row = pstmt.executeUpdate();
            System.out.println(row);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    void backButtonAction() {
        try {
            conn.close();
            Parent root = FXMLLoader.load(getClass().getResource("../../views/clubadmin/ClubAdminMain.fxml"));
            backButton.getScene().setRoot(root);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in loading [ClubAdminMain] view.");
            alert.show();
        }

    }

    @FXML
    void rejectButtonAction() {
        if (fieldsEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a student ID.");
            alert.show();
        } else {
            try {
                String updateSql = "UPDATE CLUBMEMBER SET STATUSID = 1 WHERE CLUBID = ? AND STUDENTID = ?";

                PreparedStatement pstmt;
                ResultSet rs;

                int studentId = Integer.parseInt(idCombobox.getValue());

                // Prepare update sql statement and execute it.
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, Integer.parseInt(GlobalUserName.globalId));
                pstmt.setInt(2,studentId);

                int row = pstmt.executeUpdate();

                // Success message.
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Rows: " + row + " affected succesfully!");
//
                // Update club combo box and clear the fields.
                setMemberIdComboBoxValues();
                clearAllFields();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getMessage());
                alert.show();
            } catch (NullPointerException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Do not leave any field empty!");
                alert.show();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Something went wrong. Try again!");
                alert.show();
            }

        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ClubAdminMainController = new ClubAdminMainController();

            configureComboBoxes();
            try {
                Task<Void> task = new Task<Void>() {
                    @Override
                    public Void call() {
                        DBConnection dbConnection = new DBConnection();
                        conn = dbConnection.getDBConnection();
                        if (conn != null) {
//                            setMemberIdComboBoxValues();
                        } else {
                            //msgLabel.setText("ERROR: Database Connection");
                            //msgLabel.setTextFill(Color.RED);
                            //msgLabel.setAlignment(Pos.CENTER);
                        }
                        return null;
                    }
                };

                Thread thread = new Thread(task);
                thread.start();
                task.setOnSucceeded(e -> {
                    setMemberIdComboBoxValues();
                    //msgLabel.setText("Database Connection Successful.");
                    //msgLabel.setTextFill(Color.GREEN);
                    //msgLabel.setAlignment(Pos.CENTER);
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

    private void configureComboBoxes() {
       idCombobox.setVisibleRowCount(1000);
    }

    private ArrayList<String> setMemberIdComboBoxValues() {
        ArrayList<String> memberIDs = new ArrayList<>();
        try {
            String sql = "SELECT STUDENTID FROM CLUBMEMBER WHERE STATUSID = 3 AND CLUBID = ?;";
            int clubID = Integer.parseInt(GlobalUserName.globalId);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, clubID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
                memberIDs.add(String.valueOf(rs.getInt(1)));
            ObservableList<String> memberIDsObservable = FXCollections.observableArrayList(memberIDs);
            memberIDsObservable.setAll(memberIDs);
            idCombobox.setItems(memberIDsObservable);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
        return  memberIDs;
    }
}
