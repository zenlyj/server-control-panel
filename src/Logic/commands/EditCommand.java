package Logic.commands;

import Model.App;
import Model.Server;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class EditCommand extends Command {
    private final App app;
    private final int selectedIndex;
    private final String userName;
    private final String password;
    private final String serverName;
    private final String ipAddress;
    private final String editSuccessMessage = "%s has been successfully edited";
    private final String editFailureMessage = "%s already exists in the current list! Aborting edit operation...";
    private final String editNoChangeMessage = "No changes have been made to %s";

    public EditCommand(App app, int selectedIndex, String userName, String password, String serverName, String ipAddress) {
        this.app = app;
        this.selectedIndex = selectedIndex;
        this.userName = userName;
        this.password = password;
        this.serverName = serverName;
        this.ipAddress = ipAddress;
    }

    @Override
    public void execute() {
        List<Server> servers = app.getServers();
        Server oldServer = servers.get(selectedIndex);
        app.setServerInEdit(oldServer);
        Server updatedServer = new Server(ipAddress, serverName, userName, password);
        boolean isChanged = (!ipAddress.equals(oldServer.getIpAddress())) ||
                     (!serverName.equals(oldServer.getServerName())) ||
                     (!userName.equals(oldServer.getUserName())) ||
                     (!password.equals(oldServer.getPassword()));
        if (isChanged) {
            // erase old server
            servers.set(selectedIndex, new Server("", "", "", ""));
            if (!servers.contains(updatedServer)) {
                servers.set(selectedIndex, updatedServer);
                app.commit(servers);
                app.addHistory(String.format(editSuccessMessage, oldServer.getServerName()));
            } else {
                // if edit failure, set old server back to index
                servers.set(selectedIndex, oldServer);
                app.addHistory(String.format(editFailureMessage, serverName));
            }
        } else {
            app.addHistory(String.format(editNoChangeMessage, oldServer.getServerName()));
        }
    }
}
