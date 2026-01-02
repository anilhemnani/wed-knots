package com.momentsmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DatabaseSeedTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testAppUserTableSeeded() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_user_tbl", Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(3);
    }

    @Test
    void testRoleTableSeeded() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM role_tbl", Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(3);
    }

    @Test
    void testUserRolesTableSeeded() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_roles_tbl", Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(3);
    }

    @Test
    void testWeddingEventTableSeeded() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wedding_event_tbl", Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testGuestTableSeeded() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM guest_tbl", Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testHostTableSeeded() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM host_tbl", Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}
