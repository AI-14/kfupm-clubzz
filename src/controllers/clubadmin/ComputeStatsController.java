package controllers.clubadmin;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import controllers.sysadmin.SysAdminMainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import models.db.DBConnection;
import models.loggedincache.GlobalUserName;

import java.lang.management.GarbageCollectorMXBean;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ComputeStatsController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private JFXButton showTotalProjects;

    @FXML
    private JFXButton backButton;


    @FXML
    private JFXListView<String> listView;


    public ClubAdminMainController ClubAdminMainController;
    public Connection conn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ClubAdminMainController = new ClubAdminMainController();
            try {
                Task<Void> task = new Task<Void>() {
                    @Override
                    public Void call() {
                        DBConnection dbConnection = new DBConnection();
                        conn = dbConnection.getDBConnection();
                        if (conn == null) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setContentText("Database connection error.");
                            alert.show();
                        }
                        return null;
                    }
                };
                Thread thread = new Thread(task);
                thread.start();
                task.setOnSucceeded(e -> {
                    System.out.println("Connected");
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
    void signOutButtonAction() {

    }

    @FXML
    public void displayProjects() {
        String displayProjectsSql = "SELECT NAME FROM PROJECT WHERE CLUBID = ?;";
        String displayProjectNo = "SELECT COUNT(*) FROM PROJECT WHERE CLUBID = ?;";
        String displayClubName = "SELECT NAME FROM CLUB WHERE ID = ?;";
        int clubID = Integer.parseInt(GlobalUserName.globalId);
        try {
            PreparedStatement pstmt ;
            ResultSet rs;
            pstmt = conn.prepareStatement(displayProjectsSql);
            pstmt.setInt(1, clubID);
            rs = pstmt.executeQuery();
            ArrayList<String> projectNames = new ArrayList<>();
            while (rs.next()) {
                projectNames.add(rs.getString(1));
            }

            pstmt = conn.prepareStatement(displayProjectNo);
            pstmt.setInt(1, clubID);
            rs = pstmt.executeQuery();
            int projectNo = 0;
            while(rs.next()) {
                projectNo = rs.getInt(1);
            }

            String clubName = "";

            pstmt = conn.prepareStatement(displayClubName);
            pstmt.setInt(1, clubID);
            rs = pstmt.executeQuery();
            while (rs.next()){
                clubName = rs.getString(1);
            }

            if(clubName.equals("")){
                System.out.println("Error in fetching club.");
            }
            else {
                String totalProjects = "The total number of projects associated with " + clubName + " are " + projectNo + ".";
                projectNames.add(totalProjects);
            }
            ObservableList<String> projectNamesObservable = FXCollections.observableArrayList(projectNames);
            listView.setItems(projectNamesObservable);

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }

    }
}
