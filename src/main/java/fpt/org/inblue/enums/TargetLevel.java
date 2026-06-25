package fpt.org.inblue.enums;

import lombok.Getter;

@Getter
public enum TargetLevel {
    INTERN,
    FRESHER,
    JUNIOR,
    MIDDLE;

    public static TargetLevel convertFromStringToEnum(String name) {
        for (TargetLevel level : TargetLevel.values()) {
            if (level.name().equalsIgnoreCase(name)) {
                return level;
            }
        }
        throw new IllegalArgumentException("No enum constant with name " + name) {};
    }
}
