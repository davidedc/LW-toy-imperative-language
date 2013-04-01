import java.io.* ;
import java.util.*;

// The main class un-ravels the invokation parameters,
// tokenises the program and the grammar files.
// Stx.decode() then turns the tokenised grammar into a parsing (hash)table and
// returns the root terminal of the grammar. The Parser takes such terminal and
// builds a parse tree. The parse tree is used for both the preliminary
// scope checks and then for the running of the program.
// This program works with both the provided LW and LWPlus grammars.

class  LW {
    private static int MaxMem ;

    public static int MaxMem() {
        return MaxMem;
    }

	// Invokation is like this
	// java LW grammarFile programFile maxMemory dataFile
    public static void  main(String[] arg) {
        try {
            // tokenizes the program
            (new Scanner()).tokenize(arg[1],PrgLst.GimmeList());
            MaxMem = Integer.parseInt(arg[2]);
            
            // tokenizes the input data
            Vector data = new Vector();
            (new Scanner()).tokenize(arg[3],data);
            
            // parses the tokenised program using the grammar file
            // passed in the first argument.
            ASTNode temp=(new Parser()).parse(Stx.decode(arg[0]),0,0,false);
            if (temp == null){
                System.out.println("...program doesn't obey syntax." );
                throw new Exception();
            }
            // checks any problems with scoping
            // (e.g. using undeclared variables)
            temp.StaticCheck(new Vector());

            // runs the program
            temp.evaluate(new State(new Vector(),data,new Vector()));
        }
        catch (NumberFormatException e){
            System.out.println("Param.Error");
        }
        catch (Exception e){
            System.out.println("Sorry.");
            System.out.println("Usage: java LW grammarFileName progSourceName maxMem dataFile");
        }
    }
}

// Contains the tokenised program (created by) Scanner().tokenize and serves tokens
// to the Parser.
class  PrgLst {
    private static Vector Lst = new Vector();
    public static Vector GimmeList() {
        return Lst ;
    }
    public static String get(int i) {
        return (String)(Lst.elementAt(i));
    }
}

// simple tokeniser. Uses the standard java StreamTokenizer. This is used
// for all the files: the program source, the grammar, and the data.
class Scanner {
    public void tokenize(String fileName,Vector vector) throws Exception{
        BufferedReader file = null;
        try  {
            file = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " not found.");
            throw new Exception();
        }
        StreamTokenizer st = new StreamTokenizer(file);
        st.resetSyntax();
        st.wordChars('\u0000','\u00FF');
        st.whitespaceChars(0,' ');
        try  {
            for (int i = st.nextToken();
            i != st.TT_EOF;
            i = st.nextToken())
            vector.addElement(st.sval);
        }
        catch (IOException e) {
            System.out.println("I/O failure.");
            throw new Exception();
        }
    }
}

// Each Element represents an element of the grammar. Elements are
// stored in a parse (hash)table inside the Stx class.
// For example, the Parser might ask what the second
// token necessary for the "Coms" non-terminal as specified by the
// grammar. The Element response will contain ":=" as name, and the
// booleans will indicate that it's a terminal which is optional and
// non-concatenated.

class Element {
    private boolean terminal, Stk, Opt;
    private String Name;
    public Element (String a,boolean b, boolean e, boolean f){
        terminal = b;
        Name = a;
        Stk = e;
        Opt = f;
    }
    public boolean terminal() {
        return terminal;
    }
    public boolean Stk()  {
        return Stk ;
    }
    public boolean Opt()  {
        return Opt ;
    }
    public String Name() {
        return Name ;
    }
}

// Stx stands for Syntax. It could have been called grammar,
// really...
class Stx {
    // this hashtable is the data structure where
    // the grammar is stored.
    private static Hashtable table = new Hashtable();
    private static String Purify(String a, String b) {
        return (a.replace(b.charAt(0),' ')).trim();
    }
    public static Element  NeedEl(String a,int b, int c) {
        return (Element)(table.get(a+"/"+b+"/"+c));
    }

