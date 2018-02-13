package compiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

%%

%public
%class CMinusScanner
%implements Scanner

%type Token

%scanerror ScannerException

%unicode

%{

  private BufferedReader input;
  private Token next_token;

  public Token getNextToken() throws ScannerException, IOException {
    if(next_token == null) {
      next_token = yylex();
    }
    Token out = next_token;
    if(next_token.getType() != Token.TokenType.EOF) {
      next_token = yylex();
    }
    return out;
  }

  public Token viewNextToken() {
    return next_token;
  }

  private Token symbol(Token.TokenType type) {
    return new Token(type);
  }

  private Token symbol(Token.TokenType type, Object value) {
    return new Token(type, value);
  }

  public static void main(String args[]) {
    CMinusScanner scanner;

    try {
      scanner = new CMinusScanner(new BufferedReader(new FileReader(args[0])));

      Token next;
      while((next = scanner.getNextToken()).getType() != Token.TokenType.EOF) {
        if(next.getData() == null) {
          System.out.println(next.getType().toString());
        }
        else {
          System.out.println(next.getType().toString() + ": " + next.getData().toString());
        }
      }
    }
    catch(Exception ex) {
      ex.printStackTrace();
      return;
    }
  }
%}

/* main character classes */
LineTerminator = \r|\n|\r\n

WhiteSpace = {LineTerminator} | [ \t\f]

/* comments */
Comment = "/*"


/* identifiers */
Identifier = [:jletter:]+

/* integer literals */
IntegerLiteral = [0-9]+

%state COMMENT

%%

<YYINITIAL> {

  /* keywords */
  "else"                         { return symbol(Token.TokenType.ELSE); }
  "int"                          { return symbol(Token.TokenType.INT); }
  "if"                           { return symbol(Token.TokenType.IF); }
  "return"                       { return symbol(Token.TokenType.RETURN); }
  "void"                         { return symbol(Token.TokenType.VOID); }
  "while"                        { return symbol(Token.TokenType.WHILE); }


  /* separators */
  "("                            { return symbol(Token.TokenType.LPAREN); }
  ")"                            { return symbol(Token.TokenType.RPAREN); }
  "{"                            { return symbol(Token.TokenType.LCURLY); }
  "}"                            { return symbol(Token.TokenType.RCURLY); }
  "["                            { return symbol(Token.TokenType.LSQUARE); }
  "]"                            { return symbol(Token.TokenType.RSQUARE); }
  ";"                            { return symbol(Token.TokenType.SEMI); }
  ","                            { return symbol(Token.TokenType.COMMA); }

  /* operators */
  "="                            { return symbol(Token.TokenType.ASSIGN); }
  ">"                            { return symbol(Token.TokenType.GREATER); }
  "<"                            { return symbol(Token.TokenType.LESS); }
  "=="                           { return symbol(Token.TokenType.EQUAL); }
  "<="                           { return symbol(Token.TokenType.LEQUAL); }
  ">="                           { return symbol(Token.TokenType.GREQUAL); }
  "!="                           { return symbol(Token.TokenType.NEQUAL); }
  "+"                            { return symbol(Token.TokenType.PLUS); }
  "-"                            { return symbol(Token.TokenType.MINUS); }
  "*"                            { return symbol(Token.TokenType.MULT); }
  "/"                            { return symbol(Token.TokenType.DIV); }

  {IntegerLiteral}            { return symbol(Token.TokenType.NUM, new Integer(yytext())); }

  /* comments */
  {Comment}                      { yybegin(COMMENT); }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* identifiers */
  {Identifier}                   { return symbol(Token.TokenType.IDENT, yytext()); }
}

<COMMENT> {
  "*/" { yybegin(YYINITIAL); }
  [^]                              { }
}

/* error fallback */
[^]                              { throw new ScannerException("Bad token."); }
<<EOF>>                          { return symbol(Token.TokenType.EOF); }