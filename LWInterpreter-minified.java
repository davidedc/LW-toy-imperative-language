import  java.io.*  ;
import  java.util.*;
//_____ ______________________________________________________________________
class   LW     {
private static int    MaxMem ;
 public static int    MaxMem() {return MaxMem;}
 public static void   main(String[] arg) {
	try    {(new Scanner()).tokenize(arg[1],PrgLst.GimmeList());
	       MaxMem = Integer.parseInt(arg[2]);
	       Vector data = new Vector();
	       (new Scanner()).tokenize(arg[3],data);
	       ASTNode temp=(new Parser()).parse(Stx.decode(arg[0]),0,0,false);
	       if (temp == null)
		  {System.out.println("...program doesn't obey syntax." );
		  throw new Exception();}
	       temp.StaticCheck(new Vector());
	       temp.evaluate(new State(new Vector(),data,new Vector()));}
	catch  (NumberFormatException e){System.out.println("Param.Error");}
	catch  (Exception e){System.out.println("Sorry.");
  System.out.println("Usge: java LW grammarName progName maxMem dataFile");}}}
//_____ ______________________________________________________________________
class   PrgLst { 
private static Vector Lst         = new Vector();
 public static Vector GimmeList() { return Lst  ; }
 public static String get(int i)  { return (String)(Lst.elementAt(i)); }}
//_____ ______________________________________________________________________
  class Scanner  {
 public void tokenize(String fileName,Vector vector) throws Exception{
	BufferedReader file = null;
	try   {file = new BufferedReader(new FileReader(fileName));}
	catch (FileNotFoundException e) {
	      System.out.println("File " + fileName + " not found.");
	      throw new Exception();}
	StreamTokenizer st = new StreamTokenizer(file);
	st.resetSyntax();
	st.wordChars('\u0000','\u00FF');
	st.whitespaceChars(0,' ');
	try   {for (int i = st.nextToken();i != st.TT_EOF;i = st.nextToken())
		   vector.addElement(st.sval);}             
	catch (IOException e) {  System.out.println("I/O failure.");
				 throw new Exception();}}}
//_____ ______________________________________________________________________
  class Element {
private boolean terminal, Stk, Opt;
private String  Name;
 public Element (String a,boolean b, boolean e, boolean f){
		terminal = b; Name = a; Stk = e; Opt = f;}
 public boolean terminal() { return  terminal; }
 public boolean Stk()      { return  Stk     ; }
 public boolean Opt()      { return  Opt     ; }
 public String  Name()     { return  Name    ; }}
//_____ ______________________________________________________________________
  class Stx {
private static  Hashtable table = new Hashtable();
private static  String    Purify(String a, String b) 
				{return  (a.replace(b.charAt(0),' ')).trim();}
 public static  Element   NeedEl(String a,int b, int c)
				{return (Element)(table.get(a+"/"+b+"/"+c));}
 public static  String    decode(String file) throws Exception {
	Vector  Lst    =  new Vector()   ;
	(new Scanner()).tokenize(file,Lst);
	boolean Stk    =  false, IsTerm = false, Opt = false;
	int     CurAlt =  1    , NumTok = 0                 ;
	String  CurDef =  ""                                ;
	String  NewDef =  (String)(Lst.elementAt(0));
	String  NewAlt =  (String)(Lst.elementAt(1));
	String  Stick  =  (String)(Lst.elementAt(2));
	String  Term   =  (String)(Lst.elementAt(3));
	String  OptS   =  (String)(Lst.elementAt(4));
	for (int i=6; i < Lst.size(); i++) {
	    String CurTok = (String)(Lst.elementAt(i));
	    if     (CurTok.endsWith(NewDef)){
		   CurDef = Purify(CurTok,NewDef);
		   CurAlt = 1; NumTok = 0;
		   continue;}
	    if     (CurTok.equals(NewAlt)) {CurAlt++ ; NumTok = 0; continue;}
	    if     (CurTok.equals(Stick) ) {Stk=true ; continue  ;          }
	    NumTok++;
	    table.put(CurDef + "/" + CurAlt + "/" + NumTok ,
		       new Element(Purify( Purify(CurTok,Term ) ,OptS ),
				   CurTok.startsWith(Term),
				   Stk,
				   CurTok.endsWith(OptS)));
	    Stk = false;
	}
	return (String)(Lst.elementAt(5));}}