    // parses the grammar and creates the "Elements".

// For each token in the grammar, it determines the attributes
// (i.e. concatenated, terminal/non-terminal, optional)
// and stores those in a HashTable that is indexed by
// the non-terminals of the grammar.
// The attributes of each token are determined via special symbols
// (defined in the first 4 tokens in the grammar file)
// In the sample grammars for LW and LWPlus, non-terminals are
// followed by ":". Optional elements are followed by "~".
// Terminal tokens start with "#".
// All these pre and post-fixed characters must be stripped
// out before the proper Element is created and stored in the
// parse (hash)table.
// The parse (hash)table is indexed by a string built as
// follows "nonterminal string/derivation number/token number".
// For example,
//    Coms: Com #; Coms | Com 
// will have four entries in the parse table, indexed as follows
// (left to right)
//     "Coms/1/1", for "Com, "indexing a nonterminal nonoptional nonconcatenated Element
//     "Coms/1/2", for the ";", indexing a terminal nonoptional nonconcatenated Element
//     "Coms/1/3", for "Coms", indexing a nonterminal nonoptional nonconcatenated Element
//     "Coms/2/1", for the Com of the second derivation, (nonterminal nonopzional nonconcatenated)
// Finally, decode() returns the main nonterminal of the grammar, as string.

    public static String decode(String file) throws Exception {
        Vector Lst = new Vector()  ;
        (new Scanner()).tokenize(file,Lst);
        boolean Stk = false, IsTerm = false, Opt = false;
        int CurAlt = 1 , NumTok = 0 ;
        String CurDef = "" ;
        // the first few tokens are special
        String NewDef = (String)(Lst.elementAt(0));
        String NewAlt = (String)(Lst.elementAt(1));
        String Stick = (String)(Lst.elementAt(2));
        String Term  = (String)(Lst.elementAt(3));
        String OptS  = (String)(Lst.elementAt(4));
        // Lst.elementAt(5) is the root element
        for (int i=6; i < Lst.size(); i++) {
            String CurTok = (String)(Lst.elementAt(i));
            if (CurTok.endsWith(NewDef)){
                CurDef = Purify(CurTok,NewDef);
                CurAlt = 1;
                NumTok = 0;
                continue;
            }
            if (CurTok.equals(NewAlt)) {
                CurAlt++ ;
                NumTok = 0;
                continue;
            }
            if (CurTok.equals(Stick) ) {
                Stk=true ;
                continue ;
            }
            NumTok++;
            table.put(CurDef + "/" + CurAlt + "/" + NumTok ,
            new Element(Purify( Purify(CurTok,Term ) ,OptS ),
            CurTok.startsWith(Term),
            Stk,
            CurTok.endsWith(OptS)));
            Stk = false;
        }
        // returns the root element
        return (String)(Lst.elementAt(5));
    }
}

// Manages the state of a program for a particular "scope frame".
// Also it contains results of expressions. Expressions don't
// change the state, but it's more convenient to keep State and
// intermediate values in the same object, so each node in
// the parse tree can just take and receive a State object.
// When you go "deeper" in the scope (i.e. inside a "with...endw"
// block, this is what you want: variables from the outer scope
// are still accessible and modifiable, while the new variable
// shields any other outer variables with the same name.
// This is how it's implemented: the state of the outer scope is
// copied over for the new "inner scope". The new variable is added
// to this new inner scope copy (or it replaces the previously
// existing one). At the end of the scope, we
// pass back to the old scope the new "inner scope" state BUT before
// doing that we either remove the new scope variable, or
// we copy over the old value of the inner scope variable
// from the old scope into this "inner scope" state.
// So, the effect is that the outer cope is changed only in regards
// to the old outer scope variables and not in regards to the new
// inner scope variables.
// "string" contains the name of the variable, while "val" the value.
// Also note that each variable is born as "uninitialised",
// and needs to be initialised before being used (which can't be
// checked statically).

