package com.badfic.philbot.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

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

    public Set<String> getNiblings() {
        return niblings;
    }

    public void setNiblings(Set<String> niblings) {
        this.niblings = niblings;
    }

    public Set<String> getPiblings() {
        return piblings;
    }

    public void setPiblings(Set<String> piblings) {
        this.piblings = piblings;
    }

    public Set<String> getCousins() {
        return cousins;
    }

    public void setCousins(Set<String> cousins) {
        this.cousins = cousins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Family family = (Family) o;
        return Objects.equals(intro, family.intro) &&
                Objects.equals(image, family.image) &&
                Objects.equals(spouses, family.spouses) &&
                Objects.equals(exes, family.exes) &&
                Objects.equals(children, family.children) &&
                Objects.equals(grandchildren, family.grandchildren) &&
                Objects.equals(grandparents, family.grandparents) &&
                Objects.equals(parents, family.parents) &&
                Objects.equals(siblings, family.siblings) &&
                Objects.equals(niblings, family.niblings) &&
                Objects.equals(piblings, family.piblings) &&
                Objects.equals(cousins, family.cousins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intro, image, spouses, exes, children, grandchildren, grandparents, parents, siblings, niblings, piblings, cousins);
    }
}
