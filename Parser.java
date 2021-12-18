// Parser.java
// Parser for language S

public class Parser {
    Token token;          // current token 
    Lexer lexer;
    String funId = "";

    public Parser(Lexer scan) { 
        lexer = scan;		  
        token = lexer.getToken(); // get the first token
    }
  
    private String match(Token t) {
        String value = token.value();
        if (token == t)
            token = lexer.getToken();
        else
            error(t);
        return value;
    }

    private void error(Token tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token=lexer.getToken();
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token=lexer.getToken();
    }
  
    public Command command() {
    // <command> ->  <decl> | <function> | <stmt>
	    if (isType()) {
	        Decl d = decl();
	        return d;
	    }
	    if (token == Token.FUN) {
	        Function f = function();
	        return f;
	    }
	    if (token != Token.EOF) {
	        Stmt s = stmt();
            return s;
	    }
	    return null;
    }

    private Decl decl() {
    // <decl>  -> <type> id [=<expr>]; 
        Type t = type();
	    String id = match(Token.ID);
	    Decl d = null;
	    if (token == Token.LBRACKET) { // array
           match(Token.LBRACKET);
           Value v = literal(); 
	       d = new Decl(id, t, v.intValue());
           match(Token.RBRACKET);
        }
	    else if (token == Token.ASSIGN) {
	        match(Token.ASSIGN);
            Expr e = expr();
	        d = new Decl(id, t, e);
	    } else d = new Decl(id, t);

        match(Token.SEMICOLON);
        return d;
    }

    private Decls decls () {
    // <decls> -> {<decl>}
        Decls ds = new Decls ();
	    while (isType()) {
	        Decl d = decl();
	        ds.add(d);
	    }
        return ds;             
    }

    private Functions functions () {
    // <functions> -> { <function> }
	    Functions fs = new Functions();  
        while (token == Token.FUN) {
	       Function f = function(); 
	       fs.add(f);
        }  
        return fs;            	
    }

    private Function function() {
    // <function>  -> fun <type> id(<params>) <stmt> 
	    match(Token.FUN);
	    Type t = type();
	    String str = match(Token.ID);
	    funId = str; 
	    Function f = new Function(str, t);
	    match(Token.LPAREN);
	    if (token != Token.RPAREN)
            f.params = params();
	    match(Token.RPAREN);
	    Stmt s = stmt();		
	    f.stmt = s;
	    return f;
    }

    private Decls params() {
	    Decls params = new Decls();
	    Type t = type();
        String id = match(Token.ID);
	    params.add(new Decl(id, t));
        while (token == Token.COMMA) {
	        match(Token.COMMA);
	        t = type();
            id = match(Token.ID);
            params.add(new Decl(id, t));
        }
        return params;
    }

    private Type type() {
    // <type>  ->  int | bool | void | string | exc
        Type t = null;
        switch (token) {
	    case INT:
            t = Type.INT; break;
        case BOOL:
            t = Type.BOOL; break;
        case VOID:
            t = Type.VOID; break;
        case STRING:
            t = Type.STRING; break;
        case EXC:
            t = Type.EXC; break;
        default:
	        error("int | bool | void | string | exc");
	    }
        match(token);
        return t;       
    }
  
    private Stmt stmt() {
    // <stmt> -> <block> | <assignment> | <ifStmt> | <whileStmt> | ...
        Stmt s = new Empty();
        switch (token) {
	    case SEMICOLON:
            match(token.SEMICOLON); return s;
        case LBRACE:			
	        match(Token.LBRACE);		
            s = stmts();
            match(Token.RBRACE);	
	        return s;
        case IF: 	// if statement 
            s = ifStmt(); return s;
        case WHILE:      // while statement 
            s = whileStmt(); return s;
        case DO:      // do statement 
            s = doStmt(); return s;
        case FOR:      // for statement (for implementation)
            s = forStmt(); return s;
        case ID:	// assignment
            s = assignment(); return s;
	    case LET:	// let statement 
            s = letStmt(); return s;
	    case READ:	// read statement 
            s = readStmt(); return s;
	    case PRINT:	// print statment 
            s = printStmt(); return s;
	    case RETURN:	// return statement 
            s = returnStmt(); return s;
	    case TRY:	// try statement 
            s = tryStmt(); return s;
	    case RAISE:	// raise statement 
            s = raiseStmt(); return s;
        default:  
	        error("Illegal stmt"); return null; 
	    }
    }
  
