package xyz.zzj.interviewBank_zzj.model.dto.questionBank;

import lombok.Data;

import java.io.Serializable;

/**
 * @BelongsPackage: xyz.zzj.interviewBank_zzj.model.dto.questionBank
 * @ClassName: QuestionBankGetVoRequest
 * @Author: zengz
 * @CreateTime: 2025/1/29 17:28
 * @Description: TODO 描述类的功能
 * @Version: 1.0
 */

@Data
public class QuestionBankGetVoRequest implements Serializable {
    private static final long serialVersionUID = -7550312516869279769L;
    /**
     * 题库ID
     */
    private Long id;
    /**
     * 是否需要查询题目列表
     */
    private Boolean needQuestionList;

}
