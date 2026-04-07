
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class VotingServer {

	public static void main(String[] args) {
		try {
			try {
				LocateRegistry.createRegistry(1099);
			} catch (RemoteException ignored) {
				// Registry is already running.
			}

			VotingSystem service = new VotingImpl();
			try {
				Naming.rebind("rmi://localhost:1099/VotingService", service);
			} catch (Exception ex) {
				throw new RemoteException("Unable to bind RMI service", ex);
			}
			service.initialise();
			System.out.println("Voting RMI server started on localhost:1099");
		} catch (Exception ex) {
			System.out.println("Trouble: " + ex);
		}
	}
}
