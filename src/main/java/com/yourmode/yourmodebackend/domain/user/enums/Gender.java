package com.yourmode.yourmodebackend.domain.user.enums;

public enum Gender {
    FEMALE("여성"), 
    MALE("남성"), 
    UNKNOWN("선택안함");
    
    private final String displayName;
    
    Gender(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static Gender fromDisplayName(String displayName) {
        for (Gender gender : Gender.values()) {
            if (gender.displayName.equals(displayName)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown gender display name: " + displayName);
    }
}
