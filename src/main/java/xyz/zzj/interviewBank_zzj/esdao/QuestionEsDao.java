package xyz.zzj.interviewBank_zzj.esdao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import xyz.zzj.interviewBank_zzj.model.dto.question.QuestionEsDTO;

import java.util.List;

/**
 * 题目 ES 操作
 */
public interface QuestionEsDao 
    extends ElasticsearchRepository<QuestionEsDTO, Long> {

    /**
     * 根据用户 id 查询
     * @param userId
     * @return
     */
    List<QuestionEsDTO> findByUserId(Long userId);


}
