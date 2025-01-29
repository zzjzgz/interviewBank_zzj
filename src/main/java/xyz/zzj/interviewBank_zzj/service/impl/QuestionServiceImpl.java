package xyz.zzj.interviewBank_zzj.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.zzj.interviewBank_zzj.common.ErrorCode;
import xyz.zzj.interviewBank_zzj.constant.CommonConstant;
import xyz.zzj.interviewBank_zzj.exception.ThrowUtils;
import xyz.zzj.interviewBank_zzj.mapper.QuestionMapper;
import xyz.zzj.interviewBank_zzj.model.dto.question.QuestionQueryRequest;
import xyz.zzj.interviewBank_zzj.model.entity.Question;
import xyz.zzj.interviewBank_zzj.model.entity.QuestionBankQuestion;
import xyz.zzj.interviewBank_zzj.model.entity.User;
import xyz.zzj.interviewBank_zzj.model.vo.QuestionVO;
import xyz.zzj.interviewBank_zzj.model.vo.UserVO;
import xyz.zzj.interviewBank_zzj.service.QuestionBankQuestionService;
import xyz.zzj.interviewBank_zzj.service.QuestionService;
import xyz.zzj.interviewBank_zzj.service.UserService;
import xyz.zzj.interviewBank_zzj.utils.SqlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 *
 * @author zzj
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        Long userId = question.getUserId();
        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(tags), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(answer), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");

        }
        if (StringUtils.isNotBlank(content)) {
            ThrowUtils.throwIf(content.length() > 8192, ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer)) {
            ThrowUtils.throwIf(answer.length() > 8192, ErrorCode.PARAMS_ERROR, "答案过长");
        }
        if (StringUtils.isNotBlank(tags)) {
            ThrowUtils.throwIf(tags.length() == 0 , ErrorCode.PARAMS_ERROR, "标签为空");
        }
    }

    /**
     * 获取题目封装（关联用户信息）
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        // endregion

        return questionVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            return QuestionVO.objToVo(question);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // endregion
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }
    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String answer = questionQueryRequest.getAnswer();
        List<String> tagList = questionQueryRequest.getTags();
        String searchText = questionQueryRequest.getSearchText();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        Long userId = questionQueryRequest.getUserId();

        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询数据库
        QueryWrapper<Question> queryWrapper = this.getQueryWrapper(questionQueryRequest);
        //获取题库id
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        if(questionBankId!= null && questionBankId > 0){
           //拿到题库id查询对应的题目id
            Set<Long> questionIdSet = questionBankQuestionService
                    .list(new QueryWrapper<QuestionBankQuestion>()
                            .eq("questionBankId", questionBankId))
                    .stream().map(QuestionBankQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            //根据题目id查询题目
            queryWrapper.in("id",questionIdSet);
        }
        Page<Question> questionPage = this.page(new Page<>(current, size), queryWrapper);
        return questionPage;
    }

}
