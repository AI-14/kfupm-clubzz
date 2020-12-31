/**
 * Author: AMAAN IZHAR
 * Controller that handles all the functionality of updating the club admin to the database.
 * It edits and saves the data into the database.
 */

package controllers.sysadmin;

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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
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

public class UpdateClubAdminController implements Initializable {
    @FXML
    private AnchorPane rootPane;

    @FXML
    private JFXButton backButton, myAccountButton, signOutButton, editButton, saveButton;

    @FXML
    private JFXComboBox<String> clubComboBox, roleComboBox;

    @FXML
    private JFXTextField studentIdTextField;

    @FXML
    private JFXDatePicker fromDateDatePicker, toDateDatePicker;

    @FXML
    private Label msgLabel;

    public SysAdminMainController sysAdminMainController;
    public Connection conn;
    private int prevClubId, prevStudentId;
    private String prevFromDate;

    /**
     * Method that initializes the following:
     * 1. Opens mysql connection to server.
     * 2. Configures combo boxes in terms of their row visibilty.
     * 3. Populate the combo boxes with initial data.
     * 4. Displays message in case of connection failure.
     *
     * @param location  - The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources - The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            sysAdminMainController = new SysAdminMainController();
            configureComboBoxes();
            try {
                Task<Void> task = new Task<Void>() {
                        @Override
                        public Void call () {
                        DBConnection dbConnection = new DBConnection();
                        conn = dbConnection.getDBConnection();
                        if (conn != null) {
                            System.out.println("Connected");
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
                    setClubComboBoxValues();
                    setRoleComboBoxValues();
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
            }
            catch (RuntimeException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("RuntimeException");
                alert.setContentText("Something unexpected happened. Please close the application and try again.");
                alert.show();
            }
        } catch (NullPointerException e) {
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
     * Method that configures the combo boxes in terms of their row visibility.
     */
    public void configureComboBoxes() {
        clubComboBox.setVisibleRowCount(1000);
        roleComboBox.setVisibleRowCount(1000);
    }

