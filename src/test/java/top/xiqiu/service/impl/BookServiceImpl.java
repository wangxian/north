package top.xiqiu.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.Autowired;
import top.xiqiu.north.annotation.Service;
import top.xiqiu.service.BookService;
import top.xiqiu.service.TeacherService;

@Service
public class BookServiceImpl implements BookService {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    @Autowired
    private TeacherService teacherService;

    @Override
    public void showBook() {
        teacherService.sayName();
        logger.info("我有一本书【平凡的世界】");
    }
}
