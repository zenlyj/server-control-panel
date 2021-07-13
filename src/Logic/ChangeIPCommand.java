package Logic;

import Model.App;
import Model.Server;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

public class ChangeIPCommand extends Command {
    private App app;
    private Server server;
    private final String newIPAddress;
    private final String newServerName;
    private final int serverIdx;
    private final String changeIPSuccessMessage = "%1$s's ip address is changed from %2$s to %3$s\n";
    private final String changeNameSuccessMessage = "%1$s successfully renamed to %2$s\n";
    private final String noChangeMessage = "No IP change made to %s\n";
    private final String changeIPFailureMessage = "Failed to change IP for %s\n";
    private final String psUnavailableMessage = "Powershell is not available on this work station! Aborting change operation...\n";
    private final String offlineFailureMessage = "%s is offline! Aborting change ip/name operation...\n";
    private final String initChangeMessage = "Initiated ip/name change for %s\n";

    public ChangeIPCommand(App app, int serverIdx, String newIPAddress, String newServerName) {
        this.app = app;
        this.serverIdx = serverIdx;
        this.server = app.getServers().get(serverIdx);
        this.newIPAddress = newIPAddress;
        this.newServerName = newServerName;
    }

    @Override
    public void execute() {
        if (server.getIsOnline()) {
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    powerShellExec();
                    return null;
                }
            };
            new Thread(task).start();
            app.addHistory(String.format(initChangeMessage, server.getServerName()));
        } else {
            app.addHistory(String.format(offlineFailureMessage, server.getServerName()));
        }
    }

    private String lstToString(List<Server> servers) {
        String res = "";
        for (Server s : servers) {
            res += s.getIpAddress() + ",";
        }
        return res.substring(0, res.length()-1);
    }

    private boolean isIPChange() {
        return !(newIPAddress.equals(server.getIpAddress()));
    }

    private PowerShellResponse createSession(PowerShell powerShell) {
        powerShell.executeCommand(PSCommand.declareStringVar("serverIP", server.getIpAddress()));
        powerShell.executeCommand(PSCommand.declareStringVar("userName", server.getUserName()));
        powerShell.executeCommand(PSCommand.declareStringVar("password", server.getPassword()));
        powerShell.executeCommand(PSCommand.declareSecurePasswordVar("securePassword", "password"));
        powerShell.executeCommand(PSCommand.declareCredsVar("creds", "userName", "securePassword"));
        return powerShell.executeCommand(PSCommand.declareSessionVar("s", "serverIP", "creds"));
    }

    private void renameServer(PowerShell powerShell) {
        powerShell.executeCommand(PSCommand.invokeCommand("s", PSCommand.declareStringVar("newServerName", newServerName)));
        powerShell.executeCommand(PSCommand.invokeCommand("s", PSCommand.renameCommand("newServerName")));
    }

    private PowerShellResponse changeIP(PowerShell powerShell) {
        powerShell.executeCommand(PSCommand.declareStringVar("allServerIP", lstToString(app.getServers())));
        powerShell.executeCommand(PSCommand.setTrustedHosts("allServerIP"));
        powerShell.executeCommand(PSCommand.invokeCommand("s", PSCommand.declareStringVar("newIPAddr", newIPAddress)));
        powerShell.executeCommand(PSCommand.invokeCommand("s", PSCommand.declareStringVar("oldIPAddr", server.getIpAddress())));
        powerShell.executeCommand(PSCommand.invokeCommand("s", PSCommand.declareAdapterVar("adapterIndex", "NIC1")));
        powerShell.executeCommand(PSCommand.invokeCommand("s", PSCommand.newIPCommand("adapterIndex", "newIPAddr")));
        return powerShell.executeCommand(PSCommand.invokeCommand("s", PSCommand.removeIPCommand("adapterIndex", "oldIPAddr")));
    }

    private void updateMainApp(boolean isChanged) {
        Platform.runLater(() -> {
            if (isChanged) {
                EditCommand editCmd = new EditCommand(app, serverIdx, server.getUserName(), server.getPassword(), newServerName, newIPAddress);
                editCmd.execute();
                app.addHistory(String.format(changeIPSuccessMessage, server.getServerName(), server.getIpAddress(), newIPAddress));
                app.addHistory(String.format(changeNameSuccessMessage, server.getServerName(), newServerName));
            }
        });
    }

    private List<Server> lstDeepCopy(List<Server> servers) {
        List<Server> res = new ArrayList<>();
        for (Server server : servers) {
            res.add(new Server(server));
        }
        return res;
    }

    public void powerShellExec() {
        try (PowerShell powerShell = PowerShell.openSession()) {
            createSession(powerShell);
            renameServer(powerShell);
            boolean success = false;
            if (isIPChange()) {
                PowerShellResponse response = changeIP(powerShell);
                if (response.getCommandOutput().isBlank()) {
                    success = true;
                    List<Server> updatedServers = lstDeepCopy(app.getServers());
                    updatedServers.set(serverIdx, new Server(newIPAddress, newServerName, server.getUserName(), server.getPassword()));
                    powerShell.executeCommand(PSCommand.declareStringVar("updatedServerIP", lstToString(updatedServers)));
                    powerShell.executeCommand(PSCommand.setTrustedHosts("updatedServerIP"));
                } else {
                    app.addHistory(String.format(changeIPFailureMessage, server.getServerName()));
                }
            } else {
                app.addHistory(String.format(noChangeMessage, server.getServerName()));
            }

            final boolean isChanged = success;
            updateMainApp(isChanged);
        } catch (PowerShellNotAvailableException ex) {
            Platform.runLater(()->{
                app.addHistory(psUnavailableMessage);
            });
        }
    }
}