class State {
    private String string ;
    private Vector Mem ;
    private int val ;
    private Vector Input  ;
    private Vector Output ;
    public Vector Input() {
        return Input  ;
    }
    public Vector Output()  {
        return Output  ;
    }
    public int val() {
        return val ;
    }
    public String Str() {
        return string  ;
    }
    public Vector showmem()  {
        return Mem ;
    }
    public void  setval(int a) {
        val  = a ;
    }
    public void  setstr(String a)  {
        string = a ;
    }
    public State(String a)  {
        string = a ;
    }
    public State() {
    }
    public State(Vector vector, Vector a, Vector b)
    {
        Mem = vector;
        Input = a;
        Output =b;
    }
    // adds a variabile. If there was one, over-writes.
    public void allocateover(State var) {
        Mem.removeElement(new VariableSlot(var.Str()));
        Mem.addElement  (new VariableSlot(var.Str()));
    }
    // writes a value into the variable, initialising
    // it in the process.
    public void write(State var, int value) {
        int i= Mem.indexOf(new VariableSlot(var.Str()));
        ((VariableSlot)(Mem.elementAt(i))).setvalue(value);
        ((VariableSlot)(Mem.elementAt(i))).initialize();
    }
    public Vector copy() {
        Vector copy = new Vector();
        for(int i=0; i< Mem.size(); i++)
        	copy.addElement(Mem.elementAt(i));
        return copy;
    }
    // reads value from variable. If un-initialised,
    // throw an error.
    public int read(String var) throws Exception {
        int i= Mem.indexOf(new VariableSlot(var));
        if ((( VariableSlot)(Mem.elementAt(i))).Init() == false ) {
            System.out.println("Initialisation error of "+var);
            throw new Exception();
        }
        return ((VariableSlot)(Mem.elementAt(i))).value();
    }
    // copy a variable form a State to another.
    // If the "from" State doesn't contain the variable, then
    // the variable is just removed from the "to" State.
    public static void copyVariable(State var, Vector from, Vector to) {
        to.removeElement(new VariableSlot(var.Str()));
        int pos = from.indexOf(new VariableSlot(var.Str()));
        if (pos != -1) {
            to.addElement( (VariableSlot)(from.elementAt(pos)) );
        }
    }
}

// The parser creates a tree made of ASTNode, which is used for two things:
//  a) for a static check of the scope
//  b) for actual execution.

// Each node knows it own "name" (i.e. the string of its nonterminal/terminal )
// Also, if it's a nonterminal, it needs to retain which derivation option it
// represents. E.g. in the LW grammar the "one" non-terminal can be either
// "1" or "0".

class ASTNode {
    private int token_number  ;
    // Up to which token it's been matched
    private int letter_number  ;
    // Up to which letter it's been matched
    private int MatchedInt ;
    // Which branch has been matched
    private Vector ObjLst=new Vector();
    // The tree
    private String name ;
    // What has been recognized
    private State eval(int i, State S) throws Exception {
        return ((ASTNode)(ObjLst.elementAt(i-1))).evaluate(S);
    }
    private void  check(int quale, Vector vector) throws Exception{
        ((ASTNode)(ObjLst.elementAt(quale-1))).StaticCheck(vector);
    }
    private String TreeString() throws Exception {
        String s = "";
        for ( int i = 1; i <= ObjLst.size(); i++)
        	s += eval(i,null).Str();
        return s;
    }
    public int GetTokNum() {
        return token_number ;
    }
    public int GetLetNum() {
        return letter_number;
    }
    public ASTNode (Vector d, String e){
        ObjLst=d;
        name=e;
    }
    public ASTNode (int a,int b, int c, Vector d, String e){
        token_number=a;
        letter_number=b;
        MatchedInt=c;
        ObjLst=d;
        name=e;
    }

    // Static check just checks whether the program
    // is always using variables that have been declared in
    // a block. That's simple to check, just navigate
    // the three deeper in the "with" nodes, copying the
    // "scope" list and adding the new variable.
    // If any variable is referenced
    // that doesn't belong to the scope list, then
    // there is a scoping error.
    // The only slight exception here is that LW doesn't
    // require the declaration of the input and output
    // variables. They are in particular positions
    // in the program and they don't need explicit
    // declaration via "with".

