package org.vepo.sql2rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.vepo.sql2rest.SQLTreeWalker.GroupWhereStatement;
import org.vepo.sql2rest.SQLTreeWalker.Joiner;
import org.vepo.sql2rest.SQLTreeWalker.Lazy;
import org.vepo.sql2rest.SQLTreeWalker.LazyWhereStatement;
import org.vepo.sql2rest.SQLTreeWalker.ListWhereStatement;
import org.vepo.sql2rest.SQLTreeWalker.SQLData;
import org.vepo.sql2rest.SQLTreeWalker.WhereClause;
import org.vepo.sql2rest.SQLTreeWalker.WhereStatement;
import org.vepo.sql2rest.exceptions.DependencyNotResolvedException;

public class Resolver {
	private Map<String, String> dataSourceMapper;

	private Resolver(Map<String, String> dataSourceMapper) {
		this.dataSourceMapper = dataSourceMapper;
	}

	public static Resolver get(Map<String, String> dataSourceMapper) {
		return new Resolver(dataSourceMapper);
	}

	public static Resolver get() {
		return new Resolver(new HashMap<>());
	}

	/**
	 * http://www.baeldung.com/rest-api-query-search-or-operation
	 * 
	 * @param data
	 * @return
	 */
	public String toRest(SQLData data) {
		// TODO Ugly code!
		List<String> parameters = Arrays.asList(toRest(data.getWhereStatement()), fields2Rest(data));
		return getTableName(data.getTableName()) + (parameters.stream().filter(p -> p != null).count() == 0L ? ""
				: '?' + parameters.stream().filter(p -> p != null).collect(Collectors.joining("&")));
	}

	private String getTableName(String tableName) {
		return dataSourceMapper.computeIfAbsent(tableName, (t) -> "/" + t.toLowerCase());
	}

	private String fields2Rest(SQLData data) {
		if (!data.isAllFields() && !data.getFields().isEmpty()) {
			return "fields=" + data.getFields().stream().collect(Collectors.joining(","));
		} else {
			return null;
		}
	}

	private String toRest(Optional<WhereStatement> whereStatement) {
		if (whereStatement.isPresent()) {
			return "search=" + toRest(whereStatement.get());
		} else {
			return null;
		}
	}

	private String toRest(WhereStatement whereStatement) {
		if (whereStatement instanceof LazyWhereStatement) {
			if (!((Lazy) whereStatement).isResolved()) {
				throw new DependencyNotResolvedException();
			} else {
				return toRest(((LazyWhereStatement) whereStatement).getResolved());
			}
		} else if (whereStatement instanceof GroupWhereStatement) {
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
