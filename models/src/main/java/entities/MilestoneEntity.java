package entities;

import java.util.ArrayList;
import java.util.List;

public class MilestoneEntity {

    private List<String> clani, mikrostoritve, github, travis, dockerhub;
    private String opis_projekta;

    public MilestoneEntity() {
        this.clani = new ArrayList<>();
        this.mikrostoritve = new ArrayList<>();
        this.github = new ArrayList<>();
        this.travis = new ArrayList<>();
        this.dockerhub = new ArrayList<>();
    }

    public void addClan(String clan) {
        clani.add(clan);
    }

    public void addMikroStoritev(String storitev) {
        mikrostoritve.add(storitev);
    }

    public void addGitHubLink(String githubLink) {
        github.add(githubLink);
    }

    public void addTravisLink(String travisLink) {
        travis.add(travisLink);
    }

    public void addDockerHubLink(String dockerHubLink) {
        dockerhub.add(dockerHubLink);
    }

    public List<String> getClani() {
        return clani;
    }

    public void setClani(List<String> clani) {
        this.clani = clani;
    }

    public List<String> getMikrostoritve() {
        return mikrostoritve;
    }

    public void setMikrostoritve(List<String> mikrostoritve) {
        this.mikrostoritve = mikrostoritve;
    }

    public List<String> getGithub() {
        return github;
    }

    public void setGithub(List<String> github) {
        this.github = github;
    }

    public List<String> getTravis() {
        return travis;
    }

    public void setTravis(List<String> travis) {
        this.travis = travis;
    }

    public List<String> getDockerhub() {
        return dockerhub;
    }

    public void setDockerhub(List<String> dockerhub) {
        this.dockerhub = dockerhub;
    }

    public String getOpis_projekta() {
        return opis_projekta;
    }

    public void setOpis_projekta(String opis_projekta) {
        this.opis_projekta = opis_projekta;
    }
}
