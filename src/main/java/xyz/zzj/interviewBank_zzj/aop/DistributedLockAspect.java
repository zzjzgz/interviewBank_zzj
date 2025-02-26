package xyz.zzj.interviewBank_zzj.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import xyz.zzj.interviewBank_zzj.annotation.DistributedLock;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class DistributedLockAspect {

    @Resource
    private RedissonClient redissonClient;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Exception {
        String lockKey = distributedLock.key();
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();
        TimeUnit timeUnit = distributedLock.timeUnit();

        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            // 尝试获取锁
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (acquired) {
                // 获取锁成功，执行目标方法
                return joinPoint.proceed();
            } else {
                // 获取锁失败，抛出异常或处理逻辑
                throw new RuntimeException("Could not acquire lock: " + lockKey);
            }
        } catch (Throwable e) {
            throw new Exception(e);
        } finally {
            if (acquired) {
                // 释放锁
                lock.unlock();
            }
        }
    }
}
