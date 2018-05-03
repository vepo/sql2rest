package org.vepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.vepo.SQLParser.GroupedWhereClauseContext;
import org.vepo.SQLParser.QueryClauseContext;
import org.vepo.SQLParser.WhereExprContext;

public class SQLData extends SQLBaseListener {
	public enum Joiner {
		AND, OR
	}

	private String tableName;

	public abstract class WhereStatement {

	}

	public class GroupWhereStatement extends WhereStatement {
		private final WhereStatement where;

		public GroupWhereStatement(WhereStatement where) {
			this.where = where;
		}

		public WhereStatement getWhere() {
			return where;
		}
	}

	public class ListWhereStatement extends WhereStatement {
		private final List<WhereStatement> statements;
		private final List<Joiner> joiner;

		public ListWhereStatement(List<WhereStatement> statements, List<Joiner> joiner) {
			super();
			this.statements = statements;
			this.joiner = joiner;
		}

		public List<WhereStatement> getStatements() {
			return new ArrayList<>(statements);
		}

		public List<Joiner> getJoiner() {
			return new ArrayList<>(joiner);
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

	private Optional<WhereStatement> whereStatement;

	public void enterQuery(SQLParser.QueryContext ctx) {
		tableName = ctx.tableName().getText();
		if (null != ctx.whereExpr()) {
			this.whereStatement = Optional.of(process(ctx.whereExpr()));
		} else {
			this.whereStatement = Optional.empty();
		}
	}

	private WhereStatement process(WhereExprContext whereExpr) {
		QueryClauseContext clause = whereExpr.queryClause();
		GroupedWhereClauseContext groupedWhere = whereExpr.groupedWhereClause();
		if (clause != null) {
			return new WhereClause(clause.field().getText(), clause.operator().getText(), clause.value().getText());
		} else if (groupedWhere != null) {
			return new GroupWhereStatement(process(groupedWhere.whereExpr()));
		} else {
			return new ListWhereStatement(
					whereExpr.whereExpr().stream().map(exp -> process(exp)).collect(Collectors.toList()),
					whereExpr.booleanOperator().stream().map(b -> Joiner.valueOf(b.getText()))
							.collect(Collectors.toList()));
		}
	}

	public String getTableName() {
		return tableName;
	}

	public Optional<WhereStatement> getWhereStatement() {
		return whereStatement;
	}
}