package controllers.clubadmin;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import models.db.DBConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import models.loggedincache.GlobalUserName;

public class AddMemberController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private JFXComboBox<String> studentIDCombobox = new JFXComboBox<>();

    @FXML
    private JFXComboBox<String> projectNameCombobox = new JFXComboBox<>();

    @FXML
    private JFXDatePicker fromDate;

    @FXML
    private JFXDatePicker toDate;

    @FXML
    private JFXTextField roleTextField;

    @FXML
    private JFXButton addButton;

    @FXML
    private JFXButton backButton;

    @FXML
    private Label msgLabel;



    public ClubAdminMainController ClubAdminMainController;
    public Connection conn;


    @FXML
    void addButtonAction() {

        if (fieldsEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Do not leave any field empty!");
            alert.show();
        } else {
            try {
                int projectId = 0;
                int clubId = Integer.parseInt(GlobalUserName.globalId);
                String insertSql = "INSERT INTO WORKSON VALUES(?, ?, ?, ?, ?);";
                String projectIdSql = "SELECT ID FROM PROJECT WHERE NAME = ?;";
                String fromDateP = fromDate.getValue().toString();
                String toDateP = toDate.getValue().toString();

                String projectName = projectNameCombobox.getValue();
                String studentId = studentIDCombobox.getValue();
                String projectRole = roleTextField.getText();


                PreparedStatement pstmt;
                ResultSet rs;


                // Get project id.
                pstmt = conn.prepareStatement(projectIdSql);
                pstmt.setString(1, projectName);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    projectId = rs.getInt(1);
                }

                // Prepare insert sql statement and execute it.
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, studentId);
                pstmt.setInt(2, projectId);
                pstmt.setString(3, fromDateP);
                pstmt.setString(4, toDateP);
                pstmt.setString(5, projectRole);

                int row = pstmt.executeUpdate();

                // Success message.
//                msgLabel.setText("Successfully Added!" + "    Rows affected: " + row);
//                msgLabel.setTextFill(Color.GREEN);
//                msgLabel.setAlignment(Pos.CENTER);
//
                // Update club combo box and clear the fields.
                setProjectComboBoxValues();
                setMemberNameComboBoxValues();
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

    public LocalDate getDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }

    public void clearAllFields() {
        roleTextField.clear();
        projectNameCombobox.getSelectionModel().clearSelection();
        studentIDCombobox.getSelectionModel().clearSelection();
        fromDate.setValue(null);
        toDate.setValue(null);
    }

    private boolean fieldsEmpty() {
        return roleTextField.getText().isEmpty() ||
                projectNameCombobox.getSelectionModel().isEmpty() ||
                studentIDCombobox.getSelectionModel().isEmpty();
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
//                            setProjectComboBoxValues();
//                            setMemberNameComboBoxValues();

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
                    setProjectComboBoxValues();
                    setMemberNameComboBoxValues();
                    msgLabel.setText("Database Connection Successful.");
                    msgLabel.setTextFill(Color.GREEN);
                    msgLabel.setAlignment(Pos.CENTER);
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
        projectNameCombobox.setVisibleRowCount(1000);
        studentIDCombobox.setVisibleRowCount(1000);
    }

    private void setProjectComboBoxValues() {
        try {
            String sql = "SELECT NAME FROM PROJECT WHERE CLUBID = ? AND STATUSID = 11;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int clubID = Integer.parseInt(GlobalUserName.globalId);
            pstmt.setInt(1, clubID);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> projectNames = new ArrayList<>();
            while (rs.next())
                projectNames.add(rs.getString("name"));
            ObservableList<String> projectNamesObservable = FXCollections.observableArrayList(projectNames);
            projectNameCombobox.setItems(projectNamesObservable);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void setMemberNameComboBoxValues() {
        try {
            String sql = "SELECT STUDENTID FROM CLUBMEMBER WHERE CLUBID = ? AND STATUSID = 2;";
            int clubID = Integer.parseInt(GlobalUserName.globalId);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, clubID);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> memberIDs = new ArrayList<>();
            while (rs.next())
                memberIDs.add(String.valueOf(rs.getInt("studentid")));
            ObservableList<String> memberIDsObservable = FXCollections.observableArrayList(memberIDs);
            memberIDsObservable.setAll(memberIDs);
            studentIDCombobox.setItems(memberIDsObservable);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }
}
