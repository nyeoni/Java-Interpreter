import java.util.*;
// State as stack 

// <id, val> 
class Pair {
    Identifier id;
    Value val;

    Pair(Identifier id, Value v) {
        this.id = id;
        this.val = v;
    }
}

class State extends Stack<Pair> {
    public State() { super(); }

    public State(Identifier id, Value val) {
        push(id, val);
    }

    // (1) Push function Implementation
    public State push(Identifier id, Value val) {
        // Push Implementation
        // System.out.println("State push() : "+ id +" "+ val);
        Pair pair = new Pair(id, val);
        super.push(pair);
        return this;
    }

    // (2) Pop function Implementation (Optional)
    public Pair pop() {
        // Pop Implementation (Optional)
        Pair pair = super.pop();
        // System.out.println("State pop()");
        return pair;
    }

    // (3) Lookup function Implementation
    public int lookup(Identifier v) {
        // Lookup Implementation
        // 스택 탑에서부터 서치하기 위함
        int size = this.size();
        Pair[] pairs = new Pair[size];
        pairs = this.toArray(pairs);

        for (int i = size - 1; i >= 0; i--) {
            if (pairs[i].id.equals(v))
                return i;
        }
        return -1;
    }

    // (4) Set Function Implementation
    public State set(Identifier id, Value val) {
    	// Set Implementation
        // System.out.println("State set() : "+ id +" "+ val);
        int idx = lookup(id);
        Pair newPair = new Pair(id, val);
        this.set(idx, newPair);
        return this;
    }

    // (5) Get Function Implementation
    public Value get (Identifier id) {
    	// Get Implementation
        // System.out.println("State get() : "+ id);
        int idx = lookup(id);
        Pair target = this.get(idx);
        // System.out.println(target.val.getClass());

        return target.val;
    }

    public static void main(String[] args) {
        Identifier id = new Identifier("a");
        Value val = new Value(1);
        Identifier id2 = new Identifier("b");
        Value val2 = new Value(2);

        State state = new State(id, val);
        state.push(id2, val2);

        // state : 값이 스택에 잘 들어갔는지 확인
        for (Pair p : state) {
            System.out.println(p.id);
            System.out.println(p.val);
        }

        // state.lookup() : 존재하지 않는 id 값 -1 리턴
        Identifier id3 = new Identifier("c");
        System.out.println(state.lookup(id3));

        // state.set() : id 의 값에 새로운 값 set test
        Value newVal = new Value(7);
        state.set(id, newVal);
        for (Pair p : state) {
            System.out.println(p.id);
            System.out.println(p.val);
        }

        // state.get() : value 잘 가지고 오는지 확인
        System.out.println("state.get() test");
        System.out.println(state.get(id));
    }
}