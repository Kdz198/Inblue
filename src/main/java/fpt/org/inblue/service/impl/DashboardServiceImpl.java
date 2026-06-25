package fpt.org.inblue.service.impl;

import fpt.org.inblue.enums.PaymentStatus;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.model.Payment;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.DashboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final MentorRepository mentorRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public int getMentorTotal() {
        return mentorRepository.countMentorByIsActive(true);
    }

    @Override
    public int getUserTotal() {
        return userRepository.countUserByRole(Role.USER);
    }

    @Override
    public int getSessionTotal() {
        return (int) sessionRepository.count();
    }

    @Override
    public List<Payment> getPayments() {

        return paymentRepository.findAllByStatus(PaymentStatus.COMPLETED);
    }
}