    public void  StaticCheck(Vector Variables) throws Exception {
        Vector copy = new Vector();
        for (int i=0; i < Variables.size(); i++)
        	copy.addElement(Variables.elementAt(i));
        if (copy.size() > LW.MaxMem()){
            System.out.println("Out of memory.");
            throw new Exception();
        }
        // in the LW grammar, the input parameter is always
        // the second token (which is the second child of
        // the root element). While the output parameter is
        // always the 6th child of the root element, you
        // can see that from the grammar, counting the tokens
        // in the line that defines "Prog".
        if (name.equals("Prog")) {
            copy.addElement(eval(2,null).Str());
            copy.addElement(eval(6,null).Str());
            check(4,copy);
        }
        else  if (name.equals("Block")) {
            copy.addElement(eval(2,null).Str());
            check(4,copy);
        }
        else  if (name.equals("Id")) {
            if (!copy.contains(TreeString())){
                System.out.println("Scope error on: "+TreeString());
                throw new Exception();
            }
        }
        else
        	for (int i=1; i<=ObjLst.size();i++)
        		check(i,copy);
    }

    // It takes a state and it returns another state.
    // As per classic definition, Expressions shouldn't change
    // state, but we return the unchanged state anyways.
    public State evaluate(State S) throws Exception{
        if (name.equals("Prog")) {
            S.allocateover(eval(2,null));
            S.allocateover(eval(6,null));
            S.write( eval(2,null),
            ((new Integer((String)(S.Input().firstElement()))).intValue()));
            S.setval( eval(6,eval(4,S)).val());
            System.out.println("Ris: "+S.val());
            return null;
        }
        else if (name.equals("Progplus"))  {
            S = eval(2,S);
            System.out.println("Ris: " + S.Output().toString());
            return S;
        }
        else if (name.equals("Com")) {
            if (MatchedInt == 2) return S; // "skip" case
            else return eval(1,S) ;
        }
        else if (name.equals("Read"))  {
            if (S.Input().isEmpty())
            {
                System.out.println("Error: more values needed in data file.");
                throw new Exception();
            }
            S.write( eval(2,null),
            ((new Integer((String)(S.Input().firstElement()))).intValue()));
            S.Input().removeElementAt(0);
            return S;
        }
        else if (name.equals("Write"))  {
            S.Output().addElement((Object)(new Integer(eval(2,S).val())));
            return S;
        }
        else if (name.equals("Assignment")) {
            S.write( eval(1,null), eval(3,S).val() );
            return S;
        }
        else if (name.equals("IfThenElse")) {
            if  (eval(2,S).val()==eval(4,S).val()) return eval(6,S);
            else return eval(8,S);
        }
        else if (name.equals("Block"))  {
            State aux = new State(S.copy(),S.Input(),S.Output());
            aux.allocateover(eval(2,null));
            aux = eval(4,aux);
            State.copyVariable(eval(2,null),S.showmem(),aux.showmem());
            return aux;
        }
        else if (name.equals("WhileLoop")) {
            while (eval(2,S).val()!=eval(4,S).val()) S=eval(6,S);
            return S;
        }
        else if (name.equals("Coms"))  {
            if (ObjLst.size() == 1) return eval(1,S);
            else return eval(3,eval(1,S));
        }
        else if (name.equals("Expr"))  {
            switch(MatchedInt){
                case 1: S.setval(eval(2,S).val()+ eval(3,S).val());
                break;
                case 2: S.setval(eval(2,S).val()* eval(3,S).val());
                break;
                case 3: S.setval(S.read(eval(1,S).Str()) ) ;
                break;
                case 4: S.setval(eval(1,S).val());
            }
            return S;
        }
        else if (name.equals("Id")) {
            if  (!(S==null)) S.setval( S.read(TreeString()));
            else S = new State();
            S.setstr( TreeString());
            return S;
        }
        else if (name.equals("Num")) {
            switch(MatchedInt){
                case 1: S.setval( Integer.parseInt(eval(2,S).Str())) ;
                break;
                case 2: S.setval( -2147483648) ;
                break;
                case 3: S.setval(-Integer.parseInt(eval(2,S).Str())) ;
                break;
                default: S.setval( Integer.parseInt(eval(1,S).Str())) ;
                break;
            }
            return S;
        }
        // handles the case of "digit", "Let", "nine"..."zero"
        else if ((ObjLst.size()==1)&&(!(name.equals("pos"))))
        	return new State(((ASTNode)(ObjLst.firstElement())).name );
        // handles ("ninedigit"..."twodigit", "pos")
        else
        	return new State(TreeString());
    }
}

