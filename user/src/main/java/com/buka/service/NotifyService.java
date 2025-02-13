package com.buka.service;

import com.buka.util.JsonData;

/**
 * @author lhb
 */
public interface NotifyService {
    JsonData sendCode(String email);
}
