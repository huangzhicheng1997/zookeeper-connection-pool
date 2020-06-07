package com.hzc.zkpool.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: hzc
 * @Date: 2020/06/03  19:22
 * @Description:
 */
public class ZkPoolLogger {
    private static final Logger logger = LoggerFactory.getLogger(ZkPoolLogger.class);

    public static Logger getLogger() {
        return logger;
    }

    public static void info(String msg, Object o) {
        String replace = msg.replace("{}", o.toString());
        logger.info(replace);
    }

    public static void debug(String msg,Object o){
        String replace = msg.replace("{}", o.toString());
        logger.debug(replace);
    }



}
