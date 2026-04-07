import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VotingImpl extends UnicastRemoteObject implements VotingSystem {

	private final Map<String, Integer> candidates = new LinkedHashMap<>();
	private final Set<Integer> registeredVoters = new LinkedHashSet<>();
	private final Set<Integer> votedVoters = new LinkedHashSet<>();

	public VotingImpl() throws RemoteException {
		super();
	}

	@Override
	public synchronized int initialise() throws RemoteException {
		candidates.clear();
		registeredVoters.clear();
		votedVoters.clear();
		System.out.println("Election state initialized");
		return 0;
	}

	@Override
	public synchronized int reset() throws RemoteException {
		return initialise();
	}

	@Override
	public synchronized int register(int voterid) throws RemoteException {
		if (registeredVoters.contains(voterid)) {
			return 1;
		}
		registeredVoters.add(voterid);
		System.out.println("Registered voter: " + voterid);
		return 0;
	}

	@Override
	public synchronized int castvote(String name, int voterid) throws RemoteException {
		String candidateName = normalize(name);
		if (!registeredVoters.contains(voterid)) {
			return 2;
		}
		if (votedVoters.contains(voterid)) {
			return 3;
		}
		int currentVotes = candidates.containsKey(candidateName) ? candidates.get(candidateName) : 0;
		candidates.put(candidateName, currentVotes + 1);
		votedVoters.add(voterid);
		System.out.println("Vote recorded for " + candidateName + " by voter " + voterid);
		return currentVotes == 0 ? 1 : 0;
	}

	@Override
	public synchronized String[][] candidatelist() throws RemoteException {
		String[][] result = new String[candidates.size()][2];
		int index = 0;
		for (Map.Entry<String, Integer> entry : candidates.entrySet()) {
			result[index][0] = entry.getKey();
			result[index][1] = Integer.toString(entry.getValue());
			index++;
		}
		return result;
	}

	@Override
	public synchronized int votecount(String name) throws RemoteException {
		String candidateName = normalize(name);
		Integer count = candidates.get(candidateName);
		return count == null ? -1 : count;
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toUpperCase();
	}

	public synchronized List<String> snapshotCandidates() {
		return new ArrayList<>(candidates.keySet());
	}

	public synchronized int registeredCount() {
		return registeredVoters.size();
	}

	public synchronized int votedCount() {
		return votedVoters.size();
	}
}
