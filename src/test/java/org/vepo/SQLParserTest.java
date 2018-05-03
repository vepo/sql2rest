package org.vepo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.vepo.SQL2Rest.process;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("basic")
public class SQLParserTest {
	@Test
	void basicTest() {
		assertEquals("/device",
				process("SELECT * FROM Device"));
		
		assertEquals("/device?search=id:2", 
				process("SELECT * FROM Device WHERE id = 2"));
		
		assertEquals("/device?search=id:2 OR id:3", 
				process("SELECT * FROM Device WHERE id=2 OR id = 3"));
		
		assertEquals("/device?search=name:'John' AND ( id:2 OR id:3 )",
				process("SELECT * FROM Device WHERE name = 'John' AND (id=2 OR id=3)"));
		
		assertEquals("/device?search=name:'John' AND ( ( id:2 OR id:3 ) AND ( age>30 OR age<10 ) )",
				process("SELECT * FROM Device WHERE name = 'John' AND ((id=2 OR id=3) AND (age > 30 or age < 10))"));
	}
}
