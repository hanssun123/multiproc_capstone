
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import map.MyMap;

public class ParallelFirewallPools {

	// Permissions.
	MyMap<Integer, Boolean> canSend;
	MyMap<Integer, Set<Integer>> canReceiveFrom;

	// Thread pools.
	private ExecutorService configPool;
	private ExecutorService dataPool;

	// Histogram of results.
	private ConcurrentHashMap<Long, Integer> histogram;

	// Which mode.
	boolean multithreaded;

	public ParallelFirewallPools(int numThreads, MyMap<Integer, Boolean> canSend,
			MyMap<Integer, Set<Integer>> canReceiveFrom) {

		this.multithreaded = false;

		// Set permissions:
		this.canSend = canSend; // PNG.
		this.canReceiveFrom = canReceiveFrom; // R.

		// Instantiate thread pools:

		int configThreads;
		int dataThreads;
		if (numThreads % 2 != 0) {
			// For an odd number of threads, give the data pool an extra thread.
			configThreads = numThreads / 2;
			dataThreads = numThreads / 2 + 1;
		} else {
			// For an even number, spread them out evenly.
			configThreads = numThreads / 2;
			dataThreads = numThreads / 2 + 1;
		}

		this.configPool = Executors.newFixedThreadPool(configThreads);
		this.dataPool = Executors.newFixedThreadPool(dataThreads);

		this.histogram = new ConcurrentHashMap<>();
	}

	// Dispatcher Thread
	public void handlePacket(Packet pkt) {
		if (multithreaded) {
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
		} else {
			// Handle singlethreaded
			// Distribute Work:
			switch (pkt.type) {
			case ConfigPacket:
				// Create task:
				handleConfig(pkt);
				break;
			case DataPacket:
				handleData(pkt);
				break;
			}
		}
	}

	public void shutdown() {
		this.configPool.shutdown();
		this.dataPool.shutdown();
		try {
			this.configPool.awaitTermination(100000, TimeUnit.MILLISECONDS);
			this.dataPool.awaitTermination(100000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException exception) {
			System.out.println("Timed out while waiting for pools to finish");
		}
	}
	
	public void setMultithreaded(boolean mode) {
		this.multithreaded = mode;
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
		}

	}

	private class DataTask implements Runnable {
		private Packet pkt;

		public DataTask(Packet pkt) {
			this.pkt = pkt;
		}

		public void run() {
			// Check access control.

			// Check if sender can send.
			if (!canSend.containsKey(pkt.header.source)) {
				// Sender can't send.
				return;
			}

			if (!canSend.get(pkt.header.source)) {
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

	private void handleConfig(Packet pkt) {
		// Update permissions.

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
	}

	private void handleData(Packet pkt) {
		// Check access control.

		// Check if sender can send.
		if (!canSend.containsKey(pkt.header.source)) {
			// Sender can't send.
			return;
		}

		if (!canSend.get(pkt.header.source)) {
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
