package com.aims.security;

public interface TokenBlacklist {
    void add(String token, long ttlMs);
    boolean contains(String token);
}
