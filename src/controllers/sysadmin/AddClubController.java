/**
 * Author: AMAAN IZHAR
 * Controller that handles all the functionality of adding a club to the database.
 * Adds, edits and saves the data in the database.
 */

package controllers.sysadmin;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
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

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.db.DBConnection;

public class AddClubController implements Initializable {
    @FXML
    private AnchorPane rootPane;

    @FXML
    private JFXButton backButton, myAccountButton, signOutButton, addButton, editButton, saveButton;

    @FXML
    private JFXTextField clubNameTextfield, clubAddressTextField, clubPhoneTextField;

    @FXML
    private JFXTextArea clubDescrTextArea;

    @FXML
    private JFXComboBox<String> deptComboBox,
    statusComboBox,
    editClubComboBox;

    @FXML
    private Label msgLabel;

    public SysAdminMainController sysAdminMainController;
    public Connection conn;

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
                    public Void call() {
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
                    setDeptComboBoxValues();
                    setStatusComboBoxValues();
                    setEditClubComboBoxValues();
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
        deptComboBox.setVisibleRowCount(1000);
        statusComboBox.setVisibleRowCount(1000);
        editClubComboBox.setVisibleRowCount(1000);
    }

    /**
     * Method that populates department names fetched from database into combo box.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    public void setDeptComboBoxValues() {
        try {
            String sql = "SELECT NAME FROM DEPARTMENT;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> deptNames = new ArrayList<>();
            while (rs.next())
                deptNames.add(rs.getString(1));
            ObservableList<String> deptNamesObservable = FXCollections.observableArrayList(deptNames);
            deptComboBox.setItems(deptNamesObservable);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("SQLException");
            alert.setContentText(e.getMessage());
            alert.show();
        }

    }

    /**
     * Method that populates status description fetched from database into combo box.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    public void setStatusComboBoxValues() {
        try {
            String sql = "SELECT DESCR FROM STATUSES WHERE STATUSTYPEID=2;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> statusNames = new ArrayList<>();
            while (rs.next())
                statusNames.add(rs.getString(1));
            ObservableList<String> statusNamesObservable = FXCollections.observableArrayList(statusNames);
            statusComboBox.setItems(statusNamesObservable);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("SQLException");
            alert.setContentText(e.getMessage());
            alert.show();
        }

    }

    /**
     * Method that populates club names fetched from database into combo box.
     * It is used when user is in edit mode.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    public void setEditClubComboBoxValues() {
        try {
            String sql = "SELECT NAME FROM CLUB;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> clubNames = new ArrayList<>();
            while (rs.next())
                clubNames.add(rs.getString(1));
            ObservableList<String> clubNamesData = FXCollections.observableArrayList(clubNames);
            editClubComboBox.setItems(clubNamesData);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("SQLException");
            alert.setContentText(e.getMessage());
            alert.show();
        }

    }

    /**
     * Method that gets the data from the input fields/options provided in the GUI and
     * adds the data into the database. It also sets the input fields to their initial view.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    void addButtonAction() {
        if (fieldsEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("FieldsEmpty");
            alert.setContentText("Do not leave any field empty!");
            alert.show();
        } else {
            try {
                String insertSql = "INSERT INTO CLUB VALUES(?, ?, ?, ?, ?, ?, ?);";
                String getMaxIdSql = "SELECT MAX(ID) FROM CLUB;";
                String getDeptIdSql = "SELECT ID FROM DEPARTMENT WHERE NAME=?;";
                String getStatusIdSql = "SELECT ID FROM STATUSES WHERE DESCR=? AND STATUSTYPEID=2;";

                int clubId = 0;
                String clubName = clubNameTextfield.getText(),
                        clubAddr = clubAddressTextField.getText(),
                        clubPhone = clubPhoneTextField.getText(),
                        clubDescr = clubDescrTextArea.getText();
                int deptID = 0, statusID = 0;


                PreparedStatement pstmt;
                ResultSet rs;

                // Get the maximum club id (or last id) and add 1 for the insertion of new club id.
                pstmt = conn.prepareStatement(getMaxIdSql);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    clubId = rs.getInt(1) + 1;
                }

                // Get department id.
                pstmt = conn.prepareStatement(getDeptIdSql);
                pstmt.setString(1, deptComboBox.getValue());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    deptID = rs.getInt(1);
                }

                // Get status id.
                pstmt = conn.prepareStatement(getStatusIdSql);
                pstmt.setString(1, statusComboBox.getValue());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    statusID = rs.getInt(1);
                }

                // Prepare insert sql statement and execute it.
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setInt(1, clubId);
                pstmt.setString(2, clubName);
                pstmt.setString(3, clubAddr);
                pstmt.setString(4, clubPhone);
                pstmt.setString(5, clubDescr);
                pstmt.setInt(6, deptID);
                pstmt.setInt(7, statusID);

                int row = pstmt.executeUpdate();

                // Success message.
                msgLabel.setText("Successfully Added!" + "    Rows affected: " + row);
                msgLabel.setTextFill(Color.GREEN);
                msgLabel.setAlignment(Pos.CENTER);

                // Update club combo box and clear the fields.
                setEditClubComboBoxValues();
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
     * Method that enables to user to edit the input fields. It fetches the name of the club from the database
     * and populates all the fields with its corresponding data so that user can edit it.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    public void editButtonAction() {
        if (editClubComboBox.getValue() == null) {
            msgLabel.setText("Error: No club is selected!");
            msgLabel.setTextFill(Color.RED);
        } else {
            try {
                int deptID = 0, statusID = 0;

                PreparedStatement pstmt;
                ResultSet rs;

                String getClubInfoSql = "SELECT * FROM CLUB WHERE NAME=?;";

                // Get the club info and populate the fields with it.
                pstmt = conn.prepareStatement(getClubInfoSql);
                pstmt.setString(1, editClubComboBox.getValue());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    clubNameTextfield.setText(rs.getString(2));
                    clubAddressTextField.setText(rs.getString(3));
                    clubPhoneTextField.setText(rs.getString(4));
                    clubDescrTextArea.setText(rs.getString(5));
                    deptID = rs.getInt(6);
                    statusID = rs.getInt(7);
                }

                String getDeptName = "SELECT NAME FROM DEPARTMENT WHERE ID=?;";
                String getStatusName = "SELECT DESCR FROM STATUSES WHERE ID=? AND STATUSTYPEID=2;";

                // Get the department name and populate the combobox.
                pstmt = conn.prepareStatement(getDeptName);
                pstmt.setInt(1, deptID);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    deptComboBox.setValue(rs.getString(1));
                }

                // Get the status description and populate the combobox.
                pstmt = conn.prepareStatement(getStatusName);
                pstmt.setInt(1, statusID);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    statusComboBox.setValue(rs.getString(1));
                }
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("SQLException");
                alert.setContentText(e.getMessage());
                alert.show();
            }
            catch (NullPointerException e) {
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
                String updateSql = "UPDATE CLUB SET NAME=?, ADDRESS=?, PHONE=?, DESCR=?, DEPARTMENTID=?, STATUSID=? WHERE NAME=?";
                String getDeptIdSql = "SELECT ID FROM DEPARTMENT WHERE NAME=?;";
                String getStatusIdSql = "SELECT ID FROM STATUSES WHERE DESCR=? AND STATUSTYPEID=2;";

                int deptID = 0, statusID = 0;
                String clubName = clubNameTextfield.getText(),
                        clubAddr = clubAddressTextField.getText(),
                        clubPhone = clubPhoneTextField.getText(),
                        clubDescr = clubDescrTextArea.getText();


                PreparedStatement pstmt;
                ResultSet rs;

                // Get the department id.
                pstmt = conn.prepareStatement(getDeptIdSql);
                pstmt.setString(1, deptComboBox.getValue());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    deptID = rs.getInt(1);
                }

                // Get the status id.
                pstmt = conn.prepareStatement(getStatusIdSql);
                pstmt.setString(1, statusComboBox.getValue());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    statusID = rs.getInt(1);
                }

                // Prepare the update sql statement and execute it.
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setString(1, clubName);
                pstmt.setString(2, clubAddr);
                pstmt.setString(3, clubPhone);
                pstmt.setString(4, clubDescr);
                pstmt.setInt(5, deptID);
                pstmt.setInt(6, statusID);
                pstmt.setString(7, editClubComboBox.getValue());

                int row = pstmt.executeUpdate();

                // Success message.
                msgLabel.setText("Successfully Saved!" + "    Rows affected: " + row);
                msgLabel.setTextFill(Color.GREEN);
                msgLabel.setAlignment(Pos.CENTER);

                // Update the club combobox and clear the fields.
                setEditClubComboBoxValues();
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
        return clubNameTextfield.getText().isEmpty() ||
                clubAddressTextField.getText().isEmpty() ||
                clubPhoneTextField.getText().isEmpty() ||
                deptComboBox.getSelectionModel().isEmpty() ||
                statusComboBox.getSelectionModel().isEmpty();
    }

    /**
     * Method that clears the input text fields.
     */
    public void clearAllFields() {
        clubNameTextfield.clear();
        clubAddressTextField.clear();
        clubPhoneTextField.clear();
        clubDescrTextArea.clear();
        editClubComboBox.getSelectionModel().clearSelection();
        deptComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
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