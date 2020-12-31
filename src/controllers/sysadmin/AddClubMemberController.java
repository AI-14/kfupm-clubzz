/**
 * Author: AMAAN IZHAR
 * Controller that handles all the functionality of adding a club member to the database.
 * It adds, edits, and saves the data in the database.
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

public class AddClubMemberController implements Initializable {
    @FXML
    private AnchorPane rootPane;

    @FXML
    private JFXButton backButton, signOutButton, myAccountButton, addButton, editButton, saveButton;

    @FXML
    private JFXComboBox<String> clubComboBox,
            statusComboBox;
    ;

    @FXML
    private JFXTextField studentIdTextField;

    @FXML
    private JFXDatePicker fromDateDatePicker, toDateDatePicker;

    @FXML
    private Label msgLabel;

    public SysAdminMainController sysAdminMainController;
    public Connection conn;

    private int prevClubId;
    private int prevStudentId;
    String prevFromDate;

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
                    setClubComboBoxValues();
                    setStatusComboBoxValues();
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
        statusComboBox.setVisibleRowCount(1000);
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
     * Method that populates status description fetched from database into combo box.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    public void setStatusComboBoxValues() {
        try {
            String sql = "SELECT DESCR FROM STATUSES WHERE STATUSTYPEID=1;";
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
     * Method that gets the data from the input fields/options provided in the GUI and
     * adds the data into the database. It also sets the input fiels to their initial view.
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
                String clubName = clubComboBox.getValue();
                int studentID = Integer.parseInt(studentIdTextField.getText());
                String fromDate = fromDateDatePicker.getValue().toString();
                String toDate = toDateDatePicker.getValue().toString();
                String statusName = statusComboBox.getValue();

                int clubId = 0, statusId = 0;

                String insertSql = "INSERT INTO CLUBMEMBER VALUES(?,?,?,?,?);";
                String getClubIdSql = "SELECT ID FROM CLUB WHERE NAME=?;";
                String getStatusIdSql = "SELECT ID FROM STATUSES WHERE DESCR=? AND STATUSTYPEID=1;";

                PreparedStatement pstmt;
                ResultSet rs;

                // Get the club id.
                pstmt = conn.prepareStatement(getClubIdSql);
                pstmt.setString(1, clubName);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    clubId = rs.getInt(1);
                }

                // Get the status id.
                pstmt = conn.prepareStatement(getStatusIdSql);
                pstmt.setString(1, statusName);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    statusId = rs.getInt(1);
                }

                // Prepare the insert sql statement and execute it.
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setInt(1, clubId);
                pstmt.setInt(2, studentID);
                pstmt.setString(3, fromDate);
                pstmt.setString(4, toDate);
                pstmt.setInt(5, statusId);

                int row = pstmt.executeUpdate();

                String usernameUpdateSql = "UPDATE CREDS SET ROLE=3 WHERE USERNAME=?";
                pstmt = conn.prepareStatement(usernameUpdateSql);
                pstmt.setString(1, String.valueOf(studentID));
                pstmt.executeUpdate();

                // Success message.
                msgLabel.setText("Successfully Added!" + "    Rows affected: " + row);
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
     * Method that enables to user to edit the input fields.
     * At first, club name and student ID should be selected/typed and then we fetch
     * the data corresponding to it and populate the fields so that user can edit them.
     * It handles all the exceptions and displays appropriate messages to the user.
     */
    @FXML
    public void editButtonAction() {
        if (clubComboBox.getSelectionModel().isEmpty() || studentIdTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("PrimarySelection");
            alert.setContentText("Please select club name and type student ID to edit.");
            alert.show();
        } else {
            try {
                String getClubIdSql = "SELECT ID FROM CLUB WHERE NAME=?;";

                prevStudentId = Integer.parseInt(studentIdTextField.getText());
                prevClubId = 0;
                prevFromDate = "";
                String prevToDate = "";
                int prevStatusId = 0;
                String prevStatusName = "";

                PreparedStatement pstmt;
                ResultSet rs;

                // Get the current club id and make it previous club id (in case he changes).
                pstmt = conn.prepareStatement(getClubIdSql);
                pstmt.setString(1, clubComboBox.getValue());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    prevClubId = rs.getInt(1);
                }

                String getClubMemberInfoSql = "SELECT FROMDATE, TODATE, STATUSID FROM CLUBMEMBER WHERE CLUBID=? AND STUDENTID=?;";

                // Get the previous fromdate, todate, and statusid.
                pstmt = conn.prepareStatement(getClubMemberInfoSql);
                pstmt.setInt(1, prevClubId);
                pstmt.setInt(2, prevStudentId);
                rs = pstmt.executeQuery();
                if(!rs.next()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("No such clubmember present in the database.");
                    alert.show();
                }
                else {
                    do {
                        prevFromDate = rs.getString(1);
                        prevToDate = rs.getString(2);
                        prevStatusId = rs.getInt(3);
                    }while (rs.next());

                    // Get status description from its Id.
                    String getStatusNameSql = "SELECT DESCR FROM STATUSES WHERE ID=? AND STATUSTYPEID=1;";
                    pstmt = conn.prepareStatement(getStatusNameSql);
                    pstmt.setInt(1, prevStatusId);
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        prevStatusName = rs.getString(1);
                    }

                    // Populate input fields with previous values.
                    if (prevToDate == null) {
                        fromDateDatePicker.setValue(getDate(prevFromDate));
                        toDateDatePicker.setValue(null);
                        statusComboBox.setValue(prevStatusName);
                    } else {
                        fromDateDatePicker.setValue(getDate(prevFromDate));
                        toDateDatePicker.setValue(getDate(prevToDate));
                        statusComboBox.setValue(prevStatusName);
                    }
                }
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
                String currentStatusName = statusComboBox.getValue();
                int currentStatusId = 0;

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

                // Get current status Id.
                String getStatusIdSql = "SELECT ID FROM STATUSES WHERE DESCR=? AND STATUSTYPEID=1;";
                pstmt = conn.prepareStatement(getStatusIdSql);
                pstmt.setString(1, currentStatusName);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    currentStatusId = rs.getInt(1);
                }

                String updateSql = "UPDATE CLUBMEMBER SET CLUBID=?, STUDENTID=?, FROMDATE=?, TODATE=?, STATUSID=? WHERE CLUBID=? AND STUDENTID=? AND FROMDATE=?;";

                // Prepare update sql statement from previous club id and student id.
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, currentClubId);
                pstmt.setInt(2, currentStudentId);
                pstmt.setString(3, curentfromDate);
                pstmt.setString(4, currentToDate);
                pstmt.setInt(5, currentStatusId);
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
                statusComboBox.getSelectionModel().isEmpty();
    }

    /**
     * Method that clears the input text fields.
     */
    public void clearAllFields() {
        studentIdTextField.clear();
        clubComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
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