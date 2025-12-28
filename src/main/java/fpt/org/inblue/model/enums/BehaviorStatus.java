package fpt.org.inblue.model.enums;


import com.fasterxml.jackson.annotation.JsonValue;

public enum BehaviorStatus {
    TURNING_LEFT,
    TURNING_RIGHT,
    BOWING_HEAD,
    LOOKING_UP_HEAD,

    TOO_CLOSE,
    TOO_FAR,

    GLANCING_LEFT,
    GLANCING_RIGHT,
    LOOKING_UP_EYES,
    LOOKING_DOWN_EYES,

    NORMAL,
    UNKNOWN;


}