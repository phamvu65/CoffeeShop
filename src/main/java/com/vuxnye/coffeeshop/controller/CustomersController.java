package com.vuxnye.coffeeshop.controller;

import com.vuxnye.coffeeshop.dao.CustomerDAO;
import com.vuxnye.coffeeshop.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class CustomersController {

    @FXML private TextField txtSearch;
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, String> colName, colPhone;
    @FXML private TableColumn<Customer, Integer> colPoints;
    @FXML private TableColumn<Customer, Void> colAction;

    private CustomerDAO customerDAO = new CustomerDAO();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    // Icons
    private final String SVG_EDIT = "M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7";
    private final String SVG_TRASH = "M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2";

    @FXML
    public void initialize() {
        setupTable();
        refreshData();

        txtSearch.textProperty().addListener((obs, old, val) -> {
            customerList.setAll(customerDAO.searchCustomers(val));
        });
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    // Avatar giả lập bằng chữ cái đầu
                    Label avatar = new Label(item.substring(0, 1));
                    avatar.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: 900; -fx-min-width: 35; -fx-min-height: 35; -fx-alignment: CENTER; -fx-background-radius: 50;");

                    Label name = new Label(item);
                    name.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

                    HBox box = new HBox(10, avatar, name);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        colPoints.setCellValueFactory(new PropertyValueFactory<>("points"));
        colPoints.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    Label lbl = new Label(String.format("%,d", item));
                    lbl.setStyle("-fx-text-fill: #d97706; -fx-font-weight: 900; -fx-padding: 5 10; -fx-background-color: #fef3c7; -fx-background-radius: 10;");
                    setGraphic(lbl);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                String btnStyle = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-cursor: hand; -fx-min-width: 35; -fx-min-height: 35;";
                btnEdit.setStyle(btnStyle);
                btnDelete.setStyle(btnStyle);

                btnEdit.setOnAction(e -> openModal(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> {
                    if(new Alert(Alert.AlertType.CONFIRMATION, "Xóa khách hàng này?").showAndWait().get() == ButtonType.OK) {
                        customerDAO.deleteCustomer(getTableView().getItems().get(getIndex()).getId());
                        refreshData();
                    }
                });
                pane.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    btnEdit.setGraphic(createSVG(SVG_EDIT, "#3b82f6"));
                    btnDelete.setGraphic(createSVG(SVG_TRASH, "#ef4444"));
                    setGraphic(pane);
                }
            }
        });
        tblCustomers.setItems(customerList);
    }

    private SVGPath createSVG(String content, String color) {
        SVGPath svg = new SVGPath();
        svg.setContent(content);
        svg.setStyle("-fx-fill: transparent; -fx-stroke: " + color + "; -fx-stroke-width: 2;");
        svg.setScaleX(0.7); svg.setScaleY(0.7);
        return svg;
    }

    public void refreshData() {
        customerList.setAll(customerDAO.getAllCustomers());
    }

    @FXML private void handleOpenAddModal() { openModal(null); }

    private void openModal(Customer c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vuxnye/coffeeshop/view/CustomerForm.fxml"));
            Parent root = loader.load();
            CustomerFormController controller = loader.getController();
            controller.setCustomerData(c);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }
}