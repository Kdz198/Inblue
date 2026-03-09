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

    // Khai báo "bắt" tất cả các hàm nằm trong các package controller, service, repository
    @Pointcut("within(fpt.org.inblue.controller..*) || " +
            "within(fpt.org.inblue.service..*) || " +
            "within(fpt.org.inblue.repository..*)" +
            "within(fpt.org.inblue.utils..*)" +
            "within(fpt.org.inblue.event..*)") // Có thể thêm package khác nếu muốn
    public void applicationPackagePointcut() {
        // Pointcut signature
    }

    // Tự động bọc log xung quanh các hàm bị "bắt"
    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // Lấy tên class và tên hàm đang chạy
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // 1. Tự động log khi BẮT ĐẦU vào hàm
        log.info("▶ Bắt đầu chạy: {}.{}()", className, methodName);

        long start = System.currentTimeMillis();
        try {
            // Cho phép hàm chạy bình thường
            Object result = joinPoint.proceed();

            // 2. Tự động log khi chạy XONG hàm (thành công)
            long elapsedTime = System.currentTimeMillis() - start;
            log.info("✔ Chạy xong: {}.{}() - Mất: {} ms", className, methodName, elapsedTime);

            return result;
        } catch (IllegalArgumentException e) {
            // Tự động log nếu có lỗi
            log.error("✘ Lỗi tại: {}.{}() - Chi tiết: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}