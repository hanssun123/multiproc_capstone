import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class SerialFirewall {
	
	private Map<Integer, Boolean> canSend;
	private Map<Integer, Set<Integer>> canReceiveFrom;
	private Map<Long, Integer> histogram;
	
	public SerialFirewall() {
		// Instantiate permissions.
		this.canSend = new HashMap<>(); // PNG.
		this.canReceiveFrom = new HashMap<>(); // R.
		this.histogram = new HashMap<>();
	}
	
	
	// Dispatcher Thread	
	public void handlePacket(Packet pkt) {
		// Distribute Work:
		switch(pkt.type) {
		case ConfigPacket:
			handleConfigPacket(pkt);
			break;
		case DataPacket:
			handleDataPacket(pkt);
			break;
		}
	}
	
	// Make this a thread pool.
	private void handleConfigPacket(Packet pkt) {
		// Update permissions.
		// System.out.println("Handling config packet for address: " + pkt.config.address);
		
		// Update canSend permissions with personaNonGrata.
		this.canSend.put(pkt.config.address, pkt.config.personaNonGrata);
		
		// Update canReceiveFrom permissions.
		Set<Integer> oldPermissions = this.canReceiveFrom.get(pkt.config.address);
		
		if(oldPermissions == null) {
			oldPermissions = new HashSet<Integer>();
		}
		
		for(int i = pkt.config.addressBegin; i < pkt.config.addressEnd; i++) {
			if(pkt.config.acceptingRange) {
				oldPermissions.add(i);
			} else {
				oldPermissions.remove(i);
			}
		}
		this.canReceiveFrom.put(pkt.config.address, oldPermissions);
		// System.out.println("Sender can send: " + pkt.config.personaNonGrata);
		// System.out.println("Can receive from permissions: " + oldPermissions.toString());
	}
	
	
	// Make this a thread pool.
	private void handleDataPacket(Packet pkt) {
		// Check access control.
		
		// Check if sender can send.
		if(!this.canSend.containsKey(pkt.header.source) || 
		   !this.canSend.get(pkt.header.source)) {
			// Sender can't send.
			return;
		}
		
		// Check if receiver can receive from this sender.
		if(!this.canReceiveFrom.containsKey(pkt.header.dest) ||
		   !this.canReceiveFrom.get(pkt.header.dest).contains(pkt.header.source)) {
			// Receiver cannot receive from this sender.
			return;
		}
		
		// --- Successfully passed through permissions check! --- 
		// System.out.println("Passed permissions!");
		
		// Histogram the checksum.
		Fingerprint fingerprint = new Fingerprint();
		long checksum = fingerprint.getFingerprint(pkt.body.iterations, pkt.body.seed);
		
		if(!this.histogram.containsKey(checksum)) {
			this.histogram.put(checksum, 0);
		}
		
		int oldCount = this.histogram.get(checksum);
		this.histogram.put(checksum, oldCount+1);
	}
	
	public Map<Long, Integer> getHistogram() {
		return this.histogram;
	}
	
	
}
