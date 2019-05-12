package controllers;

import classes.Currence;
import classes.CurrencyConverter;
import classes.DbConnection.DbHandler;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class PersonMenuController implements Initializable {

    @FXML private JFXButton buttonCheck;
    @FXML private GridPane grindPaneCurrence;
    @FXML private ChoiceBox<Currence> choiceBoxA;
    @FXML private ChoiceBox<Currence> choiceBoxB;
    @FXML private JFXTextField fieldCheck;
    @FXML private JFXButton buttonLogOut;
    @FXML private Label labelDeposit;
    @FXML private Label labelQuantity;
    @FXML private Label labelExchangeA;
    @FXML private Label labelExchangeB;
    @FXML private Label labelRecieveA;
    @FXML private Label labelReciveB;
    @FXML private JFXButton buttonCheckOut;
    @FXML private JFXButton buttonCancel;

    private Map<Currence, Double> funds = new HashMap<>();
    private Connection connection = new DbHandler().getConnection();
    private DbHandler handler;
    private int indexOfRow = 0; // Index of GrindPane;
    private BigDecimal exchange;
    private Timer timer;
    private String name;
    private double amountEx;

    private LoginWindowController instanceLoginMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        choiceBoxA.addEventFilter(Event.ANY, x -> checkConditions());
        choiceBoxB.addEventFilter(Event.ANY, x -> checkConditions());
        fieldCheck.addEventFilter(Event.ANY, x -> {
            checkConditions();

            if (checkNumbers() && choiceBoxA.getValue() != null) {
                labelQuantity.setText(String.format("%.2f %s",
                        Double.parseDouble(fieldCheck.getText()), choiceBoxA.getValue().getCurrence()));
            }
        });
    }

    @FXML
    void cancelExchange(ActionEvent event) {

        cancelEx();
    }

    @FXML
    void checkCurrence(ActionEvent event) {

        buttonCheck.setVisible(false);
        buttonCheckOut.setVisible(true);
        buttonCancel.setVisible(true);

        labelExchangeA.setVisible(true);
        labelExchangeB.setVisible(true);

        labelRecieveA.setVisible(true);
        labelReciveB.setVisible(true);

        exchange = CurrencyConverter.convert(choiceBoxA.getValue(), choiceBoxB.getValue());

        BigDecimal big = new BigDecimal(fieldCheck.getText());
        labelExchangeB.setText(String.format("1 %s - %.4f", choiceBoxA.getValue().getCurrence(), exchange.doubleValue()));
        amountEx = exchange.multiply(big).doubleValue();
        labelReciveB.setText(String.format("%.2f %s", amountEx, choiceBoxB.getValue().getCurrence()));

        buttonCheckOut.setText(String.format("Confrim - (15)"));

        timer = new Timer();
        timer.schedule(new TimerTask() {
            int m = 15;

            @Override
            public void run() {
                Platform.runLater(() -> {

                    buttonCheckOut.setText(String.format("Confrim - (%d)", m--));
                    if (m == -2) {
                        cancelEx();
                        timer.cancel();
                    }
                });
            }
        }, 250, 1000);
    }

    @FXML
    void checkOutExchange(ActionEvent event) {

        plusFund(amountEx, Double.parseDouble(fieldCheck.getText()));

        cancelEx();
    }

    @FXML
    void logOut(ActionEvent event) {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("../fxmls/LoginWindow.fxml"));
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Login Cantor");
            buttonCheckOut.getScene().getWindow().hide();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Method connects with Database and set current amounts of currence
    private void plusFund(double amountPlus, double amountMinus) {

        String currencyPlus = choiceBoxB.getValue().getCurrence();
        double temp = funds.get(choiceBoxB.getValue()) + amountPlus;

        String currencyMinus = choiceBoxA.getValue().getCurrence();
        double temp2 = funds.get(choiceBoxA.getValue()) - amountMinus;

        String q1 = "UPDATE customers SET " + currencyPlus + " = " + temp + "WHERE names = '" + getName() + "'";
        String q2 = "UPDATE customers SET " + currencyMinus + " = " + temp2 + "WHERE names = '" + getName() + "'";

        try {
            Statement st = connection.createStatement();
            st.execute(q1);
            st.execute(q2);

            loadFunds();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addFund() {

    }

    private void cancelEx() {
        buttonCheck.setVisible(true);
        buttonCheckOut.setVisible(false);
        buttonCancel.setVisible(false);

        labelExchangeA.setVisible(false);
        labelExchangeB.setVisible(false);
        labelRecieveA.setVisible(false);
        labelReciveB.setVisible(false);

        timer.cancel();
    }

    private void loadFunds() {


        String q1 = "SELECT * FROM customers WHERE names = '" + getName() + "'";
        clearGrindPane();
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(q1);

            while (rs.next()) {
                for(Currence current : Currence.values()) {
                    String string = current.getCurrence();

                    double amount = rs.getDouble(current.getCurrence());
                    funds.put(current, amount);

                    String temp = String.format("%.2f", amount);
                    if (amount > 0) {
                        addGrindPane(string, temp);
                    }
                }
            }

            System.out.println(funds);
            addBoxesToChoiceBox();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addGrindPane(String l1, String l2) {

        Label label = new Label(l1 + ":");
        Label label2 = new Label("  " + l2);

        label.setStyle("-fx-font-size: 15; -fx-hgap: 10");
        label2.setStyle("-fx-font-size: 15; -fx-hgap: 10");


        grindPaneCurrence.add(label, 0, indexOfRow);
        grindPaneCurrence.add(label2, 1, indexOfRow);

        indexOfRow++;
    }

    private void clearGrindPane() {
        int temp = grindPaneCurrence.getChildren().size();
        grindPaneCurrence.getChildren().remove(0, temp);
    }

    private void addBoxesToChoiceBox() {

        for (Map.Entry<Currence, Double> entries : funds.entrySet()) {

            if (entries.getValue() > 0) {
                choiceBoxA.getItems().add(entries.getKey());
            }

            choiceBoxB.getItems().add(entries.getKey());
        }
    }

    //Method check conditions to setDisable(false), two choises box must be selected and numbers in textField.
    private void checkConditions() {

            if (choiceBoxA.getValue() != null && choiceBoxB.getValue() != null && checkNumbers()) {

                if(!choiceBoxA.getValue().equals(choiceBoxB.getValue()) && fieldCheck.getText().length() > 0) {

                    if (funds.get(choiceBoxA.getValue()) < Double.parseDouble(fieldCheck.getText())) {
                        buttonCheck.setDisable(true);
                    } else {
                        buttonCheck.setDisable(false);
                    }
                } else{
                    buttonCheck.setDisable(true);
                }

            } else {
                buttonCheck.setDisable(true);
            }
    }

    private boolean checkNumbers() {

        try {
            double temp = Double.parseDouble(fieldCheck.getText());
        } catch (NumberFormatException e) {
            labelQuantity.setText("");
            return false;
        }

        return true;
    }

    public void setInstanceLoginMenu(LoginWindowController instance) {
        this.instanceLoginMenu = instance;
    }

    public void setName(String s) {
        this.name = s;
        loadFunds();
    }

    public String getName() {
        return name;
    }
}
