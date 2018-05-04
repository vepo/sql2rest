package org.vepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.vepo.SQL2Rest.process;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.vepo.Resolver.toRest;

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
		assertEquals("/device?search=id:2", toRest(process("SELECT * FROM Device WHERE id = (SELECT id FROM MCI)")));
	}
}
