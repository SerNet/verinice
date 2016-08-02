header {
package sernet.verinice.service.linktable.antlr;
}
class VqlParser extends Parser;
options { 
buildAST=true;
defaultErrorHandler=false; 
}


expr : typeName (linkedType|parentType|childType)* (linkType)? property (alias)?;

as : "AS"|"as";

linkedType : LINK typeName;
parentType : PARENT typeName;
childType : CHILD typeName;

linkType :  LT linkTypeName;
property : PROP propertyName;
alias : as aliasName ;
typeName : Alphanumeric;
linkTypeName : Alphanumeric;
propertyName : Alphanumeric;
aliasName : Alphanumeric;

class VqlLexer extends Lexer;

options{ filter=WS; }

protected WS  :   (' '
        |   '\t'
        |   ('\n'|'\r'('\n')) {newline();}
        )+
    ;
LINK      : '/' ;
CHILD     : '>' ;
PARENT    : '<' ;
PROP      : '.' ;
LT        : ':' ;
Alphanumeric : ('_' | '-' | '0'..'9' | 'A'..'Z' | 'a'..'z' | 'Ä' | 'Ö' | 'Ü' | 'ä' | 'ö' | 'ü'| '(' | ')')+ ;

