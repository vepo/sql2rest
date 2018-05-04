package org.vepo;

import java.util.List;
import java.util.Optional;

import org.vepo.SQLTreeWalker.GroupWhereStatement;
import org.vepo.SQLTreeWalker.Joiner;
import org.vepo.SQLTreeWalker.ListWhereStatement;
import org.vepo.SQLTreeWalker.SQLData;
import org.vepo.SQLTreeWalker.WhereClause;
import org.vepo.SQLTreeWalker.WhereStatement;

public class Resolver {
	/**
	 * http://www.baeldung.com/rest-api-query-search-or-operation
	 * @param data
	 * @return
	 */
	public static String toRest(SQLData data) {
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
