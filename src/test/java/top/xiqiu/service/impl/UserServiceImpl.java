package top.xiqiu.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.xiqiu.north.annotation.Autowired;
import top.xiqiu.north.annotation.Service;
import top.xiqiu.service.BookService;
import top.xiqiu.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    /**
     * logger
     **/
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private BookService bookService;

    @Override
    public void sayName() {
        bookService.showBook();
        logger.info("我是用户，我的名字是小王～");
    }

}
