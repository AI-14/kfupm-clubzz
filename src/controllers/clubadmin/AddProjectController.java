package controllers.clubadmin;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import models.db.DBConnection;
import models.loggedincache.GlobalUserName;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AddProjectController implements Initializable {

    @FXML
    private JFXTextField projectNameTextField;

    @FXML
    private JFXComboBox<String> projectNameCombobox = new JFXComboBox<>();

    @FXML
    private JFXComboBox<String> leaderNameCombobox = new JFXComboBox<>();

    @FXML
    private JFXTextArea decrTextArea;

    @FXML
    private JFXDatePicker fromDate;

    @FXML
    private JFXDatePicker toDate;

    @FXML
    private JFXComboBox<String> statusCombobox = new JFXComboBox<>();

    @FXML
    private JFXComboBox<String> editProjectTypeCombobox = new JFXComboBox<>();

    @FXML
    private JFXButton addButton;

    @FXML
    private JFXButton editButton;

    @FXML
    private JFXButton saveButton;

    @FXML
    private JFXButton backButton;

    @FXML
    private JFXButton signoutButton;

    @FXML
    private Label msgLabel;

    public ClubAdminMainController ClubAdminMainController;
    public Connection conn;
    String prevToDate;
    String prevFromDate;

    int clubID = Integer.parseInt(GlobalUserName.globalId);

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
//                            setProjectTypeComboBoxValues();
//                            setStatusComboBoxValues();
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
                    setProjectTypeComboBoxValues();
                    setStatusComboBoxValues();
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

    private void setStatusComboBoxValues() {
        try {
            String sql = "SELECT DESCR FROM STATUSES WHERE STATUSTYPEID = 5;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> statusNames = new ArrayList<>();
            while (rs.next()) {
                statusNames.add(rs.getString("descr"));
            }
            ObservableList<String> statusNamesObservable = FXCollections.observableArrayList(statusNames);
            statusCombobox.setItems(statusNamesObservable);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void setProjectTypeComboBoxValues() {
        try {
            String sql = "SELECT NAME FROM PROJECTTYPE;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> projectTypes = new ArrayList<>();
            while (rs.next())
                projectTypes.add(rs.getString("name"));
            ObservableList<String> projectTypesObservable = FXCollections.observableArrayList(projectTypes);
            editProjectTypeCombobox.setItems(projectTypesObservable);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void setProjectComboBoxValues() {
        try {
            String sql = "SELECT NAME FROM PROJECT WHERE CLUBID = ?;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
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
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, clubID);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<String> memberIDs = new ArrayList<>();
            while (rs.next())
                memberIDs.add(String.valueOf(rs.getInt("studentid")));
            ObservableList<String> memberIDsObservable = FXCollections.observableArrayList(memberIDs);
            memberIDsObservable.setAll(memberIDs);
            leaderNameCombobox.setItems(memberIDsObservable);
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private void configureComboBoxes() {
        projectNameCombobox.setVisibleRowCount(1000);
        statusCombobox.setVisibleRowCount(1000);
        editProjectTypeCombobox.setVisibleRowCount(1000);
        leaderNameCombobox.setVisibleRowCount(1000);
    }

    @FXML
    void addButtonAction() {
        if (fieldsEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Do not leave any field empty!");
            alert.show();
        } else {
            try {
                int projectId = 0;
                String insertSql = "INSERT INTO PROJECT VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";
                String getMaxIdSql = "SELECT MAX(ID) FROM PROJECT;";
                String getProjectId = "SELECT ID FROM PROJECTTYPE WHERE NAME=?;";
                String getStatusIdSql = "SELECT ID FROM STATUSES WHERE DESCR=?;";
                String fromDateP = fromDate.getValue().toString();
                String toDateP = toDate.getValue().toString();

                String projectName = projectNameTextField.getText();
                String projectDesc = decrTextArea.getText();
                String projectType = editProjectTypeCombobox.getValue();
                String projectStatus = statusCombobox.getValue();
                String leaderId = leaderNameCombobox.getValue();

                int projectTypeId = 0;
                int projectStatusId = 0;

                PreparedStatement pstmt;
                ResultSet rs;

                // Get the maximum project id (or last id) and add 1 for the insertion of new project id.
                pstmt = conn.prepareStatement(getMaxIdSql);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    projectId = rs.getInt(1) + 1;
                }

                // Get project type id.
                pstmt = conn.prepareStatement(getProjectId);
                pstmt.setString(1, projectType);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                     projectTypeId = rs.getInt(1);
                }

                // Get status id.
                pstmt = conn.prepareStatement(getStatusIdSql);
                pstmt.setString(1, projectStatus);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                      projectStatusId = rs.getInt(1);
                }

                // Prepare insert sql statement and execute it.
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setInt(1, projectId);
                pstmt.setString(2, projectName);
                pstmt.setInt(3, projectTypeId);
                pstmt.setInt(4, clubID);
                pstmt.setString(5, projectDesc);
                pstmt.setString(6, fromDateP);
                pstmt.setString(7, toDateP);
                pstmt.setInt(8, projectStatusId);
                pstmt.setInt(9, Integer.parseInt(leaderId));

                int row = pstmt.executeUpdate();

                // Success message.
//                msgLabel.setText("Successfully Added!" + "    Rows affected: " + row);
//                msgLabel.setTextFill(Color.GREEN);
//                msgLabel.setAlignment(Pos.CENTER);
//
                // Update club combo box and clear the fields.
                setProjectComboBoxValues();
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
        projectNameTextField.clear();
        decrTextArea.clear();
        editProjectTypeCombobox.getSelectionModel().clearSelection();
        statusCombobox.getSelectionModel().clearSelection();
        projectNameCombobox.getSelectionModel().clearSelection();
        leaderNameCombobox.getSelectionModel().clearSelection();
        fromDate.setValue(null);
        toDate.setValue(null);
    }

    private boolean fieldsEmpty() {
        return projectNameTextField.getText().isEmpty() ||
                leaderNameCombobox.getSelectionModel().isEmpty() ||
                statusCombobox.getSelectionModel().isEmpty() ||
                editProjectTypeCombobox.getSelectionModel().isEmpty();
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
    void editButtonAction() {
        if (projectNameCombobox.getValue() == null) {
            System.out.println("No project is selected.");
            //msgLabel.setText("Error: No project is selected!");
            //msgLabel.setTextFill(Color.RED);
        } else {
            try {
                int projectTypeId = 0, projectStatusID = 0;

                PreparedStatement pstmt;
                ResultSet rs;

                String getProjectInfoSql = "SELECT * FROM PROJECT WHERE NAME=?;";

                // Get the club info and populate the fields with it.
                pstmt = conn.prepareStatement(getProjectInfoSql);
                pstmt.setString(1, projectNameCombobox.getValue());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    projectNameTextField.setText(rs.getString(2));
                    projectTypeId = rs.getInt(3);
                    decrTextArea.setText(rs.getString(5));
                    prevFromDate = rs.getString(6);
                    prevToDate = rs.getString(7);
                    projectStatusID = rs.getInt(8);
                    leaderNameCombobox.setValue(rs.getString(9));
                }

                String getProjectType = "SELECT NAME FROM PROJECTTYPE WHERE ID=?;";
                String getStatusType = "SELECT DESCR FROM STATUSES WHERE ID=?;";

                // Get the project name and populate the combobox.
                pstmt = conn.prepareStatement(getProjectType);
                pstmt.setInt(1, projectTypeId);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    editProjectTypeCombobox.setValue(rs.getString(1));
                }

                // Get the status type and populate the combobox.
                pstmt = conn.prepareStatement(getStatusType);
                pstmt.setInt(1, projectStatusID);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    statusCombobox.setValue(rs.getString(1));
                }

                fromDate.setValue(getDate(prevFromDate));
                toDate.setValue(getDate(prevToDate));
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getMessage());
                alert.show();
            }
        }
    }

    @FXML
    void saveButtonAction() {
        if (fieldsEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Do not leave any field empty!");
            alert.show();
        } else {
            try {
                String updateSql = "UPDATE PROJECT SET NAME=?, PROJECTTYPEID=?, DESCR=?, STARTDATE=?, ENDDATE=?, STATUSID = ?, LEADERID = ? WHERE NAME=?";
                String getProjectId = "SELECT ID FROM PROJECTTYPE WHERE NAME=?;";
                String getStatusIdSql = "SELECT ID FROM STATUSES WHERE DESCR=?;";
                String fromDateP = fromDate.getValue().toString();
                String toDateP = toDate.getValue().toString();


                String projectName = projectNameTextField.getText();
                String projectDesc = decrTextArea.getText();
                String projectType = editProjectTypeCombobox.getValue();
                String projectStatus = statusCombobox.getValue();
                String leaderId = leaderNameCombobox.getValue();

                int projectTypeId = 0;
                int projectStatusId = 0;


                PreparedStatement pstmt;
                ResultSet rs;


                // Get project type id.
                pstmt = conn.prepareStatement(getProjectId);
                pstmt.setString(1, projectType);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    projectTypeId = rs.getInt(1);
                }

                // Get status id.
                pstmt = conn.prepareStatement(getStatusIdSql);
                pstmt.setString(1, projectStatus);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    projectStatusId = rs.getInt(1);
                }

                // Prepare insert sql statement and execute it.
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setString(1, projectName);
                pstmt.setInt(2, projectTypeId);
                pstmt.setString(3, projectDesc);
                pstmt.setString(4, fromDateP);
                pstmt.setString(5, toDateP);
                pstmt.setInt(6, projectStatusId);
                pstmt.setInt(7, Integer.parseInt(leaderId));
                pstmt.setString(8, projectName);

                int row = pstmt.executeUpdate();

                // Success message.
//                msgLabel.setText("Successfully Added!" + "    Rows affected: " + row);
//                msgLabel.setTextFill(Color.GREEN);
//                msgLabel.setAlignment(Pos.CENTER);
//
                // Update club combo box and clear the fields.
                setProjectComboBoxValues();
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
}
