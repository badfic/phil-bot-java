package com.badfic.philbot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Family {
    private Set<String> spouses = new HashSet<>();
    private Set<String> exes = new HashSet<>();
    private Set<String> children = new HashSet<>();
    private Set<String> grandchildren = new HashSet<>();
    private Set<String> grandparents = new HashSet<>();
    private Set<String> parents = new HashSet<>();
    private Set<String> siblings = new HashSet<>();

    public Set<String> getSpouses() {
        return spouses;
    }

    public void setSpouses(Set<String> spouses) {
        this.spouses = spouses;
    }

    public Set<String> getExes() {
        return exes;
    }

    public void setExes(Set<String> exes) {
        this.exes = exes;
    }

    public Set<String> getChildren() {
        return children;
    }

    public void setChildren(Set<String> children) {
        this.children = children;
    }

    public Set<String> getGrandchildren() {
        return grandchildren;
    }

    public void setGrandchildren(Set<String> grandchildren) {
        this.grandchildren = grandchildren;
    }

    public Set<String> getGrandparents() {
        return grandparents;
    }

    public void setGrandparents(Set<String> grandparents) {
        this.grandparents = grandparents;
    }

    public Set<String> getParents() {
        return parents;
    }

    public void setParents(Set<String> parents) {
        this.parents = parents;
    }

    public Set<String> getSiblings() {
        return siblings;
    }

    public void setSiblings(Set<String> siblings) {
        this.siblings = siblings;
    }
}
