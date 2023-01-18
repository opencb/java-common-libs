package org.opencb.commons.utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;

import java.security.Key;
import java.util.Collections;

import static org.junit.Assert.*;

public class JwtManagerTest {
    private JwtManager jwtSessionManager;
    private String jwtToken;

    @Before
    public void setUp() throws Exception  {
        Key key = new SecretKeySpec(TextCodec.BASE64.decode(RandomStringUtils.randomAlphanumeric(50)), SignatureAlgorithm.HS256.getJcaName());
        jwtSessionManager = new JwtManager(SignatureAlgorithm.HS256.getValue(), key, key);
        testCreateJWTToken();
    }

    @Test
    public void testCreateJWTToken() throws Exception {
        jwtToken = jwtSessionManager.createJWTToken("testUser", Collections.emptyMap(), 60L);
    }

    @Test
    public void testParseClaims() throws Exception {
        assertEquals(jwtSessionManager.getUser(jwtToken), "testUser");
    }

    @Test(expected = JwtException.class)
    public void testExpiredToken() throws JwtException {
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcGVuQ0dBIEF1dGhlbnRpY2F0aW9uIiwiZXhwIjoxNDk2NzQ3MjI2LCJ1c2VySWQiOiJ0ZXN0VXNlciIsInR5cGUiOiJVU0VSIiwiaXAiOiIxNzIuMjAuNTYuMSJ9.cZbGHh46tP88QDATv4pwWODRf49tG9N2H_O8lXyjjIc";
        jwtSessionManager.validateToken(expiredToken);
    }

    @Test(expected = JwtException.class)
    public void testInvalidToken() throws JwtException {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJPcGVuQ0dBIEF1dGhlbnRpY2F0aW9uIiwiZXhwIjoxNDk2NzQ3MjI2LCJ1c2VySWQiOiJ0ZXN0VXNlciIsInR5cGUiOiJVU0VSIiwiaXAiOiIxNzIuMjAuNTYuMSJ9.cZbGHh46tP88QDATv4pwWODRf49tG9N2H_O8lXyjj";
        jwtSessionManager.validateToken(invalidToken);
    }

    @Test(expected = JwtException.class)
    public void testInvalidSecretKey() throws JwtException {
        jwtSessionManager.setPublicKey(new SecretKeySpec(TextCodec.BASE64.decode("wrongKey"), SignatureAlgorithm.HS256.getJcaName()));
        jwtSessionManager.validateToken(jwtToken);
    }

    @Test
    public void testNonExpiringToken() throws JwtException {
        String nonExpiringToken = jwtSessionManager.createJWTToken("System", null, -1L);
        assertEquals(jwtSessionManager.getUser(nonExpiringToken), "System");
        assertNull(jwtSessionManager.getExpiration(nonExpiringToken));
    }
}