package org.vepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.vepo.SQLParser.GroupedWhereClauseContext;
import org.vepo.SQLParser.QueryClauseContext;
import org.vepo.SQLParser.WhereExprContext;

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

		public String getTableName() {
			return tableName;
		}

		public Optional<WhereStatement> getWhereStatement() {
			return whereStatement;
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

	private final SQLData data = new SQLData();

	public void enterQuery(SQLParser.QueryContext ctx) {
		data.tableName = ctx.tableName().getText();
		if (null != ctx.whereExpr()) {
			this.data.whereStatement = Optional.of(process(ctx.whereExpr()));
		} else {
			this.data.whereStatement = Optional.empty();
		}
	}

	public SQLData getData() {
		return data;
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
					whereExpr.booleanOperator().stream().map(b -> Joiner.valueOf(b.getText().toUpperCase()))
							.collect(Collectors.toList()));
		}
	}

}