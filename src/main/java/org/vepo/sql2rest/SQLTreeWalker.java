package org.vepo.sql2rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.RuleContext;
import org.vepo.sql2rest.SQLParser.GroupedWhereClauseContext;
import org.vepo.sql2rest.SQLParser.QueryClauseContext;
import org.vepo.sql2rest.SQLParser.QueryContext;
import org.vepo.sql2rest.SQLParser.SubQueryContext;
import org.vepo.sql2rest.SQLParser.WhereExprContext;

public class SQLTreeWalker extends SQLBaseListener {

	public class GroupWhereStatement extends WhereStatement {
		private final WhereStatement where;

		public GroupWhereStatement(WhereStatement where) {
			this.where = where;
		}

		public WhereStatement getWhere() {
			return where;
		}
	}

	public enum Joiner {
		AND, OR
	}

	public class ListWhereStatement extends WhereStatement {
		private final List<WhereStatement> statements;
		private final List<Joiner> joiner;

		public ListWhereStatement(List<WhereStatement> statements, List<Joiner> joiner) {
			this.statements = statements;
			this.joiner = joiner;
		}

		public List<Joiner> getJoiner() {
			return new ArrayList<>(joiner);
		}

		public List<WhereStatement> getStatements() {
			return new ArrayList<>(statements);
		}

	}

	public class SQLData {
		private Optional<WhereStatement> whereStatement;
		private String tableName;
		private List<String> fields;
		private boolean allFields;
		private Set<SQLData> dependencies = new HashSet<>();

		public String getTableName() {
			return tableName;
		}

		public Optional<WhereStatement> getWhereStatement() {
			return whereStatement;
		}

		public Set<SQLData> getDependencies() {
			return new HashSet<>(dependencies);
		}

		public void setData(Number data) {
			relations.entrySet().forEach(entry -> {
				if (entry.getValue() == this) {
					toResolveMap.get(entry.getKey()).setResolvedData(data);
				}
			});
		}

		public void setData(String data) {
			relations.entrySet().forEach(entry -> {
				if (entry.getValue() == this) {
					toResolveMap.get(entry.getKey()).setResolvedData(data);
				}
			});
		}

		public boolean isAllFields() {
			return allFields;
		}

		public List<String> getFields() {
			return fields;
		}
	}

	public class WhereClause extends WhereStatement {

		private final String field;
		private final String operator;
		private final String value;

		public WhereClause(String field, String operator, String value) {
			this.field = field;
			this.operator = operator;
			this.value = value;
		}

		public String getField() {
			return field;
		}

		public String getOperator() {
			return operator;
		}

		public String getValue() {
			return value;
		}

	}

	public abstract class WhereStatement {

	}

	public interface LazyResolver<T> {
		public T resolve(String data);
	}

	public class LazyWhereStatement extends WhereStatement implements Lazy {
		private final QueryContext query;
		private final LazyResolver<WhereStatement> resolver;
		private WhereStatement resolved = null;

		private LazyWhereStatement(QueryContext query, LazyResolver<WhereStatement> resolver) {
			this.query = query;
			this.resolver = resolver;
		}

		@Override
		public boolean isResolved() {
			return resolved != null;
		}

		@Override
		public SQLData getResolverData() {
			return relations.get(query);
		}

		public WhereStatement getResolved() {
			return resolved;
		}

		@Override
		public void setResolvedData(String data) {
			resolved = resolver.resolve('\'' + data.replaceAll("'", "\\'") + '\'');
		}

		@Override
		public void setResolvedData(Number data) {
			resolved = resolver.resolve(data.toString());
		}

		// TODO not now
		// @Override
		// public <T> void setResolvedData(List<T> data) {
		// if()
		// resolved = resolver.resolve(data.toString());
		// }

	}

	public interface Lazy {
		public boolean isResolved();

		public SQLData getResolverData();

		public void setResolvedData(String data);

		public void setResolvedData(Number data);

		// TODO not now
		// public <T> void setResolvedData(List<T> data);
	}

	private final SQLData mainData = new SQLData();

	private final Map<QueryContext, SQLData> relations = new HashMap<>();

	private final Map<QueryContext, LazyWhereStatement> toResolveMap = new HashMap<>();

	public void enterQuery(SQLParser.QueryContext ctx) {
		SQLData data;
		if (ctx.getParent() == null) {
			data = mainData;
		} else {
			data = new SQLData();
			findParent(ctx.parent).dependencies.add(data);
		}
		relations.put(ctx, data);
		data.tableName = ctx.tableName().getText();
		if (ctx.fields().STAR() != null) {
			data.allFields = true;
			data.fields = Collections.emptyList();
		} else {
			data.allFields = false;
			data.fields = ctx.fields().field().stream().map(fc -> fc.getText()).collect(Collectors.toList());
		}
		if (null != ctx.whereExpr()) {
			data.whereStatement = Optional.of(process(ctx.whereExpr()));
		} else {
			data.whereStatement = Optional.empty();
		}
	}

	private SQLData findParent(RuleContext ctx) {
		if (ctx instanceof QueryContext) {
			return relations.get(ctx);
		}
		return findParent(ctx.parent);
	}

	public SQLData getMainData() {
		return mainData;
	}

	private WhereStatement process(WhereExprContext whereExpr) {
		QueryClauseContext clause = whereExpr.queryClause();
		GroupedWhereClauseContext groupedWhere = whereExpr.groupedWhereClause();
		if (clause != null) {
			if (clause.children.get(2) instanceof SubQueryContext) {
				LazyWhereStatement where = new LazyWhereStatement(((SubQueryContext) clause.children.get(2)).query(),
						(data) -> new WhereClause(clause.children.get(0).getText(), clause.children.get(1).getText(),
								data));
				toResolveMap.put(((SubQueryContext) clause.children.get(2)).query(), where);
				return where;
			} else if (clause.children.get(2) instanceof QueryContext) {
				LazyWhereStatement where = new LazyWhereStatement((QueryContext) clause.children.get(2),
						(data) -> new WhereClause(clause.children.get(0).getText(), clause.children.get(1).getText(),
								data));
				toResolveMap.put((QueryContext) clause.children.get(2), where);
				return where;
			} else {
				return new WhereClause(clause.children.get(0).getText(), clause.children.get(1).getText(),
						clause.children.get(2).getText());
			}
		} else if (groupedWhere != null) {
			return new GroupWhereStatement(process(groupedWhere.whereExpr()));
		} else {
			return new ListWhereStatement(
					whereExpr.whereExpr().stream().map(exp -> process(exp)).collect(Collectors.toList()),
					whereExpr.booleanOperator().stream().map(b -> Joiner.valueOf(b.getText().toUpperCase()))
							.collect(Collectors.toList()));
		}
	}

}