//_____ ______________________________________________________________________
  class State  {
private String string                                        ;
private Vector Mem                                           ;
private int    val                                           ;
private Vector Input                                         ;
private Vector Output                                        ;
 public Vector Input()               { return    Input       ; }
 public Vector Output()              { return    Output      ; }
 public int    val()                 { return    val         ; }
 public String Str()                 { return    string      ; }
 public Vector showmem()             { return    Mem         ; }
 public void   setval(int a)         { val       = a         ; }
 public void   setstr(String a)      { string    = a         ; }
 public State(String a)              { string    = a         ; }
 public State() {}
 public State(Vector vector, Vector a, Vector b)
	{ Mem  = vector; Input = a;Output =b;}
 public void allocateover(State var) {
	Mem.removeElement(new VariableSlot(var.Str()));
	Mem.addElement   (new VariableSlot(var.Str()));}
 public void    write(State var, int value)  {
	int i=  Mem.indexOf(new VariableSlot(var.Str()));
	((VariableSlot)(Mem.elementAt(i))).setvalue(value);
	((VariableSlot)(Mem.elementAt(i))).initialize();}
 public Vector  copy() {
	Vector  copy = new Vector();
	for(int i=0;i<Mem.size();i++)copy.addElement(Mem.elementAt(i));
	return  copy;}
 public int     read(String var) throws Exception {
	int i=  Mem.indexOf(new VariableSlot(var));
	if  ((( VariableSlot)(Mem.elementAt(i))).Init() == false )  {
	    System.out.println("Initialisation error of "+var);
	    throw new Exception();}
	return  ((VariableSlot)(Mem.elementAt(i))).value();}
 public static void copyVariable(State var, Vector from, Vector to) {
	to.removeElement(new VariableSlot(var.Str()));
	int pos = from.indexOf(new VariableSlot(var.Str()));
	if (pos != -1) {to.addElement( (VariableSlot)(from.elementAt(pos)) );}}}
//_____ ______________________________________________________________________
  class ASTNode {
private int    token_number       ; // Up to which token it's been matched
private int    letter_number      ; // Up to which letter it's been matched
private int    MatchedInt         ; // Which branch has been matched
private Vector ObjLst=new Vector(); // The tree
private String name               ; // What has been recognized
private State  eval(int i, State S) throws Exception {
	       return ((ASTNode)(ObjLst.elementAt(i-1))).evaluate(S);}
private void   check(int quale, Vector vector)  throws Exception{
	       ((ASTNode)(ObjLst.elementAt(quale-1))).StaticCheck(vector);}
private String TreeString() throws Exception {
			    String s = "";
			    for ( int i = 1; i <= ObjLst.size(); i++)
			    s += eval(i,null).Str();
			    return s;}
 public int    GetTokNum() { return token_number ; }
 public int    GetLetNum() { return letter_number; }
 public ASTNode (Vector d, String e){ObjLst=d;name=e;}
 public ASTNode (int a,int b, int c, Vector d, String e){
	       token_number=a;letter_number=b;MatchedInt=c;ObjLst=d;name=e;}
 public void   StaticCheck(Vector Variables) throws Exception {
	Vector copy = new Vector();
	for    (int i=0; i <Variables.size(); i++)
	       copy.addElement(Variables.elementAt(i));
	if     (copy.size() > LW.MaxMem()){
	       System.out.println("Out of memory.");
	       throw new Exception();} 
	if     (name.equals("Prog"))            {
	       copy.addElement(eval(2,null).Str());
	       copy.addElement(eval(6,null).Str());
	       check(4,copy);}
	else   if (name.equals("Block"))        {
	       copy.addElement(eval(2,null).Str());
	       check(4,copy);}
	else   if (name.equals("Id"))           {
	       if (!copy.contains(TreeString())){
		  System.out.println("Scope error on: "+TreeString());
		  throw new Exception();}}
	else   for (int i=1; i<=ObjLst.size(); i++) check(i,copy);}
 public State  evaluate(State S) throws Exception{
if (name.equals("Prog"))            {
	S.allocateover(eval(2,null));
	S.allocateover(eval(6,null));
	S.write( eval(2,null),
	 ((new Integer((String)(S.Input().firstElement()))).intValue()));
	S.setval( eval(6,eval(4,S)).val());
	System.out.println("Ris: "+S.val());
	return null;}
else if (name.equals("Progplus"))   {
	S = eval(2,S); 
	System.out.println("Ris: " + S.Output().toString());
	return S;}
else if (name.equals("Com"))        {
	if    (MatchedInt == 2) return S;
	else  return eval(1,S) ;}
else if (name.equals("Read"))       {
	if (S.Input().isEmpty())
	   {System.out.println("Error: more values needed in data file.");
	   throw new Exception();}
	S.write( eval(2,null),
	 ((new Integer((String)(S.Input().firstElement()))).intValue()));
	S.Input().removeElementAt(0);
	return S;}
else if (name.equals("Write"))      {
	S.Output().addElement((Object)(new Integer(eval(2,S).val())));
	return S;               }
else if (name.equals("Assignment")) {
	S.write( eval(1,null), eval(3,S).val() );
	return S;}
else if (name.equals("IfThenElse")) {
	if   (eval(2,S).val()==eval(4,S).val()) return eval(6,S);
	else return eval(8,S);}
else if (name.equals("Block"))      {
	State aux = new State(S.copy(),S.Input(),S.Output());
	aux.allocateover(eval(2,null));
	aux = eval(4,aux);
	State.copyVariable(eval(2,null),S.showmem(),aux.showmem());
	return aux;}
else if (name.equals("WhileLoop"))  {
	while  (eval(2,S).val()!=eval(4,S).val()) S=eval(6,S);
	return S; }
else if (name.equals("Coms"))       {
	if    (ObjLst.size() == 1) return eval(1,S);
	else  return eval(3,eval(1,S));}
else if (name.equals("Expr"))       {
	switch(MatchedInt){
	case  1: S.setval(eval(2,S).val()+ eval(3,S).val());break;
	case  2: S.setval(eval(2,S).val()* eval(3,S).val());break;
	case  3: S.setval(S.read(eval(1,S).Str())    )     ;break;
	case  4: S.setval(eval(1,S).val());}
	return S; }
else if (name.equals("Id"))         {
	if   (!(S==null)) S.setval( S.read(TreeString()));
	else S = new State();
	S.setstr( TreeString());
	return S;}
else if (name.equals("Num"))        {
	switch(MatchedInt){
	case  1: S.setval( Integer.parseInt(eval(2,S).Str())) ;break;
	case  2: S.setval( -2147483648)                       ;break;
	case  3: S.setval(-Integer.parseInt(eval(2,S).Str())) ;break;
	default: S.setval( Integer.parseInt(eval(1,S).Str())) ;break;}
	return S;}
else if ((ObjLst.size()==1)&&(!(name.equals("pos"))))
	return new State(((ASTNode)(ObjLst.firstElement())).name );
else    return new State(TreeString());}}
//_____ ______________________________________________________________________
  class VariableSlot   {
private boolean Init ;
private int     value;
private String  id   ;
 public VariableSlot   (String a, int b, boolean c){id=a;value=b;Init=c;}
 public VariableSlot   (String a )                 {id=a;}
 public boolean equals (Object a ){return id.equals(((VariableSlot)a).id());}
 public boolean Init()          {return Init ;}                                
 public void    initialize()    {Init = true ;}                                
 public String  id()            {return id   ;}                                
 public int     value()         {return value;}                                
 public void    setvalue(int a) {value=a     ;}}                              
