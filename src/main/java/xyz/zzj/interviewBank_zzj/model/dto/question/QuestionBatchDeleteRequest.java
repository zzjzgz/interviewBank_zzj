package xyz.zzj.interviewBank_zzj.model.dto.question;



import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量删除题目请求参数
 */
@Data
public class QuestionBatchDeleteRequest implements Serializable {

    /**
     * 批量题目 id 列表
     */
    private List<Long> questionIds;

    private static final long serialVersionUID = 1L;
}