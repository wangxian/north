package top.xiqiu.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.Service;
import top.xiqiu.service.TeacherService;

@Service
public class TeacherServiceImpl implements TeacherService {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(TeacherServiceImpl.class);

    @Override
    public void sayName() {
        logger.info("我是张老师，我是这本书的作者。");
    }
}
