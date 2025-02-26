package xyz.zzj.interviewBank_zzj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import xyz.zzj.interviewBank_zzj.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import xyz.zzj.interviewBank_zzj.model.entity.QuestionBankQuestion;
import xyz.zzj.interviewBank_zzj.model.entity.User;
import xyz.zzj.interviewBank_zzj.model.vo.QuestionBankQuestionVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题库题目关联服务
 *
 * @author zzj
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add 对创建的数据进行校验
     */
    void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest);
    
    /**
     * 获取题库题目关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request);

    /**
     * 分页获取题库题目关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request);


    /**
     * 批量添加题库题目关联
     * @param questionIdList 题目id列表
     * @param questionBankId 题库id
     * @param loginUser 用户
     * 无需返回值，出现异常则抛出异常，数据库中有事务控制，保证数据一致性
     */
    void batchAddQuestionBankQuestion(List<Long> questionIdList, Long questionBankId, User loginUser);


    /**
     * 批量删除题目和题库关联
     *
     * @param questionIdList 题目id列表
     * @param questionBankId 题库id
     */
    @Transactional(rollbackFor = Exception.class)
    void batchRemoveQuestionBankQuestion(List<Long> questionIdList, Long questionBankId);

    /**
     * 批量添加题目和题库关联 内部方法，不对外暴露
     * @param questionBankQuestionLists 题库题目关联列表
     */
    @Transactional(rollbackFor = Exception.class)
    void batchAddQuestionBankQuestionInner(List<QuestionBankQuestion> questionBankQuestionLists);
}
