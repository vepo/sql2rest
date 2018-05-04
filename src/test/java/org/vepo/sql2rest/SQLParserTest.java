package org.vepo.sql2rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.vepo.sql2rest.Resolver.toRest;
import static org.vepo.sql2rest.SQL2Rest.process;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.vepo.sql2rest.exceptions.DependencyNotResolvedException;
import org.vepo.sql2rest.exceptions.SyntaxException;

@Tag("basic")
public class SQLParserTest {
	@Test
	void basicTest() throws SyntaxException {
		assertEquals("/device", toRest(process("SELECT * FROM Device")));

		assertEquals("/device?search=id:2", toRest(process("SELECT * FROM Device WHERE id = 2")));

		assertEquals("/device?search=id:2 OR id:3", toRest(process("SELECT * FROM Device WHERE id=2 OR id = 3")));

		assertEquals("/device?search=name:'John' AND ( id:2 OR id:3 )",
				toRest(process("SELECT * FROM Device WHERE name = 'John' AND (id=2 OR id=3)")));

		assertEquals("/device?search=name:'John' AND ( ( id:2 OR id:3 ) AND ( age>30 OR age<10 ) )", toRest(
				process("SELECT * FROM Device WHERE name = 'John' AND ((id=2 OR id=3) AND (age > 30 or age < 10))")));
	}

	@Test
	void syntaxErrorTest() {
		assertThrows(SyntaxException.class, () -> toRest(process("Select * FROM Device WHERE")));
	}

	@Test
	void subQueryTest() throws SyntaxException {
		assertThrows(DependencyNotResolvedException.class, () -> assertEquals("/device?search=id:2",
				toRest(process("SELECT * FROM Device WHERE id = (SELECT id FROM MCI)"))));
	}
}