    private Stmts stmts () {
    // <stmts> -> {<stmt>}
        Stmts ss = new Stmts();
	    while((token != Token.RBRACE) && (token != Token.END))
	        ss.stmts.add(stmt()); 
        return ss;
    }

    private For forStmt() {
        match(Token.FOR);
        match(Token.LPAREN);
        Decl decl = decl();
        match(Token.SEMICOLON);
        Expr expr = expr();
        match(Token.SEMICOLON);
        Stmt assignment = assignment();
        match(Token.RPAREN);
        Stmt stmt = stmt();
        return new For(decl, expr, (Assignment) assignment, stmt);
    }

    
    //(6) Let Parser Implementation
    private Let letStmt () {
    	// <letStmt> -> let <decls> in <block> end
    	// Let Implementation
        match(Token.LET);
        Decls ds = decls();
        match(Token.IN);
        Stmts ss = stmts();
        match(Token.END);
        match(Token.SEMICOLON);
        return new Let(ds, null, ss);
    }

	//(7) Read Parser Implementation
    private Read readStmt() {
    	// <readStmt> -> read id;
    	// Read Implementation
        match(Token.READ);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.SEMICOLON);
        return new Read(id);
    }

	//(8) Print Parser Implementation
    private Print printStmt() {
    	// <printStmt> -> print <expr>;
    	// Print Implementation
        match(Token.PRINT);
        Expr expr = expr();
        match(Token.SEMICOLON);
        return new Print(expr);
    }

    private Return returnStmt() {
    // <returnStmt> -> return <expr>; 
        match(Token.RETURN);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Return(funId, e);
    }

    
	// (3) Assignment Parser Implementation
    private Stmt assignment() {
    	// <assignment> -> id = <expr>;   
    	// Assignment Implementation
        Identifier id = new Identifier(match(Token.ID));
        match(Token.ASSIGN);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Assignment(id, e);
    }
    

    private Call call(Identifier id) {
    // <call> -> id(<expr>{,<expr>});
	    match(Token.LPAREN);
        Call c = new Call(id, arguments());
        match(Token.RPAREN);
	    match(Token.SEMICOLON);
        return c;
    }

    
	// (4) If Parser Implementation
    private If ifStmt () {
    	// <ifStmt> -> if (<expr>) then <stmt> [else <stmt>]
    	// If Implementation
        match(Token.IF);
        match(Token.LPAREN);
        Expr e = expr();
        match(Token.RPAREN);
        match(Token.THEN);
        Stmt s1 = stmt();
        Stmt s2 = new Empty();
        if (token == Token.ELSE) {
            match(Token.ELSE);
            s2 = stmt();
        }
        return new If(e, s1, s2);
    }

	// (5) While Parser Implementation
    private While whileStmt () {
    	// <whileStmt> -> while (<expr>) <stmt>
    	// While Implementation
        match(Token.WHILE);
        match(Token.LPAREN);
        Expr e = expr();
        match(Token.RPAREN);
        Stmt s = stmt();
        return new While(e, s);
    }
    
    private Stmts doStmt() {
    // <whileStmt> -> do <stmt> while (<expr>) 
        match(Token.DO);
        Stmt s = stmt();
        match(Token.WHILE);
        match(Token.LPAREN);
        Expr e = expr();
        match(Token.RPAREN);
        Stmts ss = new Stmts(s); 
        ss.stmts.add(new While(e, s));
        return ss;
    }

    private Try tryStmt () {
    // <tryStmt> -> try <stmt> {catch(id) <stmt>}
        match(Token.TRY);
        Stmt s1 = stmt(); 
        match(Token.CATCH);
        match(Token.LPAREN);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.RPAREN);
        Stmt s2 = stmt();
        s1 = new Try(id, s1, s2);
        while (token == Token.CATCH) {
            match(Token.CATCH);
            match(Token.LPAREN);
            id = new Identifier(match(Token.ID));
            match(Token.RPAREN);
            s2 = stmt();
            s1 = new Try(id, s1, s2);
        }
        return  (Try) s1; // new Try(id, s1, s2);
    }

    private Raise raiseStmt() {
    // <raiseStmt> -> raise id; 
        match(Token.RAISE);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.SEMICOLON);
        return new Raise(id); 
    }

    private Expr expr () {
    // <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false
        switch (token) {
	    case NOT:
	        Operator op = new Operator(match(token));
	        Expr e = expr();
            return new Unary(op, e);
        case TRUE:
            match(Token.TRUE);
            return new Value(true);
        case FALSE:
            match(Token.FALSE);
            return new Value(false);
        }

        Expr e = bexp();
        while (token == Token.AND || token == Token.OR) {
            Operator op = new Operator(match(token));
            Expr b = bexp();
            e = new Binary(op, e, b);
        }
        return e;
    }

    private Expr bexp() {
        // <bexp> -> <aexp> [ (< | <= | > | >= | == | !=) <aexp> ]
        Expr e = aexp();
        switch(token) {
        case LT: case LTEQ: case GT: case GTEQ: case EQUAL: case NOTEQ:
            Operator op = new Operator(match(token));
            Expr a = aexp();
            e = new Binary(op, e, a);
        }
        return e;
    }
  
    
    // (1) Expr Implementation 1 (aexp -> "+" & "-")
    private Expr aexp () {
        // <aexp> -> <term> { + <term> | - <term> }
        // aexp implementation
        Expr e = term();
        while (token == Token.PLUS || token == Token.MINUS) {
            Operator op = new Operator(match(token));
            Expr t = term();
            e = new Binary(op, e, t);
        }
        return e;
    }
  
    // (2) Expr Implementation 2 (term -> "*" & "/")
    private Expr term () {
        // <term> -> <factor> { * <factor> | / <factor>}
        // term Implementation
        Expr t = factor();
        while (token == Token.MULTIPLY || token == Token.DIVIDE) {
            Operator op = new Operator(match(token));
            Expr f = factor();
            t = new Binary(op, t, f);
        }
        return t;
    }
    
  
    private Expr factor() {
        // <factor> -> [-](id | <call> | literal | '('<aexp> ')')
        Operator op = null;
        if (token == Token.MINUS) 
            op = new Operator(match(Token.MINUS));

        Expr e = null;
        switch(token) {
        case ID:
            Identifier v = new Identifier(match(Token.ID));
            e = v;
            if (token == Token.LPAREN) {  // function call
                match(Token.LPAREN); 
                Call c = new Call(v,arguments());
                match(Token.RPAREN);
                e = c;
            } else if (token == Token.LBRACKET) {  // array 
                match(Token.LBRACKET); 
                Array a = new Array(v,expr());
                match(Token.RBRACKET);
                e = a;
            }
            break;
        case NUMBER: case STRLITERAL: 
            e = literal();
            break; 
        case LPAREN: 
            match(Token.LPAREN); 
            e = aexp();       
            match(Token.RPAREN);
            break; 
        default: 
            error("Identifier | Literal"); 
        }

        if (op != null)
            return new Unary(op, e);
        else return e;
    }
  
    private Exprs arguments() {
    // arguments -> [ <expr> {, <expr> } ]
        Exprs es = new Exprs();
        while (token != Token.RPAREN) {
            es.add(expr());
            if (token == Token.COMMA)
                match(Token.COMMA);
            else if (token != Token.RPAREN)
                error("Exprs");
        }  
        return es;  
    }

    private Value literal( ) {
        String s = null;
        switch (token) {
        case NUMBER:
            s = match(Token.NUMBER);
            return new Value(Integer.parseInt(s));
        case STRLITERAL:
            s = match(Token.STRLITERAL);
            return new Value(s);
        }
        throw new IllegalArgumentException( "no literal");
    }
 
    private boolean isType( ) {
        switch(token) {
        case INT: case BOOL: case STRING:  case EXC: 
            return true;
        default: 
            return false;
        }
    }
    
    public static void main(String args[]) {
	    Parser parser;
	    if (args.length == 0) {
            System.out.println("Begin parsing... ");
	        System.out.print(">> ");
	        Lexer.interactive = true;
	        parser  = new Parser(new Lexer());
	        do {
	            if (parser.token == Token.EOF) {
		            parser.token = parser.lexer.getToken();
		        }
		        Command command = null;
                try {
                    command = parser.command();
                } catch (Exception e) {
                    System.err.println(e);
                }
		        System.out.print("\n>> ");
	        } while(true);
	    }
    	else {
	        System.out.println("Begin parsing... " + args[0]);
	        parser  = new Parser(new Lexer(args[0]));
	        Command command = null;
	        do {
			    if (parser.token == Token.EOF)
                    break;

                try {
		            command = parser.command();
                } catch (Exception e) {
                    System.err.println(e);
                }
	        } while (command != null);
	    }
    } //main
} // Parser