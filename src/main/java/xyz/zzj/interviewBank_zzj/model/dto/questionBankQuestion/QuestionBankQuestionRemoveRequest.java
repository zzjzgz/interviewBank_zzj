package xyz.zzj.interviewBank_zzj.model.dto.questionBankQuestion;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除题库题目关联请求
 *
 * @author zzj
 */
@Data
public class QuestionBankQuestionRemoveRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}