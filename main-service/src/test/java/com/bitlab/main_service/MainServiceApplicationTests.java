package com.bitlab.main_service;

import org.junit.jupiter.api.Test;

class MainServiceApplicationTests {

	@Test
	void applicationClassExists() {
		// Full context test needs a running PostgreSQL, so it is added later
		// together with the service tests (Mockito).
		new MainServiceApplication();
	}
}