grammar SQL;

query
: 
	SELECT fields FROM tableName
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

SELECT
: 
	[Ss][Ee][Ll][Ee][Cc][Tt]
;

FROM
: 
	[Ff][Rr][Oo][Mm]
;

TABLE_NAME
: 
	[A-Z][a-zA-Z0-9]*
;

FIELD_NAME
:
	[a-z][a-zA-Z0-9]*
;	
STAR: '*';
COMMA: ',';
WS : [ \t\r\n]+ -> skip;
