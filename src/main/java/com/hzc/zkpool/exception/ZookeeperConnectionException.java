package com.hzc.zkpool.exception;

/**
 * @author: hzc
 * @Date: 2020/05/28  20:29
 * @Description:
 */
public class ZookeeperConnectionException extends RuntimeException {
    public ZookeeperConnectionException(String message) {
        super(message);
    }
}
