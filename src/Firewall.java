class SerialFirewall {
  public static void main(String[] args) {
    final int numPackets = Integer.parseInt(args[0]);    
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final short experimentNumber = Short.parseShort(args[4]);
    long fingerprint = 0;
    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    Fingerprint residue = new Fingerprint();
    
    if( uniformFlag == true ) {
      timer.startTimer();
      for( int i = 0; i < numSources; i++ ) {
        for( int j = 0; j < numPackets; j++ ) {
          Packet tmp = pkt.getUniformPacket(i);
          fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
        }
      }
      timer.stopTimer();
    }
    else {
      timer.startTimer();
      for( int i = 0; i < numSources; i++ ) {
        for( int j = 0; j < numPackets; j++ ) {
          Packet tmp = pkt.getExponentialPacket(i);
          fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
        }
      }
      timer.stopTimer();
    }
    System.out.println(timer.getElapsedTime());
  }
}


class SerialQueueFirewall {
  public static void main(String[] args) {
    final int numPackets = Integer.parseInt(args[0]);    
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final int queueDepth = Integer.parseInt(args[4]);
    final short experimentNumber = Short.parseShort(args[5]);
    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    Fingerprint residue = new Fingerprint();
    // ...
    // allocate and initialize bank of numSources Lamport queues
    // each with depth queueDepth
    // they should throw FullException and EmptyException upon those conditions
    // ...
        
    long fingerprint = 0;
    timer.startTimer();
    for( int i = 0; i < numSources; i++ ) {
      for( int j = 0; j < numPackets; j++ ) {
        Packet tmp;
        if( uniformFlag == true )
          tmp = pkt.getUniformPacket(i);
        else
          tmp = pkt.getExponentialPacket(i);
        try {
          // ...
          // enqueue tmp in the ith Lamport queue
          // ...
        } catch (FullException e) {;}
        try {
          // ...
          // dequeue the next packet from the ith Lamport queue into tmp
          // ...
        } catch (EmptyException e) {;}
        fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
      }
    }
    timer.stopTimer();
    System.out.println(timer.getElapsedTime());
  }
}


class ParallelFirewall {
  public static void main(String[] args) {
    final int numPackets = Integer.parseInt(args[0]);    
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final int queueDepth = Integer.parseInt(args[4]);
    final short experimentNumber = Short.parseShort(args[5]);
    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    // ...
    // Allocate and initialize bank of Lamport queues, as in SerialQueueFirewall
    // ...
    // Allocate and initialize a Dispatcher class implementing Runnable
    // and a corresponding Dispatcher Thread
    // ...
    // Allocate and initialize an array of Worker classes, implementing Runnable
    // and the corresponding Worker Threads
    // ...
    // Call start() for each worker
    // ...
    timer.startTimer();
    // ...
    // Call start() for the Dispatcher thread
    // ...
    // Call join() for Dispatcher thread
    // ...
    // Call join() for each Worker thread
    // ...
    timer.stopTimer();
    System.out.println(timer.getElapsedTime());
  }
}