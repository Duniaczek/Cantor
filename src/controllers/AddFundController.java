package controllers;

import classes.CheckNumber;
import classes.Currence;
import classes.DbConnection.DbHandler;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class AddFundController {

    @FXML private ChoiceBox<Currence> choiseBoxFunds;
    @FXML private JFXTextField fieldFunds;
    @FXML private JFXButton buttonMainMenu;
    @FXML private JFXButton buttonAdd;

    private String name;
    private Connection connection;
    private DbHandler handler;

    @FXML
    void initialize() {

        handler = new DbHandler();
        name = PersonMenuController.getInstance().getName();
        addCurrenceToChoiceBox();

        choiseBoxFunds.setValue(Currence.PLN);
        fieldFunds.addEventFilter(Event.ANY, x -> checkNumbers());
    }

    @FXML
    void addFunds(ActionEvent event) {

        String currencyPlus = choiseBoxFunds.getValue().getCurrence();
        connection = handler.getConnection();

        double temp = 0;
        String q1 = "SELECT " + currencyPlus + " FROM CUSTOMERS WHERE " + "names = " + "'" + name + "'" ;

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(q1);

            if (rs.next()) {
                temp = rs.getDouble(choiseBoxFunds.getValue().getCurrence());
            }

            if (Double.parseDouble(fieldFunds.getText()) > 0) {
                String q2 = "UPDATE customers SET " + currencyPlus + " = " + (temp + Double.parseDouble(fieldFunds.getText())) + "WHERE names = '" + name + "'";
                st.execute(q2);
                System.out.println("Funds has been added!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void backToMainMenu(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxmls/PersonWindow.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            int i;
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addCurrenceToChoiceBox() {

        for (Currence current : Currence.values()) {
            choiseBoxFunds.getItems().add(current);
        }
    }

    private void checkNumbers() {

        if (choiseBoxFunds.getValue() != null && CheckNumber.checkNumbers(fieldFunds.getText())) {
            buttonAdd.setDisable(false);
        } else {
            buttonAdd.setDisable(true);
        }
    }
}
