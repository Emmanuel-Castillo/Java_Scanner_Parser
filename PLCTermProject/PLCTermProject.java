import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class PLCTermProject{

    //global declarations
    //variables
    static int charClass;
    static char[] lexeme;
    static char nextChar;
    static int lexLen;
    static int nextToken;
    static int lineNumber = 1;

    //symbol table
    public static HashMap<String, String> symbol_table = new HashMap<>(); 
    static boolean inside_DECL_STMT = false;   

    //file reading
    static File in_fp;
    static FileInputStream fileInputStream;
    static InputStreamReader inputStreamReader;

    //reserved words table
    static HashMap<String, Integer> reserved_words = new HashMap<>();

    //character classes
    static final int LETTER = 0;
    static final int DIGIT = 1;
    static final int UNDERSCORE = 2;
    static final int UNKNOWN = 99;

    //token codes
    static final int INT_LIT = 10;
    static final int IDENT = 11;
    static final int RESERVED = 1000;
    static final int ASSIGN_OP = 20;     // :=
    static final int ADD_OP = 21;
    static final int SUB_OP = 22;
    static final int MULT_OP = 23;
    static final int DIV_OP = 24;
    static final int LEFT_PAREN = 25;
    static final int RIGHT_PAREN = 26;
    static final int LESS_THAN = 50;
    static final int GREATER_THAN = 51;
    static final int EQUALS = 52;
    static final int NOT_EQUAL = 53;     // <>
    static final int TERMINATE = 10000;  // ;
    static final int DECLARECOMMA = 20000;    // ,
    static final int DECLARECOLON = 20001;   // :

    //reserved word codes
    static final int PROGRAM = 100;
    static final int BEGIN = 101;
    static final int END = 102;
    static final int IF = 103;
    static final int THEN = 104;
    static final int ELSE = 105;
    static final int INPUT = 106;
    static final int OUTPUT = 107;
    static final int INT = 108;
    static final int DOUBLE = 109;
    static final int FLOAT = 110;
    static final int WHILE = 111;
    static final int LOOP = 112;

    //end of file
    static final int EOF = -1;

    //start and end index of lexeme
    static boolean metNewLine = false;

    public static void main(String[] args){
        reserved_words.put("program", PROGRAM);
        reserved_words.put("begin", BEGIN);
        reserved_words.put("end", END);
        reserved_words.put("if", IF);
        reserved_words.put("then", THEN);
        reserved_words.put("else", ELSE);
        reserved_words.put("input", INPUT);
        reserved_words.put("output", OUTPUT);
        reserved_words.put("int", INT);
        reserved_words.put("double", DOUBLE);
        reserved_words.put("float", FLOAT);
        reserved_words.put("while", WHILE);
        reserved_words.put("loop", LOOP);
        
        // Check if there are arguments
        if (args.length == 0) {
            System.err.println("Please provide a file name as an argument.");
            return;  // Exit the program
        }
   
        try{
        //open the file for reading and process its contents

        in_fp = new File(args[0]);
        fileInputStream = new FileInputStream(in_fp);
        inputStreamReader = new InputStreamReader(fileInputStream);

        getChar();
        lex();
        program();

        inputStreamReader.close();
        }

        catch (IOException e){
            System.out.println("Error reading the file: " + e.getMessage());
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    //lex - a simple lexical analyzer for arithmetic expressions
    static int lex() throws IOException, Exception{
        lexLen = 0;
        lexeme = new char[100];
        getNonBlank();
        if(metNewLine){
            lineNumber++;
            metNewLine = false;
        }
        switch(charClass){

            //parse identifiers
            case LETTER:
            case UNDERSCORE:
                addChar();
                getChar();
                while(charClass == LETTER || charClass == DIGIT || charClass == UNDERSCORE){
                    addChar();
                    getChar();
                }

                String lexeme_String = new String(lexeme, 0, lexLen);
                //figure out if reserved word
                    if(reserved_words.containsKey(lexeme_String)){
                        nextToken = reserved_words.get(lexeme_String);
                    }
                    else{
                        //otherwise it's identifier
                        nextToken = IDENT;
                    }
                break;

            //parse integer literals
            case DIGIT:
                addChar();
                getChar();
                while(charClass == DIGIT){
                    addChar();
                    getChar();
                }
                nextToken = INT_LIT;
                break;

            //parenthesis and operators
            case UNKNOWN:
                lookup(nextChar);
                getChar();
                break;

            //eof
            case EOF:
                nextToken = EOF;
                lexeme[0] = 'E';
                lexeme[1] = 'O';
                lexeme[2] = 'F';
                lexeme[3] = 0;
                break;
        }

        //end of switch statement
        //System.out.println("Next token is: " + nextToken + ", Next lexeme is " + String.valueOf(lexeme) + ", line number: " + lineNumber);

        return nextToken;
    }

    //getChar - a function to get the next character of input and determine
    //          its character class
    static void getChar() throws IOException{
        int nextCharValue;

            // Read the next character from the file
            if ((nextCharValue = inputStreamReader.read()) != EOF) {
                nextChar = (char) nextCharValue;
                if(nextChar == '\n'){
                    metNewLine = true;
                }
                if(Character.isAlphabetic(nextChar))
                    charClass = LETTER;
                else if (Character.isDigit(nextChar))
                    charClass = DIGIT;
                else if (nextChar == '_')
                    charClass = UNDERSCORE;
                else
                    charClass = UNKNOWN;

                //System.out.println("Character [" + nextChar + "] at line: " + lineNumber + ", found new line? " + metNewLine);
            }
            else{
                charClass = EOF;
            }

    }

    //getNonBlank - a function to call getChar until it returns 
    //              a non-whitespace character
    static void getNonBlank() throws IOException{
        while(Character.isWhitespace(nextChar)){
            getChar();
            if(metNewLine){
            lineNumber++;
            metNewLine = false;
        }
        }
    }

    //addChar - a function to add nextChar to lexeme
    static void addChar() throws Exception{
        if(lexLen <= 98){
            lexeme[lexLen++] = nextChar;
            lexeme[lexLen] = 0;
        }
        else
            throw new Exception("Error on line " + lineNumber + " - lexeme is too long.");
    }

    //lookup - a function to lookup operators and parentheses
    //         and return the token
    static int lookup(char ch) throws IOException, Exception{
        switch(ch){
            case '(':
                addChar();
                nextToken = LEFT_PAREN;
                break;
            case ')':
                addChar();
                nextToken = RIGHT_PAREN;
                break;
            case '+':
                addChar();
                nextToken = ADD_OP;
                break;
            case '-':
                addChar();
                nextToken = SUB_OP;
                break;
            case '*':
                addChar();
                nextToken = MULT_OP;
                break;
            case '/':
                addChar();
                nextToken = DIV_OP;
                break;
            case '<':
                addChar();

                getChar();
                if(nextChar == '>'){
                    addChar();
                    nextToken = NOT_EQUAL;
                }
                else{
                    nextToken = LESS_THAN;
                }
                break;
            case '>':
                addChar();
                nextToken = GREATER_THAN;
                break;
            case '=':
                addChar();
                nextToken = EQUALS;
                break;
            case ';':
                addChar();
                nextToken = TERMINATE;
                break;
            case ',':
                addChar();
                nextToken = DECLARECOMMA;
                break;
            case ':':
                addChar();

                getChar();
                if(nextChar == '='){
                    addChar();
                    nextToken = ASSIGN_OP;
                }
                else{
                    nextToken = DECLARECOLON;
                }
                break;
            default:
                throw new Exception("Error on line: " + lineNumber + ", special character " + nextChar + " not allowed");
        }
    
        return nextToken;
    }

/*
 * Rule 01: PROGRAM -> program DECL_SEC begin STMT_SEC end;
 */
 static void program() throws IOException, Exception{
        System.out.println("PROGRAM");
        if(nextToken != PROGRAM)
            throw new Exception("Error on line " + lineNumber + ": Lexeme 'program' is expected");

        lex();
        if (nextToken == BEGIN){
            lex();
            stmt_sec();     
        }
        else{
            decl_sec();    
            if(nextToken == BEGIN){
                lex();
                stmt_sec();    
            }
            else
                throw new Exception("Error on line " + lineNumber + ": Lexeme 'begin' expected");
        }
        if(nextToken == END){
            lex();
            if(nextToken == TERMINATE){
                lex();
                return;
            }
            else
                throw new Exception("Error on line " + lineNumber + ": Terminator operator expected.");
        }
        else
            throw new Exception("Error on line " + lineNumber + ": Lexeme 'end' expected");
    
    }

    /*
     * Rule 02: DECL_SEC -> DECL | DECL DECL_SEC
     */
    public static void decl_sec() throws IOException, Exception{
        System.out.println("DECL_SEC");

        inside_DECL_STMT = true;

        //parse first nonterminal
        decl();    

        while(nextToken != BEGIN){
            decl_sec();    
        }

        inside_DECL_STMT = false;
    }

    /*
     * Rule 03: DECL -> ID_LIST : TYPE ;
     */
    public static void decl() throws IOException, Exception{
        System.out.println("DECL");

        //parse first nonterminal
        id_list();    


        if (nextToken == DECLARECOLON){
            lex();
            type();     
            if(nextToken == TERMINATE){
                lex();
                return;
            }
            else
                throw new Exception("Error on line " + lineNumber + ": Terminator operator expected");
        }
        else 
            throw new Exception("Error on line " + lineNumber + ": Declaration operator expected");
    }

    /*
     * Rule 04: ID_LIST -> ID | ID, ID_LIST
     */
    public static void id_list() throws IOException, Exception{
        System.out.println("ID_LIST");

        //parse first nonterminal
        id();     

        while(nextToken == DECLARECOMMA){
            lex();
            id_list();    
        }
    }

    /*
     * Rule 05: ID -> numbers & digits (no need to produce rule)
     */
    public static void id() throws IOException, Exception{
        if(nextToken >= PROGRAM && nextToken <= LOOP)
            throw new Exception("Error on line " + lineNumber + ": " + String .valueOf(lexeme) + " cannot be used as identifier. It's a reserved word.");

        String variableName = String.valueOf(lexeme);
        if(inside_DECL_STMT){
            if(!symbol_table.containsKey(variableName)){
                symbol_table.put(variableName, "variable");
            }
            else{
                throw new Exception("Error on line: " + lineNumber + ", variable " + variableName + " already declared");
            }
        }

        if(symbol_table.containsKey(variableName)){
            lex();
            return;
        }
        else
            throw new Exception("ERROR !! identifier not declared in Line " + lineNumber + ".");
    }

    /*
     * Rule 06: STMT_SEC -> STMT | STMT STMT_SEC
     */
    public static void stmt_sec() throws IOException, Exception{
        System.out.println("STMT_SEC");

        //parse first term
        stmt(); 

        while(nextToken != END && nextToken != ELSE){
            stmt_sec();  
        }

    }

    /*
     * Rule 07: STMT -> ASSIGN | IFSTMT | WHILESTMT | INPUT | OUTPUT
     */
    public static void stmt() throws IOException, Exception{
        System.out.println("STMT");

        if(nextToken == IF){
            ifstmt();
        }
        else if(nextToken == WHILE){
            whilestmt();
        }
        else if(nextToken == INPUT){
            input();
        }
        else if(nextToken == OUTPUT){
            output();
        }
        else if(nextToken == IDENT){
            assign();
        }

    }

    /*
     * Rule 08: ASSIGN -> ID := EXPR ;
     */
    public static void assign() throws IOException, Exception{
        System.out.println("ASSIGN");

        //parse the first term
        id();     

        if(nextToken == ASSIGN_OP){
            lex();
            expr();    
            if(nextToken == TERMINATE){
                lex();
                return;
            }
            else
                throw new Exception("Error on line " + lineNumber + ": Terminator operator expected");
        }
    }

    /*
     * Rule 09: IFSTMT -> if COMP then STMT_SEC else STMT_SEC end if ; |
     *                    if COMP then STMT_SEC end if ;
     */
    public static void ifstmt() throws IOException, Exception{
        System.out.println("IF_STMT");

        if(nextToken != IF){
            System.out.println("Error: Lexeme 'if' expected");
            return;
        }

        lex();
        comp();  

        if(nextToken == THEN){
            lex();
            stmt_sec();   
            if(nextToken == ELSE){
                lex();
                stmt_sec();    
            }
            if(nextToken == END){
                lex();
                if(nextToken == IF){
                    lex();
                    if(nextToken == TERMINATE){
                        lex();
                        return;
                    }
                    else
                        throw new Exception("ERROR !! Semicolon missing in Line " + lineNumber + ".");
                }
                else
                    throw new Exception("Error on line " + lineNumber + ": Lexeme 'if' expected");
            }
            else
                throw new Exception("Error on line " + lineNumber + ": Lexeme 'end' expected");
        }
        else
            throw new Exception("Error on line " + lineNumber + ": Lexeme 'then' expected");
    }

    /*
     * Rule 10: WHILESTMT -> while COMP loop STMT_SEC end loop ;
     */
    public static void whilestmt() throws IOException, Exception{ 
        System.out.println("WHILE_STMT");

        if(nextToken != WHILE){
            throw new Exception("Error on line " + lineNumber + ": Lexeme 'while' expected");
        }

        lex();
        comp(); 

        if(nextToken == LOOP){
            lex();
            stmt_sec();
            if(nextToken == END){
                lex();
                if(nextToken == LOOP){
                    lex();
                    if(nextToken == TERMINATE){
                        lex();
                        return;
                    }
                    else
                        throw new Exception("ERROR !! Semicolon missing in Line " + lineNumber + ".");
                }
                else
                    throw new Exception("Error on line " + lineNumber + ": Lexeme 'loop' expected");
            }
            else
                throw new Exception("Error on line " + lineNumber + ": Lexeme 'end' expected");
        }
        else
            throw new Exception("Error on line " + lineNumber + ": Lexeme 'loop' expected");
    }

    /*
     * Rule 11: INPUT -> input ID_LIST ;
     */
    public static void input() throws IOException, Exception{   
        System.out.println("INPUT");

        if(nextToken != INPUT){
            throw new Exception("Error on line " + lineNumber + ": Lexeme 'input' expected");
        }

        lex();
        id_list();

        if(nextToken == TERMINATE){
            lex();
            return;
        }
        else
            throw new Exception("ERROR !! Semicolon missing in Line " + lineNumber + ".");
    }

    /*
     * Rule 12: OUTPUT -> output ID_LIST | output NUM ;
     */
    public static void output() throws IOException, Exception{
        System.out.println("OUTPUT");

        if(nextToken != OUTPUT){
            throw new Exception("Error on line " + lineNumber + ": Lexeme 'output' expected");
        }

        lex();

        if(nextToken == IDENT){
            id_list();
        }
        else if(nextToken == INT_LIT){
            num();
        }
        if(nextToken == TERMINATE){
            lex();
            return;
        }
        else
            throw new Exception("ERROR !! Semicolon missing in Line " + lineNumber + ".");

    }

    /*
     * Rule 13: EXPR -> FACTOR | FACTOR + EXPR | FACTOR - EXPR
     */
    public static void expr() throws IOException, Exception{
        System.out.println("EXPR");

        //parse the first term
        factor();

        while(nextToken == ADD_OP || nextToken == SUB_OP){
            lex();
            expr();
        }
    }

    /*
     * Rule 14: FACTOR -> OPERAND | OPERAND * FACTOR | OPERAND / FACTOR
     */
    public static void factor() throws IOException, Exception{
        System.out.println("FACTOR");

        //parse the first term
        operand();

        while(nextToken == MULT_OP || nextToken == DIV_OP){
            lex();
            factor();
        }
    }

    /*
     * Rule 15: OPERAND -> NUM | ID | (EXPR)
     */
    public static void operand() throws IOException, Exception{
        System.out.println("OPERAND");

        if(nextToken == INT_LIT){
            num();
        }
        else if(nextToken == IDENT){
            id();
        }
        else if(nextToken == LEFT_PAREN){
            lex();
            expr();
            if(nextToken == RIGHT_PAREN){
                lex();
                return;
            }
            else
                throw new Exception("Error on line " + lineNumber + ": Lexeme ')' expected");
        }
        else
            throw new Exception("Error on line " + lineNumber + ": Operand not detected");
    }

    /*
     * Rule 16: digit or float (no need to make rule)
     */
    public static void num() throws IOException, Exception{
        lex();
    }

    /*
     * Rule 17: COMP -> ( OPERAND = OPERAND ) | ( OPERAND <> OPERAND ) 
     *                  | ( OPERAND > OPERAND ) | ( OPERAND < OPERAND )
     */
    public static void comp() throws IOException, Exception{
        System.out.println("COMP");
        if(nextToken == LEFT_PAREN){
            lex();
            operand();

            if (nextToken == EQUALS || nextToken == NOT_EQUAL || 
                nextToken == LESS_THAN || nextToken == GREATER_THAN){
                    lex();
                    operand();

                    if(nextToken == RIGHT_PAREN){
                        lex();
                        return;
                    }
                    else
                        throw new Exception("Error on line " + lineNumber + ": Lexeme ')' expected");
                }
            else
                throw new Exception("Error on line " + lineNumber + ": Comparison operator expected");
        }
    }

    /*
     * Rule 18: TYPE -> int | float | double (no need for rule)
     */
    public static void type() throws IOException, Exception{
        lex();
    }

    
}