    /**
     * Method that populates club names fetched from database into combo box.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    public void setClubComboBoxValues() {
        try {
            String sql = "SELECT NAME FROM CLUB;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> clubNames = new ArrayList<>();
            while (rs.next())
                clubNames.add(rs.getString(1));
            ObservableList<String> clubNamesData = FXCollections.observableArrayList(clubNames);
            clubComboBox.setItems(clubNamesData);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("SQLException");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    /**
     * Method that populates role names of the club admin.
     */
    public void setRoleComboBoxValues() {
        try {
            String sql = "SELECT DISTINCT(ROLE) FROM CLUBADMIN;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> roleNames = new ArrayList<>();
            while (rs.next())
                roleNames.add(rs.getString(1));
            ObservableList<String> roleNamesData = FXCollections.observableArrayList(roleNames);
            roleComboBox.setItems(roleNamesData);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("SQLException");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    /**
     * Method that enables to user to edit the input fields.
     * At first, user provides the club name and student ID. Then we fetch the data
     * corresponding to it and populate the fields so that user can edit.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    public void editButtonAction() {
        if (clubComboBox.getSelectionModel().isEmpty() || studentIdTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("PrimarySelection");
            alert.setContentText("Please select club and enter student ID to edit.");
            alert.show();
        } else {
            try {
                String getClubIdSql = "SELECT ID FROM CLUB WHERE NAME=?;";

                prevStudentId = Integer.parseInt(studentIdTextField.getText());
                prevClubId = 0;
                prevFromDate = "";
                String prevToDate = "";
                String prevRole = "";


                PreparedStatement pstmt;
                ResultSet rs;

                // Get the current club id and make it previous (in case user edits it).
                pstmt = conn.prepareStatement(getClubIdSql);
                pstmt.setString(1, clubComboBox.getValue());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    prevClubId = rs.getInt(1);
                }

                String getClubMemberInfoSql = "SELECT FROMDATE, TODATE, ROLE FROM CLUBADMIN WHERE CLUBID=? AND STUDENTID=?;";

                // Get previous fromdate, todate, and role.
                pstmt = conn.prepareStatement(getClubMemberInfoSql);
                pstmt.setInt(1, prevClubId);
                pstmt.setInt(2, prevStudentId);
                rs = pstmt.executeQuery();
                if(!rs.next()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("No such club/clubadmin present in the database.");
                    alert.show();
                }
                else {
                    do {
                        prevFromDate = rs.getString(1);
                        prevToDate = rs.getString(2);
                        prevRole = rs.getString(3);
                    } while(rs.next());
                    System.out.println(prevFromDate + " % " + prevToDate + " % " + prevRole);

                    // Populate input fields with previous values.
                    if (prevToDate == null) {
                        fromDateDatePicker.setValue(getDate(prevFromDate));
                        toDateDatePicker.setValue(null);
                        roleComboBox.setValue(prevRole);
                    } else {
                        fromDateDatePicker.setValue(getDate(prevFromDate));
                        toDateDatePicker.setValue(getDate(prevToDate));
                        roleComboBox.setValue(prevRole);
                    }
                }
           }
            catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("SQLException");
                alert.setContentText(e.getMessage());
                alert.show();
            } catch (NullPointerException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("NullPointerException");
                alert.setContentText("Do not leave any field empty!");
                alert.show();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Exception");
                alert.setContentText("Something went wrong. Try again!");
                alert.show();
            }
        }
    }

    /**
     * Method to return the correct date pattern.
     *
     * @param date - a date in the format 'yyyy-MM-dd'.
     * @return a LocalDate object after parsing to the given format.
     */
    public LocalDate getDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }

    /**
     * Method that saves the edited data in the database and sets the input fields to their initial view.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    public void saveButtonAction() {
        if (fieldsEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("FieldsEmpty");
            alert.setContentText("Do not leave any field empty!");
            alert.show();
        } else {
            try {
                int currentClubId = 0;
                int currentStudentId = Integer.parseInt(studentIdTextField.getText());
                String currentClubName = clubComboBox.getValue();
                String curentfromDate = fromDateDatePicker.getValue().toString();
                String currentToDate = toDateDatePicker.getValue().toString();
                String currentRole = roleComboBox.getValue();

                PreparedStatement pstmt;
                ResultSet rs;

                // Get current club Id.
                String getClubIdSql = "SELECT ID FROM CLUB WHERE NAME=?;";
                pstmt = conn.prepareStatement(getClubIdSql);
                pstmt.setString(1, currentClubName);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    currentClubId = rs.getInt(1);
                }

                String updateSql = "UPDATE CLUBADMIN SET CLUBID=?, STUDENTID=?, FROMDATE=?, TODATE=?, ROLE=? WHERE CLUBID=? AND STUDENTID=? AND FROMDATE=?;";

                // Prepare update sql statement and execute it.
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, currentClubId);
                pstmt.setInt(2, currentStudentId);
                pstmt.setString(3, curentfromDate);
                pstmt.setString(4, currentToDate);
                pstmt.setString(5, currentRole);
                pstmt.setInt(6, prevClubId);
                pstmt.setInt(7, prevStudentId);
                pstmt.setString(8, prevFromDate);

                int row = pstmt.executeUpdate();

                // Success message.
                msgLabel.setText("Successfully Saved!" + "    Rows affected: " + row);
                msgLabel.setTextFill(Color.GREEN);
                msgLabel.setAlignment(Pos.CENTER);

                clearAllFields();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("SQLException");
                alert.setContentText(e.getMessage());
                alert.show();
            } catch (NullPointerException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("NullPointerException");
                alert.setContentText("Do not leave any field empty!");
                alert.show();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Exception");
                alert.setContentText("Something went wrong. Try again!");
                alert.show();
            }
        }
    }

    /**
     * Method that checks if the input fields are empty.
     *
     * @return boolean value.
     */
    public boolean fieldsEmpty() {
        return clubComboBox.getSelectionModel().isEmpty() || studentIdTextField.getText().isEmpty() ||
                roleComboBox.getSelectionModel().isEmpty();
    }

    /**
     * Method that clears the input text fields.
     */
    public void clearAllFields() {
        studentIdTextField.clear();
        clubComboBox.getSelectionModel().clearSelection();
        roleComboBox.getSelectionModel().clearSelection();
        fromDateDatePicker.setValue(null);
        toDateDatePicker.setValue(null);
    }

    /**
     * Method that takes back the user to login view. It also closes the connection to the database.
     * It handles all the exceptions and displays appropriate messages to the user.
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
            alert.setContentText("Error in loading [LogIn] view.");
            alert.show();
        }
    }
}