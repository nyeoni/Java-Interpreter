// TypeChecker.java
// Static type checker for S

public class TypeChecker {

    static TypeEnv tenv = new TypeEnv();

    public static Type Check(Command p) {
        if (p instanceof Decl) {
            Decl d = (Decl) p;
	    if (tenv.contains(d.id))
                error(d, "duplicate variable declaration");
	    else
                return Check(d,tenv);
        }

	    if (p instanceof Function) {
            Function f = (Function) p;
            if (tenv.contains(f.id))
                error(f, "duplicate function definition");
	        else
                return Check(f,tenv);
        }

	    if (p instanceof Stmt)
	        return Check((Stmt) p, tenv);

	    throw new IllegalArgumentException("undefined command");
    }

    public static Type Check(Decl decl, TypeEnv te) {
        if (decl.arraysize == 0 && decl.expr != null)
	    if (decl.type != Check(decl.expr, te))
	        error(decl, "type error: incorrect initialization to " + decl.id);
        te.push(decl.id, decl.type);
        return decl.type;
    }

    public static Type Check(Function f, TypeEnv te) {
	     te.push(f.id, new ProtoType(f.type, f.params));
         for (Decl d : f.params)
             te.push(d.id, d.type);

	     Type t = Check(f.stmt, te); // type check the function body
	     System.out.println(t);
	     if (t != f.type)
	         error(f, "incorrect return type");

         for (Decl d : f.params)
             te.pop();
         te.pop();                   // pop and push prototype type
	     te.push(f.id, new ProtoType(f.type, f.params));
         return f.type;
    }

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    static void error(Command c, String msg) {
        c.type = Type.ERROR;
        System.err.println("\n"+msg);
    }

    static Type Check(Expr e, TypeEnv te) {
        if (e instanceof Value) {
	        Value v = (Value)e;
            return v.type;
	    }

        if (e instanceof Identifier) {
            Identifier id = (Identifier) e;
            if (!te.contains(id))
                error(id, "undeclared variable: " + id);
	        else id.type = te.get(id);
            return id.type;
        }

        if (e instanceof Array) {
            Array ar = (Array) e;
            if (!te.contains(ar.id))
                error(ar, "undeclared variable: " + ar.id);
	        else if (Check(ar.expr, te) == Type.INT)
		        ar.type = te.get(ar.id);
	        else
		        error(ar, "non-int index: " + ar.expr);
            return ar.type;
        }

        if (e instanceof Binary)
            return Check((Binary) e, te);

        if (e instanceof Unary)
            return Check((Unary) e, te);

        if (e instanceof Call)
            return Check((Call) e, te);

        throw new IllegalArgumentException("undefined operator");
    }


    // (1) Binary Type Check Implementation
    static Type Check(Binary b, TypeEnv te) {
        // Binary Implementation
        Type t1 = Check(b.expr1, te);
        Type t2 = Check(b.expr2, te);

        switch (b.op.val) {
            case "+": case "-": case "*": case "/":
                if (t1 == Type.INT && t2 == Type.INT)
                    b.type = Type.INT;
                else error(b, "type error for " + b.op);
                break;
            case "<": case "<=": case ">": case ">=": case "|": case "&": case "!=": case "==":
                if (t1 == t2)
                    b.type = Type.BOOL;
                else error(b, "type error for "+ b.op);
                break;
        }
        return b.type;
    }

    // (2) Unary Type Check Implementation
    static Type Check(Unary u, TypeEnv te) {
        // Unary Implementation
        Type t1 = Check(u.expr, te);

        switch (u.op.val) {
            case "!":
                if (t1 == Type.BOOL)
                    u.type = Type.BOOL;
                else error(u, "! has non-bool operand");
                break;
            case "-": case "+":
                if (t1 == Type.INT)
                    u.type = Type.INT;
                else error(u, "Unary - has non-int operand");
                break;
        }
        return u.type;
    }


