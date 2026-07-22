package com.mediguardian.profile.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lifestyle {
    
    @Enumerated(EnumType.STRING)
    private SmokingStatus smoking;
    
    @Enumerated(EnumType.STRING)
    private AlcoholConsumption alcohol;
    
    @Enumerated(EnumType.STRING)
    private ExerciseLevel exercise;
}
