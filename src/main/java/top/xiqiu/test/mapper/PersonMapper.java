package top.xiqiu.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import top.xiqiu.test.entity.Person;

import java.util.List;

public interface PersonMapper extends BaseMapper<Person> {

    @Select("select * from person")
    List<Person> findAll();
}