// for each variable in the State: name, value, and whether it's
// been initialised.
class VariableSlot  {
    private boolean Init ;
    private int value;
    private String id  ;
    public VariableSlot  (String a, int b, boolean c){
        id=a;
        value=b;
        Init=c;
    }
    public VariableSlot  (String a ) {
        id=a;
    }
    public boolean equals (Object a ){
        return id.equals(((VariableSlot)a).id());
    }
    public boolean Init()  {
        return Init ;
    }
    public void initialize() {
        Init = true ;
    }
    public String id() {
        return id  ;
    }
    public int value() {
        return value;
    }
    public void setvalue(int a) {
        value=a ;
    }
}

// takes as input the nonterminal to be parsed, the position in the program
// source code where to start from, and a boolean indicating whether
// the recognition can start from the middle of a token or not
// The outer loop scans all the possible derivations that the grammar
// defines for the [what] non-terminal.
// The next loop scans the elements of each derivation.
// The Parser recoursively parses further nonterminals found on the way.
// Each parsed node is returned in a ASTNode, which are collected in
// a list of lists.
// Terminals are matched with a simple string comparison.
// In case of a failed match (for either a terminal or non-terminal), then there
// are two options:
//       * the match was optional. In this case the Parser considers
//         the match a success anyways.
//       * the match wasn't optional: Parser resets the list of the items
//         matched so far and moves on to the next derivation. If there
//         are no more derivations, it returns null.
//  The Parser returns as soon as a derivation is matched - so the order of
//  the derivations in the grammar is relevant.
class Parser {
    private Vector Lst  =new Vector();
    private boolean ToConc(boolean a,Element b) {
        if (Lst.isEmpty()) return a;
        else return b.Stk();
    }
    public ASTNode  parse(String what,int InCurTok,int InCurLet,boolean ExtStk){
        NewAlt: for (int Intrp=1; true; Intrp++) {
            Lst.removeAllElements();
            int CurTok = InCurTok;
            int CurLet = InCurLet;
            NewTok: for (int token = 1; true ; token++) {
                Element El = Stx.NeedEl( what, Intrp ,token);
                if ((El==null)&& (token==1)) break NewAlt;
                if ((El==null)&& (token!=1)) break NewTok;
                if (!ToConc(ExtStk,El)) {
                    if (CurLet == ((PrgLst.get(CurTok).length())) ){
                        CurTok ++  ;
                        CurLet  = 0;
                    }
                    if (CurLet != 0 )
                    if (El.Opt()) break NewTok;
                    else continue NewAlt  ;
                }
                if  (El.terminal())
                if  (PrgLst.get(CurTok).startsWith(El.Name(),CurLet)) {
                    CurLet +=(El.Name()).length();
                    ASTNode temp =new ASTNode(new Vector(),El.Name());
                    Lst.addElement (temp);
                }
                else if ( El.Opt()) break NewTok;
                else continue NewAlt;
                else {
                    ASTNode aux = (new Parser()).parse(El.Name(),CurTok,
                    CurLet,ToConc(ExtStk,El));
                    if  (aux == null)
                    if ( El.Opt()) break NewTok;
                    else continue NewAlt;
                    else {
                        CurTok  = aux.GetTokNum();
                        CurLet  = aux.GetLetNum();
                        Lst.addElement (aux);
                    }
                }
            }
            return new ASTNode(CurTok,CurLet,Intrp,Lst, what);
        }
        return null;
    }
}