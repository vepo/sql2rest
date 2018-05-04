package org.vepo;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.vepo.SQLTreeWalker.SQLData;

public class SQL2Rest {
	/**
	 * @param sqlData
	 * @return
	 */
	public static SQLData process(String sql) throws SyntaxException {
		try {
			SQLLexer lexer = new SQLLexer(CharStreams.fromString(sql));

			lexer.removeErrorListeners();
			lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

			CommonTokenStream tokens = new CommonTokenStream(lexer);

			SQLParser parser = new SQLParser(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(ThrowingErrorListener.INSTANCE);

			ParseTree tree = parser.query();
			ParseTreeWalker walker = new ParseTreeWalker();
			SQLTreeWalker treeWalker = new SQLTreeWalker();
			walker.walk(treeWalker, tree);
			SQLData data = treeWalker.getMainData();
			return data;
		} catch (ParseCancellationException pce) {
			throw new SyntaxException();
		}
	}

}