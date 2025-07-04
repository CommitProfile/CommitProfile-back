package goormton.univ.portfolio.dto;

import goormton.univ.portfolio.entity.AboutMeField;
import lombok.Getter;

import java.util.Set;

@Getter
public class PortfolioAboutMeDto {
    private Set<AboutMeField> includedFields;
    private String name;
    private String shortintro;
    private String fullintro;
    private String email;
    private String blog;
    private String youtube;
    private String github;
    private String instagram;
    private String techstack;
}
