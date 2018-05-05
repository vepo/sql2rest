package org.vepo.sql2rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.vepo.sql2rest.SQL2Rest.process;

import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vepo.sql2rest.SQLTreeWalker.SQLData;
import org.vepo.sql2rest.exceptions.DependencyNotResolvedException;
import org.vepo.sql2rest.exceptions.SyntaxException;

@Tag("basic")
public class SQLParserTest {
	@ParameterizedTest
	@MethodSource("basicTestProvider")
	void basicTest(String rest, String sql) throws SyntaxException {
		assertEquals(rest, Resolver.get().toRest(process(sql)));
	}

	static Stream<Arguments> basicTestProvider() {
		return Stream.of(Arguments.of("/device", "SELECT * FROM Device"),
				Arguments.of("/device?search=id:2", "SELECT * FROM Device WHERE id = 2"),
				Arguments.of("/device?search=id:2 OR id:3", "SELECT * FROM Device WHERE id=2 OR id = 3"),
				Arguments.of("/device?search=name:'John' AND ( id:2 OR id:3 )",
						"SELECT * FROM Device WHERE name = 'John' AND (id=2 OR id=3)"),
				Arguments.of("/device?search=name:'John' AND ( ( id:2 OR id:3 ) AND ( age>30 OR age<10 ) )",
						"SELECT * FROM Device WHERE name = 'John' AND ((id=2 OR id=3) AND (age > 30 or age < 10))"));
	}

	@Test
	void syntaxErrorTest() {
		assertThrows(SyntaxException.class, () -> Resolver.get().toRest(process("Select * FROM Device WHERE")));
	}

	@FunctionalInterface
	interface ProcessSQLData {
		void process(SQLData d);
	}

	@ParameterizedTest
	@MethodSource("subQueryTestProvider")
	void subQueryTest(String sqlQuery, String subqueryRest, ProcessSQLData processor, String queryRest)
			throws SyntaxException {
		Resolver resolver = Resolver.get();
		SQLData data = process(sqlQuery);
		assertThrows(DependencyNotResolvedException.class, () -> resolver.toRest(data));

		SQLData dep = data.getDependencies().stream().findFirst().get();
		assertEquals(subqueryRest, resolver.toRest(dep));

		processor.process(dep);

		assertEquals(queryRest, resolver.toRest(data));
	}

	static Stream<Arguments> subQueryTestProvider() {
		ProcessSQLData p1 = (d) -> d.setData(2);
		ProcessSQLData p2 = (d) -> d.setData("clock");
		return Stream.of(
				Arguments.of("SELECT * FROM Device WHERE id = (SELECT * FROM MCI)", "/mci", p1, "/device?search=id:2"),
				Arguments.of("SELECT * FROM Device WHERE deviceName = (SELECT name FROM MCI WHERE id=2)",
						"/mci?search=id:2&fields=name", p2, "/device?search=deviceName:'clock'"));
	}

	@Test
	void fieldsTest() throws SyntaxException {
		assertEquals("/device?fields=id,name", Resolver.get().toRest(process("SELECT id, name FROM Device")));
	}

	@SuppressWarnings("serial")
	@Test
	void fullRestTest() throws SyntaxException {
		assertEquals("http://localhost:8080/rest/api/device?fields=id,name",
				Resolver.get(new HashMap<String, String>() {
					{
						put("Device", "http://localhost:8080/rest/api/device");
					}
				}).toRest(process("SELECT id, name FROM Device")));
	}
}
