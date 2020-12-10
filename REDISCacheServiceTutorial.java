package com.aem.community.core.services;

public interface REDISCacheServiceTutorial {

	String get(String prefix, String key);

	boolean set(String prefix, String key, String value, Integer expirationTTL);

}