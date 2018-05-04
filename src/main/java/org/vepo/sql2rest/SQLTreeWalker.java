package org.vepo.sql2rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.RuleContext;
import org.vepo.sql2rest.SQLBaseListener;
import org.vepo.sql2rest.SQLParser;
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

	public class LazyWhereStatement<T extends WhereStatement> extends WhereStatement implements Lazy {
		private boolean resolved;
		private final QueryContext query;
		private final LazyResolver<T> resolver;
		private WhereStatement resolvedWhere;

		private LazyWhereStatement(QueryContext query, LazyResolver<T> resolver) {
			this.query = query;
			this.resolver = resolver;
		}

		@Override
		public boolean isResolved() {
			return resolved;
		}

		@Override
		public SQLData getResolverData() {
			return relations.get(query);
		}

		public WhereStatement getResolved() {
			return resolvedWhere;
		}

		@Override
		public void setResolvedData(String data) {
			resolvedWhere = resolver.resolve(data);
		}

	}

	public interface Lazy {
		public boolean isResolved();

		public SQLData getResolverData();

		public void setResolvedData(String data);
	}

	private final SQLData mainData = new SQLData();

	private final Map<QueryContext, SQLData> relations = new HashMap<QueryContext, SQLData>();

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
				return new LazyWhereStatement<WhereClause>(((SubQueryContext) clause.children.get(2)).query(),
						(data) -> new WhereClause(clause.children.get(0).getText(), clause.children.get(1).getText(),
								data));
			} else if (clause.children.get(2) instanceof QueryContext) {
				return new LazyWhereStatement<WhereClause>((QueryContext) clause.children.get(2),
						(data) -> new WhereClause(clause.children.get(0).getText(), clause.children.get(1).getText(),
								data));
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