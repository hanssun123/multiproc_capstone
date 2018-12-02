
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelFirewallPools {

	// Constants
	int MAX_T = 2;

	// Permissions.
	private ConcurrentHashMap<Integer, Boolean> canSend;
	private ConcurrentHashMap<Integer, Set<Integer>> canReceiveFrom;

	// Thread pools.
	private ExecutorService configPool;
	private ExecutorService dataPool;

	// Histogram of results.
	private ConcurrentHashMap<Long, Integer> histogram;

	public ParallelFirewallPools() {
		// Instantiate permissions:
		this.canSend = new ConcurrentHashMap<>(); // PNG.
		this.canReceiveFrom = new ConcurrentHashMap<>(); // R.

		// Instantiate thread pools:
		this.configPool = Executors.newFixedThreadPool(MAX_T);
		this.dataPool = Executors.newFixedThreadPool(MAX_T);

		this.histogram = new ConcurrentHashMap<>();

		// Instantiate threads / register in work maps.
	}

	// Dispatcher Thread
	public void handlePacket(Packet pkt) {
		// Distribute Work:
		switch (pkt.type) {
		case ConfigPacket:
			// Create task:
			ConfigTask configTask = new ConfigTask(pkt);
			this.configPool.execute(configTask);
			break;
		case DataPacket:
			DataTask dataTask = new DataTask(pkt);
			this.dataPool.execute(dataTask);
			break;
		}
	}

	public Map<Long, Integer> getHistogram() {
		return this.histogram;
	}

	private class ConfigTask implements Runnable {
		private Packet pkt;

		public ConfigTask(Packet pkt) {
			this.pkt = pkt;
		}

		public void run() {
			// Update permissions.
			// System.out.println("Handling config packet for address: " + pkt.config.address);

			// Update canSend permissions with personaNonGrata.
			canSend.put(pkt.config.address, pkt.config.personaNonGrata);

			// Update canReceiveFrom permissions.
			Set<Integer> oldPermissions = canReceiveFrom.get(pkt.config.address);

			if (oldPermissions == null) {
				oldPermissions = new HashSet<Integer>();
			}

			for (int i = pkt.config.addressBegin; i < pkt.config.addressEnd; i++) {
				if (pkt.config.acceptingRange) {
					oldPermissions.add(i);
				} else {
					oldPermissions.remove(i);
				}
			}
			canReceiveFrom.put(pkt.config.address, oldPermissions);
			// System.out.println("Sender can send: " + pkt.config.personaNonGrata);
			// System.out.println("Can receive from permissions: " + oldPermissions.toString());
		}

	}

	private class DataTask implements Runnable {
		private Packet pkt;

		public DataTask(Packet pkt) {
			this.pkt = pkt;
		}

		public void run() {
			// Make this a thread pool.
			// Check access control.

			// Check if sender can send.
			if (!canSend.containsKey(pkt.header.source) || !canSend.get(pkt.header.source)) {
				// Sender can't send.
				return;
			}

			// Check if receiver can receive from this sender.
			if (!canReceiveFrom.containsKey(pkt.header.dest)
					|| !canReceiveFrom.get(pkt.header.dest).contains(pkt.header.source)) {
				// Receiver cannot receive from this sender.
				return;
			}

			// --- Successfully passed through permissions check! ---
			// System.out.println("Passed permissions!");

			// Histogram the checksum.
			Fingerprint fingerprint = new Fingerprint();
			long checksum = fingerprint.getFingerprint(pkt.body.iterations, pkt.body.seed);

			if (!histogram.containsKey(checksum)) {
				histogram.put(checksum, 0);
			}

			int oldCount = histogram.get(checksum);
			histogram.put(checksum, oldCount + 1);
		}

	}

}
