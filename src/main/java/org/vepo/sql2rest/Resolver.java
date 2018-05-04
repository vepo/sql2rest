package org.vepo.sql2rest;

import java.util.List;
import java.util.Optional;

import org.vepo.sql2rest.SQLTreeWalker.GroupWhereStatement;
import org.vepo.sql2rest.SQLTreeWalker.Joiner;
import org.vepo.sql2rest.SQLTreeWalker.Lazy;
import org.vepo.sql2rest.SQLTreeWalker.ListWhereStatement;
import org.vepo.sql2rest.SQLTreeWalker.SQLData;
import org.vepo.sql2rest.SQLTreeWalker.WhereClause;
import org.vepo.sql2rest.SQLTreeWalker.WhereStatement;
import org.vepo.sql2rest.exceptions.DependencyNotResolvedException;

public class Resolver {
	/**
	 * http://www.baeldung.com/rest-api-query-search-or-operation
	 * 
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
		if (whereStatement instanceof Lazy && !((Lazy) whereStatement).isResolved()) {
			throw new DependencyNotResolvedException();
		}
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
