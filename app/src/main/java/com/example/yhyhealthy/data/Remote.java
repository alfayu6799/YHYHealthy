package com.example.yhyhealthy.data;

public class Remote {
    int image;
    String Name;
    Double degree;

    public Remote(int image, String name, Double degree) {
        this.image = image;
        Name = name;
        this.degree = degree;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Double getDegree() {
        return degree;
    }

    public void setDegree(Double degree) {
        this.degree = degree;
    }
}
