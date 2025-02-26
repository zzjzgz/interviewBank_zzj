package xyz.zzj.interviewBank_zzj.job.once;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.zzj.interviewBank_zzj.esdao.QuestionEsDao;
import xyz.zzj.interviewBank_zzj.model.dto.question.QuestionEsDTO;
import xyz.zzj.interviewBank_zzj.model.entity.Question;
import xyz.zzj.interviewBank_zzj.service.QuestionService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FullSyncQuestionToEs implements CommandLineRunner {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionEsDao questionEsDao;

    @Override
    public void run(String... args) {
        // 全量获取题目（数据量不大的情况下使用）
        List<Question> questionList = questionService.list();
        if (CollUtil.isEmpty(questionList)) {
            return;
        }
        // 转为 ES 实体类
        List<QuestionEsDTO> questionEsDTOList = questionList.stream()
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());
        // 分页批量插入到 ES
        final int pageSize = 200; // 调整批量大小
        int total = questionEsDTOList.size();
        log.info("FullSyncQuestionToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            try {
                questionEsDao.saveAll(questionEsDTOList.subList(i, end));
            } catch (Exception e) {
                log.error("Failed to sync data from {} to {}", i, end, e);
                // 可以在这里添加重试逻辑
            }
        }
        log.info("FullSyncQuestionToEs end, total {}", total);
    }
}