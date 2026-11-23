package org.iplacex.proyectos.discografia;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/?serverSelectionTimeoutMS=5000",
        "spring.data.mongodb.database=discografia-test"
})
class DiscografiaApplicationTests {

	@Test
	void contextLoads() {
	}

}
