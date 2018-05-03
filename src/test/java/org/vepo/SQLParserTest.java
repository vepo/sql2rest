package org.vepo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("basic")
public class SQLParserTest {
	@Test
	void basicTest() {
		assertEquals("/device", SQL2Rest.process("SELECT * FROM Device"));
		assertEquals("/device?search=id:2", SQL2Rest.process("SELECT * FROM Device WHERE id = 2"));
		assertEquals("/device?search=id:2 OR id:3", SQL2Rest.process("SELECT * FROM Device WHERE id=2 OR id = 3"));
		assertEquals("/device?search=name:'John' AND ( id:2 OR id:3 )", SQL2Rest.process("SELECT * FROM Device WHERE name = 'John' AND (id=2 OR id=3)"));
	}
}
