package com.badfic.philbot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Family {
    private String intro;
    private String image;
    private Set<String> spouses = new HashSet<>();
    private Set<String> exes = new HashSet<>();
    private Set<String> children = new HashSet<>();
    private Set<String> grandchildren = new HashSet<>();
    private Set<String> grandparents = new HashSet<>();
    private Set<String> parents = new HashSet<>();
    private Set<String> siblings = new HashSet<>();
    private Set<String> niblings = new HashSet<>();
    private Set<String> piblings = new HashSet<>();
    private Set<String> cousins = new HashSet<>();
}
