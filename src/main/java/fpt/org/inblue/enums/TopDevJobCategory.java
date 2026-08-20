package fpt.org.inblue.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TopDevJobCategory {
    SOFTWARE_DEVELOPER(2, "Software Developer"),
    MACHINE_LEARNING_AI_ENGINEER(3, "Machine Learning / AI Engineer"),
    AUGMENTED_REALITY_DEVELOPER(4, "Augmented Reality (AR) Developer"),
    IOT_DEVELOPER(5, "Internet of Things (IoT) Developer"),
    BLOCKCHAIN_DEVELOPER(6, "Blockchain Developer"),
    DEVOPS_ENGINEER(7, "DevOps Engineer"),
    DATA_ENGINEER_SCIENTIST_ANALYST(8, "Data Engineer / Scientist / Analyst"),
    NETWORK_ENGINEER_CYBER_SECURITY(9, "Network Engineer / Cyber Security Expert"),
    QA_TESTER(10, "QA / Tester"),
    PRODUCT_MANAGER_BUSINESS_ANALYST(11, "Product Manager / Business Analyst"),
    IT_SUPPORT_SPECIALIST(12, "IT Support Specialist"),
    IT_HARDWARE_NETWORK(13, "IT - Hardware / Network"),
    UX_UI_DESIGNER(67, "UX/UI Designer");

    private final int id;
    private final String displayName;
}
