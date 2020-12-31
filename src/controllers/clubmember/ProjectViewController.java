package controllers.clubmember;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import models.clubmember.ProjNameDescr;
import models.db.DBConnection;
import models.loggedincache.GlobalUserName;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;


    public class ProjectViewController implements Initializable {
        private String selectedClub = GlobalUserName.clubMemberSelect;

        public Connection conn;

        @FXML
        private AnchorPane rootPane;

        @FXML
        private TableView<ProjNameDescr> projectTable = new TableView<>();

        @FXML
        private TableColumn<ProjNameDescr, String> projName;

        @FXML
        private TableColumn<ProjNameDescr, String> projDesc;

        @FXML
        private Button VolunteerBtn;

        @FXML
        private Button backButton;

        @FXML
        private TextField RoleDesc;

        @FXML
        private DatePicker FromDate;

        @FXML
        private DatePicker ToDate;

        /** Initializes the Project View window, which shows all available projects from
         *  the club selected in ClubMemberMain window. User can choose to volunteer for
         *  a selected project.*/
        @Override
        public void initialize(URL location, ResourceBundle resources) {
            try {
                Task<Void> task = new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        DBConnection dbConnection = new DBConnection();
                        conn = dbConnection.getDBConnection();
                        setProjectTable();
                        return null;
                    }
                };
                Thread thread = new Thread(task);
                thread.start();
                task.setOnSucceeded(e -> {
                    System.out.println("Succeeded");
                }
                );

                System.out.println(selectedClub +" INITIALIZED.");
            } catch (Exception e) {
                System.out.println("Error");
            }

        }

        /** When Volunteer button is clicked, ROLE and Dates are checked. If the entries
         * are valid, then the user is inserted into the WORKSON database.*/
        @FXML
        void Volunteer() {
            ProjNameDescr val = projectTable.getSelectionModel().getSelectedItem();
            String Role = RoleDesc.getText();
            LocalDate fromDate  = FromDate.getValue();
            LocalDate toDate = ToDate.getValue();
            if (val == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please Select a club to volunteer for.");
                alert.show();
            } else if (Role.isEmpty() || Role.length()>20) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Enter a Role (not empty and max 20 letters)");
                alert.show();
            } else if (fromDate == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Enter a From Date.");
                alert.show();
            } else if (toDate != null && fromDate.compareTo(toDate)>0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("From Date is occuring after To Date.");
                alert.show();
            } else if (toDate != null && fromDate.compareTo(toDate)==0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Both dates are same.");
                alert.show();
            } else {
                try {
                    int ProjId = 0;
                    DBConnection dbConnection = new DBConnection();
                    conn = dbConnection.getDBConnection();


                    System.out.println(val.getName() + " " + val.getDesc());

                    String sql1 = "SELECT ID FROM PROJECT WHERE NAME = '"+ val.getName()+"';";
                    PreparedStatement pstmt1 = conn.prepareStatement(sql1);
                    ResultSet rs = pstmt1.executeQuery();
                    if(rs.next()) {
                        ProjId += rs.getInt(1);
                    }
                    System.out.println(rs.getInt(1));
                    String sql2 = "INSERT INTO WORKSON VALUES("+GlobalUserName.globalId+", "+ProjId+", '"+fromDate+"', '"+toDate+"', '"+Role+"');";
                    PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                    pstmt2.executeUpdate();
                    System.out.println("BROTHER DONE");
                    conn.close();

                } catch(SQLException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(e.getMessage());
                    alert.show();
                }
            }
        }


        @FXML
        void back() throws Exception {
                conn.close();
                GlobalUserName.clubMemberSelect = null;
                AnchorPane root = FXMLLoader.load(getClass().getResource("../../views/clubmember/ClubMemberMain.fxml"));
                backButton.getScene().setRoot(root);
        }

        /** Sets the values for the projects list shown in projectview scene*/

        public void setProjectTable() {
            try{
                String getClubIdSql = "SELECT ID FROM CLUB WHERE NAME=?";

                PreparedStatement pstmt;
                ResultSet rs;

                pstmt = conn.prepareStatement(getClubIdSql);
                pstmt.setString(1, selectedClub);
                rs = pstmt.executeQuery();
                int id = 0;
                while(rs.next()) {
                    id = rs.getInt("id");
                }

                // Populating tableview with data.
                ArrayList<ProjNameDescr> projNameDescrArrayList = new ArrayList<>();
                String sql = "SELECT NAME, DESCR FROM PROJECT WHERE CLUBID=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("name");
                    String descr = rs.getString("descr");
                    System.out.println(name + " $ " + descr);
                    projNameDescrArrayList.add(new ProjNameDescr(name, descr));
                }
                ObservableList<ProjNameDescr> obsList = FXCollections.observableArrayList(projNameDescrArrayList);
                projName.setCellValueFactory(new PropertyValueFactory<ProjNameDescr, String>("name"));
                projDesc.setCellValueFactory(new PropertyValueFactory<ProjNameDescr, String>("desc"));

                projectTable.setItems(obsList);

            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getMessage());
                alert.show();
            }
        }
}
