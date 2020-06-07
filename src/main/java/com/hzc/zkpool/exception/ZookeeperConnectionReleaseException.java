package com.hzc.zkpool.exception;

/**
 * @author: hzc
 * @Date: 2020/06/02  21:42
 * @Description:
 */
public class ZookeeperConnectionReleaseException extends Exception {
    public ZookeeperConnectionReleaseException() {
        super("connection is in using");
    }
}
