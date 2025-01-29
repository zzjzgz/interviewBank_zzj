package xyz.zzj.interviewBank_zzj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.zzj.interviewBank_zzj.common.BaseResponse;
import xyz.zzj.interviewBank_zzj.common.ResultUtils;
import xyz.zzj.interviewBank_zzj.model.dto.question.QuestionQueryRequest;
import xyz.zzj.interviewBank_zzj.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import xyz.zzj.interviewBank_zzj.model.entity.Question;
import xyz.zzj.interviewBank_zzj.model.entity.QuestionBankQuestion;
import xyz.zzj.interviewBank_zzj.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 题目服务
 *
 * @author zzj
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     *
     * @param question
     * @param add 对创建的数据进行校验
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);
    
    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionQueryRequest
     * @return
     */
    Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest);

}
