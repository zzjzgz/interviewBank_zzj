package xyz.zzj.interviewBank_zzj.mapper;

import org.apache.ibatis.annotations.Select;
import xyz.zzj.interviewBank_zzj.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Date;
import java.util.List;

/**
* @author zengz
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2025-01-26 00:17:37
* @Entity xyz.zzj.interviewBank_zzj.model.entity.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT * FROM question WHERE  updateTime >= #{fiveMinutesAgoDate}")
    List<Question> listQuestionWithDelete(Date fiveMinutesAgoDate);

}




