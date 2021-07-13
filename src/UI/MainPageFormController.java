package UI;

import Logic.DeleteCommand;
import Logic.PingCommand;
import Logic.RemoteDesktopCommand;
import Logic.ShutdownCommand;
import Model.App;
import Model.Server;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainPageFormController {
    private App app;
    @FXML
    private Label userNameDetails;
    @FXML
    private Label passwordDetails;
    @FXML
    private Label serverNameDetails;
    @FXML
    private Label ipDetails;
    @FXML
    private TextArea historyBox;
    @FXML
    private TableView tableView;

    @FXML
    public void handleAdd(Event e) {
        try {
            Stage stage = new Stage();
            stage.setScene(AddForm.getForm(app));
            stage.show();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    public void handleDelete(Event e) {
        try {
            List<Server> serversToDelete = tableView.getSelectionModel().getSelectedItems();
            DeleteCommand deleteCommand = new DeleteCommand(app, serversToDelete);
            deleteCommand.execute();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    public void handlePing(Event e) {
        List<Integer> serverIndices = tableView.getSelectionModel().getSelectedIndices();
        PingCommand pingCommand = new PingCommand(app, serverIndices);
        pingCommand.execute();
    }

    @FXML
    public void handleShutdown(Event e) {
        List<Server> serversToShutdown = (List<Server>) tableView.getSelectionModel().getSelectedItems();
        ShutdownCommand shutdownCommand = new ShutdownCommand(app, serversToShutdown);
        shutdownCommand.execute();
    }

    @FXML
    public void handleEdit(Event e) {
        try {
            int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
            Stage stage = new Stage();
            stage.setScene(EditForm.getForm(app, selectedIndex));
            stage.show();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    public void handleChangeID(Event e) {
        try {
            int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
            Stage stage = new Stage();
            stage.setScene(ChangeIPForm.getForm(app, selectedIndex));
            stage.show();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    public void handleRemoteDesktop(Event e) {
        Server server = (Server) tableView.getSelectionModel().getSelectedItem();
        RemoteDesktopCommand remoteDesktopCommand = new RemoteDesktopCommand(app, server);
        remoteDesktopCommand.execute();
    }

    public void initTableView() {
        TableView.TableViewSelectionModel<Server> selectionModel = tableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Server, String> column1 = new TableColumn<>("Server Name");
        column1.setCellValueFactory(new PropertyValueFactory<>("serverName"));
        TableColumn<Server, String> column2 = new TableColumn<>("Status");
        column2.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);
        setTableData(app.getServers());
        app.getServers().addListener((ListChangeListener) change -> setTableData(app.getServers()));
    }

    private void setTableData(List<Server> servers) {
        List<Integer> selectedIndices = new ArrayList<>(tableView.getSelectionModel().getSelectedIndices());
        tableView.getItems().clear();
        servers.forEach(server -> {
            tableView.getItems().add(server);
        });
        for (Integer i : selectedIndices) {
            tableView.getSelectionModel().select(i.intValue());
        }
    }

    public void initHistoryBox() {
        StringProperty history = app.getHistory();
        history.addListener((observable, oldValue, newValue) -> {
            historyBox.setText(history.get());
            historyBox.setScrollTop(Double.MAX_VALUE);
        });
        historyBox.setEditable(false);
    }

    public void initServerDetails() {
        TableView.TableViewSelectionModel<Server> selectionModel = tableView.getSelectionModel();
        selectionModel.selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            setServerInfo(newSelection);
        });
    }

    private void setServerInfo(Server newSelection) {
        userNameDetails.setText("");
        passwordDetails.setText("");
        serverNameDetails.setText("");
        ipDetails.setText("");
        if (newSelection == null) return;
        userNameDetails.setText("User name: " + newSelection.getUserName());
        passwordDetails.setText("Password: " + newSelection.getPassword());
        serverNameDetails.setText("Server name: " + newSelection.getServerName());
        ipDetails.setText("IP Address: " + newSelection.getIpAddress());
    }

    public void init(App app) {
        this.app = app;
        initHistoryBox();
        initServerDetails();
        initTableView();
    }
}