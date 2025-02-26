package xyz.zzj.interviewBank_zzj.model.dto.questionBankQuestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量删除题库题目关联请求
 *
 * @author zzj
 */
@Data
public class QuestionBankQuestionBatchRemoveRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 批量题目 id
     */
    private List<Long> questionId;

    private static final long serialVersionUID = 1L;
}