//_____ ______________________________________________________________________
  class Parser {
private Vector  Lst   =new Vector();
private boolean ToConc(boolean a,Element b) {if    (Lst.isEmpty()) return a;
					    else  return b.Stk();}
 public ASTNode   parse(String what,int InCurTok,int InCurLet,boolean ExtStk){
NewAlt: for (int Intrp=1; true;Intrp++) {
	     Lst.removeAllElements();
	     int CurTok         = InCurTok;
	     int CurLet         = InCurLet;
	  NewTok:for (int token = 1; true ;token++) {
		 Element El     = Stx.NeedEl( what, Intrp ,token);
		 if ((El==null)&& (token==1)) break NewAlt;
		 if ((El==null)&& (token!=1)) break NewTok;
		 if (!ToConc(ExtStk,El))    {
		    if (CurLet == ((PrgLst.get(CurTok).length())) ){
		       CurTok  ++   ;
		       CurLet   =  0;}
		    if (CurLet !=  0 )
		       if  (El.Opt())  break  NewTok;
		       else  continue  NewAlt       ;}
		 if   (El.terminal()) 
		      if   (PrgLst.get(CurTok).startsWith(El.Name(),CurLet)) {
			   CurLet        +=(El.Name()).length();
			   ASTNode temp    =new ASTNode(new Vector(),El.Name());
			   Lst.addElement  (temp);}
		      else if ( El.Opt())  break NewTok;
			   else  continue  NewAlt;
		 else {ASTNode aux =  (new  Parser()).parse(El.Name(),CurTok,
					   CurLet,ToConc(ExtStk,El));
		      if   (aux  == null)
			   if ( El.Opt())  break NewTok;
			   else  continue  NewAlt;
		      else {CurTok      =  aux.GetTokNum();
			   CurLet       =  aux.GetLetNum();
			   Lst.addElement  (aux);}}
		 }
	     return new ASTNode(CurTok,CurLet,Intrp,Lst, what);
	 }
	 return null;}}
