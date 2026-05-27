package fpt.org.inblue.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(fpt.org.inblue.controller..*) || " +
            "within(fpt.org.inblue.service..*) || " +
            "within(fpt.org.inblue.repository..*) || " +
            "within(fpt.org.inblue.utils..*) || " +
            "within(fpt.org.inblue.event..*)")
    public void applicationPackagePointcut() {}

    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // Đo thời gian từ lúc bắt đầu vào hàm
        long start = System.currentTimeMillis();

        try {
            // Cho phép hàm chạy bình thường
            Object result = joinPoint.proceed();

            // CHỈ LOG 1 LẦN DUY NHẤT KHI CHẠY XONG THÀNH CÔNG
            long elapsedTime = System.currentTimeMillis() - start;
            log.info("✔ Hoàn thành: {}.{}() - Mất: {} ms", className, methodName, elapsedTime);

            return result;
        } catch (Throwable e) {
            // Log nếu có lỗi xảy ra và vẫn tính được tổng thời gian đã tiêu tốn
            long elapsedTime = System.currentTimeMillis() - start;
            log.error("✘ Lỗi tại: {}.{}() - Sau: {} ms - Chi tiết: {}", className, methodName, elapsedTime, e.getMessage());
            throw e;
        }
    }
}