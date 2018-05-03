package org.vepo;

import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.vepo.SQLData.GroupWhereStatement;
import org.vepo.SQLData.Joiner;
import org.vepo.SQLData.ListWhereStatement;
import org.vepo.SQLData.WhereClause;
import org.vepo.SQLData.WhereStatement;

public class SQL2Rest {
	/**
	 * http://www.baeldung.com/rest-api-query-search-or-operation
	 * 
	 * @param sql
	 * @return
	 */
	public static String process(String sql) {
		SQLLexer lexer = new SQLLexer(CharStreams.fromString(sql));

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SQLParser parser = new SQLParser(tokens);
		ParseTree tree = parser.query();
		ParseTreeWalker walker = new ParseTreeWalker();
		SQLData data = new SQLData();
		walker.walk(data, tree);
		return "/" + data.getTableName().toLowerCase() + toRest(data.getWhereStatement());
	}

	private static String toRest(Optional<WhereStatement> whereStatement) {
		if (whereStatement.isPresent()) {
			return "?search=" + toRest(whereStatement.get());
		} else {
			return "";
		}

	}

	private static String toRest(WhereStatement whereStatement) {
		if (whereStatement instanceof GroupWhereStatement) {
			return "( " + toRest(((GroupWhereStatement) whereStatement).getWhere()) + " )";
		} else if (whereStatement instanceof ListWhereStatement) {
			List<Joiner> joiners = ((ListWhereStatement) whereStatement).getJoiner();
			return ((ListWhereStatement) whereStatement).getStatements().stream().map(w -> toRest(w)).reduce(null,
					(w1, w2) -> w1 != null ? w1 + ' ' + joiners.remove(0) + ' ' + w2 : w2);
		} else {
			return ((WhereClause) whereStatement).getField() + getOperator(((WhereClause) whereStatement).getOperator())
					+ ((WhereClause) whereStatement).getValue();
		}
	}

	private static String getOperator(String operator) {
		switch (operator) {
		case "=":
			return ":";
		default:
			return operator;
		}
	}
}