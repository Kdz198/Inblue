package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.Payment;
import fpt.org.inblue.enums.PaymentStatus;
import fpt.org.inblue.enums.Role;
import fpt.org.inblue.repository.*;
import fpt.org.inblue.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class DashboardServiceImpl implements DashboardService {
    @Autowired
    private MentorRepository mentorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private PaymentRepository paymentRepository;


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
