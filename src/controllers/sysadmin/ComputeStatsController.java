/**
 * Author: AMAAN IZHAR
 * Controller class to handle the computations (related to the club) and rendering of the piechart.
 */

package controllers.sysadmin;

import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.db.DBConnection;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ComputeStatsController implements Initializable {
    @FXML
    private AnchorPane rootPane;

    @FXML
    private JFXButton backButton, myAccountButton, signOutButton, displayMembersChartButton, displayProjectsChartButton;

    @FXML
    private PieChart pieChart;

    @FXML
    private Label msgLabel;

    public SysAdminMainController sysAdminMainController;
    public Connection conn;

    /**
     * Method that initializes the following:
     * 1. Opens mysql connection to server.
     * 2. Displays message in case of connection failure.
     *
     * @param location  - The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources - The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            sysAdminMainController = new SysAdminMainController();
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
     * Method that displays members per club to the user.
     */
    @FXML
    void displayMembersChartButtonAction() {
        String membersPerClubSql = "SELECT CM.CLUBID, C.NAME, COUNT(CM.CLUBID) FROM CLUBMEMBER CM, CLUB C WHERE C.ID=CM.CLUBID GROUP BY CM.CLUBID;";
        msgLabel.setText("Members/Club");
        displayChart(membersPerClubSql);
    }

    /**
     * Method that displays projects per club to the user.
     */
    @FXML
    void displayProjectsChartButtonAction() {
        String projectsPerClubSql = "SELECT P.CLUBID, C.NAME, COUNT(P.CLUBID) AS TOTOLPROJECTS FROM PROJECT P, CLUB C WHERE C.ID=P.CLUBID GROUP BY P.CLUBID;";
        msgLabel.setText("Projects/Club");
        displayChart(projectsPerClubSql);
    }

    /**
     * A utility method that loads the data into the piechart and displays it.
     *
     * @param sql - an sql query that computes clubs per member or porjects per member.
     */
    public void displayChart(String sql) {
        try {
            HashMap<String, Integer> map = new HashMap<>();

            PreparedStatement pstmt;
            ResultSet rs;

            // Populating the hashmap with values according to the sql query.
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if(!rs.next()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("There is no data.");
                alert.show();
            }
            else {
                do {
                    map.put(rs.getString(2), rs.getInt(3));
                } while (rs.next());

                ArrayList<PieChart.Data> pieData = new ArrayList<>();
                for (Map.Entry<String, Integer> pair : map.entrySet()) {
                    pieData.add(new PieChart.Data(pair.getKey(), pair.getValue()));
                }

                ObservableList<PieChart.Data> obsList = FXCollections.observableList(pieData);
                pieChart.setData(obsList);

                // Setting values to be displayed on the chart.
                for (PieChart.Data data : pieChart.getData()) {
                    data.nameProperty().set(data.getName() + " : " + (int) data.getPieValue());
                }

                // Adding fade transition.
                FadeTransition ft = new FadeTransition(Duration.seconds(3), pieChart);
                ft.setFromValue(0);
                ft.setToValue(3);
                ft.play();
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
