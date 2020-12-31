package controllers.guest;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPasswordField;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import models.PasswordEncryptDecrypt;
import models.clubmember.ProjNameDescr;
import models.db.DBConnection;
import models.guest.ClubNameDescr;
import models.loggedincache.GlobalUserName;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GuestMainController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TableView<ClubNameDescr> clubTable;
    @FXML
    private TableColumn<ClubNameDescr, String> clubColumn;

    @FXML
    private TableColumn<ClubNameDescr, String> descrColumn;

    @FXML
    private JFXButton registerClubButton;

    @FXML
    private JFXButton joinClubButton;

    @FXML
    private JFXDatePicker fromDate;

    @FXML
    private JFXDatePicker toDate;

    @FXML
    private JFXTextField usernameTextField;

    @FXML
    private JFXPasswordField passwordTextField;

    @FXML
    private JFXPasswordField confirmPasswordTextField;

    @FXML
    private JFXButton backButton;

    public Connection conn;
    public PasswordEncryptDecrypt pEncDec;
    String username, password, confPassword;

    /** Initializes the GuestMain Scene. A table of clubs to join is shown.
     *  If the guest wants to register, they need to choose 1 club.*/
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pEncDec = new PasswordEncryptDecrypt();
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() {
                DBConnection dbConnection = new DBConnection();
                conn = dbConnection.getDBConnection();
                if (conn != null) {
                    System.out.println("Connected guest");
                    setClubTable();

                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.start();


    }

    @FXML
    void backButtonAction() {
        try {
            username = null;
            password = null;
            confPassword = null;
            Parent root = FXMLLoader.load(getClass().getResource("../../views/login/LoginMain.fxml"));
            backButton.getScene().setRoot(root);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error in loading [LogIn] view.");
            alert.show();
        }
    }


/** This method checks for various conditions regarding validity of the entries.
 *  Only guests that are existing students can register. After registration, the credentials
 *  are added into the database, club request is sent, and the user is sent back to
 *  the Login screen.*/

    @FXML
    void registerClubButtonAction() {

            ClubNameDescr val = clubTable.getSelectionModel().getSelectedItem();
            LocalDate From = fromDate.getValue();
            LocalDate To   = toDate.getValue();

            if (usernameTextField.getText().isEmpty() || passwordTextField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please enter username/password.");
                alert.show();
            } else if(!validateUsername(usernameTextField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("You are not a valid student. Only students can register.");
                alert.show();
            } else if (val == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please select ONE club to join in.");
                alert.show();
            }else if (confirmPasswordTextField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please confirm password.");
                alert.show();
            } else if (usernameTextField.getText().length() > 10 || passwordTextField.getText().length() > 22) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Username should not exceed 10 characters.\nPassword should not exceed 12 characters");
                alert.show();
            } else if (!passwordTextField.getText().equals(confirmPasswordTextField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Confirm password does not match.");
                alert.show();
            } else if(From == null || To == null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("From and/or To Date Fields are empty.");
                alert.show();
            } else if( (To != null) && (From.compareTo(To)>0 || From.compareTo(To)==0)){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Enter a valid Date Combo. Date mismatch.");
                alert.show();
            } else {
                username = usernameTextField.getText();
                password = passwordTextField.getText();
                confPassword = confirmPasswordTextField.getText();
//                DBConnection dbConnection = new DBConnection();
//                conn = dbConnection.getDBConnection();
                String encPassword = pEncDec.getEncryptedPassword(password);

                try {
                    String newUser = "INSERT into CREDS VALUES('"+username+"', '"+encPassword+"', 4)";
                    int clubID = 0;
                    PreparedStatement pstmt1 = conn.prepareStatement(newUser);
                    pstmt1.executeUpdate();

                    String findClub = "SELECT ID FROM CLUB WHERE NAME='"+val.getName()+"' ;";
                    PreparedStatement pstmt2 = conn.prepareStatement(findClub);
                    ResultSet rs = pstmt2.executeQuery();
                    while(rs.next()) {
                        clubID = rs.getInt(1);
                    }

                    String newMember = "INSERT INTO CLUBMEMBER VALUES("+clubID+", "+username+", '"+From+"', '"+To+"', 3)";
                    PreparedStatement pstmt3 = conn.prepareStatement(newMember);
                    pstmt3.executeUpdate();

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setContentText("You have Registered, and you have applied for your first club. All the best!");
                    alert.show();

                    backButtonAction();

                } catch(SQLException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(e.getMessage());
                    alert.show();
                }
            }
    }


    /** Method for checking valid username
     *  (if it is in integer form, and if it is an existing studentID) */

    public boolean validateUsername(String name) {
        try {
            int studentID = Integer.parseInt(name);
            String checkStudent = "SELECT ID FROM STUDENT WHERE ID ="+studentID+" ;" ;
            PreparedStatement pstmt = conn.prepareStatement(checkStudent);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                return false;
            }
        }  catch (SQLException e) {
            return false;
        }  catch (NumberFormatException n) {
            return false;

        } return true;
    }


    /** Sets the values for the club list on the guest scene.*/

    public void setClubTable() {
        try {
            String getClubNameDescr = "SELECT NAME, DESCR FROM CLUB;";
            PreparedStatement pstmt;
            ResultSet rs;
            pstmt = conn.prepareStatement(getClubNameDescr);
            rs = pstmt.executeQuery();
            ArrayList<ClubNameDescr> clubNameDescrArrayList = new ArrayList<>();

            while (rs.next()) {
                String name = rs.getString("name");
                String descr = rs.getString("descr");
                clubNameDescrArrayList.add(new ClubNameDescr(name, descr));
            }

            ObservableList<ClubNameDescr> obsList = FXCollections.observableArrayList(clubNameDescrArrayList);
            clubColumn.setCellValueFactory(new PropertyValueFactory<ClubNameDescr, String>("name"));
            descrColumn.setCellValueFactory(new PropertyValueFactory<ClubNameDescr, String>("desc"));
            clubTable.setItems(obsList);
        }
        catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }
}