package xyz.zzj.interviewBank_zzj.job.cycle;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.zzj.interviewBank_zzj.annotation.DistributedLock;
import xyz.zzj.interviewBank_zzj.esdao.QuestionEsDao;
import xyz.zzj.interviewBank_zzj.mapper.QuestionMapper;
import xyz.zzj.interviewBank_zzj.model.dto.question.QuestionEsDTO;
import xyz.zzj.interviewBank_zzj.model.entity.Question;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// todo 取消注释开启任务
@Component
@Slf4j
public class IncSyncQuestionToEs {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionEsDao questionEsDao;


    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    @DistributedLock(key = "testLock", leaseTime = 20000, waitTime = 5000)
    public void run() {
        // 查询近 5 分钟内的数据，每分钟执行一次
        long FIVE_MINUTES = 5 * 60 * 1000L;
        // 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - FIVE_MINUTES);
        List<Question> questionList = questionMapper.listQuestionWithDelete(fiveMinutesAgoDate);
        if (CollUtil.isEmpty(questionList)) {
            log.info("无可跟新的数据");
            return;
        }
        List<QuestionEsDTO> questionEsDTOList = questionList.stream()
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());
        final int pageSize = 500;
        int total = questionEsDTOList.size();
        log.info("增量同步开始, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            // 批量保存
            questionEsDao.saveAll(questionEsDTOList.subList(i, end));
        }
        log.info("IncSyncQuestionToEs end, total {}", total);
    }
}
