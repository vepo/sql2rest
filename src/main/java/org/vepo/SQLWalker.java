package org.vepo;

public class SQLWalker extends SQLBaseListener {
	public void enterQuery(SQLParser.QueryContext ctx) {
		System.out.println("Table Name: " + ctx.tableName().getText());
		ctx.fields().field().forEach(f -> System.out.println(f.getText()));
	}
}