import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;


public class LockFreeHashSet<T> {
	public static int THRESHOLD = 16;
    protected BucketList<T>[] bucket;
    protected AtomicInteger bucketSize;
    protected AtomicInteger setSize;
    public LockFreeHashSet(int capacity) {
        bucket = (BucketList<T>[]) new BucketList[capacity];
        bucket[0] = new BucketList<T>();
        bucketSize = new AtomicInteger(2);
        setSize = new AtomicInteger(0);
    }
    public boolean add(T x) {
        int myBucket = x.hashCode() % bucketSize.get();
        BucketList<T> b = getBucketList(myBucket);
        if (!b.add(x))
            return false;
        int setSizeNow = setSize.getAndIncrement();
        int bucketSizeNow = bucketSize.get();
        if (setSizeNow / bucketSizeNow > THRESHOLD)
            bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);
        return true;
    }
    private BucketList<T> getBucketList(int myBucket) {
        if (bucket[myBucket] == null)
            initializeBucket(myBucket);
        return bucket[myBucket];
    }
    private void initializeBucket(int myBucket) {
        int parent = getParent(myBucket);
        if (bucket[parent] == null)
            initializeBucket(parent);
        BucketList<T> b = bucket[parent].getSentinel(myBucket);
        if (b != null)
            bucket[myBucket] = b;
    }
    private int getParent(int myBucket){
        int parent = bucketSize.get();
        do {
            parent = parent >> 1;
        } while (parent > myBucket);
        parent = myBucket - parent;
        return parent;
    }
}

class BucketList<T>{
    static final int HI_MASK = 0x00800000;
    static final int MASK = 0x00FFFFFF;
    Node head;
    
    public BucketList() {
        head = new Node(0);
        head.next = new AtomicMarkableReference<Node>(new Node(Integer.MAX_VALUE), false);
    }
    
    public BucketList(Node<T> head) {
        this.head = head;
    }
   
    public int makeOrdinaryKey(T x) {
        int code = x.hashCode() & MASK; // take 3 lowest bytes
        return Integer.reverse(code | HI_MASK);
    }
    
    private static int makeSentinelKey(int key) {
        return Integer.reverse(key & MASK);
    }
    
    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Window<T> window = find(head, key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key == key) {
                return false;
            } else {
                Node<T> node = new Node<T>(item);
                node.next = new AtomicMarkableReference<Node>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    return true;
                }
            }
        }
    }
    
    public boolean contains(T x) {
        int key = makeOrdinaryKey(x);
        Window window = find(head, key);
        Node pred = window.pred;
        Node curr = window.curr;
        return (curr.key == key);
    }
    
    public BucketList<T> getSentinel(int index) {
        int key = makeSentinelKey(index);
        boolean splice;
        while (true) {
            Window window = find(head, key);
            Node pred = window.pred;
            Node curr = window.curr;
            if (curr.key == key) {
                return new BucketList<T>(curr);
            } else {
                Node node = new Node(key);
                node.next.set(pred.next.getReference(), false);
                splice = pred.next.compareAndSet(curr, node, false, false);
                if (splice)
                    return new BucketList<T>(node);
                else
                    continue;
            }
        }
    }
    
    public class Window<T> {
        public Node<T> pred, curr;

        public Window(Node<T> myPred, Node<T> myCurr) {
            pred = myPred;
            curr = myCurr;
        }

    }
    
    private Window find(Node<T> head, int key) {
        Node<T> pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        boolean snip;
        retry: while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) continue retry;
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key)
                return new Window(pred, curr);
                pred = curr;
                curr = succ;
            }
        }
    }
   
    private class Node<T> {
		T item;
		int key;
		AtomicMarkableReference<Node> next = new AtomicMarkableReference<Node>(null, false);

		Node(T i) {
			item = i;
			key = i.hashCode();
		}

		// for setting the key manually
		Node(T i, int k) {
			item = i;
			key = k;
		}
	}
}
