grammar SQL;

query
: 
	SELECT fields FROM tableName (WHERE whereExpr)?
;

tableName
:
	TABLE_NAME
;

field
:
	FIELD_NAME
;

fields
:
	field (COMMA field)*
	| STAR
;


queryClause
:
	field operator value
;

value
:
	StringLiteral
	| NumberLiteral
;

booleanOperator:
	OR
	| AND
;

whereExpr
:
	queryClause
	| groupedWhereClause
	| whereExpr (booleanOperator whereExpr)+
;

groupedWhereClause
:
	'(' whereExpr ')'
;

operator
:
	'='
	| '!='
	| '>'
	| '<'
	| '>='
	| '<='
	| LIKE
;

LIKE
:
	[Ll][Ii][Kk][Ee]
;

OR
:
	[Oo][Rr]
;

AND
:
	[Aa][Nn][Dd]
;


SELECT
: 
	[Ss][Ee][Ll][Ee][Cc][Tt]
;

FROM
: 
	[Ff][Rr][Oo][Mm]
;

WHERE
:
	[Ww][Hh][Ee][Rr][Ee]
;

TABLE_NAME
: 
	[A-Z][a-zA-Z0-9]*
;

NumberLiteral
:
	NUMERIC_LITERAL
;

StringLiteral
: 
	UnterminatedSingleQuoteStringLiteral '\''
	| UnterminatedDoubleQuoteStringLiteral '"'
;

UnterminatedSingleQuoteStringLiteral
: 
	'\'' (~['\\\r\n] | '\\' (. | EOF))*
;

UnterminatedDoubleQuoteStringLiteral
: 
	'"' (~["\\\r\n] | '\\' (. | EOF))*
;

FIELD_NAME
:
	[a-z][a-zA-Z0-9]*
;	

NUMERIC_LITERAL
: 
	DIGIT+ ( '.' DIGIT* )? ( E [-+]? DIGIT+ )?
 	| '.' DIGIT+ ( E [-+]? DIGIT+ )?
;


fragment DIGIT : [0-9];
fragment E : [eE];

STAR: '*';
COMMA: ',';
WS : [ \t\r\n]+ -> skip;
