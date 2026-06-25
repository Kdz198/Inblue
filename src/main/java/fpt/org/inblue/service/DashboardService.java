package fpt.org.inblue.service;

import fpt.org.inblue.model.Payment;
import java.util.List;

public interface DashboardService {
    int getMentorTotal();

    int getUserTotal();

    int getSessionTotal();

    List<Payment> getPayments();
}