    public static Type Check(Stmt s, TypeEnv te) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Empty)
	         return Type.VOID;
        if (s instanceof Assignment)
            return Check((Assignment) s, te);
	    if (s instanceof Read)
            return Check((Read) s, te);
        if (s instanceof Print)
            return Check((Print) s, te);
        if (s instanceof If)
           return Check((If) s, te);
        if (s instanceof While)
           return Check((While) s, te);
        if (s instanceof Stmts)
           return Check((Stmts) s, te);
        if (s instanceof Let)
           return Check((Let) s, te);
        if (s instanceof Call)
           return Check((Call) s, te);
	    if (s instanceof Return)
           return Check((Return) s, te);
	    if (s instanceof Raise)
           return Check((Raise) s, te);
	    if (s instanceof Try)
           return Check((Try) s, te);
        throw new IllegalArgumentException("undefined statement");
    }

    static Type Check(Print p, TypeEnv te) {
        Type t = Check(p.expr,te);
        if (t != Type.ERROR)
	    p.type = Type.VOID;
        else
            error(p, "type error in expr: " + p.expr);
        return p.type;
    }

    static Type Check(Read r, TypeEnv te) {
	    Type t = Check(r.id, te);
        if ( t == Type.INT || t == Type.BOOL || t==Type.STRING)
	        r.type = Type.VOID;
	    else
	        error(r, " undefined variable in read: " + r.id);
        return r.type;
     }

     static Type Check(Return r, TypeEnv te) {
        Type t = Check(r.expr,te);
        if (t == Type.ERROR)
            error(r, "type error in expr: " + r.expr);
        else
	        r.type = t;
        return r.type;
    }

    // (3) Assignment Type Check Implementation
    static Type Check(Assignment a, TypeEnv te) {
        // Assignment Implementation
        if (!te.contains(a.id)) {
            error(a, " undefined variable in assignment: " + a.id);
            return Type.ERROR;
        }
        Type t1 = te.get(a.id);
        Type t2 = Check(a.expr, te);
        if (t1 == t2)
            a.type = Type.VOID;
        else
            error(a, "mixed mode assignment to " + a.id);
        return a.type;
    }

    // (4) If Type Check Implementation
    static Type Check(If c, TypeEnv te) {
    	// If Implementation
        Type t = Check(c.expr, te);
        Type t1 = Check(c.stmt1, te);
        Type t2 = Check(c.stmt2, te);
        if (t == Type.BOOL)
            if (t1 == t2)
                c.type = t1;
            else
                error(c, "non-equal type in two brances");
        else
            error(c, "non-bool test in condition");
        return c.type;
    }

    // (5) While Type Check Implementation
    static Type Check(While l, TypeEnv te) {
        // While Implementation
        Type t = Check(l.expr, te);
        Type t1 = Check(l.stmt, te);
        if (t == Type.BOOL)
            if (t1 == Type.VOID)
                l.type = t1;
            else
                error(l, "return in loop..");
        else
            error(l, "non-bool test in loop");
        return l.type;
    }

    // (6) Stmts Type Check Implementation
    static Type Check(Stmts ss, TypeEnv te) {
	    // Stmts Implementation
        Type t = Type.VOID;
        for (int i = 0; i < ss.stmts.size(); i++) {
            t = Check(ss.stmts.get(i), te);
            if (t != Type.VOID && i != ss.stmts.size() - 1)
                error(ss, "return in Stmts");
        }

        if (ss.type != Type.ERROR) ss.type = t;
        return ss.type;
    }

    // (7) Let Type Check Implementation
    static Type Check(Let l, TypeEnv te) {
        // Let Type Check Implementation
        addType(l.decls, te);
        l.type = Check(l.stmts, te);
        deleteType(l.decls, te);
        return l.type;
    }

    static Type Check(Raise r, TypeEnv te) {
        Type t = Check(r.eid,te);
        if (t == Type.EXC)
	    r.type = Type.VOID;
        else
            error(r, "type error in exception: " + r.eid);
        return r.type;
    }

    static Type Check(Try t, TypeEnv te) {
        Type t0 = Check(t.eid,te);
        if (t0 != Type.EXC)
            error(t, "type error in exception : " + t.eid);

        Type t1 = Check(t.stmt1, te);
        Type t2 = Check(t.stmt2, te);
        if (t1 == Type.VOID && t2 == Type.VOID)
	        t.type = Type.VOID;
        else
            error(t, "type error in try-catch ");

        return t.type;
    }

    static Type Check(Call c, TypeEnv te) {
       if (!te.contains(c.fid)) {
           error(c, "undefined function: " + c.fid);
           return c.type;
       }
       Exprs args = c.args;
       ProtoType p = (ProtoType)te.get(c.fid);
       c.type = p.result;
       // check arguments against the ProtoType
       if (args.size() == p.params.size())
           for (int i=0; i<args.size(); i++) {  // match arg types with param types
                Expr e = (Expr)args.get(i);
                Type t1 = Check(e,te);
                Type t2 = ((Decl)p.params.get(i)).type;
                if (t1 != t2)
                    error(c, "argument type does not match parameter");
           }
       else
           error(c, "do not match numbers of arguments and params");

       return c.type;
    }

    public static TypeEnv addType (Decls ds, TypeEnv te) {
        // put the variable decls into a symbol table(TypeEnv)
        if (ds != null)
        for (Decl decl : ds)
	        Check(decl, te);

        return te;
    }

    public static TypeEnv addType (Decls ds, Functions fs, TypeEnv te) {
        // put the variable decls into a symbol table(TypeEnv)
        if (ds != null)
        for (Decl decl : ds)
	        Check(decl, te);

        if (fs != null)
        for (Function f : fs)
	        Check(f, te);

        return te;
    }

    static TypeEnv deleteType(Decls ds, TypeEnv te) {
        if (ds != null)
        for (Decl decl : ds)
            te.pop();

        return te;
    }

    static TypeEnv deleteType(Decls ds, Functions fs, TypeEnv te) {
        if (ds != null)
        for (Decl decl : ds)
            te.pop();

        if (fs != null)
        for (Function f: fs)
            te.pop();

        return te;
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            Sint sint = new Sint();
            Lexer.interactive = true;
            System.out.println("Begin parsing... ");
            System.out.print(">> ");
            Parser parser  = new Parser(new Lexer());

            do { // Program = Command*
                Command command = null;
                if (parser.token==Token.EOF) {
                    parser.token = parser.lexer.getToken();
                }

                try {
                    command = parser.command();
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                    System.out.print(">> ");
                    continue;
                }

                System.out.println("\nType checking...");
                try {
                    TypeChecker.Check(command);
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                }
                System.out.println("\nCommand type :" + command.type);
                System.out.print(">> ");
            } while(true);
        }
        else {
            Command command = null;
    	    System.out.println("Begin parsing... " + args[0]);
            Parser parser  = new Parser(new Lexer(args[0]));
            do {
			    if (parser.token == Token.EOF)
                    break;

                try {
                    command = parser.command();
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                    continue;
                }

                System.out.println("\nType checking...");
                try {
                    TypeChecker.Check(command);
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                }
                System.out.println("\nCommand type :" + command.type);
            } while (command != null);
        }
    } //main
}