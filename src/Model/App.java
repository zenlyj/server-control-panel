package Model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class App {
    private ObservableList<Server> servers;
    private Set<Integer> serversInEdit;
    private Storage db;

    public App() {
        db = new Storage();
        List<Server> retrieved = db.retrieve();
        servers = FXCollections.observableList(retrieved);
        serversInEdit = new HashSet<>();
    }

    public void commit(List<Server> servers) {
        db.store(servers);
    }

    public ObservableList<Server> getServers() {
        return this.servers;
    }

    public void setServerInEdit(int serverInEdit) {
        this.serversInEdit.add(serverInEdit);
    }

    public void removeServerInEdit(int serverInEdit) {
        this.serversInEdit.remove(serverInEdit);
    }

    public boolean isServerInEdit(int serverIndex) {
        return this.serversInEdit.contains(serverIndex);
    }
}
