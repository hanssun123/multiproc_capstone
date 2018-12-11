import java.io.FileWriter;
import java.io.PrintWriter;
import map.MyMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

class Main {
	public static void main(String[] args) {		
		
		/*
		ParallelFirewallTester parallelTester = new ParallelFirewallTester();
		SerialFirewallTester serialTester = new SerialFirewallTester();
		PacketGenerator currGenerator;
		MyMap<Integer, Boolean> currCanSend;
		MyMap<Integer, Set<Integer>> currCanRecvFrom;
		currGenerator = generatePG(3);
		currCanSend = generateCanSend(1);
		currCanRecvFrom = generateCanReceiveFrom(1);
		parallelTester.test(10000000, 4, currCanSend, currCanRecvFrom, currGenerator);
		currGenerator = generatePG(3);
		serialTester.test(10000000, currGenerator);
		*/
		
		
		StopWatch stopwatch = new StopWatch();
		stopwatch.startTimer();
		
		// int numPG = 8;
		int numThreadTypes = 5; // 1, 2, 4, 8, 16
		int numMaps = 6; // Change to 6.
		int numPackets = 1000000;
		int numTrials = 5;
		
		SerialFirewallTester serialTester = new SerialFirewallTester();
		ParallelFirewallTester parallelTester = new ParallelFirewallTester();
		
		PacketGenerator currGenerator;
		MyMap<Integer, Boolean> currCanSend;
		MyMap<Integer, Set<Integer>> currCanRecvFrom;
		
		// List of rows to print to file later on.
		List<String> rows = new ArrayList<>();
		rows.add(getHeader());
		
		int pgIter = 3;
		//for (int pgIter = 0; pgIter < numPG; pgIter++) {
			for (int mapIter = 0; mapIter < numMaps; mapIter++) {
				System.out.println(mapIter + " of " + numMaps);
				
				if(mapIter == numMaps-1) {
					// Run sequential
					long totalTime = 0;
					for (int trial = 0; trial < numTrials; trial++) {
						long time = serialTester.test(numPackets, generatePG(pgIter));
						totalTime += time;
					}
					// Finished trial.
					long avgTime = totalTime / numTrials;
					rows.add(infoToRow(pgIter, "serial", 1, numPackets, avgTime));
				
				} else {
					// Run parallel
					for (int numThreadsIter = 0; numThreadsIter < numThreadTypes; numThreadsIter++) {
						long totalTime = 0;
						for (int trial = 0; trial < numTrials; trial++) {
							currGenerator = generatePG(pgIter);
							currCanSend = generateCanSend(mapIter);
							currCanRecvFrom = generateCanReceiveFrom(mapIter);
							// Instantiate firewalls.
							long time = parallelTester.test(numPackets,
															getNumThreads(numThreadsIter), 
															currCanSend,
															currCanRecvFrom,
															currGenerator);
							totalTime += time;
						}
						// Finished trial.
						long avgTime = totalTime / numTrials;
						rows.add(infoToRow(pgIter, getMapType(mapIter), getNumThreads(numThreadsIter), numPackets, avgTime));
					}
				}
			}
			rows.add("-,-,-,-,- \n");
		//}

		try {

			FileWriter fileWriter = new FileWriter("/Users/tristinfalk-lefay/Desktop/testfile" + pgIter + ".csv");
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for(String row : rows) {
				printWriter.print(row);
			}
			printWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		stopwatch.stopTimer();
		System.out.println("Total time to process: " + stopwatch.getElapsedTime());
		// parallel.test(numPackets);
	}

	private static String infoToRow(int pgType, String mapType, int numThreads, int numPackets, long time) {
		return "" + pgType + ", " + mapType + ", " + numThreads + ", " + numPackets + ", " + time + "\n";
	}

	private static String getHeader() {
		return "pg_type, map_type, num_threads, num_packets, time \n";
	}

	private static MyMap<Integer, Boolean> generateCanSend(int iteration) {
		switch (iteration) {
		case 0:
			return new bookMaps.CoarseHashMap<Integer, Boolean>(1000);
		case 1:
			return new bookMaps.StripedHashMap<Integer, Boolean>(1000);
		case 2:
			return new javaMaps.CoarseGrainedMap<Integer, Boolean>();
		case 3:
			return new javaMaps.ConcurrentMap<Integer, Boolean>();
		case 4:
			return new javaMaps.NonBlockingFastMap<Integer, Boolean>();
		default:
			// Do nothing;
		}
		return null;
	}

	private static MyMap<Integer, Set<Integer>> generateCanReceiveFrom(int iteration) {
		switch (iteration) {
		case 0:
			return new bookMaps.CoarseHashMap<Integer, Set<Integer>>(1000);
		case 1:
			return new bookMaps.StripedHashMap<Integer, Set<Integer>>(1000);
		case 2:
			return new javaMaps.CoarseGrainedMap<Integer, Set<Integer>>();
		case 3:
			return new javaMaps.ConcurrentMap<Integer, Set<Integer>>();
		case 4:
			return new javaMaps.NonBlockingFastMap<Integer, Set<Integer>>();
		default:
			// Do nothing;
		}
		return null;
	}

	private static String getMapType(int iteration) {
		switch (iteration) {
		case 0:
			return "book_coarse";
		case 1:
			return "book_striped";
		case 2:
			return "java_coarse";
		case 3:
			return "java_concurrent";
		case 4:
			return "github_nonblocking";
		default:
			return "Unknown";
		}
	}

	private static PacketGenerator generatePG(int iteration) {
		switch (iteration) {
		case 0:
			return new PacketGenerator(4, 12, 5, 1, 3, 3, 6000, 0.24, 0.04, 0.96);
		case 1:
			return new PacketGenerator(4, 10, 1, 3, 3, 1, 4000, 0.11, 0.09, 0.92);
		case 2:
			return new PacketGenerator(4, 10, 4, 3, 6, 2, 5000, 0.1, 0.03, 0.9);
		case 3:
			return new PacketGenerator(4, 10, 5, 5, 6, 2, 1000, 0.08, 0.05, 0.9);
		case 4:
			return new PacketGenerator(5, 14, 9, 16, 7, 10, 8000, 0.02, 0.1, 0.84);
		case 5:
			return new PacketGenerator(5, 15, 9, 10, 9, 9, 10000, 0.01, 0.2, 0.77);
		case 6:
			return new PacketGenerator(5, 15, 10, 13, 8, 10, 9000, 0.04, 0.18, 0.8);
		case 7:
			return new PacketGenerator(6, 14, 15, 12, 9, 5, 12000, 0.04, 0.19, 0.76);
		default:
			// Do nothing;
		}
		return null;
	}

	private static int getNumThreads(int iteration) {
		switch (iteration) {
		case 0:
			return 1;
		case 1:
			return 2;
		case 2:
			return 4;
		case 3:
			return 8;
		case 4:
			return 16;
		default:
			return 1;
		}
	}
}

class SerialFirewallTester {
	public long test(int numPackets, PacketGenerator gen) {
		System.out.println("Running Serial Firewall Tester");
		StopWatch stopwatch = new StopWatch();
		stopwatch.startTimer();
		

		SerialFirewall firewall = new SerialFirewall();

		// Process config packets first.
		int numLogAddress = 6;
		int A = (int) Math.pow(numLogAddress, 1.5);

		
		for (int i = 0; i < A; i++) {
			Packet pkt = gen.getConfigPacket();
			firewall.handlePacket(pkt);
		}
		
		
		for (int i = 0; i < numPackets; i++) {
			Packet pkt = gen.getPacket();
			firewall.handlePacket(pkt);
		}

		stopwatch.stopTimer();
		System.out.println("Time to process: " + stopwatch.getElapsedTime());
		return stopwatch.getElapsedTime();
	}
}

class ParallelFirewallTester {
	public long test(int numPackets, int numThreads, MyMap<Integer, Boolean> canSend,
			MyMap<Integer, Set<Integer>> canReceiveFrom, PacketGenerator gen) {
		System.out.println("Running Parallel Firewall Tester");
		StopWatch stopwatch = new StopWatch();
		stopwatch.startTimer();

		int numLogAddress = 6;
		ParallelFirewallPools firewall = new ParallelFirewallPools(numThreads, canSend, canReceiveFrom);

		// Process config packets first.
		int A = (int) Math.pow(numLogAddress, 1.5);

		for (int i = 0; i < A; i++) {
			Packet pkt = gen.getConfigPacket();
			firewall.handlePacket(pkt);
		}
		
		
		for (int i = 0; i < numPackets; i++) {
			Packet pkt = gen.getPacket();
			firewall.handlePacket(pkt);
		}
		
		
		firewall.shutdown();
		stopwatch.stopTimer();
		
		System.out.println("Total time to process: " + stopwatch.getElapsedTime());
		return stopwatch.getElapsedTime();
	}
}