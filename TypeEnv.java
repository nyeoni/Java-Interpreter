// TypeEnv.java
// Type environment for S
import java.util.*;

// <id, type>
class Entry {
   Identifier id;
   Type type;

   Entry (Identifier id, Type t) {
     this.id = id;
     this.type = t;
   }
}

class TypeEnv extends Stack<Entry> {
    public TypeEnv() { super(); }

    public TypeEnv(Identifier id, Type t) {
        push(id, t);
    }

    public TypeEnv push(Identifier id, Type t) {
        super.push(new Entry(id, t));
	    return this;
    }

    public int lookup(Identifier v) {
        // Lookup Implementation
        // 스택 탑에서부터 서치하기 위함
        int size = this.size();
        Entry[] entries = new Entry[size];
        entries = this.toArray(entries);

        for (int i = size - 1; i >= 0; i--) {
            if (entries[i].id.equals(v))
                return i;
        }
        return -1;
    }


    // (1) Contatins Function Implementation
    public boolean contains (Identifier v) {
       // Contains Implementation
        if (lookup(v) != -1)
            return true;
        else
            return false;
    }

    // (2) Get Function Implementation
    public Type get (Identifier v) {
        // Get Implementation
        int idx = lookup(v);
        Entry target = this.get(idx);

        return target.type;
    }

    public static void main(String[] args) {
        Identifier id = new Identifier("a");
        Type type = new Type("int");

        TypeEnv env = new TypeEnv();
        env.push(new Entry(id, type));

        for (Entry e : env) {
            System.out.println(e.id);
            System.out.println(e.type);
        }

        Identifier fakeId = new Identifier("fake");
        System.out.println(env.contains(id));
        System.out.println(env.contains(fakeId));

        System.out.println(env.get(id));
        System.out.println(env.get(fakeId));
    }
}