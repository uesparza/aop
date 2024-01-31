package com.dapex.aog.dto;

/**
 * Created by mmacpher on 12/4/18.
 */
public class AOG {

    private String aog;

    private Long id;

    public String getAog() {
        return aog;
    }

    public void setAog(String aog) {
        this.aog = aog;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "com.dapex.aog.dto.aog.AOG{" +
                "aog='" + aog + '\'' +
                ", id=" + id +
                '}';
    }

}
