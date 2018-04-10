package org.vepo;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class SQL2Rest {
	public static void main(String[] args) throws Exception {
		SQLLexer lexer = new SQLLexer(CharStreams.fromString("Select xsa, asa from DB"));

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SQLParser parser = new SQLParser(tokens);
		ParseTree tree = parser.query();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new SQLWalker(), tree);
	}
}